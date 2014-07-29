/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
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
package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.log4j.Logger;

import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;

/**
 * The class UGHUtils provides utility methods used in the plug-in.
 * 
 * @author unascribed
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
class UGHUtils {
	private static final Logger myLogger = Logger.getLogger(UGHUtils.class);

	/**
	 * The function addMetadatum() adds the meta data element given in terms of
	 * type identifier String and value String to the given document structure
	 * element, using the given rule set.
	 * 
	 * @param inStruct
	 *            structure element to add the meta data element to
	 * @param inPrefs
	 *            rule set to use
	 * @param inMetadataType
	 *            type of the meta data element to add
	 * @param inValue
	 *            value of the meta data element to add
	 */
	private static void addMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType, String inValue) {
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
			myLogger.error(e);
		} catch (MetadataTypeNotAllowedException e) {
			myLogger.error(e);
		} catch (Exception e) {
			myLogger.error(e);
		}
	}

	/**
	 * The function addMetadatum() adds meta data elements of the type whose
	 * identifier String is given for all value Strings to the given document
	 * structure element, using the given rule set.
	 * 
	 * @param inStruct
	 *            structure element to add the meta data elements to
	 * @param inPrefs
	 *            rule set to use
	 * @param inMetadataType
	 *            type of the meta data elements to add
	 * @param inValues
	 *            values of the meta data elements to add
	 */
	private static void addMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType, Iterable<String> inValues) {
		for (String inValue : inValues) {
			addMetadatum(inStruct, inPrefs, inMetadataType, inValue);
		}
	}

	/**
	 * The function replaceMetadatum() removes all meta data elements whose type
	 * is equal to the type identified by the given String from a document
	 * structure element and adds a new meta data element with the given meta
	 * data element given in terms of type identifier String and value String to
	 * the given document structure element, using the given rule set.
	 * 
	 * @param inStruct
	 *            structure element to replace the meta data elements in
	 * @param inPrefs
	 *            rule set to use
	 * @param inMetadataType
	 *            type of the meta data elements to replace
	 * @param inValue
	 *            value of the meta data element to add
	 */
	static void replaceMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType, String inValue) {
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
	 * The function replaceMetadatum() removes all meta data elements whose type
	 * is equal to the type identified by the given String from a document
	 * structure element and adds new meta data elements of the type whose
	 * identifier String is given for all value Strings to the given document
	 * structure element, using the given rule set.
	 * 
	 * @param inStruct
	 *            structure element to replace the meta data elements in
	 * @param inPrefs
	 *            rule set to use
	 * @param inMetadataType
	 *            type of the meta data elements to replace
	 * @param inValues
	 *            values of the meta data elements to add
	 */
	static void replaceMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType, Iterable<String> inValues) {
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
		addMetadatum(inStruct, inPrefs, inMetadataType, inValues);
	}

	/**
	 * The function convertLanguage() uses one of the mapping files
	 * “goobi_opacLanguages.txt” to replace the passed-in value by a
	 * configurable replacement. The mapping file is expected to be a plain text
	 * file, encoded as UTF-8, where each line defines a replacement pair as:
	 * replacement—white space (U+0020) character—value to be replaced. If no
	 * replacement is found, if the value to replace contains white space
	 * characters, or if an error occurs (i.e. the mapping file cannot be read),
	 * the value passed-in is returned. Which mapping is used depends on the
	 * availability of a user context, @see {@link #open(String)}.
	 * 
	 * @param inLanguages
	 *            values to replace
	 * @return replacements
	 */
	// TODO: Create a own class for iso 639 (?) Mappings or move this to UGH
	static String convertLanguage(String inLanguage) {
		/* Datei zeilenweise durchlaufen und die Sprache vergleichen */
		try {
			BufferedReader in = open(PicaPlugin.LANGUAGES_MAPPING_FILE);
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
	 * The function convertLanguages() uses the function
	 * {@link #convertLanguage(String)} to replace the passed-in values by a
	 * configurable replacement. If no replacement is found, if the value to
	 * replace contains white space characters, or if an error occurs (i.e. the
	 * mapping file cannot be read), the value passed-in is returned.
	 * 
	 * @param inLanguages
	 *            values to replace
	 * @return replacements
	 */
	static Iterable<String> convertLanguages(Iterable<String> inLanguages) {
		LinkedList<String> result = new LinkedList<String>();
		for (String inLanguage : inLanguages) {
			result.add(convertLanguage(inLanguage));
		}
		return result;
	}

	/**
	 * The function open() opens a file. In a user session context, the file is
	 * taken from the web application’s deployment directory
	 * (…/WEB-INF/classes), if not, it is taken from the CONFIG_DIR specified in
	 * the CONFIG_FILE.
	 * 
	 * TODO: Community needs to decide: Is this behaviour really what we want?
	 * Shouldn’t it <em>always</em> be the configured directory?
	 * 
	 * @param fileName
	 *            File to open
	 * @return a BufferedReader for reading the file
	 * @throws FileNotFoundException
	 *             if the file does not exist, is a directory rather than a
	 *             regular file, or for some other reason cannot be opened for
	 *             reading
	 * @throws UnsupportedEncodingException
	 *             If the named charset is not supported
	 */
	private static BufferedReader open(String fileName) throws IOException {
		String path = PicaPlugin.getConfigDir();
		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
			path = FilenameUtils.concat(session.getServletContext().getRealPath("/WEB-INF"), "classes");
		}
		String file = FilenameUtils.concat(path, fileName);
		return new BufferedReader(new InputStreamReader(new FileInputStream(file), CharEncoding.UTF_8));
	}

}
