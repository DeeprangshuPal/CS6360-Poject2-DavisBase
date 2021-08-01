import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class SchemaHelper {
	public static final String davisbaseColumnsTable = "data/davisbase_columns.tbl";
//	public static final String tableNameColumn = "table_name";


	public static String[] selectColumns(String columnFile, String tableName) {
		List<String> columns = new ArrayList<>();
		try {
			RandomAccessFile tableFile = new RandomAccessFile(columnFile, "rw");
			int pageCount = Pages.getPageCount(tableFile);
			System.out.println("Page count: " + pageCount);

			for (int page = 0; page < pageCount; page++) {
				// TODO check if the it's a leaf page
				short recordCount = Pages.getCellCount(	tableFile, page);
				System.out.println("Record count: "+ recordCount);

				for (int j = 0; j < recordCount; j++) {
					short cell_offset = Pages.getCellAddress(tableFile, page, j);

					// go to record location. +2 for the payload size
					tableFile.seek(cell_offset + 2);

					// compare tableName to value
					String[] rc = Filter.getRecordData(tableFile, cell_offset);

					System.out.println("Table name column value in Davisbase_columns: "+rc[0]);
					if(rc[0].equalsIgnoreCase(tableName)){
						columns.add(rc[1]);
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		String[] result;
		result = columns.toArray(new String[columns.size()]);
		return result;
	}

	//TODO get column names from davidbase_columns
	public static String[] getColumns(String tableName){
		String[] columns ;
		try {
			columns = selectColumns(davisbaseColumnsTable, tableName);
		}catch (Exception e) {
			System.out.println(e);
			System.out.println("Error in getting column ");
			columns = new String[]{"Default_column"};
		}
		return columns;
	}

	public static void main(String[] args) {
		String[] columns = getColumns("1");
		for(int i=0; i<columns.length; i++){
			System.out.println(columns[i]);
		}
	}



}
