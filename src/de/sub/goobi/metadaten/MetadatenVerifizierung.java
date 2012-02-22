/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
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
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.exceptions.UghHelperException;

//TODO: Use generics
//TODO: Don't use Iterators
public class MetadatenVerifizierung {
//	Helper help = new Helper();
	UghHelper ughhelp = new UghHelper();
	List<DocStruct> docStructsOhneSeiten;
	Prozess myProzess;
	boolean autoSave = false;

	public boolean validate(Prozess inProzess) {
		Prefs myPrefs = inProzess.getRegelsatz().getPreferences();
		/*
		 * -------------------------------- Fileformat einlesen
		 * --------------------------------
		 */
		Fileformat gdzfile;
		try {
			gdzfile = inProzess.readMetadataFile();
		} catch (Exception e) {
			Helper.setFehlerMeldung("Can not verify, xml reading errror on: " + inProzess.getTitel(), e.getMessage());
			return false;
		}
		return validate(gdzfile, myPrefs, inProzess);
	}

	public boolean validate(Fileformat gdzfile, Prefs inPrefs, Prozess inProzess) {
		myProzess = inProzess;
		boolean ergebnis = true;

		DigitalDocument dd = null;
		try {
			dd = gdzfile.getDigitalDocument();
		} catch (Exception e) {
			Helper.setFehlerMeldung("Can not get DigitalDocument[" + inProzess.getTitel() + "]", e);
			ergebnis = false;
		}

		/*
		 * -------------------------------- PathImagesFiles prüfen
		 * --------------------------------
		 */
		if (!this.isValidPathImageFiles(dd.getPhysicalDocStruct(), inPrefs)) {
			ergebnis = false;
		}

		/*
		 * -------------------------------- auf Docstructs ohne Seiten prüfen
		 * --------------------------------
		 */
		DocStruct logicalTop = dd.getLogicalDocStruct();
		if (logicalTop == null) {
			Helper.setFehlerMeldung("[" + inProzess.getTitel() + "] " + "Verifizierung nicht erfolgreich, keine Seiten zugewiesen", "");
			ergebnis = false;
		}

		docStructsOhneSeiten = new ArrayList<DocStruct>();
		this.checkDocStructsOhneSeiten(logicalTop);
		if (docStructsOhneSeiten.size() != 0) {
			for (Iterator<DocStruct> iter = docStructsOhneSeiten.iterator(); iter.hasNext();) {
				DocStruct ds = (DocStruct) iter.next();
				Helper.setFehlerMeldung("[" + inProzess.getTitel() + "] structure without pages: ", ds.getType().getName());
			}
			ergebnis = false;
		}

		/*
		 * -------------------------------- auf Seiten ohne Docstructs prüfen
		 * --------------------------------
		 */
		List<String> seitenOhneDocstructs = null;
		try {
			seitenOhneDocstructs = checkSeitenOhneDocstructs(gdzfile);
		} catch (PreferencesException e1) {
			Helper.setFehlerMeldung("[" + inProzess.getTitel() + "] Can not check pages without docstructs: ");
			ergebnis = false;
		}
		if (seitenOhneDocstructs != null && seitenOhneDocstructs.size() != 0) {
			for (Iterator<String> iter = seitenOhneDocstructs.iterator(); iter.hasNext();) {
				String seite = (String) iter.next();
				Helper.setFehlerMeldung("[" + inProzess.getTitel() + "] " + "pages without structure: ", seite);
			}
			ergebnis = false;
		}

		/*
		 * -------------------------------- auf mandatory Values der Metadaten
		 * prüfen --------------------------------
		 */
		List<String> mandatoryList = checkMandatoryValues(dd.getLogicalDocStruct(), new ArrayList<String>());
		if (mandatoryList.size() != 0) {
			for (Iterator<String> iter = mandatoryList.iterator(); iter.hasNext();) {
				String temp = (String) iter.next();
				Helper.setFehlerMeldung("[" + inProzess.getTitel() + "] " + "mandatory value element: ", temp);
			}
			ergebnis = false;
		}

		/*
		 * -------------------------------- auf Details in den Metadaten prüfen,
		 * die in der Konfiguration angegeben wurden
		 * --------------------------------
		 */
		List<String> configuredList = checkConfiguredValidationValues(dd.getLogicalDocStruct(), new ArrayList<String>(), inPrefs);
		if (configuredList.size() != 0) {
			for (Iterator<String> iter = configuredList.iterator(); iter.hasNext();) {
				String temp = (String) iter.next();
				Helper.setFehlerMeldung("[" + inProzess.getTitel() + "] " + "Invalid data: ", temp);
			}
			ergebnis = false;
		}

		MetadatenImagesHelper mih = new MetadatenImagesHelper(inPrefs, dd);
		try {
			if (!mih.checkIfImagesValid(inProzess, inProzess.getImagesOrigDirectory()))
				ergebnis = false;
		} catch (Exception e) {
			Helper.setFehlerMeldung("[" + inProzess.getTitel() + "]", e);
			ergebnis = false;
		}

		/*
		 * -------------------------------- Metadaten ggf. zum Schluss speichern
		 * --------------------------------
		 */
		try {
			if (autoSave)
				inProzess.writeMetadataFile(gdzfile);
		} catch (Exception e) {
			Helper.setFehlerMeldung("Error while writing metadata: " + inProzess.getTitel(), e);
		}
		return ergebnis;
	}

	private boolean isValidPathImageFiles(DocStruct phys, Prefs myPrefs) {
		try {
			MetadataType mdt = ughhelp.getMetadataType(myPrefs, "pathimagefiles");
			List<? extends Metadata> alleMetadaten = phys.getAllMetadataByType(mdt);
			if (alleMetadaten != null && alleMetadaten.size() > 0) {
				@SuppressWarnings("unused")
				Metadata mmm = (Metadata) alleMetadaten.get(0);
				return true;
			} else {
				Helper.setFehlerMeldung("[" + myProzess.getTitel() + "] " + "Can not verify, image path is not set", "");
				return false;
			}
		} catch (UghHelperException e) {
			Helper.setFehlerMeldung("[" + myProzess.getTitel() + "] " + "Verify aborted, error: ", e.getMessage());
			return false;
		}
	}

	private void checkDocStructsOhneSeiten(DocStruct inStruct) {
		if (inStruct.getAllToReferences().size() == 0 && !inStruct.getType().isAnchor())
			docStructsOhneSeiten.add(inStruct);
		/* alle Kinder des aktuellen DocStructs durchlaufen */
		if (inStruct.getAllChildren() != null) {
			for (Iterator<DocStruct> iter = inStruct.getAllChildren().iterator(); iter.hasNext();) {
				DocStruct child = (DocStruct) iter.next();
				checkDocStructsOhneSeiten(child);
			}
		}
	}

	private List<String> checkSeitenOhneDocstructs(Fileformat inRdf) throws PreferencesException {
		List<String> rueckgabe = new ArrayList<String>();
		DocStruct boundbook = inRdf.getDigitalDocument().getPhysicalDocStruct();
		/* wenn boundbook null ist */
		if (boundbook == null || boundbook.getAllChildren() == null)
			return rueckgabe;

		/* alle Seiten durchlaufen und prüfen ob References existieren */
		for (Iterator<DocStruct> iter = boundbook.getAllChildren().iterator(); iter.hasNext();) {
			DocStruct ds = (DocStruct) iter.next();
			List<Reference> refs = ds.getAllFromReferences();
			String physical = "";
			String logical = "";
			if (refs.size() == 0) {
			
				for (Iterator<Metadata> iter2 = ds.getAllMetadata().iterator(); iter2.hasNext();) {
					Metadata md = (Metadata) iter2.next();
					if (md.getType().getName().equals("logicalPageNumber"))
						logical = " (" + md.getValue() + ")";
					if (md.getType().getName().equals("physPageNumber"))
						physical = md.getValue();
				}
				rueckgabe.add(physical + logical);
			}
		}
		return rueckgabe;
	}

	private List<String> checkMandatoryValues(DocStruct inStruct, ArrayList<String> inList) {
		DocStructType dst = inStruct.getType();
		List<MetadataType> allMDTypes = dst.getAllMetadataTypes();
		for (MetadataType mdt : allMDTypes) {
			String number = dst.getNumberOfMetadataType(mdt);
			List<? extends Metadata> ll = inStruct.getAllMetadataByType(mdt);
			int real = 0;
			if (ll.size() > 0) {
				real = ll.size();

				if ((number.equals("1m")|| number.equals("+"))&& real == 1 && (ll.get(0).getValue() == null || ll.get(0).getValue().equals(""))) {
					inList.add(mdt.getName() + " in " + dst.getName() + " is empty");
				}
				/* jetzt die Typen prüfen */
				if (number.equals("1m") && real != 1) {
					inList.add(mdt.getName() + " in " + dst.getName() + " must exist 1 time but exists " + real + " times");
				}
				if (number.equals("1o") && real > 1) {
					inList.add(mdt.getName() + " in " + dst.getName() + " must not exist more than 1 time but exists " + real + " times");
				}
				if (number.equals("+") && real == 0) {
					inList.add(mdt.getName() + " in " + dst.getName() + " must exist at least 1 time but does not exist");
				}
			}
		}
		/* alle Kinder des aktuellen DocStructs durchlaufen */
		if (inStruct.getAllChildren() != null) {
			for (DocStruct child : inStruct.getAllChildren())
				checkMandatoryValues(child, inList);
		}
		return inList;
	}

	/**
	 * individuelle konfigurierbare projektspezifische Validierung der Metadaten
	 * ================================================================
	 */
	private List<String> checkConfiguredValidationValues(DocStruct inStruct, ArrayList<String> inFehlerList, Prefs inPrefs) {
		/*
		 * -------------------------------- Konfiguration öffnen und die
		 * Validierungsdetails auslesen --------------------------------
		 */
		ConfigProjects cp = null;
		try {
			cp = new ConfigProjects(myProzess.getProjekt());
		} catch (IOException e) {
			Helper.setFehlerMeldung("[" + myProzess.getTitel() + "] " + "IOException", e.getMessage());
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
				mdt = ughhelp.getMetadataType(inPrefs, prop_metadatatype);
			} catch (UghHelperException e) {
				Helper.setFehlerMeldung("[" + myProzess.getTitel() + "] " + "Metadatatype does not exist: ", prop_metadatatype);
			}
			/*
			 * wenn das Metadatum des FirstChilds überprüfen werden soll, dann
			 * dieses jetzt (sofern vorhanden) übernehmen
			 */
			if (prop_doctype != null && prop_doctype.equals("firstchild"))
				if (myStruct.getAllChildren() != null && myStruct.getAllChildren().size() > 0)
					myStruct = (DocStruct) myStruct.getAllChildren().get(0);
				else
					continue;

			/*
			 * wenn der MetadatenTyp existiert, dann jetzt die nötige Aktion
			 * überprüfen
			 */
			if (mdt != null) {
				/* ein CreatorsAllOrigin soll erzeugt werden */
				if (prop_createElementFrom != null) {
					ArrayList<MetadataType> listOfFromMdts = new ArrayList<MetadataType>();
					StringTokenizer tokenizer = new StringTokenizer(prop_createElementFrom, "|");
					while (tokenizer.hasMoreTokens()) {
						String tok = tokenizer.nextToken();
						try {
							MetadataType emdete = ughhelp.getMetadataType(inPrefs, tok);
							listOfFromMdts.add(emdete);
						} catch (UghHelperException e) {
						}
					}
					if (listOfFromMdts.size() > 0)
						checkCreateElementFrom(inFehlerList, listOfFromMdts, myStruct, mdt);
				} else {
					checkStartsEndsWith(inFehlerList, prop_startswith, prop_endswith, myStruct, mdt);
				}
			}
		}
		return inFehlerList;
	}

	/**
	 * Create Element From - für alle Strukturelemente ein bestimmtes Metadatum
	 * erzeugen, sofern dies an der jeweiligen Stelle erlaubt und noch nicht
	 * vorhanden
	 * ================================================================
	 */
	private void checkCreateElementFrom(ArrayList<String> inFehlerList, ArrayList<MetadataType> inListOfFromMdts, DocStruct myStruct, MetadataType mdt) {

		/*
		 * -------------------------------- existiert das zu erzeugende
		 * Metadatum schon, dann überspringen, ansonsten alle Daten
		 * zusammensammeln und in das neue Element schreiben
		 * --------------------------------
		 */
		List<? extends Metadata> createMetadaten = myStruct.getAllMetadataByType(mdt);
		if (createMetadaten == null || createMetadaten.size() == 0) {
			try {
				Metadata createdElement = new Metadata(mdt);

				StringBuffer myValue = new StringBuffer();
				/*
				 * alle anzufügenden Metadaten durchlaufen und an das Element
				 * anhängen
				 */
				for (MetadataType mdttemp : inListOfFromMdts) {

					List<Person> fromElemente = myStruct.getAllPersons();
					if (fromElemente != null && fromElemente.size() > 0) {
						/*
						 * wenn Personen vorhanden sind (z.B. Illustrator), dann
						 * diese durchlaufen
						 */
						for (Person p : fromElemente) {

							if (p.getRole() == null) {
								Helper.setFehlerMeldung("[" + myProzess.getTitel() + " " + myStruct.getType() + "] Person without role");
								break;
							} else {
								if (p.getRole().equals(mdttemp.getName())) {
									if (myValue.length() > 0)
										myValue.append("; ");
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
		 * -------------------------------- alle Kinder durchlaufen
		 * --------------------------------
		 */
		List<DocStruct> children = myStruct.getAllChildren();
		if (children != null && children.size() > 0)
			for (Iterator<DocStruct> iter = children.iterator(); iter.hasNext();) {
				checkCreateElementFrom(inFehlerList, inListOfFromMdts, (DocStruct) iter.next(), mdt);
			}
	}

	/**
	 * Metadatum soll mit bestimmten String beginnen oder enden
	 * ================================================================
	 */
	private void checkStartsEndsWith(List<String> inFehlerList, String prop_startswith, String prop_endswith, DocStruct myStruct, MetadataType mdt) {
		/* startswith oder endswith */
		List<? extends Metadata> alleMetadaten = myStruct.getAllMetadataByType(mdt);
		if (alleMetadaten != null && alleMetadaten.size() > 0)
			for (Iterator<? extends Metadata> iter = alleMetadaten.iterator(); iter.hasNext();) {
				Metadata md = (Metadata) iter.next();

				/* prüfen, ob es mit korrekten Werten beginnt */
				if (prop_startswith != null) {
					boolean isOk = false;
					StringTokenizer tokenizer = new StringTokenizer(prop_startswith, "|");
					while (tokenizer.hasMoreTokens()) {
						String tok = tokenizer.nextToken();
						if (md.getValue() != null && md.getValue().startsWith(tok))
							isOk = true;
					}
					if (!isOk && !autoSave)
						inFehlerList.add(md.getType().getName() + " with value " + md.getValue() + " does not start with " + prop_startswith);
					if (!isOk && autoSave)
						md.setValue(new StringTokenizer(prop_startswith, "|").nextToken() + md.getValue());
				}
				/* prüfen, ob es mit korrekten Werten endet */
				if (prop_endswith != null) {
					boolean isOk = false;
					StringTokenizer tokenizer = new StringTokenizer(prop_endswith, "|");
					while (tokenizer.hasMoreTokens()) {
						String tok = tokenizer.nextToken();
						if (md.getValue() != null && md.getValue().endsWith(tok))
							isOk = true;
					}
					if (!isOk && !autoSave) {
						inFehlerList.add(md.getType().getName() + " with value " + md.getValue() + " does not end with " + prop_endswith);
					}
					if (!isOk && autoSave) {
						md.setValue(md.getValue() + new StringTokenizer(prop_endswith, "|").nextToken());
					}
				}
			}
	}

	/**
	 * automatisch speichern lassen, wenn Änderungen nötig waren
	 * ================================================================
	 */
	public boolean isAutoSave() {
		return autoSave;
	}

	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
	}

}
