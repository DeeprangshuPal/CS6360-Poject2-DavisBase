import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Filter {
	public static final String datePattern = "yyyy-MM-dd_HH:mm:ss";
	static long pageSize = 512;


	/**
	 * get the size in bytes of the column type
	 */
	public static String readData(RandomAccessFile table, int loc, byte columnDataType) {
		int stringLen = 0;
		try {
			table.seek(loc);
			switch (columnDataType) {
				case 0x00:
					return " ";

				case 0x01:
				case 0x08:
					return Byte.toString(table.readByte());

				case 0x02:
					return Short.toString(table.readShort());

				case 0x03:
					return Integer.toString(table.readInt());

				case 0x04:
					return Long.toString(table.readLong());

				case 0x09:
				case 0x05:
					// loading as string
					stringLen = 4;
					break;

				case 0x06:
					return String.valueOf(table.readDouble());


				case 0x0B:
				case 0x0A:
					stringLen = 8;
					break;
				default:
					stringLen = columnDataType - 0x0C;
			}
			byte[] bytes = new byte[stringLen];
			for (int j = 0; j < stringLen; j++)
				bytes[j] = table.readByte();
			return new String(bytes);
		} catch (Exception e) {
			System.out.println(e);
		}
		return "";
	}

	public static void changeData(RandomAccessFile table, int loc, byte columnDataType, String value) {
		try {

			table.seek(loc);

			System.out.println("Column Data Type is = " + columnDataType);
			int maxByte = 0;

			switch (columnDataType) {
				case 0x00:
					return;

				case 0x01:
				case 0x08:
					table.writeByte(Byte.parseByte(value));
					return;

				case 0x02:
					table.writeShort(Short.parseShort(value));
					return;

				case 0x03:
					table.writeInt(Integer.parseInt(value));
					return;

				case 0x04:
					table.writeLong(Long.parseLong(value));
					return;

				case 0x09:
				case 0x05:
					maxByte = 4;
//					table.write(value.getBytes());
//					table.writeFloat(Float.parseFloat(value));
					break;

				case 0x06:
					table.writeDouble(Double.parseDouble(value));
					return;


				case 0x0A:
				case 0x0B:
					maxByte = 8;
					break;

				default:
					maxByte = columnDataType - 0x0C;
					break;


//			case 0x07:  payload[i] = Long.toString(file.readLong());
//				break;


			}

			byte[] b = value.getBytes();
			for (int j = 0; j < b.length; j++)
				table.writeByte(b[j]);

			String str = " ";
			byte[] space = str.getBytes();

			if (b.length < maxByte) {
				for (int i = 0; i < maxByte - b.length; i++)
					table.writeByte(space[0]);
			}

		} catch (Exception e) {
			System.out.println(e);
		}
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
	public static String[] getRecordData(RandomAccessFile table, short address, int rowid, int[] positions) {
		try {
			table.seek(address + 6);
			byte numColumns = table.readByte();

			short recordCurrentAddress = (short) (address + 7 + numColumns);
			short columnTypeCurrentAddress = (short) (address + 7);

			String[] results;
			if (positions.length == 0) {
				results = new String[numColumns + 1];
			} else {
				results = new String[positions.length + 1];
			}

			results[0] = Integer.toString(rowid);

			int j = 0;

			for (int i = 0; i < numColumns; i++) {
				table.seek(columnTypeCurrentAddress + i);
				byte column_type = table.readByte();
				int valueSize = getDataTypeSize(column_type);
				table.seek(recordCurrentAddress);
				if (positions.length == 0) {
					results[i + 1] = readData(table, recordCurrentAddress, column_type);
				} else {
					if (positions[j] == i) {
						results[j + 1] = readData(table, recordCurrentAddress, column_type);
						j++;
						if (j >= positions.length) {
							break;
						}
					}
				}
				recordCurrentAddress += valueSize;
			}
			return results;
		} catch (Exception e) {
			System.out.println(e);
		}
		return new String[0];
	}

	public static void updateData(RandomAccessFile table, short address, String set_value, int column_number) {
		try {
			System.out.println("Inside the update code");
			table.seek(address + 6);
			byte numColumns = table.readByte();

			short recordCurrentAddress = (short) (address + 7 + numColumns);
			short columnTypeCurrentAddress = (short) (address + 7);


			for (int i = 0; i < numColumns; i++) {
				table.seek(columnTypeCurrentAddress + i);
				byte column_type = table.readByte();
				int valueSize = getDataTypeSize(column_type);
				table.seek(recordCurrentAddress);
				if (column_number == i) {
					changeData(table, recordCurrentAddress, column_type, set_value);
				}
				recordCurrentAddress += valueSize;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void displayRow(String[] row, boolean includeRowId) {
//		for (int i = 0; i < row.length; i++) {
		try {
			int l = row.length;
			if (includeRowId) {
				System.out.format(" ".repeat(10) + "rowid" + "%15s".repeat(l) + "%n", row);
			} else {
				System.out.format("%15s".repeat(l) + "%n", row);
			}
		} catch (Exception e) {
			System.out.println("");
		}
	}


	public static boolean compareRowId(int input, int currentValue, String operator) {
		boolean flag = false;

		if (operator.equals("=")) {
			flag = (input == currentValue) ? true : false;
		} else if (operator.equals(">")) {
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
	 * columns: columns in the table
	 * userColumns: columns requested by user
	 */
	public static int[] getColumnPositions(String[] columns, String[] userColumns) {
		int[] positions = new int[userColumns.length];


		for (int i = 0; i < userColumns.length; i++) {

			for (int j = 0; j < columns.length; j++) {
				if (columns[j].equalsIgnoreCase(userColumns[i]) && !userColumns[i].equalsIgnoreCase("rowid")) {
					positions[i] = j;
				}
			}


		}

		if (positions.length != userColumns.length) {
			System.out.println("Couldn't find all columns. Please check the column list entered!");
		}
		Arrays.sort(positions);
		return positions;
	}

	public static String[] getSortedColumns(String[] columns, int[] positions) {
		String[] sortedColumns = new String[positions.length];
		int i = 0;
		int j = 0;
		while (i < columns.length && j < positions.length) {
			if (i == positions[j]) {
				sortedColumns[j] = columns[i];
				j++;
			}
			i++;
		}
		return sortedColumns;
	}


	/**
	 * Select records with given rowIdValue and operator
	 *
	 * @param-s are self-explanatory
	 * columns: columns in the table
	 * userColumns: columns requested by user
	 */
	public static void selectTable(String file_name, int rowIdValue, String operator, String[] columns, String[] userColumns) {
		// load file. parse through each page and each record in a page. if row_id match is found then display
		int[] positions = getColumnPositions(columns, userColumns);
		userColumns = getSortedColumns(columns, positions);

		try {
			RandomAccessFile tableFile = new RandomAccessFile(file_name, "rw");
			int pageCount = Pages.getPageCount(tableFile);
			displayRow(userColumns, true);


			for (int page = 0; page < pageCount; page++) {
				// TODO check if it's a leaf page
				short recordCount = Pages.getCellCount(tableFile, page);

				for (int j = 0; j < recordCount; j++) {
					short cell_offset = Pages.getCellAddress(tableFile, page, j);

					// go to record location. +2 for the payload size
					tableFile.seek(cell_offset + 2);
					int rowid = tableFile.readInt();


					if (compareRowId(rowid, rowIdValue, operator)) {
						String[] rc = getRecordData(tableFile, cell_offset, rowid, positions);
						displayRow(rc, false);
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
