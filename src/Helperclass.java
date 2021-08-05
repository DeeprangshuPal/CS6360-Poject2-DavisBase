import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helperclass{
	static long pageSize = 512;
	final static String selectRegex = "(?i)SELECT\\s+.+\\s+FROM\\s+([\\w]+)(\\s+WHERE(\\s+NOT)?(\\s+(ROWID)\\s*(>=|<=|<>|=|>|<)+\\s*(.+)))?";
	public static final String datePattern = "yyyy-MM-dd_HH:mm:ss";
	public static String showRegex = "(?i)SHOW\\s+TABLES";
	public static Pattern selectPattern = Pattern.compile(selectRegex, Pattern.MULTILINE);
	public static Pattern showPattern = Pattern.compile(showRegex, Pattern.MULTILINE);

	/**
	 * Get select query parameters by matching to a regex
	 *
	 * @param string : select query string
	 * @return: map of all (parameter, value)
	 */
	public static Map<String, String> getCondition(String string) {

		string = string.trim();
		Map<String, String> map = new HashMap<>();
		Matcher matcher = selectPattern.matcher(string);
		boolean match = matcher.matches();
		if (match) {
			map.put("match", "yes");
		} else {
			map.put("match", "no");
		}
		matcher = selectPattern.matcher(string);
		String table_name = string.split(" ")[2];
		map.put("table_name", table_name.trim());
		while (matcher.find()) {

			for (int i = 1; i <= matcher.groupCount(); i++) {
				String key = null;
				if (i == 5) {
					key = "column";
				} else if (i == 6) {
					key = "operator";
				} else if (i == 7) {
					key = "value";
				} else if (i == 1) {
					key = "table_name";
				}else if (i ==2){
					key = "condition";
				}
				if (key != null) {
					String val = matcher.group(i);
					if (val != null) {
						map.put(key, val.strip());
					}else{
						map.put(key, "");
					}
				}
			}
		}
		return map;
	}

	public static boolean validateShowCommand(String string){
		Matcher matcher = showPattern.matcher(string);
		return matcher.matches();
	}


	public static void main(String[] args) {
		String a = "select * from table where not rowid <= 5";
		Helperclass hp = new Helperclass();
		Map<String, String> map = hp.getCondition(a);
		for (String name : map.keySet())
			System.out.println(name + ": " + map.get(name));
	}
}
