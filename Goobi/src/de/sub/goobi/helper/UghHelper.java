package de.sub.goobi.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
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
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.exceptions.UghHelperException;

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
		if (mdt == null) {
			throw new UghHelperException("MetadataType does not exist in current Preferences: " + inName);
		}
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
		if (inStruct != null && inMetadataType != null) {
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
				return all.get(0);
			} else {
				return null;
			}
		}
		return null;
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
		return all.get(0);
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
		return all.get(0);
	}

	private void addMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType, String inValue) {
		/* wenn kein Wert vorhanden oder das DocStruct null, dann gleich raus */
		if (inValue.equals("") || inStruct == null || inStruct.getType() == null) {
			return;
		}
		/* andernfalls dem DocStruct das passende Metadatum zuweisen */
		MetadataType mdt = inPrefs.getMetadataTypeByName(inMetadataType);
		try {
			Metadata md = new Metadata(mdt);
			md.setType(mdt);
			md.setValue(inValue);
			inStruct.addMetadata(md);
		} catch (DocStructHasNoTypeException e) {
			Helper.setMeldung(null, "DocStructHasNoTypeException: " + inStruct.getType().getName() + " - " + inMetadataType + " - " + inValue,
					e.getMessage());
			myLogger.error(e);
		} catch (MetadataTypeNotAllowedException e) {
			Helper.setMeldung(null, "MetadataTypeNotAllowedException: " + inStruct.getType().getName() + " - " + inMetadataType + " - " + inValue,
					e.getMessage());
			myLogger.error(e);
		} catch (Exception e) {
			Helper.setMeldung(null, "Exception: " + inStruct.getType().getName() + " - " + inMetadataType + " - " + inValue, e.getMessage());
			myLogger.error(e);
		}
	}

	public void replaceMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType, String inValue) {
		/* vorhandenes Element löschen */
		MetadataType mdt = inPrefs.getMetadataTypeByName(inMetadataType);
		if (mdt == null) {
			return;
		}
		if (inStruct != null && inStruct.getAllMetadataByType(mdt).size() > 0) {
			// TODO: Use for loops
			for (Iterator<? extends Metadata> iter = inStruct.getAllMetadataByType(mdt).iterator(); iter.hasNext();) {
				Metadata md = iter.next();
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
		/* Datei zeilenweise durchlaufen und die Sprache vergleichen */
		FacesContext context = FacesContext.getCurrentInstance();
		String filename;
		try {
			if (context != null) {
				HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
				filename = session.getServletContext().getRealPath("/WEB-INF") + File.separator + "classes" + File.separator
						+ "goobi_opacLanguages.txt";
			} else {
				filename = ConfigMain.getParameter("KonfigurationVerzeichnis") + "goobi_opacLanguages.txt";
			}
			FileInputStream fis = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(fis, "UTF8");
			BufferedReader in = new BufferedReader(isr);
			String str;
			while ((str = in.readLine()) != null) {
				if (str.length() > 0 && str.split(" ")[1].equals(inLanguage)) {
					in.close();
					return str.split(" ")[0];
				}
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
		String filename;

		if (context != null) {
			HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
			filename = session.getServletContext().getRealPath("/WEB-INF") + File.separator + "classes" + File.separator + "goobi_opacUmlaut.txt";
		} else {
			filename = ConfigMain.getParameter("KonfigurationVerzeichnis") + "goobi_opacUmlaut.txt";
		}
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
