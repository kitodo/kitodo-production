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

import java.util.ArrayList;

import javax.faces.model.SelectItem;

import ugh.dl.DocStruct;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;

//TODO: Use a correct comment here
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
   private Prefs myPrefs;
   private DocStruct myDocStruct;
   private MetadatenHelper mdh;

   

   /**
    * Allgemeiner Konstruktor ()
    */
   public MetaPerson(Person p, int inID, Prefs inPrefs, DocStruct inStruct) {

      myPrefs = inPrefs;
      this.p = p;
      identifier = inID;
      myDocStruct = inStruct;
      mdh = new MetadatenHelper(inPrefs, null);
   }

   /*#####################################################
    #####################################################
    ##																															 
    ##																Getter und Setter									
    ##                                                   															    
    #####################################################
    ####################################################*/

   public int getIdentifier() {
      return identifier;
   }

   public void setIdentifier(int identifier) {
      this.identifier = identifier;
   }

   public Person getP() {
      return p;
   }

   public void setP(Person p) {
      this.p = p;
   }

   

   public String getVorname() {
	   if (p.getFirstname()==null) {
		   return "";
	   }
      return p.getFirstname();
   }

   public void setVorname(String inVorname) {
	   if (inVorname == null) {
		   inVorname = "";
	   }
      p.setFirstname(inVorname);
   }

   

   public String getNachname() {
	   if (p.getLastname()==null) {
		   return "";
	   }
      return p.getLastname();
   }

   public void setNachname(String inNachname) {
	   if (inNachname == null) {
		   inNachname = "";
	   }
      p.setLastname(inNachname);
   }

   

   public String getRolle() {
      return p.getRole();
   }

   public void setRolle(String inRolle) {
      p.setRole(inRolle);
      MetadataType mdt = myPrefs.getMetadataTypeByName(p.getRole());
      p.setType(mdt);

   }

   public ArrayList<SelectItem> getAddableRollen() {
      return mdh.getAddablePersonRoles(myDocStruct, p.getRole());
   }

   

}
