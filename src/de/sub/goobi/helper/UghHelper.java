package de.sub.goobi.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.exceptions.UghHelperException;

//TODO: Try to move this methods to UGH (ugh.util.UGHUtils would be a better place)
//TODO: Make methods static if possible. 
public class UghHelper {
	private static final Logger myLogger = Logger.getLogger(UghHelper.class);

	/**
	 * MetadataType aus Preferences eines Prozesses ermitteln
	 * 
	 * @param inProzess
	 * @param inName
	 * @return MetadataType
	 * @throws UghHelperException
	 */
	public MetadataType getMetadataType(Prozess inProzess, String inName) throws UghHelperException {
		Prefs myPrefs = inProzess.getRegelsatz().getPreferences();
		return getMetadataType(myPrefs, inName);
	}

	/**
	 * MetadataType aus Preferences ermitteln
	 * 
	 * @param inPrefs
	 * @param inName
	 * @return MetadataType
	 * @throws UghHelperException
	 */
	public MetadataType getMetadataType(Prefs inPrefs, String inName) throws UghHelperException {
		MetadataType mdt = inPrefs.getMetadataTypeByName(inName);
		if (mdt == null)
			throw new UghHelperException("MetadataType does not exist in current Preferences: " + inName);
		return mdt;
	}

	/**
	 * Metadata eines Docstructs ermitteln
	 * 
	 * @param inStruct
	 * @param inMetadataType
	 * @return Metadata
	 */
	public Metadata getMetadata(DocStruct inStruct, MetadataType inMetadataType) {
		List<? extends Metadata> all = inStruct.getAllMetadataByType(inMetadataType);
		if (all.size() == 0) {
			try {
				Metadata md = new Metadata(inMetadataType);
				md.setDocStruct(inStruct);
				inStruct.addMetadata(md);

				return md;
			} catch (MetadataTypeNotAllowedException e) {
				myLogger.debug(e.getMessage());
				return null;
			}
		}
		if (all.size() != 0) {
			return (Metadata) all.get(0);
		} else {
			return null;
		}

	}

	/**
	 * Metadata eines Docstructs ermitteln
	 * 
	 * @param inStruct
	 * @param inMetadataTypeAsString
	 * @return Metadata
	 * @throws UghHelperException
	 */
	public Metadata getMetadata(DocStruct inStruct, Prefs inPrefs, String inMetadataType) throws UghHelperException {
		MetadataType mdt = getMetadataType(inPrefs, inMetadataType);
		List<? extends Metadata> all = inStruct.getAllMetadataByType(mdt);
		if (all.size() > 0) {
			try {
				Metadata md = new Metadata(mdt);
				md.setDocStruct(inStruct);
				inStruct.addMetadata(md);

				return md;
			} catch (MetadataTypeNotAllowedException e) {
				myLogger.error(e);
			}
		}
		return (Metadata) all.get(0);
	}

	/**
	 * Metadata eines Docstructs ermitteln
	 * 
	 * @param inStruct
	 * @param inMetadataTypeAsString
	 * @return Metadata
	 * @throws UghHelperException
	 */
	public Metadata getMetadata(DocStruct inStruct, Prozess inProzess, String inMetadataType) throws UghHelperException {
		MetadataType mdt = getMetadataType(inProzess, inMetadataType);
		List<? extends Metadata> all = inStruct.getAllMetadataByType(mdt);
		if (all.size() > 0) {
			try {
				Metadata md = new Metadata(mdt);
				md.setDocStruct(inStruct);
				inStruct.addMetadata(md);

				return md;
			} catch (MetadataTypeNotAllowedException e) {
				myLogger.error(e);
			}
		}
		return (Metadata) all.get(0);
	}

	private void addMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType, String inValue) {
		/* wenn kein Wert vorhanden oder das DocStruct null, dann gleich raus */
		if (inValue.equals("") || inStruct == null || inStruct.getType() == null)
			return;
		// myLogger.debug(inStruct.getType().getName() + " - " + inMetadataType + " - " + inValue);
		/* andernfalls dem DocStruct das passende Metadatum zuweisen */
		MetadataType mdt = inPrefs.getMetadataTypeByName(inMetadataType);
		try {
			Metadata md = new Metadata(mdt);
			md.setType(mdt);
			md.setValue(inValue);
			inStruct.addMetadata(md);
		} catch (DocStructHasNoTypeException e) {
			Helper.setMeldung(null, "DocStructHasNoTypeException: " + inStruct.getType().getName() + " - " + inMetadataType + " - " + inValue, e
					.getMessage());
			myLogger.error(e);
		} catch (MetadataTypeNotAllowedException e) {
			Helper.setMeldung(null, "MetadataTypeNotAllowedException: " + inStruct.getType().getName() + " - " + inMetadataType + " - " + inValue, e
					.getMessage());
			myLogger.error(e);
		} catch (Exception e) {
			Helper.setMeldung(null, "Exception: " + inStruct.getType().getName() + " - " + inMetadataType + " - " + inValue, e.getMessage());
			myLogger.error(e);
		}
	}

	public void replaceMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType, String inValue) {
		/* vorhandenes Element löschen */
		MetadataType mdt = inPrefs.getMetadataTypeByName(inMetadataType);
		if (mdt == null)
			return;
		if (inStruct != null && inStruct.getAllMetadataByType(mdt).size() > 0) {
			// TODO: Use for loops
			for (Iterator<? extends Metadata> iter = inStruct.getAllMetadataByType(mdt).iterator(); iter.hasNext();) {
				Metadata md = (Metadata) iter.next();
				inStruct.removeMetadata(md);
			}
		}
		/* Element neu hinzufügen */
		addMetadatum(inStruct, inPrefs, inMetadataType, inValue);
	}

	/**
	 * @return
	 */
	// TODO: Create a own class for iso 639 (?) Mappings or move this to UGH

	public String convertLanguage(String inLanguage) {
		/* Pfad zur Datei ermitteln */
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		String filename = session.getServletContext().getRealPath("/WEB-INF") + File.separator + "classes" + File.separator + "opaclanguages.txt";
		/* Datei zeilenweise durchlaufen und die Sprache vergleichen */
		try {
			FileInputStream fis = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(fis, "UTF8");
			BufferedReader in = new BufferedReader(isr);
			String str;
			while ((str = in.readLine()) != null)
				if (str.length() > 0 && str.split(" ")[1].equals(inLanguage)) {
					in.close();
					return str.split(" ")[0];
				}
			in.close();
		} catch (IOException e) {
		}
		return inLanguage;
	}

	/**
	 * In einem String die Umlaute auf den Grundbuchstaben reduzieren ================================================================
	 */
	// TODO: Try to replace this with a external library
	public String convertUmlaut(String inString) {
		String temp = inString;
		/* Pfad zur Datei ermitteln */
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		String filename = session.getServletContext().getRealPath("/WEB-INF") + File.separator + "classes" + File.separator + "opacumlaut.txt";

		/* Datei zeilenweise durchlaufen und die Sprache vergleichen */
		try {
			FileInputStream fis = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(fis, "UTF8");
			BufferedReader in = new BufferedReader(isr);
			String str;
			while ((str = in.readLine()) != null) {
				if (str.length() > 0) {
					temp = temp.replaceAll(str.split(" ")[0], str.split(" ")[1]);
				}
			}
			in.close();
		} catch (IOException e) {
			myLogger.error("IOException bei Umlautkonvertierung", e);
		}
		return temp;
	}

}
