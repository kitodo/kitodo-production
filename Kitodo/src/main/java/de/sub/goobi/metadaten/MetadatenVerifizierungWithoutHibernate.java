/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package de.sub.goobi.metadaten;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.persistence.apache.FolderInformation;
import de.sub.goobi.persistence.apache.ProcessManager;
import de.sub.goobi.persistence.apache.ProcessObject;
import de.sub.goobi.persistence.apache.ProjectManager;
import de.sub.goobi.persistence.apache.ProjectObject;

public class MetadatenVerifizierungWithoutHibernate {
	List<DocStruct> docStructsOhneSeiten;
	boolean autoSave = false;

	private String title;

	public boolean validate(Prozess inProzess) {
		Prefs myPrefs = inProzess.getRegelsatz().getPreferences();
		/*
		 * -------------------------------- Fileformat einlesen --------------------------------
		 */
		Fileformat gdzfile;
		try {
			gdzfile = inProzess.readMetadataFile();
		} catch (Exception e) {
			Helper.setFehlerMeldung(Helper.getTranslation("MetadataReadError") + this.title, e.getMessage());
			return false;
		}
		return validate(gdzfile, myPrefs, inProzess.getId(), this.title);
	}

	public boolean validate(Fileformat gdzfile, Prefs inPrefs, int processId, String title) {
		ProcessObject process = ProcessManager.getProcessObjectForId(processId);
		ProjectObject project = ProjectManager.getProjectById(process.getProjekteID());
		FolderInformation fi = new FolderInformation(processId, process.getTitle());
		this.title = title;
		String metadataLanguage = (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}");
		boolean ergebnis = true;

		DigitalDocument dd = null;
		try {
			dd = gdzfile.getDigitalDocument();
		} catch (Exception e) {
			Helper.setFehlerMeldung(Helper.getTranslation("MetadataDigitalDocumentError") + title, e.getMessage());
			return false;
		}

		DocStruct logical = dd.getLogicalDocStruct();
		if (logical.getAllIdentifierMetadata() != null && logical.getAllIdentifierMetadata().size() > 0) {
			Metadata identifierTopStruct = logical.getAllIdentifierMetadata().get(0);
			try {
				if (!identifierTopStruct.getValue().replaceAll(ConfigMain.getParameter("validateIdentifierRegex", "[\\w|-]"), "").equals("")) {
					Helper.setFehlerMeldung(Helper.getTranslation("MetadataIdentifierError")
							+ identifierTopStruct.getType().getNameByLanguage(metadataLanguage) + " in DocStruct "
							+ logical.getType().getNameByLanguage(metadataLanguage) + Helper.getTranslation("MetadataInvalidCharacter"));
					ergebnis = false;
				}
				DocStruct firstChild = logical.getAllChildren().get(0);
				Metadata identifierFirstChild = firstChild.getAllIdentifierMetadata().get(0);
				if (identifierTopStruct.getValue() != null && !identifierTopStruct.getValue().isEmpty()
						&& identifierTopStruct.getValue().equals(identifierFirstChild.getValue())) {
					Helper.setFehlerMeldung(Helper.getTranslation("MetadataIdentifierError") + identifierTopStruct.getType().getName()
							+ Helper.getTranslation("MetadataIdentifierSame") + logical.getType().getName() + " and "
							+ firstChild.getType().getName());
					ergebnis = false;
				}
				if (!identifierFirstChild.getValue().replaceAll(ConfigMain.getParameter("validateIdentifierRegex", "[\\w|-]"), "").equals("")) {
					Helper.setFehlerMeldung(Helper.getTranslation("MetadataIdentifierError") + identifierFirstChild.getType().getName()
							+ " in DocStruct " + firstChild.getType().getName() + Helper.getTranslation("MetadataInvalidCharacter"));
					ergebnis = false;
				}
			} catch (Exception e) {
				// no firstChild or no identifier
			}
		} else {
			Helper.setFehlerMeldung(Helper.getTranslation("MetadataMissingIdentifier"));
			ergebnis = false;
		}
		/*
		 * -------------------------------- PathImagesFiles prüfen --------------------------------
		 */
		if (!this.isValidPathImageFiles(dd.getPhysicalDocStruct(), inPrefs)) {
			ergebnis = false;
		}

		/*
		 * -------------------------------- auf Docstructs ohne Seiten prüfen --------------------------------
		 */
		DocStruct logicalTop = dd.getLogicalDocStruct();
		this.docStructsOhneSeiten = new ArrayList<DocStruct>();
		if (logicalTop == null) {
			Helper.setFehlerMeldung(title + ": " + Helper.getTranslation("MetadataPaginationError"));
			ergebnis = false;
		} else {
			this.checkDocStructsOhneSeiten(logicalTop);
		}

		if (this.docStructsOhneSeiten.size() != 0) {
			for (Iterator<DocStruct> iter = this.docStructsOhneSeiten.iterator(); iter.hasNext();) {
				DocStruct ds = iter.next();
				Helper.setFehlerMeldung(title + ": " + Helper.getTranslation("MetadataPaginationStructure")
						+ ds.getType().getNameByLanguage(metadataLanguage));
			}
			ergebnis = false;
		}

		/*
		 * -------------------------------- auf Seiten ohne Docstructs prüfen --------------------------------
		 */
		List<String> seitenOhneDocstructs = null;
		try {
			seitenOhneDocstructs = checkSeitenOhneDocstructs(gdzfile);
		} catch (PreferencesException e1) {
			Helper.setFehlerMeldung("[" + title + "] Can not check pages without docstructs: ");
			ergebnis = false;
		}
		if (seitenOhneDocstructs != null && seitenOhneDocstructs.size() != 0) {
			for (Iterator<String> iter = seitenOhneDocstructs.iterator(); iter.hasNext();) {
				String seite = iter.next();
				Helper.setFehlerMeldung(title + ": " + Helper.getTranslation("MetadataPaginationPages"), seite);
			}
			ergebnis = false;
		}

		/*
		 * -------------------------------- auf mandatory Values der Metadaten prüfen --------------------------------
		 */
		List<String> mandatoryList = checkMandatoryValues(dd.getLogicalDocStruct(), new ArrayList<String>(), metadataLanguage);
		if (mandatoryList.size() != 0) {
			for (Iterator<String> iter = mandatoryList.iterator(); iter.hasNext();) {
				String temp = iter.next();
				Helper.setFehlerMeldung(title + ": " + Helper.getTranslation("MetadataMandatoryElement"), temp);
			}
			ergebnis = false;
		}

		/*
		 * -------------------------------- auf Details in den Metadaten prüfen, die in der Konfiguration angegeben wurden
		 * --------------------------------
		 */
		List<String> configuredList = checkConfiguredValidationValues(dd.getLogicalDocStruct(), new ArrayList<String>(), inPrefs, metadataLanguage,
				project);
		if (configuredList.size() != 0) {
			for (Iterator<String> iter = configuredList.iterator(); iter.hasNext();) {
				String temp = iter.next();
				Helper.setFehlerMeldung(title + ": " + Helper.getTranslation("MetadataInvalidData"), temp);
			}
			ergebnis = false;
		}

		MetadatenImagesHelper mih = new MetadatenImagesHelper(inPrefs, dd);
		try {
			if (!mih.checkIfImagesValid(title, fi.getImagesTifDirectory(true))) {
				ergebnis = false;
			}
		} catch (Exception e) {
			Helper.setFehlerMeldung(title + ": ", e);
			ergebnis = false;
		}

		/*
		 * -------------------------------- Metadaten ggf. zum Schluss speichern --------------------------------
		 */

		try {
			List<String> images = fi.getDataFiles();
			if (images != null) {
				int sizeOfPagination = dd.getPhysicalDocStruct().getAllChildren().size();
				int sizeOfImages = images.size();
				if (sizeOfPagination != sizeOfImages) {
					List<String> param = new ArrayList<String>();
					param.add(String.valueOf(sizeOfPagination));
					param.add(String.valueOf(sizeOfImages));
					Helper.setFehlerMeldung(Helper.getTranslation("imagePaginationError", param));
					return false;
				}
			}
		} catch (InvalidImagesException e1) {
			Helper.setFehlerMeldung(process.getTitle() + ": ", e1);
			ergebnis = false;
		}

		try {
			if (this.autoSave) {
				process.writeMetadataFile(gdzfile, fi.getMetadataFilePath(), inPrefs, project.getFileFormatInternal());
			}
		} catch (Exception e) {
			Helper.setFehlerMeldung("Error while writing metadata: " + title, e);
		}
		return ergebnis;
	}

	private boolean isValidPathImageFiles(DocStruct phys, Prefs myPrefs) {
		try {
			MetadataType mdt = UghHelper.getMetadataType(myPrefs, "pathimagefiles");
			List<? extends Metadata> alleMetadaten = phys.getAllMetadataByType(mdt);
			if (alleMetadaten != null && alleMetadaten.size() > 0) {
				@SuppressWarnings("unused")
				Metadata mmm = alleMetadaten.get(0);

				return true;
			} else {
				Helper.setFehlerMeldung(this.title + ": " + "Can not verify, image path is not set", "");
				return false;
			}
		} catch (UghHelperException e) {
			Helper.setFehlerMeldung(this.title + ": " + "Verify aborted, error: ", e.getMessage());
			return false;
		}
	}

	private void checkDocStructsOhneSeiten(DocStruct inStruct) {
		if (inStruct.getAllToReferences().size() == 0 && inStruct.getType().getAnchorClass() == null) {
			this.docStructsOhneSeiten.add(inStruct);
		}
		/* alle Kinder des aktuellen DocStructs durchlaufen */
		if (inStruct.getAllChildren() != null) {
			for (Iterator<DocStruct> iter = inStruct.getAllChildren().iterator(); iter.hasNext();) {
				DocStruct child = iter.next();
				checkDocStructsOhneSeiten(child);
			}
		}
	}

	private List<String> checkSeitenOhneDocstructs(Fileformat inRdf) throws PreferencesException {
		List<String> rueckgabe = new ArrayList<String>();
		DocStruct boundbook = inRdf.getDigitalDocument().getPhysicalDocStruct();
		/* wenn boundbook null ist */
		if (boundbook == null || boundbook.getAllChildren() == null) {
			return rueckgabe;
		}

		/* alle Seiten durchlaufen und prüfen ob References existieren */
		for (Iterator<DocStruct> iter = boundbook.getAllChildren().iterator(); iter.hasNext();) {
			DocStruct ds = iter.next();
			List<Reference> refs = ds.getAllFromReferences();
			String physical = "";
			String logical = "";
			if (refs.size() == 0) {

				for (Iterator<Metadata> iter2 = ds.getAllMetadata().iterator(); iter2.hasNext();) {
					Metadata md = iter2.next();
					if (md.getType().getName().equals("logicalPageNumber")) {
						logical = " (" + md.getValue() + ")";
					}
					if (md.getType().getName().equals("physPageNumber")) {
						physical = md.getValue();
					}
				}
				rueckgabe.add(physical + logical);
			}
		}
		return rueckgabe;
	}

	private List<String> checkMandatoryValues(DocStruct inStruct, ArrayList<String> inList, String language) {
		DocStructType dst = inStruct.getType();
		List<MetadataType> allMDTypes = dst.getAllMetadataTypes();
		for (MetadataType mdt : allMDTypes) {
			String number = dst.getNumberOfMetadataType(mdt);
			List<? extends Metadata> ll = inStruct.getAllMetadataByType(mdt);
			int real = 0;
			real = ll.size();

			if ((number.equals("1m") || number.equals("+")) && real == 1 && (ll.get(0).getValue() == null || ll.get(0).getValue().equals(""))) {
				inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
						+ Helper.getTranslation("MetadataIsEmpty"));
			}
			/* jetzt die Typen prüfen */
			if (number.equals("1m") && real != 1) {
				inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
						+ Helper.getTranslation("MetadataNotOneElement") + " " + real + Helper.getTranslation("MetadataTimes"));
			}
			if (number.equals("1o") && real > 1) {
				inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
						+ Helper.getTranslation("MetadataToManyElements") + " " + real + " " + Helper.getTranslation("MetadataTimes"));
			}
			if (number.equals("+") && real == 0) {
				inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
						+ Helper.getTranslation("MetadataNotEnoughElements"));
			}
		}
		// }
		/* alle Kinder des aktuellen DocStructs durchlaufen */
		if (inStruct.getAllChildren() != null) {
			for (DocStruct child : inStruct.getAllChildren()) {
				checkMandatoryValues(child, inList, language);
			}
		}
		return inList;
	}

	/**
	 * individuelle konfigurierbare projektspezifische Validierung der Metadaten ================================================================
	 */
	private List<String> checkConfiguredValidationValues(DocStruct inStruct, ArrayList<String> inFehlerList, Prefs inPrefs, String language,
			ProjectObject project) {
		/*
		 * -------------------------------- Konfiguration öffnen und die Validierungsdetails auslesen --------------------------------
		 */
		ConfigProjects cp = null;
		try {
			cp = new ConfigProjects(project.getTitel());
		} catch (IOException e) {
			Helper.setFehlerMeldung("[" + this.title + "] " + "IOException", e.getMessage());
			return inFehlerList;
		}
		int count = cp.getParamList("validate.metadata").size();
		for (int i = 0; i < count; i++) {

			/* Attribute auswerten */
			String prop_metadatatype = cp.getParamString("validate.metadata(" + i + ")[@metadata]");
			String prop_doctype = cp.getParamString("validate.metadata(" + i + ")[@docstruct]");
			String prop_startswith = cp.getParamString("validate.metadata(" + i + ")[@startswith]");
			String prop_endswith = cp.getParamString("validate.metadata(" + i + ")[@endswith]");
			String prop_createElementFrom = cp.getParamString("validate.metadata(" + i + ")[@createelementfrom]");
			DocStruct myStruct = inStruct;
			MetadataType mdt = null;
			try {
				mdt = UghHelper.getMetadataType(inPrefs, prop_metadatatype);
			} catch (UghHelperException e) {
				Helper.setFehlerMeldung("[" + this.title + "] " + "Metadatatype does not exist: ", prop_metadatatype);
			}
			/*
			 * wenn das Metadatum des FirstChilds überprüfen werden soll, dann dieses jetzt (sofern vorhanden) übernehmen
			 */
			if (prop_doctype != null && prop_doctype.equals("firstchild")) {
				if (myStruct.getAllChildren() != null && myStruct.getAllChildren().size() > 0) {
					myStruct = myStruct.getAllChildren().get(0);
				} else {
					continue;
				}
			}

			/*
			 * wenn der MetadatenTyp existiert, dann jetzt die nötige Aktion überprüfen
			 */
			if (mdt != null) {
				/* ein CreatorsAllOrigin soll erzeugt werden */
				if (prop_createElementFrom != null) {
					ArrayList<MetadataType> listOfFromMdts = new ArrayList<MetadataType>();
					StringTokenizer tokenizer = new StringTokenizer(prop_createElementFrom, "|");
					while (tokenizer.hasMoreTokens()) {
						String tok = tokenizer.nextToken();
						try {
							MetadataType emdete = UghHelper.getMetadataType(inPrefs, tok);
							listOfFromMdts.add(emdete);
						} catch (UghHelperException e) {

						}
					}
					if (listOfFromMdts.size() > 0) {
						checkCreateElementFrom(inFehlerList, listOfFromMdts, myStruct, mdt, language);
					}
				} else {
					checkStartsEndsWith(inFehlerList, prop_startswith, prop_endswith, myStruct, mdt, language);
				}
			}
		}
		return inFehlerList;
	}

	/**
	 * Create Element From - für alle Strukturelemente ein bestimmtes Metadatum erzeugen, sofern dies an der jeweiligen Stelle erlaubt und noch nicht
	 * vorhanden ================================================================
	 */
	private void checkCreateElementFrom(ArrayList<String> inFehlerList, ArrayList<MetadataType> inListOfFromMdts, DocStruct myStruct,
			MetadataType mdt, String language) {

		/*
		 * -------------------------------- existiert das zu erzeugende Metadatum schon, dann überspringen, ansonsten alle Daten zusammensammeln und
		 * in das neue Element schreiben --------------------------------
		 */
		List<? extends Metadata> createMetadaten = myStruct.getAllMetadataByType(mdt);
		if (createMetadaten == null || createMetadaten.size() == 0) {
			try {
				Metadata createdElement = new Metadata(mdt);
				StringBuffer myValue = new StringBuffer();
				/*
				 * alle anzufügenden Metadaten durchlaufen und an das Element anhängen
				 */
				for (MetadataType mdttemp : inListOfFromMdts) {

					List<Person> fromElemente = myStruct.getAllPersons();
					if (fromElemente != null && fromElemente.size() > 0) {
						/*
						 * wenn Personen vorhanden sind (z.B. Illustrator), dann diese durchlaufen
						 */
						for (Person p : fromElemente) {

							if (p.getRole() == null) {
								Helper.setFehlerMeldung("[" + this.title + " " + myStruct.getType().getNameByLanguage(language) + "] "
										+ Helper.getTranslation("MetadataPersonWithoutRole"));
								break;
							} else {
								if (p.getRole().equals(mdttemp.getName())) {
									if (myValue.length() > 0) {
										myValue.append("; ");
									}
									myValue.append(p.getLastname());
									myValue.append(", ");
									myValue.append(p.getFirstname());
								}
							}
						}
					}
				}

				if (myValue.length() > 0) {
					createdElement.setValue(myValue.toString());

					myStruct.addMetadata(createdElement);
				}
			} catch (DocStructHasNoTypeException e) {
			} catch (MetadataTypeNotAllowedException e) {
			}

		}

		/*
		 * -------------------------------- alle Kinder durchlaufen --------------------------------
		 */
		List<DocStruct> children = myStruct.getAllChildren();
		if (children != null && children.size() > 0) {
			for (Iterator<DocStruct> iter = children.iterator(); iter.hasNext();) {
				checkCreateElementFrom(inFehlerList, inListOfFromMdts, iter.next(), mdt, language);
			}
		}
	}

	/**
	 * Metadatum soll mit bestimmten String beginnen oder enden ================================================================
	 */
	private void checkStartsEndsWith(List<String> inFehlerList, String prop_startswith, String prop_endswith, DocStruct myStruct, MetadataType mdt,
			String language) {
		/* startswith oder endswith */
		List<? extends Metadata> alleMetadaten = myStruct.getAllMetadataByType(mdt);
		if (alleMetadaten != null && alleMetadaten.size() > 0) {
			for (Iterator<? extends Metadata> iter = alleMetadaten.iterator(); iter.hasNext();) {
				Metadata md = iter.next();

				/* prüfen, ob es mit korrekten Werten beginnt */
				if (prop_startswith != null) {
					boolean isOk = false;
					StringTokenizer tokenizer = new StringTokenizer(prop_startswith, "|");
					while (tokenizer.hasMoreTokens()) {
						String tok = tokenizer.nextToken();
						if (md.getValue() != null && md.getValue().startsWith(tok)) {
							isOk = true;
						}
					}
					if (!isOk && !this.autoSave) {
						inFehlerList.add(md.getType().getNameByLanguage(language) + " " + Helper.getTranslation("MetadataWithValue") + " "
								+ md.getValue() + " " + Helper.getTranslation("MetadataDoesNotStartWith") + " " + prop_startswith);
					}
					if (!isOk && this.autoSave) {
						md.setValue(new StringTokenizer(prop_startswith, "|").nextToken() + md.getValue());
					}
				}
				/* prüfen, ob es mit korrekten Werten endet */
				if (prop_endswith != null) {
					boolean isOk = false;
					StringTokenizer tokenizer = new StringTokenizer(prop_endswith, "|");
					while (tokenizer.hasMoreTokens()) {
						String tok = tokenizer.nextToken();
						if (md.getValue() != null && md.getValue().endsWith(tok)) {
							isOk = true;
						}
					}
					if (!isOk && !this.autoSave) {
						inFehlerList.add(md.getType().getNameByLanguage(language) + " " + Helper.getTranslation("MetadataWithValue") + " "
								+ md.getValue() + " " + Helper.getTranslation("MetadataDoesNotEndWith") + " " + prop_endswith);
					}
					if (!isOk && this.autoSave) {
						md.setValue(md.getValue() + new StringTokenizer(prop_endswith, "|").nextToken());
					}
				}
			}
		}
	}

	/**
	 * automatisch speichern lassen, wenn Änderungen nötig waren ================================================================
	 */
	public boolean isAutoSave() {
		return this.autoSave;
	}

	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
	}
	
	
	public boolean validateIdentifier(DocStruct uppermostStruct) {
		
		if (uppermostStruct.getType().getAnchorClass() != null) {
			String language = (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}");

			if (uppermostStruct.getAllIdentifierMetadata() != null && uppermostStruct.getAllIdentifierMetadata().size() > 0) {
				Metadata identifierTopStruct = uppermostStruct.getAllIdentifierMetadata().get(0);
				try {
					if (identifierTopStruct.getValue() == null || identifierTopStruct.getValue().length() == 0) {
						Helper.setFehlerMeldung(identifierTopStruct.getType().getNameByLanguage(language) + " in " + uppermostStruct.getType().getNameByLanguage(language) + " "
								+ Helper.getTranslation("MetadataIsEmpty"));
						return false;
					}
					if (!identifierTopStruct.getValue().replaceAll(ConfigMain.getParameter("validateIdentifierRegex", "[\\w|-]"), "").equals("")) {
						Helper.setFehlerMeldung(Helper.getTranslation("MetadataIdentifierError")
								+ identifierTopStruct.getType().getNameByLanguage(language) + " in DocStruct "
								+ uppermostStruct.getType().getNameByLanguage(language) + Helper.getTranslation("MetadataInvalidCharacter"));
						return false;
					}
					DocStruct firstChild = uppermostStruct.getAllChildren().get(0);
					Metadata identifierFirstChild = firstChild.getAllIdentifierMetadata().get(0);
					if (identifierFirstChild.getValue() == null || identifierFirstChild.getValue().length() == 0) {
						return false;
					}
					if (!identifierFirstChild.getValue().replaceAll(ConfigMain.getParameter("validateIdentifierRegex", "[\\w|-]"), "").equals("")) {
						Helper.setFehlerMeldung(identifierTopStruct.getType().getNameByLanguage(language) + " in " + uppermostStruct.getType().getNameByLanguage(language) + " "
								+ Helper.getTranslation("MetadataIsEmpty"));
						return false;
					}
					if (identifierTopStruct.getValue() != null && !identifierTopStruct.getValue().isEmpty()
							&& identifierTopStruct.getValue().equals(identifierFirstChild.getValue())) {
						Helper.setFehlerMeldung(Helper.getTranslation("MetadataIdentifierError") + identifierTopStruct.getType().getName()
								+ Helper.getTranslation("MetadataIdentifierSame") + uppermostStruct.getType().getName() + " and "
								+ firstChild.getType().getName());
						return false;
					}
				} catch (Exception e) {
					return false;
				}
			}
		}
		return true;	
	}
}
