package de.sub.goobi.Metadaten;

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
      //      myLogger.debug("MetaPerson p() - Konstruktor: Start -" + inID);
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
      p.setDisplayname(getNachname() + ", " + getVorname());
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
      p.setDisplayname(getNachname() + ", " + getVorname());
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
