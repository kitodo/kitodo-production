package converter.processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

public class MetaFixer implements Validatable {
	private static final Logger logger = Logger.getLogger(MetaFixer.class);
	private boolean isChanged = false;
	private String fileName = null;
	private String backupName = null;
	private Document myDoc = null;

	public MetaFixer(String inId, String inPath) {
		logger.debug("Constructor started");
		isChanged = false;
		fileName = inPath + inId + "/meta.xml";
		backupName = inPath + inId + "/meta.xml.bak";
	}

	public boolean replace() throws JDOMException, IOException {
		logger.debug("start replacing in " + fileName);
		SAXBuilder sxbuild = new SAXBuilder();
		InputSource is = new InputSource(fileName);
		
		try{
			myDoc = sxbuild.build(is);
		}catch (FileNotFoundException e){
			logger.debug("File listed in usedRuleset.xml was not found - program abnormally terminated", e);
			throw e;
		}
		
		
		Namespace ns = Namespace.getNamespace("AGORA", "GDZ:DMSDB-Semantics");

		replaceElementsRecursively(myDoc.getRootElement(),  "AGORA:Monograph",
				"AGORA:Manuscript", ns);
		replaceElementsRecursively(myDoc.getRootElement(),
				"AGORA:MultiVolumeWork", "AGORA:MultiPartManuscript", ns);
		replaceElementsRecursively(myDoc.getRootElement(), "AGORA:Volume",
				"AGORA:PartOfManuscript", ns);

		if (isChanged) {
			logger.info("file changed, save it now - name of changed file is " + fileName);
			saveFile();
			return true;
		}
		else{
			return false;
		}
	}
/* 
 * 
 */
	private void replaceElementsRecursively(Element parent, String startName,
			String endName, Namespace ns) throws FileNotFoundException,
			IOException {
		// check current element and replace
		String type = parent.getAttributeValue("Type", ns);
		String name = parent.getName();
		if (name.equals("DocStrct") && type.equals(startName)) {
			if (!isChanged) {
				saveBackup();
				isChanged = true;
			}
			parent.setAttribute("Type", endName, ns);
		}

		// call method recursive
		for (Object oe : parent.getChildren()) {
			replaceElementsRecursively((Element) oe, startName, endName, ns);
		}
	}

	private void saveBackup() throws FileNotFoundException, IOException {
		save(backupName);
	}

	private void saveFile() throws FileNotFoundException, IOException {
		save(fileName);
		logger.debug("Datei wurde gespeichert");
	}

	private void save(String inPath) throws FileNotFoundException, IOException {
		logger.debug("save " + inPath);
		XMLOutputter outp = new XMLOutputter();
		outp.setFormat(Format.getPrettyFormat());
		outp.output(myDoc, new FileOutputStream(new File(inPath)));
	}

	/*
	 * Validates after processing and saving if saved
	 * checks occurrences of replaced strings 
	 * @see com.intranda.goobi.metadataprocessing.Validatable#validate()
	 */
	public void validate() throws ContentLibException {
		logger.debug("start validating");
		String readFile = null;
		try {
			File file = new File(fileName);
			readFile = FileUtils.readFileToString(file);
		} catch (FileNotFoundException e) {
			throw new ContentLibException("Error reading file '" + fileName
					+ "'", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("file exists " + fileName);

		String searchString = "AGORA:Monograph";
		if (readFile.split(searchString).length > 1) {
			throw new ContentLibException("Found '" + searchString
					+ "' in file '" + fileName + "'");
		}
		logger.debug(fileName + " clean of 'AGORA:Monograph'");

		searchString = "AGORA:MultiVolumeWork";
		if (readFile.split(searchString).length > 1) {
			throw new ContentLibException("Found '" + searchString
					+ "' in file '" + fileName + "'");
		}
		logger.debug(fileName + " clean of 'AGORA:MultiVolumeWork'");
	
		searchString = "AGORA:Volume";
		if (readFile.split(searchString).length > 1) {
			throw new ContentLibException("Found '" + searchString
					+ "' in file '" + fileName + "'");
		}
		logger.debug(fileName + " clean of 'AGORA:Volume'");

	}

	public void setBaseFolder(String path) {
		// implementation not necessary in this class

	}

	public void setID(String id) {
		// implementation not necessary in this class

	}

	public void setSearchString(String searchExpression) {
		// implementation not necessary in this class

	}

}
