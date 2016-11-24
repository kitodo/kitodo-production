/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package de.sub.goobi.helper;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.exceptions.UghHelperException;

import java.util.List;

import org.apache.log4j.Logger;

import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;

public class UghHelper {
	private static final Logger myLogger = Logger.getLogger(UghHelper.class);

	/**
	 * MetadataType aus Preferences eines Prozesses ermitteln
	 *
	 * @param inProzess add description
	 * @param inName add description
	 * @return MetadataType
	 * @throws UghHelperException add description
	 */
	public static MetadataType getMetadataType(Prozess inProzess, String inName) throws UghHelperException {
		Prefs myPrefs = inProzess.getRegelsatz().getPreferences();
		return getMetadataType(myPrefs, inName);
	}

	/**
	 * MetadataType aus Preferences ermitteln
	 *
	 * @param inPrefs add description
	 * @param inName add description
	 * @return MetadataType
	 * @throws UghHelperException add description
	 */
	public static MetadataType getMetadataType(Prefs inPrefs, String inName) throws UghHelperException {
		MetadataType mdt = inPrefs.getMetadataTypeByName(inName);
		if (mdt == null) {
			throw new UghHelperException("MetadataType does not exist in current Preferences: " + inName);
		}
		return mdt;
	}

	/**
	 * Metadata eines Docstructs ermitteln
	 *
	 * @param inStruct add description
	 * @param inMetadataType add description
	 * @return Metadata
	 */
	public static Metadata getMetadata(DocStruct inStruct, MetadataType inMetadataType) {
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

}
