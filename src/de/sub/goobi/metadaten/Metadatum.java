package de.sub.goobi.metadaten;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.model.SelectItem;

import org.goobi.api.display.DisplayCase;
import org.goobi.api.display.Item;
import org.goobi.api.display.Modes;
import org.goobi.api.display.enums.BindState;

import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.Helper;

//TODO: Use a correct comment here
/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt 
 * mit dessen Eigenschaften und erlaubt die Bearbeitung 
 * der Schrittdetails
 * 
 * @author Steffen Hankiewicz
 * @version 1.00 - 10.01.2005
 */


public class Metadatum {
   private Metadata md;
   private int identifier;
   private Prefs myPrefs;
   private Prozess myProcess;
   private HashMap<String, DisplayCase> myValues = new HashMap<String, DisplayCase>();
   private List<SelectItem> items;
   private List<String> selectedItems;


   /**
    * Allgemeiner Konstruktor ()
    */
   public Metadatum(Metadata m, int inID, Prefs inPrefs, Prozess inProcess) {
      md = m;
      identifier = inID;
      myPrefs = inPrefs;
      myProcess = inProcess;
      for (BindState state : BindState.values()) {
    	  myValues.put(state.getTitle(), new DisplayCase(myProcess, state.getTitle(), md.getType().getName()));    	  
      }
   }

   public ArrayList<Item> getWert() {
	   String value = md.getValue();
	   if (value != null) {
		   for (Item i : myValues.get(Modes.getBindState().getTitle()).getItemList()){
			   if (i.getValue().equals(value)) {
				  i.setIsSelected(true);
			   } else {
				   i.setIsSelected(false);
			   }	   
		   }
	   }
      return  myValues.get(Modes.getBindState().getTitle()).getItemList();
   }

   public void setWert(String inWert) {
      md.setValue(inWert.trim());
   }

   

   public String getTyp() {
	   String label = md.getType().getLanguage((String) Helper
               .getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"));
       if (label == null) {
    	   label = md.getType().getName();
       }
       return label;
   }

   public void setTyp(String inTyp) {
      MetadataType mdt = myPrefs.getMetadataTypeByName(inTyp);
      md.setType(mdt);
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

   public Metadata getMd() {
      return md;
   }

   public void setMd(Metadata md) {
      this.md = md;
   }

   /******************************************************
    * 
    * new functions for use of display configuration 
    * whithin xml files
    * 
    *****************************************************/

	
	
   public String getOutputType() {
	   return myValues.get(Modes.getBindState().getTitle()).getDisplayType().getTitle();
   }
	
   
   
   public List<SelectItem> getItems() {
	   items = new ArrayList<SelectItem>();
	   selectedItems = new ArrayList<String>();
	   for (Item i : myValues.get(Modes.getBindState().getTitle()).getItemList()) {
		   items.add(new SelectItem(i.getLabel()));
		   if (i.getIsSelected()) {
			   selectedItems.add(i.getLabel());
		   }
	   }
	   return items;
   }

   public void setItems(List<SelectItem> items) {
	   for (Item i : myValues.get(Modes.getBindState().getTitle()).getItemList()) {
		   i.setIsSelected(false);
	   }
	   String val = "";
	   for (SelectItem sel : items) {
		   for (Item i : myValues.get(Modes.getBindState().getTitle()).getItemList()) {
			   if (i.getLabel().equals(sel.getValue())) {
				   i.setIsSelected(true);
				   val += i.getValue();
			   }
		   }
	   }
	   setWert(val);
   }

   
   
   
   public List<String> getSelectedItems() {
	   selectedItems = new ArrayList<String>();
	   String values = md.getValue();
	   while (values != null && values != "" && values.length() != 0) {
		   int semicolon = values.indexOf(";");
		   if (semicolon != -1) {
			   String value = values.substring(0, semicolon);
			   for (Item i : myValues.get(Modes.getBindState().getTitle()).getItemList()){
				   if (i.getValue().equals(value)){
					   selectedItems.add(i.getLabel());
					   i.setIsSelected(true);
				   }
			   }
			   int length = values.length(); 
			   values = values.substring(semicolon+1, length);
		   } else {
			   for (Item i : myValues.get(Modes.getBindState().getTitle()).getItemList()){
				   if (i.getValue().equals(values)){
					   selectedItems.add(i.getLabel());
					   i.setIsSelected(true);
				   }
			   }
			   values = "";
		   }
	   }
       return selectedItems;
   }

   
   public void setSelectedItems(List<String> selectedItems) {
	   for (Item i : myValues.get(Modes.getBindState().getTitle()).getItemList()) {
		   i.setIsSelected(false);
	   }
	   for (String sel : selectedItems) {
		   for (Item i : myValues.get(Modes.getBindState().getTitle()).getItemList()) {
			   if (i.getLabel().equals(sel)) {
				   i.setIsSelected(true);
			   }
		   }
	   }	   
	  String val = "";
	  for (Item i :myValues.get(Modes.getBindState().getTitle()).getItemList()) {
		  if (i.getIsSelected()) {
			  val += i.getValue() + ";";
		  }
	   }
	  setWert(val);
   }
	   
   
   public String getSelectedItem() {
	   String value = md.getValue();
	   if (value != "") {
		   for (Item i : myValues.get(Modes.getBindState().getTitle()).getItemList()){
			   if (i.getValue().equals(value)){
				   i.setIsSelected(true);
				   return i.getLabel();
			   }
		   }
	   }
       return "";
   }

   
   public void setSelectedItem(String selectedItem) {
	   for (Item i : myValues.get(Modes.getBindState().getTitle()).getItemList()){
		   i.setIsSelected(false);
	   }
	   for (Item i : myValues.get(Modes.getBindState().getTitle()).getItemList()) {
		   if (i.getLabel().equals(selectedItem)) {
			   setWert(i.getValue());
		   }
	   }
   }
   
   public void setValue(String value) {
	   setWert(value);   
   }
   
   
   
   public String getValue(){
	   return md.getValue();
   }
   
}
