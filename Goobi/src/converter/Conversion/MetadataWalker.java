package converter.Conversion;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

//class fetches all metadata files, provides them for the conversion and organizes backup and save 

public class MetadataWalker implements Iterator<File> {

	private static boolean flagInstanceExists;

	private ArrayList<String> mySubDirs = null;
	private String basePath = null; 
	private int index = 0;

	private MetadataWalker() throws Exception {
		if (flagInstanceExists) {
			throw new Exception("This class can only be instantiated once.");
		}
		flagInstanceExists = true;
	}

	public MetadataWalker(String filePath) throws Exception {
		this();
		File dir = new File(filePath);
		basePath = filePath;
		mySubDirs = new ArrayList<String>();
		for (String subDir : dir.list()) {
			if (new File(filePath + "/" + subDir + "/").isDirectory()) {
				mySubDirs.add(new String(subDir));
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		flagInstanceExists = false;
		super.finalize();
	}

	public Iterator<File> iterator() {
		index = 0;
		return this;
	}

	// Interface Iterator starts here
	public boolean hasNext() {

		if (mySubDirs.size() > index) {
			return true;
		}
		return false;
	}

	public File next() {
		return new File(basePath + "/" + mySubDirs.get(index++) + "/meta.xml");
	}

	public void remove() {
		throw new UnsupportedOperationException(
				"The method remove is not implemented for this Iterator");
	}

}
