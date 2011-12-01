package de.sub.goobi.config;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Node;

public class ConfigOpacCatalogue {
   private static final Logger myLogger = Logger.getLogger(ConfigOpacCatalogue.class);
   private String title = "";
   private String description = "";
   private String address = "";
   private String database = "";
   private String iktlist = "";
   private int port = 80;
   private String cbs;
   private String charset = "iso-8859-1";
   private ArrayList<ConfigOpacCatalogueBeautifier> beautifySetList;


   public ConfigOpacCatalogue(String title, String desciption, String address, String database,
         String iktlist, int port, ArrayList<ConfigOpacCatalogueBeautifier> inBeautifySetList) {
      this.title = title;
      this.description = desciption;
      this.address = address;
      this.database = database;
      this.iktlist = iktlist;
      this.port = port;
      beautifySetList = inBeautifySetList;
   }
   
   
   //Constructor that also takes a charset, a quick hack for DPD-81
   public ConfigOpacCatalogue(String title, String desciption, String address, String database,
	         String iktlist, int port, String charset, String cbs, ArrayList<ConfigOpacCatalogueBeautifier> inBeautifySetList) {
	      //Call the contructor above
	   	  this(title, desciption, address, database, iktlist, port,  inBeautifySetList);
	      this.charset = charset;
	      this.setCbs(cbs);
	   }
   

   public String getTitle() {
      return title;
   }

   public String getDescription() {
      return description;
   }

   public String getAddress() {
      return address;
   }

   public String getDatabase() {
      return database;
   }

   public String getIktlist() {
      return iktlist;
   }

   public int getPort() {
      return port;
   }

   public String getCharset() {
	      return charset;
	   }
   
   public Node executeBeautifier(Node myHitlist) {
      /* Ausgabe des Opac-Ergebnissen in Datei */
     
      if (!ConfigMain.getParameter("debugFolder", "").equals("") && new File(ConfigMain.getParameter("debugFolder")).canWrite()) 
         debugMyNode(myHitlist, ConfigMain.getParameter("debugFolder") + "/opacBeautifyBefore.xml");

      /* ---------------------
       * aus dem Dom-Node ein JDom-Object machen
       * -------------------*/
      Document doc = new DOMBuilder().build(myHitlist.getOwnerDocument());
//      myLogger.debug("executeBeautifier(Node)" + doc.getRootElement().getName()
//            + doc.getRootElement().getChildren().size());

      /* ---------------------
       * Im JDom-Object alle Felder durchlaufen und die notwendigen Ersetzungen vornehmen
       * -------------------*/
      /* alle Records durchlaufen */
      //TODO: Use for each loop
      for (Iterator it = doc.getRootElement().getChildren().iterator(); it.hasNext();) {
         Element el = (Element) it.next();
         /* in jedem Record den Beautifier anwenden */
         executeBeautifierForElement(el);
      }

      /* ---------------------
       * aus dem JDom-Object wieder ein Dom-Node machen
       * -------------------*/
      DOMOutputter doutputter = new DOMOutputter();
      try {
         myHitlist = doutputter.output(doc);
         myHitlist = myHitlist.getFirstChild();
      } catch (JDOMException e) {
         myLogger.error("JDOMException in executeBeautifier(Node)", e);
      }

      /* Ausgabe des überarbeiteten Opac-Ergebnisses */
       //debugMyNode(myHitlist, "D:/temp_opac2.xml");
      if (!ConfigMain.getParameter("debugFolder", "").equals("") && new File(ConfigMain.getParameter("debugFolder")).canWrite())
         debugMyNode(myHitlist, ConfigMain.getParameter("debugFolder") + "/opacBeautifyAfter.xml");
      return myHitlist;
   }

   /**
    * Beautifier für ein JDom-Object durchführen
    * ================================================================*/
   private void executeBeautifierForElement(Element el) {
//      myLogger.debug("executeBeautifier(Node) - ----------------- " + el.getName());
      for (ConfigOpacCatalogueBeautifier beautifier : beautifySetList) {
         Element elementToChange = null;
         /* eine Kopie der zu prüfenden Elemente anlegen (damit man darin löschen kann */
         ArrayList<ConfigOpacCatalogueBeautifierElement> prooflist = new ArrayList<ConfigOpacCatalogueBeautifierElement>(beautifier.getTagElementsToProof());
         /* von jedem Record jedes Field durchlaufen */
         //TODO: Use for each loop
         for (Iterator<Element> itField = el.getChildren("field").iterator(); itField.hasNext();) {
            Element field = itField.next();
            String tag = field.getAttributeValue("tag");
            /* von jedem Field alle Subfelder durchlaufen */
            //TODO: Use for each loop
            for (Iterator<Element> itSub = field.getChildren("subfield").iterator(); itSub.hasNext();) {
               Element subfield = itSub.next();
               String subtag = subfield.getAttributeValue("code");
               String value = subfield.getText();
               
               if (beautifier.getTagElementToChange().getTag().equals(tag) && beautifier.getTagElementToChange().getSubtag().equals(subtag))
                  elementToChange = subfield;
               /* wenn die Werte des Subfeldes in der Liste der zu prüfenden Beutifier-Felder stehen, 
                * dieses aus der Liste der Beautifier entfernen */
               for (ConfigOpacCatalogueBeautifierElement cocbe : beautifier.getTagElementsToProof()) {
                  if (cocbe.getTag().equals(tag) && cocbe.getSubtag().equals(subtag)
                        && value.matches(cocbe.getValue())) {
                     prooflist.remove(cocbe);
                  }
               }
            }
         }
         /* ---------------------
          * wenn in der Kopie der zu prüfenden Elemente keine Elemente mehr enthalten sind, 
          * kann der zu ändernde Wert wirklich geändert werden
          * -------------------*/
         if (prooflist.size()==0 && elementToChange!=null) {
            elementToChange.setText(beautifier.getTagElementToChange().getValue());
         }
         

      }

   }

   /**
    * Print given DomNode to defined File
    * ================================================================*/
   private void debugMyNode(Node inNode, String fileName) {
      try {
         XMLOutputter outputter = new XMLOutputter();
         Document tempDoc = new DOMBuilder().build(inNode.getOwnerDocument());
         FileOutputStream output = new FileOutputStream(fileName);
         outputter.output(tempDoc.getRootElement(), output);
      } catch (FileNotFoundException e) {
         myLogger.error("debugMyNode(Node, String)", e);
      } catch (IOException e) {
         myLogger.error("debugMyNode(Node, String)", e);
      }

   }


/**
 * @param cbs the cbs to set
 */
public void setCbs(String cbs) {
	this.cbs = cbs;
}


/**
 * @return the cbs
 */
public String getCbs() {
	return cbs;
}

}
