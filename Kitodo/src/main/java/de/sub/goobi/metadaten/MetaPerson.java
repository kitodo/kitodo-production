package de.sub.goobi.metadaten;

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
import de.sub.goobi.config.ConfigMain;

import java.util.ArrayList;

import javax.faces.model.SelectItem;

import org.goobi.production.constants.Parameters;

import ugh.dl.DocStruct;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen Eigenschaften und erlaubt die Bearbeitung
 * der Schrittdetails
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 10.01.2005
 */
public class MetaPerson {
	private Person p;
	private int identifier;
	private final Prefs myPrefs;
	private final DocStruct myDocStruct;
	private final MetadatenHelper mdh;

	/**
	 * Allgemeiner Konstruktor ()
	 */
	public MetaPerson(Person p, int inID, Prefs inPrefs, DocStruct inStruct) {
		this.myPrefs = inPrefs;
		this.p = p;
		this.identifier = inID;
		this.myDocStruct = inStruct;
		this.mdh = new MetadatenHelper(inPrefs, null);
	}

	/*##########################################################################################################
	 ##
	 ##	Getter und Setter
	 ##
	 #########################################################################################################*/

	public int getIdentifier() {
		return this.identifier;
	}

	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}

	public Person getP() {
		return this.p;
	}

	public void setP(Person p) {
		this.p = p;
	}

	/**
	 * @return add description
	 */
	public String getVorname() {
		if (this.p.getFirstname() == null) {
			return "";
		}
		return this.p.getFirstname();
	}

	/**
	 * @param inVorname add description
	 */
	public void setVorname(String inVorname) {
		if (inVorname == null) {
			inVorname = "";
		}
		this.p.setFirstname(inVorname);
		this.p.setDisplayname(getNachname() + ", " + getVorname());
	}

	/**
	 * @return add description
	 */
	public String getNachname() {
		if (this.p.getLastname() == null) {
			return "";
		}
		return this.p.getLastname();
	}

	/**
	 * @param inNachname add description
	 */
	public void setNachname(String inNachname) {
		if (inNachname == null) {
			inNachname = "";
		}
		this.p.setLastname(inNachname);
		this.p.setDisplayname(getNachname() + ", " + getVorname());
	}

	/**
	 * @return add description
	 */
	public String getRecord() {
		String authorityValue = this.p.getAuthorityValue();
		if (authorityValue == null || authorityValue.isEmpty()) {
			authorityValue = ConfigMain.getParameter(Parameters.AUTHORITY_DEFAULT, "");
		}
		return authorityValue;
	}

	public void setRecord(String record) {
		String[] authorityFile = Metadaten.parseAuthorityFileArgs(record);
		this.p.setAutorityFile(authorityFile[0], authorityFile[1], authorityFile[2]);
	}

	public String getRolle() {
		return this.p.getRole();
	}

	/**
	 * @param inRolle add description
	 */
	public void setRolle(String inRolle) {
		this.p.setRole(inRolle);
		MetadataType mdt = this.myPrefs.getMetadataTypeByName(this.p.getRole());
		this.p.setType(mdt);

	}

	public ArrayList<SelectItem> getAddableRollen() {
		return this.mdh.getAddablePersonRoles(this.myDocStruct, this.p.getRole());
	}
}
