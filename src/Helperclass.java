import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helperclass {
	static long pageSize = 512;
	final static String selectRegex = "(?i)SELECT\\s+.+\\s+FROM\\s+([\\w]+)\\s+WHERE(\\s+NOT)?(\\s+(ROWID)\\s+(>=|<=|<>|=|>|<)+(.+))";

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


	public static String[] getRecordData(RandomAccessFile table, short address) {
		return new String[1];
	}

	public static void displayRow(String[] row) {
		for (int i = 0; i < row.length; i++){
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
		if(operator == "="){
			flag = (input==currentValue)? true:false;
		}else if(operator == ">"){
			flag = (input > currentValue)? true:false;
		}else if(operator == ">="){
			flag = (input >= currentValue)? true:false;
		}else if(operator == "<"){
			flag = (input < currentValue)? true:false;
		}else if(operator == "<="){
			flag = (input <= currentValue)? true:false;
		}else if(operator == "<>"){
			flag = (input != currentValue)? true:false;
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
				short recordCount = Pages.getCellNumber(tableFile, page);

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
