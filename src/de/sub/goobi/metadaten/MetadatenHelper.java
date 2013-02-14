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

//TODO: Look if this can be merged into UGH
//TODO: Don't use arrays
//TODO: Try to avoid the usage of Iteratos
//TODO: Use generics
//TODO: Use generics
//TODO: Don't use Iterators

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperComparator;

public class MetadatenHelper implements Comparator<Object> {
	private static final Logger myLogger = Logger.getLogger(MetadatenHelper.class);
	public static final int PAGENUMBER_FIRST = 0;
	public static final int PAGENUMBER_LAST = 1;

	private Prefs myPrefs;
	private DigitalDocument mydocument;

	/* =============================================================== */

	public MetadatenHelper(Prefs inPrefs, DigitalDocument inDocument) {
		myPrefs = inPrefs;
		mydocument = inDocument;
	}

	/* =============================================================== */

	public DocStruct ChangeCurrentDocstructType(DocStruct inOldDocstruct, String inNewType) throws DocStructHasNoTypeException,
			MetadataTypeNotAllowedException, TypeNotAllowedAsChildException, TypeNotAllowedForParentException {

		DocStructType dst = myPrefs.getDocStrctTypeByName(inNewType);
		DocStruct newDocstruct = mydocument.createDocStruct(dst);

		/*
		 * -------------------------------- alle Metadaten hinzufügen --------------------------------
		 */
		if (inOldDocstruct.getAllVisibleMetadata() != null) {
			for (Iterator<Metadata> iterator = inOldDocstruct.getAllMetadata().iterator(); iterator.hasNext();) {
				Metadata md = iterator.next();

				List<String> addierbareTypenAlsStringListe = new ArrayList<String>();
				if (newDocstruct.getAddableMetadataTypes() != null) {
					for (Iterator<MetadataType> iterator2 = newDocstruct.getAddableMetadataTypes().iterator(); iterator2.hasNext();)
						addierbareTypenAlsStringListe.add(((MetadataType) iterator2.next()).getName());
				}
				if (addierbareTypenAlsStringListe.contains(md.getType().getName())) {
					newDocstruct.addMetadata(md);
				} 
			}
		}

		/*
		 * -------------------------------- alle Personen hinzufügen --------------------------------
		 */
		// TODO: get rid of Iterators, use a for Loop instead
		if (inOldDocstruct.getAllPersons() != null) {
			for (Iterator<Person> iterator = inOldDocstruct.getAllPersons().iterator(); iterator.hasNext();) {
				Person p = (Person) iterator.next();
				List<String> addierbareTypenAlsStringListe = new ArrayList<String>();
				if (newDocstruct.getAddableMetadataTypes() != null) {
					for (Iterator<MetadataType> iterator2 = newDocstruct.getAddableMetadataTypes().iterator(); iterator2.hasNext();)
						addierbareTypenAlsStringListe.add(((MetadataType) iterator2.next()).getName());
				}
				if (addierbareTypenAlsStringListe.contains(p.getType().getName())) {
					newDocstruct.addPerson(p);
				} 
			}
		}

		/*
		 * -------------------------------- alle Seiten hinzufügen --------------------------------
		 */
		if (inOldDocstruct.getAllToReferences() != null) {
			// TODO: get rid of Iterators, use a for Loop instead
			for (Iterator<Reference> iterator = inOldDocstruct.getAllToReferences().iterator(); iterator.hasNext();) {
				Reference p = (Reference) iterator.next();
				newDocstruct.addReferenceTo(p.getTarget(), p.getType());
			}
		}

		/*
		 * -------------------------------- alle Docstruct-Children hinzufügen --------------------------------
		 */
		if (inOldDocstruct.getAllChildren() != null) {
			// TODO: get rid of Iterators, use a for Loop instead
			for (Iterator<DocStruct> iterator = inOldDocstruct.getAllChildren().iterator(); iterator.hasNext();) {
				DocStruct dskind = (DocStruct) iterator.next();
				if (newDocstruct.getType().getAllAllowedDocStructTypes().contains(dskind.getType().getName())) {
				
					newDocstruct.addChild(dskind);
				} 
			}
		}

		/*
		 * -------------------------------- neues Docstruct zum Parent hinzufügen und an die gleiche Stelle schieben, wie den Vorg?nger
		 * --------------------------------
		 */
		inOldDocstruct.getParent().addChild(newDocstruct);
		int i = 1;
		// TODO: get rid of Iterators, use a for Loop instead
		for (Iterator<DocStruct> iter = newDocstruct.getParent().getAllChildren().iterator(); iter.hasNext(); i++)
			if ((DocStruct) iter.next() == inOldDocstruct)
				break;
		for (int j = newDocstruct.getParent().getAllChildren().size() - i; j > 0; j--)
			KnotenUp(newDocstruct);

		/*
		 * -------------------------------- altes Docstruct vom Parent entfernen und neues als aktuelles nehmen --------------------------------
		 */
		inOldDocstruct.getParent().removeChild(inOldDocstruct);
		return newDocstruct;
	}

	/* =============================================================== */

	public void KnotenUp(DocStruct inStruct) throws TypeNotAllowedAsChildException {
		DocStruct parent = inStruct.getParent();
		if (parent == null)
			return;
		List<DocStruct> alleDS = null;

		/* das erste Element kann man nicht nach oben schieben */
		if (parent.getAllChildren().get(0) == inStruct)
			return;

		/* alle Elemente des Parents durchlaufen */
		for (DocStruct tempDS : parent.getAllChildren()) {
			/*
			 * wenn das folgende Element das zu verschiebende ist dabei die Exception auffangen, falls es kein nächstes Kind gibt
			 */
			try {
				if (parent.getNextChild(tempDS) == inStruct)
					alleDS = new ArrayList<DocStruct>();
			} catch (Exception e) {
			}

			/*
			 * nachdem der Vorg?nger gefunden wurde, werden alle anderen Elemente aus der Child-Liste entfernt und separat gesammelt
			 */
			if (alleDS != null && tempDS != inStruct)
				alleDS.add(tempDS);
		}

		/* anschliessend die Childs entfernen */
		for (Iterator<DocStruct> iter = alleDS.iterator(); iter.hasNext();) {
			parent.removeChild(iter.next());
		}

		/* anschliessend die Childliste korrigieren */
		for (Iterator<DocStruct> iter = alleDS.iterator(); iter.hasNext();) {
			parent.addChild(iter.next());
		}
	}

	/* =============================================================== */

	public void KnotenDown(DocStruct inStruct) throws TypeNotAllowedAsChildException {
		DocStruct parent = inStruct.getParent();
		if (parent == null)
			return;
		List<DocStruct> alleDS = new ArrayList<DocStruct>();

		/* alle Elemente des Parents durchlaufen */
		// TODO: get rid of Iterators, use a for Loop instead
		for (Iterator<DocStruct> iter = parent.getAllChildren().iterator(); iter.hasNext();) {
			DocStruct tempDS = (DocStruct) iter.next();

			/* wenn das aktuelle Element das zu verschiebende ist */
			if (tempDS != inStruct) {
				alleDS.add(tempDS);
			} else {
				if (iter.hasNext())
					alleDS.add((DocStruct) iter.next());
				alleDS.add(inStruct);
			}
		}

		/* anschliessend alle Children entfernen */
		for (Iterator<DocStruct> iter = alleDS.iterator(); iter.hasNext();) {
			parent.removeChild(iter.next());
		}

		/* anschliessend die neue Childliste anlegen */
		for (Iterator<DocStruct> iter = alleDS.iterator(); iter.hasNext();) {
			parent.addChild(iter.next());
		}
	}

	/* =============================================================== */

	/**
	 * die MetadatenTypen zurückgeben
	 */
	public SelectItem[] getAddableDocStructTypen(DocStruct inStruct, boolean checkTypesFromParent) {
		/*
		 * -------------------------------- zuerst mal die addierbaren Metadatentypen ermitteln --------------------------------
		 */
		List<String> types;
		SelectItem myTypes[] = new SelectItem[0];

		try {
			if (!checkTypesFromParent)
				types = inStruct.getType().getAllAllowedDocStructTypes();
			else
				types = inStruct.getParent().getType().getAllAllowedDocStructTypes();
		} catch (RuntimeException e) {
			return myTypes;
		}

		if (types == null) {
			return myTypes;
		}

		List<DocStructType> newTypes = new ArrayList<DocStructType>();
		for (String tempTitel : types) {
			DocStructType dst = myPrefs.getDocStrctTypeByName(tempTitel);
			if (dst != null)
				newTypes.add(dst);
			else {
				Helper.setMeldung(null, "Regelsatz-Fehler: ", " DocstructType " + tempTitel + " nicht definiert");
				myLogger.error("getAddableDocStructTypen() - Regelsatz-Fehler: DocstructType " + tempTitel + " nicht definiert");
			}
		}

		/*
		 * -------------------------------- die Metadatentypen sortieren --------------------------------
		 */
		HelperComparator c = new HelperComparator();
		c.setSortierart("DocStructTypen");
		// TODO: Uses generics, if possible
		Collections.sort(newTypes, c);

		/*
		 * -------------------------------- nun ein Array mit der richtigen Größe anlegen --------------------------------
		 */
		int zaehler = newTypes.size();
		myTypes = new SelectItem[zaehler];

		/*
		 * -------------------------------- und anschliessend alle Elemente in das Array packen --------------------------------
		 */
		zaehler = 0;
		Iterator<DocStructType> it = newTypes.iterator();
		while (it.hasNext()) {
			DocStructType dst = it.next();
			String label = dst.getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"));
			if (label == null)
				label = dst.getName();
			myTypes[zaehler] = new SelectItem(dst.getName(), label);
			zaehler++;
		}
		return myTypes;
	}

	/**
	 * alle unbenutzen Metadaten des Docstruct löschen, Unterelemente rekursiv aufrufen
	 * ================================================================
	 */
	public void deleteAllUnusedElements(DocStruct inStruct) {
		inStruct.deleteUnusedPersonsAndMetadata();
		if (inStruct.getAllChildren() != null && inStruct.getAllChildren().size() > 0) {
			// TODO: get rid of Iterators, use a for Loop instead
			for (Iterator<DocStruct> it = inStruct.getAllChildren().iterator(); it.hasNext();) {
				DocStruct ds = (DocStruct) it.next();
				deleteAllUnusedElements(ds);
			}
		}
	}

	/**
	 * die erste Imagenummer zurückgeben ================================================================
	 */
	// FIXME: alphanumerisch
	@SuppressWarnings("unchecked")
	public String getImageNumber(DocStruct inStrukturelement, int inPageNumber) {
		String rueckgabe = "";

		if (inStrukturelement == null)
			return "";
		List<Reference> listReferenzen = inStrukturelement.getAllReferences("to");
		if (listReferenzen != null && listReferenzen.size() > 0) {
			/*
			 * -------------------------------- Referenzen sortieren --------------------------------
			 */
			Collections.sort(listReferenzen, new Comparator() {
				public int compare(final Object o1, final Object o2) {
					final Reference r1 = (Reference) o1;
					final Reference r2 = (Reference) o2;
					Integer page1 = 0;
					Integer page2 = 0;
					final MetadataType mdt = myPrefs.getMetadataTypeByName("physPageNumber");
					List<? extends Metadata> listMetadaten = r1.getTarget().getAllMetadataByType(mdt);
					if (listMetadaten != null && listMetadaten.size() > 0) {
						final Metadata meineSeite = (Metadata) listMetadaten.get(0);
						page1 = Integer.parseInt(meineSeite.getValue());
					}
					listMetadaten = r2.getTarget().getAllMetadataByType(mdt);
					if (listMetadaten != null && listMetadaten.size() > 0) {
						final Metadata meineSeite = (Metadata) listMetadaten.get(0);
						page2 = Integer.parseInt(meineSeite.getValue());
					}
					return page1.compareTo(page2);
				}
			});

			MetadataType mdt = myPrefs.getMetadataTypeByName("physPageNumber");
			List<? extends Metadata> listSeiten = listReferenzen.get(0).getTarget().getAllMetadataByType(mdt);
			if (inPageNumber == PAGENUMBER_LAST) {
				listSeiten = ((Reference) listReferenzen.get(listReferenzen.size() - 1)).getTarget().getAllMetadataByType(mdt);
			}
			if (listSeiten != null && listSeiten.size() > 0) {
				Metadata meineSeite = listSeiten.get(0);
				rueckgabe += meineSeite.getValue();
			}
			mdt = myPrefs.getMetadataTypeByName("logicalPageNumber");
			listSeiten = ((Reference) listReferenzen.get(0)).getTarget().getAllMetadataByType(mdt);
			if (inPageNumber == PAGENUMBER_LAST) {
				listSeiten = ((Reference) listReferenzen.get(listReferenzen.size() - 1)).getTarget().getAllMetadataByType(mdt);
			}
			if (listSeiten != null && listSeiten.size() > 0) {
				Metadata meineSeite = listSeiten.get(0);
				rueckgabe += ":" + meineSeite.getValue();
			}
		}
		return rueckgabe;
	}

	/**
	 * vom übergebenen DocStruct alle Metadaten ermitteln und um die fehlenden DefaultDisplay-Metadaten ergänzen
	 * ================================================================
	 */
	@SuppressWarnings("deprecation")
	public List<? extends Metadata> getMetadataInclDefaultDisplay(DocStruct inStruct, String inLanguage, boolean inIsPerson, Prozess inProzess) {
		List<MetadataType> displayMetadataTypes = inStruct.getDisplayMetadataTypes();
		/* sofern Default-Metadaten vorhanden sind, diese ggf. ergänzen */
		if (displayMetadataTypes != null) {
			for (MetadataType mdt : displayMetadataTypes) {
				// check, if mdt is already in the allMDs Metadata list, if not
				// - add it
				if (!(inStruct.getAllMetadataByType(mdt) != null && inStruct.getAllMetadataByType(mdt).size() != 0)) {
					try {
						if (mdt.getIsPerson()) {
							Person p = new Person(mdt);

							p.setRole(mdt.getName());
							inStruct.addPerson(p);
						} else {
							Metadata md = new Metadata(mdt);
							inStruct.addMetadata(md); // add this new metadata
							// element
						}
					} catch (DocStructHasNoTypeException e) {
						continue;
					} catch (MetadataTypeNotAllowedException e) {
						continue;
					}
				}
			}
		}

		/*
		 * wenn keine Sortierung nach Regelsatz erfolgen soll, hier alphabetisch sortieren
		 */
		/*
		 * wenn keine Sortierung nach Regelsatz erfolgen soll, hier alphabetisch sortieren
		 */
		if (inIsPerson) {
			List<Person> persons = inStruct.getAllPersons();
			if (persons != null && !inProzess.getRegelsatz().isOrderMetadataByRuleset()) {
				Collections.sort(persons, new MetadataComparator(inLanguage));
			}
			return persons;
		} else {
			List<Metadata> metadata = inStruct.getAllMetadata();
			if (metadata != null && !inProzess.getRegelsatz().isOrderMetadataByRuleset()) {
				Collections.sort(metadata, new MetadataComparator(inLanguage));
			}
			return getAllVisibleMetadataHack(inStruct);

		}
	}

	/** TODO: Replace it, after Maven is kicked :) */
	private List<Metadata> getAllVisibleMetadataHack(DocStruct inStruct) {

		// Start with the list of all metadata.
		List<Metadata> result = new LinkedList<Metadata>();

		// Iterate over all metadata.
		if (inStruct.getAllMetadata() != null) {
			for (Metadata md : inStruct.getAllMetadata()) {
				// If the metadata has some value and it does not start with the
				// HIDDEN_METADATA_CHAR, add it to the result list.
				if (!md.getType().getName().startsWith("_")) {
					result.add(md);
				}
			}
		}
		if (result.isEmpty()) {
			result = null;
		}
		return result;
	}

	/**
	 * prüfen, ob es sich hier um eine rdf- oder um eine mets-Datei handelt ================================================================
	 */
	public static String getMetaFileType(String file) throws IOException {
		/*
		 * --------------------- Typen und Suchbegriffe festlegen -------------------
		 */
		HashMap<String, String> types = new HashMap<String, String>();
		types.put("metsmods", "ugh.fileformats.mets.MetsModsImportExport".toLowerCase());
		types.put("mets", "www.loc.gov/METS/".toLowerCase());
		types.put("rdf", "<RDF:RDF ".toLowerCase());
		types.put("xstream", "<ugh.dl.DigitalDocument>".toLowerCase());

		FileReader input = new FileReader(file);
		BufferedReader bufRead = new BufferedReader(input);
		char[] buffer = new char[200];
		while ((bufRead.read(buffer)) >= 0) {

			String temp = new String(buffer).toLowerCase();
			Iterator<Entry<String, String>> i = types.entrySet().iterator();
			while (i.hasNext()) {
				Entry<String, String> entry = (Entry<String, String>) i.next();
				if (temp.contains(entry.getValue())) {
					bufRead.close();
					input.close();
					return entry.getKey();
				}
			}
		}
		bufRead.close();

		return "-";
	}

	/**
	 * @param inMdt
	 * @return localized Title of metadata type ================================================================
	 */
	public String getMetadatatypeLanguage(MetadataType inMdt) {
		String label = inMdt.getLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"));
		if (label == null)
			label = inMdt.getName();
		return label;
	}

	/**
	 * Comparator für die Metadaten ================================================================
	 */
	// TODO: Uses generics, if possible
	public class MetadataComparator implements Comparator<Object> {
		private String language = "de";

		public MetadataComparator(String inLanguage) {
			language = inLanguage;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public int compare(Object o1, Object o2) {
			Metadata s1 = (Metadata) o1;
			Metadata s2 = (Metadata) o2;
			if (o1 == null) {
				return -1;
			}
			if (o2 == null) {
				return 1;
			}
			String name1 = "", name2 = "";
			try {
				MetadataType mdt1 = s1.getType();
				MetadataType mdt2 = s2.getType();
				name1 = mdt1.getNameByLanguage(language);
				name2 = mdt2.getNameByLanguage(language);
			} catch (java.lang.NullPointerException e) {
				myLogger.error("metadata is null", e);
				return 0;
			}
			if (name1 == null || name1.length() == 0) {
				name1 = s1.getType().getName();
				if (name1 == null) {
					return -1;
				}
			}
			if (name2 == null || name2.length() == 0) {
				name2 = s2.getType().getName();
				if (name2 == null) {
					return 1;
				}
			}

			return name1.compareToIgnoreCase(name2);
		}
	}

	/**
	 * Alle Rollen ermitteln, die für das übergebene Strukturelement erlaubt sind
	 * 
	 * @param Strukturtyp
	 * @param Rollenname
	 *            der aktuellen Person, damit diese ggf. in die Liste mit übernommen wird ================================================
	 *            ================
	 */
	public ArrayList<SelectItem> getAddablePersonRoles(DocStruct myDocStruct, String inRoleName) {
		ArrayList<SelectItem> myList = new ArrayList<SelectItem>();
		/*
		 * -------------------------------- zuerst mal alle addierbaren Metadatentypen ermitteln --------------------------------
		 */
		List<MetadataType> types = myDocStruct.getAddableMetadataTypes();
		if (types == null)
			types = new ArrayList<MetadataType>();
		if (inRoleName != null && inRoleName.length() > 0) {
			boolean addRole = true;
			for (MetadataType mdt : types) {
				if (mdt.getName().equals(inRoleName))
					addRole = false;
			}

			if (addRole)
				types.add(myPrefs.getMetadataTypeByName(inRoleName));
		}
		/*
		 * --------------------- alle Metadatentypen, die keine Person sind, oder mit einem Unterstrich anfangen rausnehmen -------------------
		 */
		for (MetadataType mdt : new ArrayList<MetadataType>(types)) {
			if (!mdt.getIsPerson())
				types.remove(mdt);
		}

		/*
		 * -------------------------------- die Metadatentypen sortieren --------------------------------
		 */
		HelperComparator c = new HelperComparator();
		c.setSortierart("MetadatenTypen");
		Collections.sort(types, c);

		for (MetadataType mdt : types)
			myList.add(new SelectItem(mdt.getName(), getMetadatatypeLanguage(mdt)));
		return myList;
	}

	public int compare(Object o1, Object o2) {
		String imageSorting = ConfigMain.getParameter("ImageSorting", "number");
		String s1 = (String) o1;
		String s2 = (String) o2;
		// comparing only prefixes of files:
		s1 = s1.substring(0, s1.lastIndexOf("."));
		s2 = s2.substring(0, s2.lastIndexOf("."));

		if (imageSorting.equalsIgnoreCase("number")) {
			try {
				Integer i1 = Integer.valueOf(s1);
				Integer i2 = Integer.valueOf(s2);
				return i1.compareTo(i2);
			} catch (NumberFormatException e) {
				return s1.compareToIgnoreCase(s2);
			}
		} else if (imageSorting.equalsIgnoreCase("alphanumeric")) {
			return s1.compareToIgnoreCase(s2);
		} else {
			return s1.compareToIgnoreCase(s2);
		}
	}

}
