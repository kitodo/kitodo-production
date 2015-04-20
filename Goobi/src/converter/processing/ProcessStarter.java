package converter.processing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;

/**
 * @author Wulf
 * 
 */

public class ProcessStarter {
	private static final Logger myLogger = Logger
			.getLogger(ProcessStarter.class);

	/**
	 * @param args
	 * @throws JDOMException
	 * @throws IOException
	 * @throws ConfigurationException
	 * @throws ContentLibException
	 * @throws ConfigurationException
	 */
	public static void main(String[] args) throws IOException, JDOMException,
			ContentLibException, ConfigurationException, Exception {

		String basePath = getInput("Bitte den Grundpfad der Metadaten angeben (" + "././docs/metadata" + " wird ohne weitere Eingabe verwendet):");
		if (basePath.length() == 0){
			basePath = "c:/workspace/GoobiXMLProcessor/docs/metadata/";
		}

		String usedRulesSetPath = getInput("Bitte den Pfad zu usedRulesSet.xml angeben (" + "././docs/auxData/usedRuleSets.xml" + " wird ohne weitere Eingabe verwendet):");
		if (usedRulesSetPath.length() == 0)
			usedRulesSetPath = "c:/workspace/GoobiXMLProcessor/docs/auxData/usedRuleSets.xml";

		myLogger.info("File " + usedRulesSetPath + " is used as usedRulesSet,\n\t metadata folder will be looked up in " + basePath + ".");

		List<String> myIds = UsedRuleSetParser.getIds(usedRulesSetPath);

		myLogger
				.info("The following ids match the search pattern and will be examined for the occurrence of replacement conditions:"
						+ myIds);

		try {

			// validate filepath
			for (String id1 : myIds) {
				myLogger.debug("validating filepath id:" + id1);
				Validatable validateFP = new ValidateFilePath();
				validateFP.setBaseFolder(basePath);
				validateFP.setID(id1);
			}

		}
		// if caught here filepath validator had failed
		catch (Exception e) {
			myLogger
					.fatal("The program was terminated with an exception before examining any metadata because an inconsistency was detected in the filesystem. Folders supposed to be existent according to usedRules.xml or contained files within (meta.xml) are missing."
							+ e.getStackTrace());
			throw e;
		}

		List<String> myFixedIDs = new ArrayList<String>();

		// fix metadata and validate
		try {
			for (String id2 : myIds) {
				myLogger.debug("processing id:" + id2);
				MetaFixer fixMeta = new MetaFixer(id2, basePath);
				//try{
					if (fixMeta.replace()) {
							myFixedIDs.add(id2);
					}
					fixMeta.validate();
				//} catch (Exception e) {
					// TODO: take this error handler out before running in Dresden
					//myLogger.debug("probably missing file - error message is"
					//		+ e.getMessage());
				//}
			}
		}catch (FileNotFoundException e){
		// validation of fix failed
			myLogger
			.fatal("The program was terminated with an exception after replacement in metadata and still detecting replaceable strings in meta.xml."
					+ e.getStackTrace());
			throw e;
		
		}catch (Exception e) {
			myLogger
					.fatal("The program was terminated with an exception after replacement in metadata and still detecting replaceable strings in meta.xml."
							+ e.getStackTrace());
			throw e;
		}

		try {
			for (String id3 : myFixedIDs) {
				myLogger.debug("validating meta.xml.bak id:" + id3);
				Validatable validateMetaBackup = new ValidateBackup();
				validateMetaBackup.setBaseFolder(basePath);
				validateMetaBackup.setID(id3);
			}
		}

		catch (Exception e) {
			myLogger
					.fatal("The program was terminated with an exception after replacement in metadata and detecting missing backup files of the original data."
							+ e.getStackTrace());
			throw e;
		}

		myLogger
				.info("Program terminated normally. The presented data was examined and replaced in case of occurrences of the conditions required");

		/*
		 * - regex - vorhandensein der ordner - vorhandensein der meta.xml -
		 * nach der durchführung vorhandensein der backups bei den hits - nach
		 * der durchführung bei hits unterschiedliche inhalte (via regex) (erste
		 * regex kann ja allgemein auch prüfen, ob kriterium überhaupt zutrifft
		 * - wo also der xml-parser nicht reagiert hat) Rückwärtssuche
		 */
		// XMLAttributeProcessor usedRules = new XMLAttributeProcessor();
		//
		// String fileName = new String(usedRulesSetPath);
		//
		// List<String> args1 = new ArrayList<String>();
		//
		// args1.add(fileName);
		// /* args1.add(new String("DocStrct"));
		// args1.add(new String("AGORA:Type"));
		// args1.add(new String("AGORA:Monograph"));*/
		//
		// args1.add(new String("ruleset"));
		// args1.add(new String("title"));
		// args1.add(new String("manuscript_test"));
		// args1.add(new String("process"));
		// args1.add(new String("isTemplate"));
		// args1.add(new String("false"));
		//
		// //System.out.println(usedRules.getElementList(true, null,
		// args1).size());
		//
		// List<Element> elements = usedRules.getElementList(false, null, args1,
		// null);
		//
		// if (elements == null) {
		// myLogger.error("usedRulesSet.xml not found for processing");
		// return;
		// }
		//
		// for (Element e : elements) {
		// myLogger.debug("processing id:" + e.getAttributeValue("id"));
		// MetaFixer test = new MetaFixer(e.getAttributeValue("id"), basePath);
		// test.replace();
		// }
	}

	private static String getInput(String message) throws IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		System.out.print(message);
		input = in.readLine();
		return input;
	}
}