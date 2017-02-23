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

import java.util.ArrayList;

import javax.faces.model.SelectItem;

import org.goobi.production.constants.Parameters;

import ugh.dl.DocStruct;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import de.sub.goobi.config.ConfigMain;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt
 * mit dessen Eigenschaften und erlaubt die Bearbeitung
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

   /*#####################################################
    #####################################################
    ##
    ##                                                              Getter und Setter
    ##
    #####################################################
    ####################################################*/

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

   public String getVorname() {
       if (this.p.getFirstname()==null) {
           return "";
       }
      return this.p.getFirstname();
   }

   public void setVorname(String inVorname) {
       if (inVorname == null) {
           inVorname = "";
       }
      this.p.setFirstname(inVorname);
      this.p.setDisplayname(getNachname() + ", " + getVorname());
   }

   public String getNachname() {
       if (this.p.getLastname()==null) {
           return "";
       }
      return this.p.getLastname();
   }

   public void setNachname(String inNachname) {
       if (inNachname == null) {
           inNachname = "";
       }
      this.p.setLastname(inNachname);
      this.p.setDisplayname(getNachname() + ", " + getVorname());
   }

    public String getRecord() {
        String authorityValue = this.p.getAuthorityValue();
        if (authorityValue == null || authorityValue.isEmpty())
            authorityValue = ConfigMain.getParameter(Parameters.AUTHORITY_DEFAULT, "");
        return authorityValue;
    }

    public void setRecord(String record) {
        String[] authorityFile = Metadaten.parseAuthorityFileArgs(record);
        this.p.setAutorityFile(authorityFile[0], authorityFile[1], authorityFile[2]);
    }

   public String getRolle() {
      return this.p.getRole();
   }

   public void setRolle(String inRolle) {
      this.p.setRole(inRolle);
      MetadataType mdt = this.myPrefs.getMetadataTypeByName(this.p.getRole());
      this.p.setType(mdt);

   }

   public ArrayList<SelectItem> getAddableRollen() {
      return this.mdh.getAddablePersonRoles(this.myDocStruct, this.p.getRole());
   }
}
