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

package org.kitodo.production.metadata;

import java.util.List;

import javax.faces.model.SelectItem;

import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.production.config.ConfigCore;
import org.kitodo.production.config.enums.ParameterCore;
import org.kitodo.production.helper.metadata.MetadataHelper;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen
 * Eigenschaften und erlaubt die Bearbeitung der Schrittdetails.
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 10.01.2005
 */
public class MetaPerson {
    private PersonInterface p;
    private int identifier;
    private final PrefsInterface myPrefs;
    private final DocStructInterface myDocStruct;
    private final MetadataHelper mdh;

    /**
     * Allgemeiner Konstruktor().
     */
    public MetaPerson(PersonInterface p, int inID, PrefsInterface inPrefs, DocStructInterface inStruct) {
        this.myPrefs = inPrefs;
        this.p = p;
        this.identifier = inID;
        this.myDocStruct = inStruct;
        this.mdh = new MetadataHelper(inPrefs, null);
    }

    public int getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public PersonInterface getP() {
        return this.p;
    }

    public void setP(PersonInterface p) {
        this.p = p;
    }

    /**
     * Get first name.
     *
     * @return String
     */
    public String getVorname() {
        if (this.p.getFirstName() == null) {
            return "";
        }
        return this.p.getFirstName();
    }

    /**
     * Set first name.
     *
     * @param inVorname
     *            String
     */
    public void setVorname(String inVorname) {
        if (inVorname == null) {
            inVorname = "";
        }
        this.p.setFirstName(inVorname);
        this.p.setDisplayName(getNachname() + ", " + getVorname());
    }

    /**
     * Get surname.
     *
     * @return String
     */
    public String getNachname() {
        if (this.p.getLastName() == null) {
            return "";
        }
        return this.p.getLastName();
    }

    /**
     * Set surname.
     *
     * @param inNachname
     *            String
     */
    public void setNachname(String inNachname) {
        if (inNachname == null) {
            inNachname = "";
        }
        this.p.setLastName(inNachname);
        this.p.setDisplayName(getNachname() + ", " + getVorname());
    }

    /**
     * Get record.
     *
     * @return String
     */
    public String getRecord() {
        String authorityValue = this.p.getAuthorityValue();
        if (authorityValue == null || authorityValue.isEmpty()) {
            authorityValue = ConfigCore.getParameter(ParameterCore.AUTHORITY_DEFAULT, "");
        }
        return authorityValue;
    }

    public void setRecord(String record) {
        String[] authorityFile = MetadataProcessor.parseAuthorityFileArgs(record);
        this.p.setAutorityFile(authorityFile[0], authorityFile[1], authorityFile[2]);
    }

    public String getRolle() {
        return this.p.getRole();
    }

    /**
     * Set role.
     *
     * @param inRolle
     *            String
     */
    public void setRolle(String inRolle) {
        this.p.setRole(inRolle);
        MetadataTypeInterface mdt = this.myPrefs.getMetadataTypeByName(this.p.getRole());
        this.p.setType(mdt);
    }

    public List<SelectItem> getAddableRollen() {
        return this.mdh.getAddablePersonRoles(this.myDocStruct, this.p.getRole());
    }
}
