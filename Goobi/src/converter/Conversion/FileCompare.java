package converter.Conversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.jdom.JDOMException;

public class FileCompare {

	private static String errorMsg = "";

	public static Boolean filesSameContent(File fileA, File fileB) throws IOException {

		return filesSameContent(fileA.getAbsolutePath(), fileB.getAbsolutePath());
	}

	public static Boolean filesSameContent(String fileA, String fileB) throws IOException {
		String cont1;
		String cont2;

		cont1 = readFileAsString(fileA);
		cont2 = readFileAsString(fileB);

		cont1 = cont1.trim();
		cont2 = cont2.trim();

		return cont1.equals(cont2);

	}

	private static String readFileAsString(String filePath) throws java.io.IOException {

		File file = new File(filePath);

		return FileUtils.readFileToString(file);
	}

	public static Boolean getTokenizerValidation(File fileA, File fileB) throws JDOMException, IOException {
		XmlCommentCleaner cleaner = new XmlCommentCleaner();
		errorMsg = "";
		File fileAClean = new File(fileA.getAbsolutePath().replace(".xml", ".Clean.xml").replace(".bak", ".Clean.xml"));
		File fileBClean = new File(fileB.getAbsolutePath().replace(".xml", ".Clean.xml").replace(".bak", ".Clean.xml"));
		cleaner.cleanCommentsFromXmlFile(fileA, fileAClean);
		cleaner.cleanCommentsFromXmlFile(fileB, fileBClean);

		StringTokenizer tok1 = getTokenizedString(fileAClean);
		StringTokenizer tok2 = getTokenizedString(fileBClean);

		return analyzeTokens(tok1, tok2);
	}

	public static String getErrorMSG() {
		return errorMsg;
	}

	private static Boolean analyzeTokens(StringTokenizer tok1, StringTokenizer tok2) {

		List<String> alist1 = new ArrayList<String>();
		List<String> alist2 = new ArrayList<String>();

		while (tok1.hasMoreElements()) {
			alist1.add(tok1.nextToken());
		}
		while (tok2.hasMoreElements()) {
			alist2.add(tok2.nextToken());
		}

		for (String elem1 : new ArrayList<String>(alist1)) {
			for (String elem2 : new ArrayList<String>(alist2)) {
				if (elem1.equals(elem2)) {
					alist1.remove(elem1);
					alist2.remove(elem2);
					break;
				}
			}
		}
		
		Boolean validated = false;
		String returnString = "";
		for (String elem1 : alist1) {
			returnString = returnString + elem1 + ";";
		}
		if (returnString.length() > 0) {
			returnString = " |remaining tokenizer content: |##### | " + returnString + "<- from original file| ### |";
		} else {
			returnString = " |no tokenizer content remained:|##### | " + returnString + "<- from original file| ### |";
			validated = true;
		}
		for (String elem2 : alist2) {
			returnString = returnString + elem2 + ";";
		}
		if (returnString.length() > 0) {
			returnString = returnString + " <- from converted file|";
		}

		errorMsg = returnString;

		return validated;

	}

	private static StringTokenizer getTokenizedString(File fileTokenize) throws IOException {
		String tokenize;
		String delim = "<> \t\n\r\f\\/;,";
		tokenize = readFileAsString(fileTokenize.getAbsolutePath());

		StringTokenizer tokenizer = new StringTokenizer(tokenize, delim);

		return tokenizer;

	}

}