import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HPClass{
	final static String updateRegex = "(?i)UPDATE\\s+([\\w]+)\\s+SET\\s+([\\w]+)\\s*(>=|<=|<>|=|>|<)+\\s*(.+)\\s+WHERE(\\s+(ROWID)\\s*(>=|<=|<>|=|>|<)+\\s*(.+))";
	public static Pattern updatePattern = Pattern.compile(updateRegex, Pattern.MULTILINE);
	/**
	 * Get update query parameters by matching to a regex
	 *
	 * @param string : update query string
	 * @return: map of all (parameter, value)
	 */
	public static Map<String, String> getCondition(String string) {

		string = string.trim();
		//using trim() instead of split()
		Map<String, String> map = new HashMap<>();
		Matcher matcher = updatePattern.matcher(string);
		boolean match = matcher.matches();
		if (match) {
			map.put("match", "yes");
		} else {
			map.put("match", "no");
		}
		String table_name = string.split(" ")[1];
		map.put("table_name", table_name.trim());
		String column_name = string.split(" ")[3];
		map.put("column_name", column_name.trim());
		String operator1 = string.split(" ")[4];
		map.put("operator1", operator1.trim());
		String operator1_value = string.split(" ")[5];
		map.put("operator1_value", operator1_value.trim());
		String operator2 = string.split(" ")[8];
		map.put("operator2", operator2.trim());
		String operator2_value = string.split(" ")[9];
		map.put("operator2_value", operator2_value.trim());
		return map;
	}

	public static void main(String[] args) {
		String a = "update table set rowid = 'hello' where rowid = 5";
		Map<String, String> map = HPClass.getCondition(a);
		for (String name : map.keySet())
			System.out.println(name + ": " + map.get(name));
	}
}
