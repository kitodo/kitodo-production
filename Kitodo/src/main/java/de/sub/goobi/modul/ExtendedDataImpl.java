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

package de.sub.goobi.modul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.kitodo.data.database.beans.Prozess;
import org.kitodo.data.database.beans.Prozesseigenschaft;
import org.kitodo.data.database.beans.Vorlage;
import org.kitodo.data.database.beans.Vorlageeigenschaft;
import org.kitodo.data.database.beans.Werkstueck;
import org.kitodo.data.database.beans.Werkstueckeigenschaft;
import de.sub.goobi.forms.ModuleServerForm;
import de.sub.goobi.helper.Helper;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ProzessDAO;
import de.unigoettingen.goobi.module.api.dataprovider.process.data.DataImpl;
import de.unigoettingen.goobi.module.api.exception.GoobiException;
import de.unigoettingen.goobi.module.api.types.GoobiProcessProperty;

/**
 * Namenraum Process.Data
 * Adressierung von Prozessmetadaten
 * Prozessmetadaten werden über eine Kombination aus SessionID, Typ, Nummer und Name angesprochen.
 * ·   Der Typ ist dabei ein String um z.B. zwischen Scanvorlage („TEMPLATE“) 
 *     und Werkstück („WORKPIECE“) unterscheiden zu können.
 * ·   Die Nummer ist dabei die Nummer von z.B. Scanvorlage oder Werkstück.
 * Für Daten des Prozesses ist der Typ entweder „PROCESS“ oder leer, die Nummer wird ignoriert.
 * 
 * Für zukünftige Versionen bleiben Feldnamen mit dem Präfix „#“ reserviert, 
 * sie dürfen durch die API nicht ausgelesen oder geschrieben werden.
 * Für die Adressierung und den Austausch von einzelnen Prozesseigenschaften 
 * wird die Datenstruktur „Process Property“ verwendet. Dabei wird die Struktur 
 * abhängig vom Kontext interpretiert:
 * ·   Die Methode „add“ ignoriert das Feld „id“.
 * ·   Die Methode „set“ kann das Feld „name“ ignorieren oder es zur Validierung einsetzen.
 * 
 * @author Steffen Hankiewicz
 */
public class ExtendedDataImpl extends DataImpl {
   private static final String isProcess = "PROCESS";
   private static final String isWorkpiece = "WORKPIECE";
   private static final String isTemplate = "TEMPLATE";
   
   /**
    * Diese Methode wird benötigt um Metadaten zu schreiben.
    * @param sessionId
    * @param type
    * @param count
    * @param pp
    * @return Status (Fehler)
    * @throws GoobiException: 1, 2, 6, 7, 254, 1500, 1501, 1502
    * ================================================================*/
   @Override
public int add(String sessionId, String type, int count, HashMap pp) throws GoobiException {
      super.add(sessionId, type, count, pp);
      Prozess p = ModuleServerForm.getProcessFromShortSession(sessionId);
      GoobiProcessProperty gpp = new GoobiProcessProperty(pp);
      if (gpp.getName().startsWith("#"))
         throw new GoobiException(5, "Parameter not allowed");

      /* --------------------------------
       * Prozesseigenschaft
      * --------------------------------*/
      if (type.equals("") || type.equals(isProcess)) {
         if (p.getEigenschaftenInitialized() == null)
            p.setEigenschaften(new HashSet<Prozesseigenschaft>());
         Prozesseigenschaft pe = new Prozesseigenschaft();
         pe.setProzess(p);
         pe.setTitel(gpp.getName());
         pe.setWert(gpp.getValue());
         p.getEigenschaftenInitialized().add(pe);
      }

      /* --------------------------------
       * Werkstückeigenschaft
      * --------------------------------*/
      if (type.equals(isWorkpiece)) {
         /* wenn auf Werkstück zugegriffen werden soll, was nicht existiert, raus */
         if (p.getWerkstueckeSize() - 1 < count)
            throw new GoobiException(1500, "Workpiece does not exist");
         Werkstueck w = p.getWerkstueckeList().get(count);
         if (w.getEigenschaften() == null)
            w.setEigenschaften(new HashSet<Werkstueckeigenschaft>());
         Werkstueckeigenschaft we = new Werkstueckeigenschaft();
         we.setWerkstueck(w);
         we.setTitel(gpp.getName());
         we.setWert(gpp.getValue());
         w.getEigenschaften().add(we);
      }

      /* --------------------------------
       * Scanvorlageneigenschaft
      * --------------------------------*/
      if (type.equals(isTemplate)) {
         /* wenn auf Scanvorlage zugegriffen werden soll, die nicht existiert, raus */
         if (p.getVorlagenSize() - 1 < count)
            throw new GoobiException(1500, "Template does not exist");
         Vorlage v = p.getVorlagenList().get(count);
         if (v.getEigenschaften() == null)
            v.setEigenschaften(new HashSet<Vorlageeigenschaft>());
         Vorlageeigenschaft ve = new Vorlageeigenschaft();
         ve.setVorlage(v);
         ve.setTitel(gpp.getName());
         ve.setWert(gpp.getValue());
         v.getEigenschaften().add(ve);
      }

      try {
         new ProzessDAO().save(p);
      } catch (DAOException e) {
         throw new GoobiException(1400, "******** wrapped DAOException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      }
      return 0;
   }

   /**
    * Diese Methode wird benötigt um feste Eigenschaften von Metadaten auszulesen.
    * @param sessionId
    * @param type
    * @param count
    * @return Liste von Namen – Wert Paaren
    * @throws GoobiException: 1, 2, 6, 254, 1500, 1501, 1502
    * ================================================================*/
   @Override
public HashMap<String, String> getData(String sessionId, String type, int count) throws GoobiException {
      super.getData(sessionId, type, count);

      Prozess p = ModuleServerForm.getProcessFromShortSession(sessionId);
      HashMap<String, String> rueckgabe = new HashMap<String, String>();
      /* --------------------------------
       * feste Prozesseigenschaften
      * --------------------------------*/
      if (type.equals("") || type.equals(isProcess)) {
         rueckgabe.put("id", String.valueOf(p.getId().intValue()));
         rueckgabe.put("title", p.getTitel());
         if (p.getAusgabename() != null)
            rueckgabe.put("outputname", p.getAusgabename());
         rueckgabe.put("project", p.getProjekt().getTitel());
      }

      /* --------------------------------
       * feste Werkstückeigenschaften
      * --------------------------------*/
      if (type.equals(isWorkpiece)) {
         /* wenn auf Werkstück zugegriffen werden soll, was nicht existiert, raus */
         if (p.getWerkstueckeSize() - 1 < count)
            throw new GoobiException(1500, "Workpiece does not exist");
         Werkstueck w = p.getWerkstueckeList().get(count);
         rueckgabe.put("id", String.valueOf(w.getId().intValue()));
      }

      /* --------------------------------
       * feste Scanvorlageneigenschaften
      * --------------------------------*/
      if (type.equals(isTemplate)) {
         /* wenn auf Scanvorlage zugegriffen werden soll, die nicht existiert, raus */
         if (p.getVorlagenSize() - 1 < count)
            throw new GoobiException(1500, "Template does not exist");
         Vorlage v = p.getVorlagenList().get(count);
         rueckgabe.put("id", String.valueOf(v.getId().intValue()));
         rueckgabe.put("origin", (v.getHerkunft()==null?"":v.getHerkunft()));
      }
      return rueckgabe;
   }

   /**
    * Diese Methode wird benötigt um Eigenschaften von Metadaten auszulesen
    * @param sessionId
    * @param type
    * @param count
    * @return Liste von Namen – Wert Paaren
    * @throws GoobiException: 1, 2, 6, 254, 1501, 1502
    * ================================================================*/
   @Override
public ArrayList<GoobiProcessProperty> getProperties(String sessionId, String type, int count)
         throws GoobiException {
      super.getProperties(sessionId, type, count);
      ArrayList<GoobiProcessProperty> gpps = new ArrayList<GoobiProcessProperty>();
      Prozess p = ModuleServerForm.getProcessFromShortSession(sessionId);
      /* --------------------------------
       * Prozesseigenschaften
       * --------------------------------*/
      if (type.equals("") || type.equals(isProcess)) {
    	  //TODO: Use for loops
         for (Iterator<Prozesseigenschaft> it = p.getEigenschaftenList().iterator(); it.hasNext();) {
            Prozesseigenschaft pe = it.next();
            if (!pe.getTitel().startsWith("#"))
               gpps.add(new GoobiProcessProperty(pe.getTitel(), String.valueOf(pe.getId().intValue()), pe
                     .getWert()));
         }
      }

      /* --------------------------------
       * Werkstückeigenschaften
      * --------------------------------*/
      if (type.equals(isWorkpiece)) {
         /* wenn auf Werkstück zugegriffen werden soll, was nicht existiert, raus */
         if (p.getWerkstueckeSize() - 1 < count)
            throw new GoobiException(1500, "Workpiece does not exist");
         Werkstueck w = p.getWerkstueckeList().get(count);
         //TODO: Use for loops
         for (Iterator<Werkstueckeigenschaft> it = w.getEigenschaftenList().iterator(); it.hasNext();) {
            Werkstueckeigenschaft we = it.next();
            if (!we.getTitel().startsWith("#"))
               gpps.add(new GoobiProcessProperty(we.getTitel(), String.valueOf(we.getId().intValue()), we
                     .getWert()));
         }
      }

      /* --------------------------------
       * Scanvorlageneigenschaften
      * --------------------------------*/
      if (type.equals(isTemplate)) {
         /* wenn auf Scanvorlage zugegriffen werden soll, die nicht existiert, raus */
         if (p.getVorlagenSize() - 1 < count)
            throw new GoobiException(1500, "Template does not exist");
         Vorlage v = p.getVorlagenList().get(count);
         //TODO: Use for loops
         for (Iterator<Vorlageeigenschaft> it = v.getEigenschaftenList().iterator(); it.hasNext();) {
            Vorlageeigenschaft ve = it.next();
            if (!ve.getTitel().startsWith("#"))
               gpps.add(new GoobiProcessProperty(ve.getTitel(), String.valueOf(ve.getId().intValue()), ve
                     .getWert()));
         }
      }
      return gpps;
   }

   /**
    * Diese Methode wird benötigt um Metadaten zu schreiben.
    * @param sessionId
    * @param type
    * @param count
    * @param pp
    * @return Status (Fehler)
    * @throws GoobiException: 1, 2, 6, 7, 254, 1501, 1502
    * ================================================================*/
   @Override
public int set(String sessionId, String type, int count, HashMap pp) throws GoobiException {
      super.set(sessionId, type, count, pp);
      Prozess p = ModuleServerForm.getProcessFromShortSession(sessionId);
      GoobiProcessProperty gpp = new GoobiProcessProperty(pp);
      if (gpp.getName().startsWith("#"))
         throw new GoobiException(5, "Parameter not allowed");
      /* --------------------------------
       * Prozesseigenschaft
      * --------------------------------*/
      String myquery = "from Prozesseigenschaft where prozess=" + p.getId().intValue();
      /* --------------------------------
       * Werkstückeigenschaft
      * --------------------------------*/
      if (type.equals(isWorkpiece)) {
         if (p.getWerkstueckeSize() - 1 < count)
            throw new GoobiException(1500, "Workpiece does not exist");
         Werkstueck w = p.getWerkstueckeList().get(count);
         myquery = "from Werkstueckeigenschaft where werkstueck=" + w.getId().intValue();

      }

      /* --------------------------------
       * Scanvorlageneigenschaft
      * --------------------------------*/
      if (type.equals(isTemplate)) {
         if (p.getVorlagenSize() - 1 < count)
            throw new GoobiException(1500, "Template does not exist");
         Vorlage v = p.getVorlagenList().get(count);
         myquery = "from Vorlageeigenschaft where vorlage=" + v.getId().intValue();
      }
      myquery += " and titel='" + gpp.getName() + "' and id=" + gpp.getId();

      try {
    	 //TODO: Use generics
         List hits = new ProzessDAO().search(myquery);
         if (hits.size() > 0) {
            if (type.equals("") || type.equals(isProcess)) {
               Prozesseigenschaft pe = (Prozesseigenschaft) hits.get(0);
               pe.setWert(gpp.getValue());
            }
            if (type.equals(isWorkpiece)) {
               Werkstueckeigenschaft we = (Werkstueckeigenschaft) hits.get(0);
               we.setWert(gpp.getValue());
            }
            if (type.equals(isTemplate)) {
               Vorlageeigenschaft ve = (Vorlageeigenschaft) hits.get(0);
               ve.setWert(gpp.getValue());
            }
            new ProzessDAO().save(p);
         } else {
            throw new GoobiException(1500, "Property " + gpp.getName() + " with id " + gpp.getId()
                  + " does not exist");
         }
      } catch (DAOException e) {
         throw new GoobiException(1400, "******** wrapped DAOException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
      }
      return 0;
   }

}
