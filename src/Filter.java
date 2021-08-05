import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Filter {
	public static final String datePattern = "yyyy-MM-dd_HH:mm:ss";
	static long pageSize = 512;


	/**
	 * get the size in bytes of the column type
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
					return Byte.toString(table.readByte());

				case 0x02:
					return Short.toString(table.readShort());

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
	
	public static String changeData(RandomAccessFile table, int loc, byte columnDataType, String value) {
		try {
			SimpleDateFormat format = new SimpleDateFormat(datePattern);

			table.seek(loc);
			
			System.out.println("Column Data Type is = "+columnDataType);
			
			switch (columnDataType) {
				case 0x00:
					return " ";

				case 0x01:
				case 0x09:
				case 0x08:
					table.writeByte(Byte.parseByte(value));
					return " ";

				case 0x02:
					table.writeShort(Short.parseShort(value));
					return " ";

				case 0x03:
					table.writeInt(Integer.parseInt(value));
					return " ";

				case 0x04:
					table.writeLong(Long.parseLong(value));
					return " ";

				case 0x05:
					table.writeFloat(Float.parseFloat(value));
					return " ";

				case 0x06:
					table.writeDouble(Double.parseDouble(value));
					return " ";


				case 0x0A:
					Date dateTime = new Date(table.readLong());
					String x = format.format(dateTime);
					table.writeLong(Long.parseLong(x));
					return " ";


				case 0x0B:
					Date date = new Date(table.readLong());
					String y = format.format(date).substring(0, 10);
					table.writeLong(Long.parseLong(y));
					return " ";
//			case 0x07:  payload[i] = Long.toString(file.readLong());
//				break;


			}
			
			byte[] b = value.getBytes();
			for (int j = 0; j < b.length; j++)
				table.writeByte(b[j]);
			
			String str = " ";
			byte[] space = str.getBytes();
			
			if(b.length<columnDataType-0x0C) {
				for(int i=0; i<columnDataType-0x0C-b.length; i++)
					table.writeByte(space[0]);
			}
			
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
	
	public static String[] updateData(RandomAccessFile table, short address, String set_value, int column_number) {
		try {
			System.out.println("Inside the update code");
			table.seek(address + 6);
			byte numColumns = table.readByte();

			short recordCurrentAddress = (short) (address + 7 + numColumns);
			short columnTypeCurrentAddress = (short) (address + 7);

			String[] results = new String[numColumns];

			for (int i = 0; i < numColumns; i++) {
				if(column_number==i) {
				table.seek(columnTypeCurrentAddress + i);
				byte column_type = table.readByte();
				int valueSize = getDataTypeSize(column_type);
				table.seek(recordCurrentAddress);

				results[i] = readData(table, recordCurrentAddress, column_type);
				changeData(table, recordCurrentAddress, column_type, set_value);
				recordCurrentAddress += valueSize;
				}
			}
			return results;
		} catch (Exception e) {
			System.out.println(e);
		}
		return new String[0];
	}

	public static void displayRow(String[] row) {
		for (int i = 0; i < row.length; i++) {
			System.out.print(row[i] + "\t");
		}
		System.out.println("\n");
	}


	// TODO implement
	public static boolean compareValue(String input, String currentValue, String operator) {
		return true;
	}

	public static boolean compareRowId(int input, int currentValue, String operator) {
		boolean flag = false;

		if (operator.equals("=")) {
			flag = (input == currentValue) ? true : false;
		} else if (operator.equals( ">")) {
			flag = (input > currentValue) ? true : false;
		} else if (operator.equals(">=")) {
			flag = (input >= currentValue) ? true : false;
		} else if (operator.equals("<")) {
			flag = (input < currentValue) ? true : false;
		} else if (operator.equals("<=")) {
			flag = (input <= currentValue) ? true : false;
		} else if (operator.equals("<>")) {
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
			System.out.println("Page count: " + pageCount);
			displayRow(columns);

			System.out.println("Displaying table....");

			for (int page = 0; page < pageCount; page++) {
				// TODO check if the it's a leaf page
				short recordCount = Pages.getCellCount(	tableFile, page);
//				System.out.println("Record count: "+ recordCount);

				for (int j = 0; j < recordCount; j++) {
					short cell_offset = Pages.getCellAddress(tableFile, page, j);

					// go to record location. +2 for the payload size
					tableFile.seek(cell_offset + 2);
					int rowid = tableFile.readInt();
//					System.out.println("Row id: " + rowid);


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
}
