import java.io.RandomAccessFile;

public class Pages {
	static long pageSize = 512;


	public static short getCellCount(RandomAccessFile table, int pageNum) {
		try {
			table.seek(pageNum * pageSize + 2);
			return table.readShort();
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error getting cell number!!");
		}
		return 0;
	}

	/**
	 * Get cell address
	 *
	 * @param table : table
	 * @param page: page number
	 * @param cell: cell number
	 * @return
	 */
	public static short getCellAddress(RandomAccessFile table, int page, int cell) {
		try {
			table.seek(page * pageSize + 16 + 2 * cell);
			return (short) (page * pageSize + table.readShort());
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error getting cell location!!");
		}
		;
		return (short) 0;
	}

	public static int getPageCount(RandomAccessFile file) {
		int pages = 0;
		try {
			pages = (int) (file.length() / pageSize);
		} catch (Exception e) {
			System.out.println(e);
		}
		return pages;
	}

}
