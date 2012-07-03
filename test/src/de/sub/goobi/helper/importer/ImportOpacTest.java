package de.sub.goobi.helper.importer;

import org.junit.Ignore;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import de.sub.goobi.importer.ImportOpac;
import de.sub.goobi.helper.Helper;

@Ignore("Test defintion incorrect.")
public class ImportOpacTest {
   public static void main(String[] args) {
      String atstsl = "";
      try {
         ImportOpac myImportOpac = new ImportOpac();
         /* den Opac abfragen und ein RDF draus bauen lassen */
         Prefs myPrefs = new Prefs();
         myPrefs.loadPrefs(args[0]);
         Fileformat myRdf = myImportOpac.OpacToDocStruct("7", "9783518456934", "SBB", myPrefs);

         atstsl = myImportOpac.getAtstsl();
         System.out.println(atstsl);
         myRdf.write(args[1]);
      } catch (Exception e) {
         Helper.setFehlerMeldung("Fehler beim Einlesen des Opac-Ergebnisses ", e);
         e.printStackTrace();
      }

   }

}
