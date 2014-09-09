/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package de.sub.goobi.metadaten.copier;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import de.sub.goobi.beans.Prozess;

/**
 * A CopierData object contains all the data the data copier has access to. It
 * has been implemented as an own bean class to allow to easily add variables
 * later without needing to extend many interfaces.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class CopierData {

	/**
	 * The workspace file to modify
	 */
	private final Fileformat fileformat;

	/**
	 * The Goobi process corresponding to the workspace file
	 */
	private final Prozess process;

	/**
	 * Creates a new CopierData bean.
	 * 
	 * @param fileformat
	 *            the document to modify
	 * @param process
	 *            the related goobi process
	 */
	public CopierData(Fileformat fileformat, Prozess process) {
		this.fileformat = fileformat;
		this.process = process;
	}

	/**
	 * Returns the top-level element of the logical document structure tree.
	 * 
	 * @return the logical document structure
	 */
	public DocStruct getLogicalDocStruct() {
		return getDigitalDocument().getLogicalDocStruct();
	}

	/**
	 * Returns the digital document contained in the fileformat passed-in in the
	 * constructor.
	 * 
	 * @return the digital document
	 */
	private DigitalDocument getDigitalDocument() {
		try {
			return fileformat.getDigitalDocument();
		} catch (PreferencesException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the ruleset to be used with the fileformat.
	 * 
	 * @return the required ruleset.
	 */
	public Prefs getPreferences() {
		return process.getRegelsatz().getPreferences();
	}

	/**
	 * Returns a string that textually represents this bean.
	 * 
	 * @return a string representation of this object
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{fileformat: " + fileformat.toString() + ", process: " + process.toString() + '}';
	}
}
