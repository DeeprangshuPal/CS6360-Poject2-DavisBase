import java.io.RandomAccessFile;

public class Pages {
	static long pageSize = 512;


	public static short getCellNumber(RandomAccessFile file, int pageNum) {
		//TODO implement
		return (short) 1;
	}


	public static short getCellAddress(RandomAccessFile file, int page, int cell){
		return (short) 1;
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
