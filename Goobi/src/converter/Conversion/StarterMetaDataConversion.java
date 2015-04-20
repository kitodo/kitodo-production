package converter.Conversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.apache.log4j.Logger;

import ugh.dl.DigitalDocument;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.ReadException;
import ugh.fileformats.excel.RDFFile;
import ugh.fileformats.mets.MetsMods;

import converter.logger.Commit;
import converter.logger.Filesave;
import converter.logger.Rollback;
import converter.logger.UGH;

@SuppressWarnings("deprecation")
public class StarterMetaDataConversion {

	protected static final Logger myLogger = Logger.getLogger(StarterMetaDataConversion.class);

	protected static final Logger mySaveLog = Logger.getLogger(Filesave.class);
	protected static final Logger myUghLog = Logger.getLogger(UGH.class);
	protected static final Logger myCommitLog = Logger.getLogger(Commit.class);
	protected static final Logger myRollbackLog = Logger.getLogger(Rollback.class);

	public static void main(String[] args) throws Exception {

		String basePath = getInput("Bitte den Grundpfad der Metadaten angeben (C:/XMLCheck/metas/ wird als Standard verwendet, wenn keine Eingabe erfolgt):");
		if (basePath.length() == 0) {
			basePath = "C:/XMLCheck/metas/";
		}

		Prefs pref = new Prefs();
		// here you put the path to the new valid ruleset
		String prefPath = getInput("Bitte den Pfad zum gueltigen Regelsatz angeben (ohne Angabe wird C:/XMLCheck/ruleset.xml verwendet):");
		if (prefPath.length() == 0) {
			prefPath = "C:/XMLCheck/ruleset.xml";
		}

		myLogger.info("Conversion Session started on sub direcories of " + basePath + "\r\n" + "ruleset '" + prefPath + "' is used");

		pref.loadPrefs(prefPath);
		myLogger.info("Loading ruleset '" + prefPath + "'");

		// subfolders contain meta.xml metadata
		MetadataWalker walker = new MetadataWalker(basePath);
		File procFile = null;
		Fileformat metsOutput = null;

		myLogger.info("Original files are backuped to meta.bak(n) - (highest number = latest backup)");
	
		for (Iterator<File> iterator = walker.iterator(); iterator.hasNext();) {

			Boolean flagError = false;
			Boolean flagErrorBackup = false;

			procFile = iterator.next();
			File newFile = new File(procFile.getParentFile().getAbsolutePath().replace("\\", "/") + "/" + "meta.bak");

			Integer i = 0;
			while (newFile.exists()) {
				newFile = new File(procFile.getParentFile().getAbsolutePath().replace("\\", "/") + "/" + "meta(" + ++i + ").bak");
			}

			try {
				copy(procFile, newFile);
				myLogger.info(procFile.getAbsolutePath() + " was copied to " + newFile.getAbsolutePath());
				mySaveLog.info(procFile.getAbsolutePath() + " was copied to " + newFile.getAbsolutePath());
			} catch (IOException e) {
				flagErrorBackup = true;
			}

			if (flagErrorBackup) {
				myLogger.info("Error creating backup. Processing of '" + newFile.getAbsolutePath() + "' cancelled");
				myRollbackLog.info(procFile.getAbsolutePath() + " - backup and processing cancelled");
			} else {

				Validators myValidators = new Validators();

				// now loading rdf format with ugh classes
				RDFFile rdfInput = new RDFFile(pref);
				myLogger.debug("loading:'" + newFile.getAbsolutePath() + "' as RDF in ugh");

				try {
					if (rdfInput.read(procFile.getAbsolutePath())) {

						metsOutput = new MetsMods(pref);
						metsOutput.setDigitalDocument(rdfInput.getDigitalDocument());

						// Sort first so equals method returns equals
						metsOutput.getDigitalDocument().getLogicalDocStruct().sortMetadata(pref);
						metsOutput.getDigitalDocument().getPhysicalDocStruct().sortMetadata(pref);

						rdfInput.getDigitalDocument().getLogicalDocStruct().sortMetadata(pref);
						rdfInput.getDigitalDocument().getPhysicalDocStruct().sortMetadata(pref);

						DigitalDocument digDoc1 = metsOutput.getDigitalDocument();
						DigitalDocument digDoc2 = rdfInput.getDigitalDocument();
						new Validator().validate(metsOutput, pref, newFile.getAbsolutePath()); 
						/*######## Begin of 1. Equals Validation  #########*/
						// if conversion doesn't generates equal digDoc
						if (!myValidators.getEqualsValidation(digDoc1, digDoc2)) {
							myLogger.info("File " + procFile.getAbsolutePath() + " is not equals to the original file, will not be written");
							myRollbackLog.info(newFile.getAbsolutePath() + " - mets digDoc is different - processing cancelled");
							flagError = true;
							// if conversion doesn't generates equal digDoc
						} else {
							myLogger.info("File " + procFile.getAbsolutePath() + " digital document is equal");
						}
						/*######## End of 1. Equals Validator  #########*/
						
						
					} else {
						myLogger.info("File " + procFile.getAbsolutePath() + " could not be read, will not be written");
						myRollbackLog.info(newFile.getAbsolutePath() + " - RDF couldn't be read - processing cancelled");
						flagError = true;
					}

				} catch (Exception e) {
					myLogger.debug("read error for file " + procFile.getAbsolutePath(), e);
					myRollbackLog.info(newFile.getAbsolutePath() + " - RDF couldn't be read - processing cancelled");
					flagError = true;
				}

				// comparing the digital documents

				if (!flagError) {
					try {
						if (!metsOutput.write(procFile.getAbsolutePath())) {

							myLogger.error("File " + procFile.getAbsolutePath() + " couldn't be written in mets format");

							myRollbackLog.info(newFile.getAbsolutePath() + " - Mets couldn't be saved - processing cancelled");
							flagError = true;
						} else {

							myLogger.debug("File " + procFile.getAbsolutePath() + " was written in Mets format");

							mySaveLog.info(procFile.getAbsolutePath() + " was written in Mets format");
						}

					} catch (Exception e) {

						myLogger.error("File " + procFile.getAbsolutePath() + " couldn't be written in mets format " + "Exception '" + e.getMessage()
								+ "' was thrown in ugh");

						myRollbackLog.info(newFile.getAbsolutePath() + " - Mets couldn't be saved - ugh Error - processing cancelled");

						myUghLog.info(newFile.getAbsolutePath() + " - Mets couldn't be saved - processing cancelled", e);

						flagError = true;
					}

					if (!flagError) {

						// reading in again just written file for comparism with
						// equals
						// method
						metsOutput = new MetsMods(pref);
						try {
							metsOutput.read(procFile.getAbsolutePath());
							Fileformat rdfCompare = new RDFFile(pref);

							rdfCompare.setDigitalDocument(metsOutput.getDigitalDocument());

							// Sort first so equals method returns equals
							rdfCompare.getDigitalDocument().getLogicalDocStruct().sortMetadata(pref);
							rdfCompare.getDigitalDocument().getPhysicalDocStruct().sortMetadata(pref);

							rdfInput.getDigitalDocument().getLogicalDocStruct().sortMetadata(pref);
							rdfInput.getDigitalDocument().getPhysicalDocStruct().sortMetadata(pref);

							DigitalDocument digDoc1 = rdfInput.getDigitalDocument();
							DigitalDocument digDoc2 = rdfCompare.getDigitalDocument();

							Boolean conversionFailure = false;

							// Validator
							
							if (myValidators.getEqualsValidation(digDoc1, digDoc2)) {
								myLogger.info("File " + procFile.getAbsolutePath() + " was successfully verified by equals validator in mets format");

								myCommitLog.info(procFile.getAbsolutePath()
										+ " was successfully written and verified by equals validator in mets format");
							} else {
								/* Validator turned off
								//conversionFailure = true;
								myLogger.info("File " + procFile.getAbsolutePath()
										+ " digital document from the reloaded mets is not equal to the originally loaded digital document");

								myRollbackLog.info(procFile.getAbsolutePath()
										+ " digital document from the reloaded mets is not equal to the originally loaded digital document");
								*/
							}
							
							//next line only if previous block is taken out from //validator
							//myCommitLog.info(procFile.getAbsolutePath()	+ " was NOT verified by equalsValidator in mets format");

							// writing reconverted rdf file and writing originally loaded rdf
							File fileA = new File(procFile.getAbsolutePath().replace(".xml", ".fromMets.rdf.xml"));
							File fileB = new File(procFile.getAbsolutePath().replace(".xml", ".orig.rdf.xml"));

							rdfCompare.write(fileA.getAbsolutePath());
							rdfInput.write(fileB.getAbsolutePath());
							
							
							/*
							// Validator 
							if (myValidators.getFileStringValidation(fileA, fileB)) {
								myLogger.info("File " + procFile.getAbsolutePath() + " was successfully verified by stringValidator in mets format");
								myCommitLog.info(procFile.getAbsolutePath()
										+ " was successfully written and verified by stringValidator in mets format");

							} else {
								conversionFailure = true;
								myLogger.info("File " + procFile.getAbsolutePath()
										+ " the file reloaded from mets, reconverted to rdf and saved as '" + fileA.getAbsolutePath() + "' is not equal to the originally read and saved '" + fileB.getAbsolutePath() + "'");

								myRollbackLog.info(procFile.getAbsolutePath()
										+ " the file reloaded from mets, reconverted to rdf and saved as '" + fileA.getAbsolutePath() + "' is not equal to the originally read and saved '" + fileB.getAbsolutePath() + "'");
							}
							*/

							myCommitLog.info(procFile.getAbsolutePath()	+ " stringValidator turned off - " + conversionFailure.toString());

							// Tokenizer Validation, using newFile which is the backup of the original file
							if (myValidators.getTokenizerValidation(newFile, fileA)) {

								myLogger.info("File " + procFile.getAbsolutePath()
										+ " was successfully written and verified by tokenizingValidator in mets format |###| tokenizer message -> " + FileCompare.getErrorMSG());

								myCommitLog.info(procFile.getAbsolutePath()
										+ " was successfully written and verified by tokenizingValidator in mets format |###| tokenizer message -> " + FileCompare.getErrorMSG());

							} else {
								conversionFailure = true;
								myLogger.info("File " + procFile.getAbsolutePath()
										+ " the file reloaded from mets, reconverted to rdf and saved as '" + fileA.getAbsolutePath() + "' is not equal to the originally read and saved '" + newFile.getAbsolutePath() + "'"
								+ "tokenizer returned  ->" + FileCompare.getErrorMSG());

								myRollbackLog.info(procFile.getAbsolutePath()
										+ " the file reloaded from mets, reconverted to rdf and saved as '" + fileA.getAbsolutePath() + "' is not equal to the originally read and saved '" + fileB.getAbsolutePath() + "'"
								+ "tokenizer returned  ->" + FileCompare.getErrorMSG());
							}
							
							if (conversionFailure){
								myRollbackLog.info(procFile.getAbsolutePath()  + " conversion couldn't satisfy validators");
								myCommitLog.info(procFile.getAbsolutePath() + " conversion couldn't satisfy validators");
							}

						} catch (ReadException e) {

							myLogger.info("File " + procFile.getAbsolutePath() + " could not be read into ugh with used ruleset " + prefPath);
							myRollbackLog.info(newFile.getAbsolutePath() + " - verify failed - mets was saved but couldn't get reloaded with ugh", e);
							myUghLog.info(newFile.getAbsolutePath() + " - verify failed - mets was saved but couldn't get reloaded with ugh", e);
						}
					}
				}
			}
		}
		myLogger.info("Conversion Session terminated normally on sub direcories of " + basePath + "\r\n" + "ruleset '" + prefPath + "' was used");

	}

	private static String getInput(String message) throws IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		System.out.print(message);
		input = in.readLine();
		return input;
	}

	// copy for backup
	private static void copy(File fileFrom, File fileTo) throws IOException {
		FileInputStream from = null;
		FileOutputStream to = null;

		try {
			from = new FileInputStream(fileFrom);
			to = new FileOutputStream(fileTo);
			byte[] buffer = new byte[16384];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1) {
				to.write(buffer, 0, bytesRead); // write
			}

			myLogger.debug("backup of file '" + fileFrom + "' written to '" + fileTo + "'");

		} finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) {
					;
				}
			if (to != null)
				try {
					to.close();
				} catch (IOException e) {
					;
				}
		}
	}
}
