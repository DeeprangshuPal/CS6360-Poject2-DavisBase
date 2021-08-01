
import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.*;
import java.util.regex.Pattern;

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

	/**
	 * Stub method for executing queries
	 *
	 * @param queryString is a String of the user input
	 */
	public static void parseQuery(String queryString) {
		out.println("STUB: This is the parseQuery method");
		out.println("\tParsing the string:\"" + queryString + "\"");
		try {
			Map<String,String> map = Helperclass.getCondition(queryString);
			if (map.get("match") == "yes"){
				String table_name = map.get("table_name");

				String[] columns = SchemaHelper.getColumns(table_name);


				String file_name = "data/" + table_name + ".tbl";
				RandomAccessFile table_file = new RandomAccessFile(file_name, "rw");

				String column = map.get("column");
				String operator = map.get("operator");
				String value = map.get("value");
				Helperclass.selectTable(file_name, Integer.parseInt(value), operator, columns);
			}else {
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
	}


	/**
	 * Stub method for creating new tables
	 */
	public static void parseCreateTable(String createTableString) {
		String s = createTableString;
		s = s.replace("(", " ");
		s = s.replace(")", " ");


		ArrayList<String> createTableTokens = new ArrayList<String>(Arrays.asList(s.split(" ")));

		/* Define table file name */
		String tableFileName = "data/" + createTableTokens.get(2) + ".tbl";

		/* YOUR CODE GOES HERE */
/*
		ArrayList<insert_data> meta_data_to_insert = new ArrayList<insert_data>();


		try{
			for(int i = 3; i < createTableTokens.size(); i+=2){
				col_names.add(createTableTokens.get(i));
				col_types.add(createTableTokens.get(i+1));
			}
		}
		catch(Exception e){
			out.println("Error :  did not define data types for all columns");
			return;
		}

*/
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
				// NOTE : THIS IS A PLACE HOLDER, RIGHT NOW THERE IS NO B+ TREE SO IT JUST ADDS TO THE FIRST PAGE
				ArrayList<String> data = new ArrayList<String>();
				data.add(createTableTokens.get(2));
				ArrayList<String> data_type = new ArrayList<String>();
				data_type.add("TEXT");
				insertIntoPage(0, davisbase_tables, data_type, data);

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
				if (data_types.get(i).toUpperCase().equals("TEXT")) {
					payload_size += data.get(i).getBytes().length;
				} else {
					payload_size += 2;
				}

				byte serial_code = getDataTypeCode(data_types.get(i));
				serial_code += (serial_code == TEXT) ? data.get(i).getBytes().length : 0; // If it is a text data type then the code is 0x0c + n
				record_header[i] = serial_code;
			}

			payload_size += data_types.size(); // Adds the size of the record header (just the number of types in bytes)

			int cell_size = payload_size + 2 + 4; // total size of the cell (payload+size of payload_size + row_id;


			/* Checks if there is enough space for the record*/
			long start_location = (page_location + cell_offset) - cell_size; // The location to start writing the record

			if (start_location < end_of_array + 2) {
				out.println("Error : not enough space in the page");
				return false;
			}

			/*Write the data to the table*/
			tableFile.seek(start_location);
			tableFile.writeShort(payload_size);
			tableFile.writeInt(row_id);

			increment_row_id(); //updates row_id

			tableFile.write(record_header);

			try {
				for (int i = 0; i < record_header.length; i++) {
					switch (record_header[i]) {
						case NULL:
							tableFile.writeByte(0x00);
							break;
						case TINYINT:
							tableFile.writeByte(Byte.parseByte(data.get(i)));
						case SMALLINT:
							tableFile.writeShort(Short.parseShort(data.get(i)));
						case INT:
							tableFile.writeInt(Integer.parseInt(data.get(i)));
						case BIGINT_LONG:
							tableFile.writeLong(Long.parseLong(data.get(i)));
						case DOUBLE:
							tableFile.writeDouble(Double.parseDouble(data.get(i)));
						default:
							tableFile.write(data.get(i).getBytes());
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
	 * @param type : data type you want the code for
	 * @return : returns the serial code of the data type
	 */
	static byte getDataTypeCode(String type) {
		type = type.toUpperCase();
		switch (type) {
			case "NULL":
				return NULL;
			case "TINTINT":
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

}