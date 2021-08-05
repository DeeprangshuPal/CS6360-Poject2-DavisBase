import java.io.RandomAccessFile;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;


import static java.lang.System.out;

/**
 * @author Chris Irwin Davis
 * @version 1.0
 * <b>
 * <p>This is an example of how to create an interactive prompt</p>
 * <p>There is also some guidance to get started wiht read/write of
 * binary data files using RandomAccessFile class</p>
 * </b>
 */
public class DavisBasePromptExample {

	// Locations of meta data
	final static String davisbase_columns = "data/davisbase_columns.tbl";
	final static String davisbase_tables = "data/davisbase_tables.tbl";

	/* Variables for all Hexadecimal Meanings*/

	//Type of page
	final static byte b_tree_index_interior_page = 0x02;
	final static byte b_tree_table_interior_page = 0x05;
	final static byte b_tree_index_leaf_page = 0x0a;
	final static byte b_tree_table_leaf_page = 0x0d;

	//Data types
	final static byte NULL = 0x00;
	final static byte TINYINT = 0x01;
	final static byte SMALLINT = 0x02;
	final static byte INT = 0x03;
	final static byte BIGINT_LONG = 0x04;
	final static byte FLOAT = 0x05;
	final static byte DOUBLE = 0x06;
	final static byte YEAR = 0x08;
	final static byte TIME = 0x09;
	final static byte DATETIME = 0x0A;
	final static byte DATE = 0x0B;
	final static byte TEXT = 0x0C; //+n

	// Keeps track of the next available row id
	static int row_id = 1;

	/* This can be changed to whatever you like */
	static String prompt = "davisql> ";
	static String version = "v1.0b(example)";
	static String copyright = "©2016 Chris Irwin Davis";
	static boolean isExit = false;
	/*
	 * Page size for alll files is 512 bytes by default.
	 * You may choose to make it user modifiable
	 */
	static long pageSize = 512;

	/*
	 *  The Scanner class is used to collect user commands from the prompt
	 *  There are many ways to do this. This is just one.
	 *
	 *  Each time the semicolon (;) delimiter is entered, the userCommand
	 *  String is re-populated.
	 */
	static Scanner scanner = new Scanner(System.in).useDelimiter(";");

	/**
	 * **********************************************************************
	 * Main method
	 */
	public static void main(String[] args) {

		/* Display the welcome screen */
		splashScreen();

		/* This method will initialize the database storage if it doesn't exit */
		initializeDataStore();

		/* Variable to collect user input from the prompt */
		String userCommand = "";

		while (!isExit) {
			System.out.print(prompt);
			/* toLowerCase() renders command case insensitive */
			userCommand = scanner.next().replace("\n", " ").replace("\r", "").trim().toLowerCase();
			// userCommand = userCommand.replace("\n", "").replace("\r", "");
			parseUserCommand(userCommand);
		}
		System.out.println("Exiting...");
	}

	/** ***********************************************************************
	 *  Static method definitions
	 */

	/**
	 * Display the splash screen
	 */
	public static void splashScreen() {
		System.out.println(line("-", 80));
		System.out.println("Welcome to DavisBaseLite"); // Display the string.
		System.out.println("DavisBaseLite Version " + getVersion());
		System.out.println(getCopyright());
		System.out.println("\nType \"help;\" to display supported commands.");
		System.out.println(line("-", 80));
	}

	/**
	 * @param s   The String to be repeated
	 * @param num The number of time to repeat String s.
	 * @return String A String object, which is the String s appended to itself num times.
	 */
	public static String line(String s, int num) {
		String a = "";
		for (int i = 0; i < num; i++) {
			a += s;
		}
		return a;
	}

	public static void printCmd(String s) {
		System.out.println("\n\t" + s + "\n");
	}

	public static void printDef(String s) {
		System.out.println("\t\t" + s);
	}

	/**
	 * Help: Display supported commands
	 */
	public static void help() {
		out.println(line("*", 80));
		out.println("SUPPORTED COMMANDS\n");
		out.println("All commands below are case insensitive\n");
		out.println("SHOW TABLES;");
		out.println("\tDisplay the names of all tables.\n");
		//printCmd("SELECT * FROM <table_name>;");
		//printDef("Display all records in the table <table_name>.");
		out.println("SELECT <column_list> FROM <table_name> [WHERE <condition>];");
		out.println("\tDisplay table records whose optional <condition>");
		out.println("\tis <column_name> = <value>.\n");
		out.println("DROP TABLE <table_name>;");
		out.println("\tRemove table data (i.e. all records) and its schema.\n");
		out.println("UPDATE TABLE <table_name> SET <column_name> = <value> [WHERE <condition>];");
		out.println("\tModify records data whose optional <condition> is\n");
		out.println("VERSION;");
		out.println("\tDisplay the program version.\n");
		out.println("HELP;");
		out.println("\tDisplay this help information.\n");
		out.println("EXIT;");
		out.println("\tExit the program.\n");
		out.println(line("*", 80));
	}

	/**
	 * return the DavisBase version
	 */
	public static String getVersion() {
		return version;
	}

	public static String getCopyright() {
		return copyright;
	}

	public static void displayVersion() {
		System.out.println("DavisBaseLite Version " + getVersion());
		System.out.println(getCopyright());
	}

	public static void parseUserCommand(String userCommand) {

		/* commandTokens is an array of Strings that contains one token per array element
		 * The first token can be used to determine the type of command
		 * The other tokens can be used to pass relevant parameters to each command-specific
		 * method inside each case statement */
		// String[] commandTokens = userCommand.split(" ");
		ArrayList<String> commandTokens = new ArrayList<String>(Arrays.asList(userCommand.split(" ")));


		/*
		 *  This switch handles a very small list of hardcoded commands of known syntax.
		 *  You will want to rewrite this method to interpret more complex commands.
		 */
		switch (commandTokens.get(0)) {
			case "select":
				System.out.println("CASE: SELECT");
				parseQuery(userCommand);
				break;
			case "show":
				if (Helperclass.validateShowCommand(userCommand)) {
					userCommand = "select * from davisbase_tables where rowid>0";
					System.out.println("CASE: SHOW");
					parseQuery(userCommand);
				} else {
					System.out.println("Invalid show command!!");
				}
				break;
			case "drop":
				System.out.println("CASE: DROP");
				dropTable(userCommand);
				break;
			case "create":
				System.out.println("CASE: CREATE");
				parseCreateTable(userCommand);
				break;
			case "update":
				System.out.println("CASE: UPDATE");
				parseUpdate(userCommand);
				break;
			case "insert":
				System.out.println("CASE: INSERT");
				parseInsert(userCommand);
				break;
			case "help":
				help();
				break;
			case "version":
				displayVersion();
				break;
			case "exit":
				isExit = true;
				break;
			case "quit":
				isExit = true;
			default:
				System.out.println("I didn't understand the command: \"" + userCommand + "\"");
				break;
		}
	}

	/**
	 * Stub method for executing queries
	 *
	 * @param insertString is a String of the user input
	 */
	public static void parseInsert(String insertString) {
		String s = insertString;
		s = s.replaceAll("\\s+", " ");
		s = s.replaceAll("\\(\\s+", "(");
		s = s.replaceAll("\\s+\\)", ")");
		s = s.replaceAll("values\\(", " (");
		s = s.replaceAll(",\\s+", ",");
		System.out.println("S" + s);
		ArrayList<String> insertTableTokens = new ArrayList<String>(Arrays.asList(s.split(" ")));

		String table_name;
		String col_names;
		String values_string;

		try{
			table_name = insertTableTokens.get(4);
			col_names = insertTableTokens.get(3);
			values_string = insertTableTokens.get(6);
		}catch(Exception e){

			out.println("\nError in insertion : did not follow the correct syntax");
			out.println("Syntax : INSERT INTO TABLE (column_list) table_name VALUES (value1,value2,value3, ...);\n");
			return;
		}
		values_string = values_string.replace("(", "");
		values_string = values_string.replace(")", "");
		values_string = values_string.replace("\"", "");
		ArrayList<String> values_to_insert = new ArrayList<String>(Arrays.asList(values_string.split(",")));


		String tableFileName = "data/" + table_name + ".tbl";
		System.out.println("Table name" + tableFileName);

		try {
			File f = new File(tableFileName);
			if (!f.exists()) {
				out.println("\nTable does not exist\n");
				return;
			}

			int table_row_id = find_table_row_id(table_name);

			if (table_row_id == -1) {
				out.println("\nCould not find table in meta data\n");
				return;
			}

			ArrayList<column_info> columns = get_columns(table_row_id);
			columns.sort(Comparator.comparing(column_info::getOrdinal_position));

			ArrayList<String> data = new ArrayList<String>();
			ArrayList<String> data_types = new ArrayList<String>();

			for (int i = 0; i < columns.size(); i++) {
				if (i < values_to_insert.size()) {
					data_types.add(columns.get(i).getData_type());
					data.add(values_to_insert.get(i));
				} else {
					data.add("NULL");
					data_types.add("NULL");
					boolean isPk = columns.get(i).getColumn_key().equals("PRI");
					boolean isNullable = columns.get(i).getIsnullable().equals("YES");
					if (isPk) {
						out.println("\nColumn " + columns.get(i).getColumn_name() + " is a primary key and cannot be null\n");
						return;
					}
					if (!isNullable) {
						out.println("\nColumn " + columns.get(i).getColumn_name() + " cannot be null\n");
						return;
					}
				}
			}

			try {
				for (int i = 0; i < data_types.size(); i++) {
					switch (data_types.get(i)) {
						case "TINYINT":
							Byte.parseByte(data.get(i));
							break;
						case "SMALLINT":
							Short.parseShort(data.get(i));
							break;
						case "INT":
							Integer.parseInt(data.get(i));
							break;
						case "BIGINT_LONG":
							Long.parseLong(data.get(i));
							break;
						case "DOUBLE":
							Double.parseDouble(data.get(i));
							break;
					}

				}
			} catch (Exception e) {
				out.println("\ndata does not match data types\n");
				return;
			}

			insertIntoPage(0, tableFileName, data_types, data);
		} catch (Exception e) {
			out.println("\nError in parseInsert() method\n");
			return;
		}
	}

	/**
	 * @param table_row_id is the row id of the table you want the columns for
	 * @return An ArrayList of columns and their data retrieved from davisbase_columns
	 */
	public static ArrayList<column_info> get_columns(int table_row_id) {
		ArrayList<column_info> cols = new ArrayList<column_info>();

		try {
			RandomAccessFile tableFile = new RandomAccessFile(davisbase_columns, "rw");

			long num_pages = tableFile.length() / pageSize; // Number of pages in tableFile

			for (int i = 0; i < num_pages; i++) {
				long start_of_page = pageSize * i;

				tableFile.seek(start_of_page);
				tableFile.skipBytes(2);// Location in header of the number of cells on the page

				short num_cells_on_page = tableFile.readShort();

				long start_of_cell_array = start_of_page + 0x10;// Location where cell array starts

				for (int j = 0; j < num_cells_on_page; j++) {
					tableFile.seek(start_of_cell_array + (j * 2));// What the current cell you are reading is

					short cell_location = tableFile.readShort();

					tableFile.seek(start_of_page + cell_location);
					tableFile.skipBytes(2); // Skip the payload size
					tableFile.skipBytes(4); // Skip the row_id of the record

					byte num_cols = tableFile.readByte();

					byte[] record_header = new byte[num_cols];

					tableFile.readFully(record_header);

					int record_table_row_id = tableFile.readInt(); // the row_id of the table the current column belongs to

					if (record_table_row_id == table_row_id) {
						column_info col = new column_info();

						byte size_of_string = record_header[1];
						size_of_string -= TEXT;
						byte[] b = new byte[size_of_string];

						//Reads the column name
						tableFile.readFully(b);
						String col_name = new String(b, StandardCharsets.UTF_8);

						//Reads the column data type
						size_of_string = record_header[2];
						size_of_string -= TEXT;
						b = new byte[size_of_string];
						tableFile.readFully(b);
						String data_type = new String(b, StandardCharsets.UTF_8);

						byte ordinal_pos = tableFile.readByte(); //Reads the column position

						//Reads if the column is nullable
						size_of_string = record_header[4];
						size_of_string -= TEXT;
						b = new byte[size_of_string];
						tableFile.readFully(b);
						String isnullable = new String(b, StandardCharsets.UTF_8);

						//Reads the other constraints on the columns
						size_of_string = record_header[5];
						size_of_string -= TEXT;
						b = new byte[size_of_string];
						tableFile.readFully(b);
						String col_key = new String(b, StandardCharsets.UTF_8);

						col.setColumn_key(col_key);
						col.setColumn_name(col_name);
						col.setData_type(data_type);
						col.setIsnullable(isnullable);
						col.setOrdinal_position(ordinal_pos);
						cols.add(col);
					}


				}
			}
			tableFile.close();

		} catch (Exception e) {
			out.println(e);
			return null;
		}

		return cols;
	}

	/**
	 * @param table_name is the name of the table you want the row_id for
	 */
	public static int find_table_row_id(String table_name) {
		try {
			RandomAccessFile tableFile = new RandomAccessFile(davisbase_tables, "rw");

			long num_pages = tableFile.length() / pageSize; // Number of pages in tableFile

			for (int i = 0; i < num_pages; i++) {
				long start_of_page = pageSize * i;
				tableFile.seek(start_of_page + 0x02); // Location in header of the number of cells on the page

				short num_cells_on_page = tableFile.readShort();

				long start_of_cell_array = start_of_page + 0x10;// Location where cell array starts

				for (int j = 0; j < num_cells_on_page; j++) {
					tableFile.seek(start_of_cell_array + (j * 2));// What the current cell you are reading is

					short cell_location = tableFile.readShort();

					tableFile.seek(cell_location);
					tableFile.skipBytes(2); // Skip the payload size
					int current_row_id = tableFile.readInt(); // the row_id of the current cell

					tableFile.skipBytes(1); // Skip the number of columns, since this is davisbase_tables we know there is only one column

					byte size_of_string = tableFile.readByte();
					size_of_string -= 0x0c;

					byte[] b = new byte[size_of_string];

					tableFile.readFully(b);

					String cell_name = new String(b, StandardCharsets.UTF_8);

					if (cell_name.equals(table_name)) {
						return current_row_id;
					}
				}

			}
			tableFile.close();
		} catch (Exception e) {

		}
		return -1;
	}


	/**
	 * Stub method for dropping tables
	 *
	 * @param dropTableString is a String of the user input
	 */
	public static void dropTable(String dropTableString) {
		String s = dropTableString;
		s = s.replace("(", " ");
		s = s.replace(")", " ");

		ArrayList<String> dropTableTokens = new ArrayList<String>(Arrays.asList(s.split(" ")));

		/* Define table file name */
		String tableFileName = "data/" + dropTableTokens.get(2) + ".tbl";

		/*  Code to create a .tbl file to contain table data */
		try {
			File del = new File(tableFileName);
			del.delete();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/*** Get columns given table name*/
	public static String[] getStringColumns(String tableName) {
		int table_row_id = find_table_row_id(tableName);
		ArrayList<column_info> column_infos = get_columns(table_row_id);
		String[] columns = new String[column_infos.size()];
		for (int i = 0; i < column_infos.size(); i++) {
			columns[i] = column_infos.get(i).getColumn_name();
		}
		return columns;
	}

	/**
	 * Stub method for executing queries
	 *
	 * @param queryString is a String of the user input
	 */
	public static void parseQuery(String queryString) {
		out.println("STUB: This is the parseQuery method");
		try {
			Map<String, String> map = Helperclass.getCondition(queryString);
			if(map.get("condition").isEmpty()){
				queryString = queryString.concat(" where rowid>0");
			}
			map = Helperclass.getCondition(queryString);
			if (map.get("match") == "yes") {

				// if no conditions attached, just show all rows

				String table_name = map.get("table_name");


				String[] columns = getStringColumns(table_name);


				String file_name = "data/" + table_name + ".tbl";

//				String column = map.get("column");
				String operator = map.get("operator");
				String value = map.get("value");
//				System.out.println(operator);
//				System.out.println(value);
//				System.out.println(table_name);

				Filter.selectTable(file_name, Integer.parseInt(value), operator, columns);
			} else {
				out.println("Invalid Select Command!!");
			}
		} catch (Exception e) {
			out.println("Error Parsing Select command!!");
			out.println(e);
		}


		// first match pattern
		// extract table name, condition(negation, which column, operator, value)
		// load table file
		// do i have a mapping of order or columns? yes --> ordinal_position from davisbase_columns
		// once we know the column number(i.e. 2nd column) and value, we go to each page and search each record for the column value
		// and if a match is found then just display
	}

	/**
	 * Stub method for updating records
	 *
	 * @param updateString is a String of the user input
	 */
	public static void parseUpdate(String updateString) {
		System.out.println("STUB: This is the dropTable method");
		System.out.println("Parsing the string:\"" + updateString + "\"");
		
		try {
			Map<String, String> map = HPClass.getCondition(updateString);
			if (map.get("match") == "yes") {
				String table_name = map.get("table_name");


				String[] columns = getStringColumns(table_name);

				String file_name = "data/" + table_name + ".tbl";
				
				//String operator1 = map.get("operator1");
				String operator1_value = map.get("operator1_value");
				String operator2 = map.get("operator2");
				String operator2_value = map.get("operator2_value");
				String column_name = map.get("column_name");
				
				int i=0;
				for(i=0; i<columns.length; i++) {
					if(columns[i].equals(column_name)) {
							System.out.println(columns[i]);
				            break;
					}
				}
				int column_number = i;
				            
				System.out.println("The correct column number is "+column_number);
				
				int rowIdValue = Integer.parseInt(operator2_value);
				
				RandomAccessFile tableFile = new RandomAccessFile(file_name, "rw");
				int pageCount = Pages.getPageCount(tableFile);
				//System.out.println("The page count is = "+ pageCount);

				for (int page = 0; page < pageCount; page++) {
					// TODO check if the it's a leaf page
					short recordCount = Pages.getCellCount(	tableFile, page);
					//System.out.println("The record count is = "+ recordCount);

					for (int j = 0; j < recordCount; j++) {
						short cell_offset = Pages.getCellAddress(tableFile, page, j);

						// go to record location. +2 for the payload size
						tableFile.seek(cell_offset + 2);
						int rowid = tableFile.readInt();


						if (Filter.compareRowId(rowid, rowIdValue, operator2)) {
						System.out.println("The update is working");
							Filter.updateData(tableFile, cell_offset, operator1_value, column_number);
						}
					}
				}
				//Filter.selectTable(file_name, Integer.parseInt(value), operator, columns);
			} else {
				out.println("Invalid Update Command!!");
			}
		} catch (Exception e) {
			out.println("Error Parsing Update command!!");
			out.println(e);
		}
	}


	/**
	 * Stub method for creating new tables
	 *
	 * @param queryString is a String of the user input
	 */
	public static void parseCreateTable(String createTableString) {
		String s = createTableString;
		s = s.replace("(", " ");
		s = s.replace(")", " ");
		s = s.replace(",", " ");
		s = s.replaceAll("\\s+", " ");

		s = s.replaceAll("(?i)\\[NOT NULL\\]", "\\[not_null\\]");
		s = s.replaceAll("(?i)\\[PRIMARY KEY\\]", "\\[primary_key\\]");
		s = s.replaceAll("\\]\\[", "] [");
		s = s.replaceAll("\\[", " [");
		s = s.replaceAll("\\s+", " ");

		ArrayList<String> createTableTokens = new ArrayList<String>(Arrays.asList(s.split(" ")));

		/* Define table file name */
		String table_name = createTableTokens.get(2);
		String tableFileName = "data/" + table_name + ".tbl";

		/* YOUR CODE GOES HERE */

		ArrayList<String> col_names = new ArrayList<>();
		ArrayList<String> col_types = new ArrayList<>();
		ArrayList<String> col_keys = new ArrayList<>();
		ArrayList<String> col_isnullable = new ArrayList<>();

		try {
			for (int i = 3; i < createTableTokens.size(); i += 2) {
				String col_name = createTableTokens.get(i);
				String col_type = createTableTokens.get(i + 1);

				if (i + 2 < createTableTokens.size() && createTableTokens.get(i + 2).startsWith("[")) {
					ArrayList<String> temp = new ArrayList<>();
					for (int j = i + 2; j < createTableTokens.size(); j++) {
						if (!createTableTokens.get(j).startsWith("["))
							break;
						if (!temp.contains(createTableTokens.get(j))) {
							temp.add(createTableTokens.get(j));
						}
						i += 1;
					}

					if (temp.contains("[primary_key]")) {
						col_keys.add("PRI");
						col_isnullable.add("NO");
					} else if (temp.contains("[unique]")) {
						col_keys.add("UNI");
					} else {
						col_keys.add("NULL");
					}
					if (temp.contains("[not_null]") && !temp.contains("[primary_key]")) {
						col_isnullable.add("NO");
					}
					if (!temp.contains("[not_null]") && !temp.contains("[primary_key]")) {
						col_isnullable.add("YES");
					}
				} else {
					col_isnullable.add("YES");
					col_keys.add("NULL");
				}

				col_names.add(col_name);
				col_types.add(col_type);
			}
		} catch (Exception e) {
			out.println("Error :  did not define data types for all columns");
			return;
		}


		/*  Code to create a .tbl file to contain table data */
		try {
			/*  Create RandomAccessFile tableFile in read-write mode.
			 *  Note that this doesn't create the table file in the correct directory structure
			 */
			File f = new File(tableFileName);
			if (!f.exists()) {

				/*
				 * add to meta data first, if it fails don't create the table
				 */

				// Insert table to meta data
				ArrayList<String> data = new ArrayList<String>();
				data.add(table_name);
				ArrayList<String> data_type = new ArrayList<String>();
				data_type.add("TEXT");
				boolean meta_table = insertIntoPage(0, davisbase_tables, data_type, data);

				if (meta_table == false) {
					out.println("Error adding table information to table meta data");
					return;
				}

				//Insert columns to davisbase_columns meta data
				String table_row_id = "" + (row_id - 1);
				try {
					for (int i = 0; i < col_names.size(); i++) {
						ArrayList<String> temp_data = new ArrayList<>();
						ArrayList<String> temp_types = new ArrayList<>();
						String col_name = col_names.get(i);
						String type = col_types.get(i);
						String ordinal_position = "" + i;
						String is_nullable = col_isnullable.get(i);
						String col_key = col_keys.get(i);

						temp_data.add(table_row_id);
						temp_types.add("INT");

						temp_data.add(col_name);
						temp_types.add("TEXT");

						temp_data.add(type);
						temp_types.add("TEXT");

						temp_data.add(ordinal_position);
						temp_types.add("TINYINT");

						temp_data.add(is_nullable);
						temp_types.add("TEXT");

						temp_data.add(col_key);
						temp_types.add("TEXT");

						insertIntoPage(0, davisbase_columns, temp_types, temp_data);
					}
				} catch (Exception e) {
					out.println("Error in method parseCreateTable(), constraints not defined for all columns");
					return;
				}


				RandomAccessFile tableFile = new RandomAccessFile(tableFileName, "rw");
				tableFile.setLength(pageSize);
				tableFile.seek(0);

				tableFile.writeByte(b_tree_table_leaf_page); // First byte of header, states this is a leaf node page
				tableFile.writeByte(0x00); // Unused byte
				tableFile.writeShort(0x0000); // Number of cells on the page, 2 byte int (short)
				tableFile.writeShort(0x0000); // Start of the cell content area (by default 0x00 = 65536)
				tableFile.writeInt(0x00000000); // Page number of sibling to the right (in creation there is no sibling)
				tableFile.writeInt(0xFFFFFFFF); // Page number of parent (This is the root page so no parent [-1])
				tableFile.writeByte(0x00); // Unused byte

				tableFile.close();
			} else {
				out.println("Table name is taken");
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		/*  Code to insert a row in the davisbase_tables table
		 *  i.e. database catalog meta-data
		 */

		/*  Code to insert rows in the davisbase_columns table
		 *  for each column in the new table
		 *  i.e. database catalog meta-data
		 */
	}

	/**
	 * Method for inserting data into a table page
	 *
	 * @param pageNum      is the number of the page to insert to
	 * @param fileLocation is the tbl file to insert to
	 * @param data_types   : Type of data to be inserted
	 * @param data         : The data to be inserted
	 */
	public static boolean insertIntoPage(int pageNum, String fileLocation, ArrayList<String> data_types, ArrayList<String> data) {
		File f = new File(fileLocation);

		if (!f.exists()) {
			out.println("Table does not exist");
			return false;
		}

		if (data_types.size() != data.size()) {
			out.println("Not enough data types for all data");
			return false;
		}

		long page_location = pageSize * pageNum;

		try {
			RandomAccessFile tableFile = new RandomAccessFile(fileLocation, "rw");

			/* Load headers of the page*/
			tableFile.seek(page_location);
			byte page_type = tableFile.readByte(); // Tells if page is interior or leaf
			tableFile.readByte(); // Unused byte
			short num_cells = tableFile.readShort(); // The number of cells on the page
			short cell_offset = tableFile.readShort(); // The location of where the content starts
			cell_offset = (cell_offset == 0) ? 0x01FF : cell_offset;

			int end_of_array = (num_cells * 2) + 0x10; // Keeps track of where the end of the 2-byte array is

			/* Cell header and body to be inserted */
			short payload_size = 0;
			byte[] record_header = new byte[data_types.size()];

			// Calculates payload_size
			for (int i = 0; i < data.size(); i++) {
				if (data_types.get(i).toUpperCase().equals("NULL")) {
					payload_size += 1;
				} else if (data_types.get(i).toUpperCase().equals("TINYINT")) {
					payload_size += 1;
				} else if (data_types.get(i).toUpperCase().equals("SMALLINT")) {
					payload_size += 2;
				} else if (data_types.get(i).toUpperCase().equals("INT")) {
					payload_size += 4;
				} else if (data_types.get(i).toUpperCase().equals("BIGINT") || data_types.get(i).toUpperCase().equals("LONG")) {
					payload_size += 8;
				} else if (data_types.get(i).toUpperCase().equals("FLOAT")) {
					payload_size += 4;
				} else if (data_types.get(i).toUpperCase().equals("DOUBLE")) {
					payload_size += 8;
				} else {
					payload_size += data.get(i).getBytes().length;
				}

				byte serial_code = getDataTypeCode(data_types.get(i));
				serial_code += (serial_code == TEXT) ? data.get(i).getBytes().length : 0; // If it is a text data type then the code is 0x0c + n
				record_header[i] = serial_code;
			}

			payload_size += data_types.size(); // Adds the size of the record header (just the number of types in bytes)
			payload_size += 1; // An extra byte to account for column number

			int cell_size = payload_size + 2 + 4; // total size of the cell (payload+size of payload_size + row_id;


			/* Checks if there is enough space for the record*/
			long start_location = (page_location + cell_offset) - cell_size; // The location to start writing the record

			if (start_location < end_of_array + 2) {
				if ((pageNum + 1) * pageSize == tableFile.length()) {
					createNewPage(fileLocation);
					long num_pages = tableFile.length() / pageSize;
					long last_page = num_pages - 1;
					return insertIntoPage((int) last_page, fileLocation, data_types, data);
				} else {
					return insertIntoPage(pageNum + 1, fileLocation, data_types, data);
				}
			}

			/*Write the data to the table*/
			tableFile.seek(start_location);
			tableFile.writeShort(payload_size);
			tableFile.writeInt(row_id);

			increment_row_id(); //updates row_id

			tableFile.writeByte(data.size());
			tableFile.write(record_header);

			try {
				for (int i = 0; i < record_header.length; i++) {
					switch (record_header[i]) {
						case NULL:
							tableFile.writeByte(0x00);
							break;
						case TINYINT:
							tableFile.writeByte(Byte.parseByte(data.get(i)));
							break;
						case SMALLINT:
							tableFile.writeShort(Short.parseShort(data.get(i)));
							break;
						case INT:
							tableFile.writeInt(Integer.parseInt(data.get(i)));
							break;
						case BIGINT_LONG:
							tableFile.writeLong(Long.parseLong(data.get(i)));
							break;
						case DOUBLE:
							tableFile.writeDouble(Double.parseDouble(data.get(i)));
							break;
						default:
							tableFile.write(data.get(i).getBytes());
							break;
					}

				}
			} catch (Exception e) {
				out.println("Data does not match data types");
				return false;
			}

			// Update cell array
			tableFile.seek((page_location + 0x02));
			tableFile.writeShort((num_cells + 1));

			// Update page offset for first cell
			tableFile.writeShort(cell_offset - cell_size);

			// Update num cells
			tableFile.seek((page_location + end_of_array));
			tableFile.writeShort(cell_offset - cell_size);

			tableFile.close();
		} catch (Exception e) {
			out.println(e);
		}


		return true;
	}

	/**
	 * Creates a new page in the specified file with appropriate headers
	 *
	 * @param file : data type you want the code for
	 */
	static void createNewPage(String fileLocation) {
		try {
			RandomAccessFile file = new RandomAccessFile(fileLocation, "rw");
			file.setLength(file.length() + pageSize);
			long num_pages = file.length() / pageSize;
			long last_page = num_pages - 1;

			/* Set file pointer to the beginnning of the file */
			file.seek(pageSize * last_page);

			file.writeByte(b_tree_table_leaf_page); // First byte of header, states this is a leaf node page
			file.writeByte(0x00); // Unused byte
			file.writeShort(0x0000); // Number of cells on the page, 2 byte int (short)
			file.writeShort(0x0000); // Start of the cell content area (by default 0x00 = 65536)
			file.writeInt(0x00000000); // Page number of sibling to the right (in creation there is no sibling)
			file.writeInt(0xFFFFFFFF); // Page number of parent (This is the root page so no parent [-1])
			file.writeByte(0x00); // Unused byte

			file.close();
		} catch (Exception e) {
			out.println(e.getMessage());
		}
	}

	/**
	 * @param type : data type you want the code for
	 * @return : returns the serial code of the data type
	 */
	static byte getDataTypeCode(String type) {
		type = type.toUpperCase();
		switch (type) {
			case "NULL":
				return NULL;
			case "TINYINT":
				return TINYINT;
			case "SMALLINT":
				return SMALLINT;
			case "INT":
				return INT;
			case "BIGINT":
			case "LONG":
				return BIGINT_LONG;
			case "FLOAT":
				return FLOAT;
			case "DOUBLE":
				return DOUBLE;
			case "YEAR":
				return YEAR;
			case "TIME":
				return TIME;
			case "DATETIME":
				return DATETIME;
			case "DATE":
				return DATE;
			case "TEXT":
				return TEXT;
		}
		return NULL;
	}


	//  Every time a number is used update the saved row_id so it can't be used again
	public static void increment_row_id() {
		try {
			row_id += 1;
			RandomAccessFile current_row_id = new RandomAccessFile("data/current_row_id.txt", "rw");
			current_row_id.seek(0);
			current_row_id.writeUTF(("" + row_id));
			current_row_id.close();
		} catch (Exception e) {
			out.println(e);
		}
	}

	static void initializeDataStore() {

		/** Create data directory at the current OS location to hold */
		try {
			File dataDir = new File("data");
			if (!dataDir.exists()) {
				dataDir.mkdir();
				String[] oldTableFiles;
				oldTableFiles = dataDir.list();
				for (int i = 0; i < oldTableFiles.length; i++) {
					File anOldFile = new File(dataDir, oldTableFiles[i]);
					anOldFile.delete();
				}
			}
		} catch (SecurityException se) {
			out.println("Unable to create data container directory");
			out.println(se);
		}

		/** Create txt file to keep track of row_id*/
		try {
			File dir = new File("data/current_row_id.txt");
			if (!dir.exists()) {
				RandomAccessFile current_row_id = new RandomAccessFile("data/current_row_id.txt", "rw");
				current_row_id.seek(0);
				current_row_id.writeUTF("1");
				current_row_id.close();
				row_id = 1;
			} else {
				RandomAccessFile current_row_id = new RandomAccessFile("data/current_row_id.txt", "rw");
				current_row_id.seek(0);
				row_id = Integer.parseInt(current_row_id.readUTF());
				current_row_id.close();

			}
		} catch (Exception e) {
			out.println("Unable to create current_row_id.txt");
			out.println(e);
		}

		/** Create davisbase_tables system catalog */
		try {
			File tablesCatalog = new File("data/davisbase_tables.tbl");
			if (!tablesCatalog.exists()) {
				RandomAccessFile davisbaseTablesCatalog = new RandomAccessFile("data/davisbase_tables.tbl", "rw");
				/* Initially, the file is one page in length */
				davisbaseTablesCatalog.setLength(pageSize);
				/* Set file pointer to the beginnning of the file */
				davisbaseTablesCatalog.seek(0);

				davisbaseTablesCatalog.writeByte(b_tree_table_leaf_page); // First byte of header, states this is a leaf node page
				davisbaseTablesCatalog.writeByte(0x00); // Unused byte
				davisbaseTablesCatalog.writeShort(0x0000); // Number of cells on the page, 2 byte int (short)
				davisbaseTablesCatalog.writeShort(0x0000); // Start of the cell content area (by default 0x00 = 65536)
				davisbaseTablesCatalog.writeInt(0x00000000); // Page number of sibling to the right (in creation there is no sibling)
				davisbaseTablesCatalog.writeInt(0xFFFFFFFF); // Page number of parent (This is the root page so no parent [-1])
				davisbaseTablesCatalog.writeByte(0x00); // Unused byte

				davisbaseTablesCatalog.close();
			}
		} catch (Exception e) {
			out.println("Unable to create the database_tables file");
			out.println(e);
		}

		/** Create davisbase_columns systems catalog */
		try {
			File columnsCatalog = new File("data/davisbase_columns.tbl");

			if (!columnsCatalog.exists()) {
				RandomAccessFile davisbaseColumnsCatalog = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
				/** Initially the file is one page in length */
				davisbaseColumnsCatalog.setLength(pageSize);

				davisbaseColumnsCatalog.seek(0);

				davisbaseColumnsCatalog.writeByte(b_tree_table_leaf_page); // First byte of header, states this is a leaf node page
				davisbaseColumnsCatalog.writeByte(0x00); // Unused byte
				davisbaseColumnsCatalog.writeShort(0x0000); // Number of cells on the page, 2 byte int (short)
				davisbaseColumnsCatalog.writeShort(0x0000); // Start of the cell content area (by default 0x00 = 65536)
				davisbaseColumnsCatalog.writeInt(0x00000000); // Page number of sibling to the right (in creation there is no sibling)
				davisbaseColumnsCatalog.writeInt(0xFFFFFFFF); // Page number of parent (This is the root page so no parent [-1])
				davisbaseColumnsCatalog.writeByte(0x00); // Unused byte

				davisbaseColumnsCatalog.close();
			}
		} catch (Exception e) {
			out.println("Unable to create the database_columns file");
			out.println(e);
		}
	}

//	public static void main(String[] args) {
//		String query = "select * from davisbase_columns where rowid=2";
////		parseQuery(query);
//		String[] columns = getStringColumns("tbl_4");
//		for(String elm: columns){
//			out.println(elm);
//		}
//	}

}
