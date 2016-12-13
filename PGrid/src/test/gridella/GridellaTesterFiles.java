package test.gridella;

import java.io.*;
import java.security.SecureRandom;

public class GridellaTesterFiles {

	private static final String NAMES_FILE = "Files-gridella.dat";

	private static final String SHARED_EXT = "gif;htm;html;jpg;pdf;ps;tar.gz;txt;zip";

	private static final int VALUES = 35000;

	public static final String[] mNames = new String[VALUES];

	private final SecureRandom rnd = new SecureRandom();

	private int mNameCount = 0;

	public GridellaTesterFiles() {
		try {
			// read file names
			InputStream inStream = getClass().getResourceAsStream("/" + NAMES_FILE);
			BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
			String inputLine;
			mNameCount = 0;
			while ((inputLine = in.readLine()) != null) {
				mNames[mNameCount++] = inputLine;
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	void createFiles(String path) {
		File root = new File(path);
		if (!root.exists()) {
			new IllegalArgumentException("File path does not exist!");
		}
		if (!root.isDirectory()) {
			new IllegalArgumentException("File path is not a directory!");
		}
		File file = new File(NAMES_FILE);
		try {
			FileWriter writer = new FileWriter(file);
			readFiles(writer, root, path);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	private void readFiles(FileWriter writer, File root, String path) throws IOException {
		File[] files = root.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				String fileName = files[i].getPath().substring(path.length() + 1);
				String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
				if ((SHARED_EXT.indexOf(";" + extension + ";")) < 0)
					continue;
				String filePath = fileName.substring(0, fileName.lastIndexOf(System.getProperty("file.separator")));
				writer.write(filePath + "\t" + fileName.substring(filePath.length() + 1) + "\t" + Long.toString(files[i].length()) + "\n");
			} else if (files[i].isDirectory()) {
				readFiles(writer, files[i], path);
			}
		}
	}

	public String[] uniformFilenames(int amount) {
		String[] files = new String[amount];
		int[] index = new int[amount];
		for (int i = 0; i < amount; i++) {
			index[i] = rnd.nextInt(mNameCount);
			files[i] = mNames[index[i]];
		}
		return files;
	}

}