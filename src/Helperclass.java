import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helperclass {
	static long pageSize = 512;
	final static String selectRegex = "(?i)SELECT\\s+.+\\s+FROM\\s+([\\w]+)\\s+WHERE(\\s+NOT)?(\\s+(ROWID)\\s+(>=|<=|<>|=|>|<)+(.+))";
	public static final String datePattern = "yyyy-MM-dd_HH:mm:ss";

	/**
	 * Get select query parameters by matching to a regex
	 *
	 * @param string : select query string
	 * @return: map of all (parameter, value)
	 */
	public static Map<String, String> getCondition(String string) {


		Map<String, String> map = new HashMap<>();
		Pattern pattern = Pattern.compile(selectRegex, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(string);
		boolean match = matcher.matches();
		if (match) {
			map.put("match", "yes");
		} else {
			map.put("match", "no");
		}
		matcher = pattern.matcher(string);
		String table_name = string.split(" ")[2];
		map.put("table_name", table_name.strip());
		while (matcher.find()) {

			for (int i = 1; i <= matcher.groupCount(); i++) {
				String key = null;
				if (i == 4) {
					key = "column";
				} else if (i == 5) {
					key = "operator";
				} else if (i == 6) {
					key = "value";
				} else if (i == 1) {
					key = "table_name";
				}
				if (key != null) {
					map.put(key, matcher.group(i).strip());
				}
			}
		}
		return map;
	}

	/**
	 * get the size in bytes of the column type
	 *
	 * @return
	 */
	public static String readData(RandomAccessFile table, int loc, byte columnDataType) {
		try {
			SimpleDateFormat format = new SimpleDateFormat(datePattern);

			table.seek(loc);
			switch (columnDataType) {
				case 0x00:
					return " ";

				case 0x01:
				case 0x09:
				case 0x08:
					return Integer.toString(table.readByte());

				case 0x02:
					return Integer.toString(table.readShort());

				case 0x03:
					return Integer.toString(table.readInt());

				case 0x04:
					return Long.toString(table.readLong());

				case 0x05:
					return String.valueOf(table.readFloat());

				case 0x06:
					return String.valueOf(table.readDouble());


				case 0x0A:
					Date dateTime = new Date(table.readLong());
					return format.format(dateTime);


				case 0x0B:
					Date date = new Date(table.readLong());
					return format.format(date).substring(0, 10);
//			case 0x07:  payload[i] = Long.toString(file.readLong());
//				break;


			}
			int stringLen = columnDataType - 0x0C;
			byte[] bytes = new byte[stringLen];
			for (int j = 0; j < stringLen; j++)
				bytes[j] = table.readByte();
			return new String(bytes);
		} catch (Exception e) {
			System.out.println(e);
		}
		return "";
	}

	/**
	 * get the size in bytes of the column type
	 *
	 * @return
	 */
	public static int getDataTypeSize(byte columnDataType) {
		switch (columnDataType) {
			case 0x00:
				return 0;

			case 0x01:
				return 1;

			case 0x02:
				return 2;

			case 0x03:

			case 0x05:

			case 0x09:
				return 4;

			case 0x04:

			case 0x06:

			case 0x0A:

			case 0x0B:
				return 8;

//			case 0x07:  payload[i] = Long.toString(file.readLong());
//				break;

			case 0x08:
				return 1;
		}
		return columnDataType - 0x0C;
	}


	/**
	 * Get record data given the table and address of record()
	 *
	 * @param table
	 * @param address
	 * @return
	 */
	public static String[] getRecordData(RandomAccessFile table, short address) {
		try {
			table.seek(address + 6);
			byte numColumns = table.readByte();

			short recordCurrentAddress = (short) (address + 7 + numColumns);
			short columnTypeCurrentAddress = (short) (address + 7);

			String[] results = new String[numColumns];

			for (int i = 0; i < numColumns; i++) {
				table.seek(columnTypeCurrentAddress + i);
				byte column_type = table.readByte();
				int valueSize = getDataTypeSize(column_type);
				table.seek(recordCurrentAddress);

				results[i] = readData(table, recordCurrentAddress, column_type);
				recordCurrentAddress += valueSize;

			}

			return results;
		} catch (Exception e) {
			System.out.println(e);
		}
		return new String[0];
	}

	public static void displayRow(String[] row) {
		for (int i = 0; i < row.length; i++) {
			System.out.println(row[i] + "\t");
		}
		System.out.println("\n");
	}


	// TODO implement
	public static boolean compareValue(String input, String currentValue, String operator) {
		return true;
	}

	public static boolean compareRowId(int input, int currentValue, String operator) {
		boolean flag = false;
		if (operator == "=") {
			flag = (input == currentValue) ? true : false;
		} else if (operator == ">") {
			flag = (input > currentValue) ? true : false;
		} else if (operator == ">=") {
			flag = (input >= currentValue) ? true : false;
		} else if (operator == "<") {
			flag = (input < currentValue) ? true : false;
		} else if (operator == "<=") {
			flag = (input <= currentValue) ? true : false;
		} else if (operator == "<>") {
			flag = (input != currentValue) ? true : false;
		}
		return flag;
	}


	/**
	 * Select records with given rowIdValue and operator
	 *
	 * @param-s are self-explanatory
	 */
	public static void selectTable(String file_name, int rowIdValue, String operator, String[] columns) {
		// load file. parse through each page and each record in a page. if row_id match is found then display

		try {
			RandomAccessFile tableFile = new RandomAccessFile(file_name, "rw");
			int pageCount = Pages.getPageCount(tableFile);
			displayRow(columns);

			for (int page = 0; page < pageCount; page++) {
				// TODO check if the it's a leaf page
				short recordCount = Pages.getCellCount(tableFile, page);

				for (int j = 0; j < recordCount; j++) {
					short cell_offset = Pages.getCellAddress(tableFile, page, j);

					// go to record location. +2 for the payload size
					tableFile.seek(cell_offset + 2);
					int rowid = tableFile.readInt();


					if (compareRowId(rowid, rowIdValue, operator)) {
						String[] rc = getRecordData(tableFile, cell_offset);
						displayRow(rc);
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}


	public static void main(String[] args) {
		String a = "select * from table where not rowid <= 5";
		Helperclass hp = new Helperclass();
		Map<String, String> map = hp.getCondition(a);
		for (String name : map.keySet())
			System.out.println(name + ": " + map.get(name));
	}


}
