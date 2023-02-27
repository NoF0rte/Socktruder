package burp;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.Scanner;

public class Util {
	public static List<String> readLines(String path) throws Exception {
		List<String> lines = new ArrayList<String>();

		File file = new File(path);
		Scanner reader = new Scanner(file);
		while (reader.hasNextLine()) {
			lines.add(reader.nextLine());
		}
		reader.close();

		return lines;
	}
}
