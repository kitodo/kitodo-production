/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
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
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package de.sub.goobi.modul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Prozesseigenschaft;
import de.sub.goobi.beans.Vorlage;
import de.sub.goobi.beans.Vorlageeigenschaft;
import de.sub.goobi.beans.Werkstueck;
import de.sub.goobi.beans.Werkstueckeigenschaft;
import de.sub.goobi.forms.ModuleServerForm;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
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
   BeanHelper beanhelp = new BeanHelper();

   private String isProcess = "PROCESS";
   private String isWorkpiece = "WORKPIECE";
   private String isTemplate = "TEMPLATE";
   
   /**
    * Diese Methode wird benötigt um Metadaten zu schreiben.
    * @param SessionID, Type, Count, Property
    * @return Status (Fehler)
    * @throws GoobiException: 1, 2, 6, 7, 254, 1500, 1501, 1502
    * ================================================================*/
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
         if (p.getEigenschaften() == null)
            p.setEigenschaften(new HashSet<Prozesseigenschaft>());
         Prozesseigenschaft pe = new Prozesseigenschaft();
         pe.setProzess(p);
         pe.setTitel(gpp.getName());
         pe.setWert(gpp.getValue());
         p.getEigenschaften().add(pe);
      }

      /* --------------------------------
       * Werkstückeigenschaft
      * --------------------------------*/
      if (type.equals(isWorkpiece)) {
         /* wenn auf Werkstück zugegriffen werden soll, was nicht existiert, raus */
         if (p.getWerkstueckeSize() - 1 < count)
            throw new GoobiException(1500, "Workpiece does not exist");
         Werkstueck w = (Werkstueck) p.getWerkstueckeList().get(count);
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
         Vorlage v = (Vorlage) p.getVorlagenList().get(count);
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
    * @param SessionID, Type, Count
    * @return Liste von Namen – Wert Paaren
    * @throws GoobiException: 1, 2, 6, 254, 1500, 1501, 1502
    * ================================================================*/
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
         Werkstueck w = (Werkstueck) p.getWerkstueckeList().get(count);
         rueckgabe.put("id", String.valueOf(w.getId().intValue()));
      }

      /* --------------------------------
       * feste Scanvorlageneigenschaften
      * --------------------------------*/
      if (type.equals(isTemplate)) {
         /* wenn auf Scanvorlage zugegriffen werden soll, die nicht existiert, raus */
         if (p.getVorlagenSize() - 1 < count)
            throw new GoobiException(1500, "Template does not exist");
         Vorlage v = (Vorlage) p.getVorlagenList().get(count);
         rueckgabe.put("id", String.valueOf(v.getId().intValue()));
         rueckgabe.put("origin", (v.getHerkunft()==null?"":v.getHerkunft()));
      }
      return rueckgabe;
   }

   /**
    * Diese Methode wird benötigt um Eigenschaften von Metadaten auszulesen
    * @param SessionID, Type, Count
    * @return Liste von Namen – Wert Paaren
    * @throws GoobiException: 1, 2, 6, 254, 1501, 1502
    * ================================================================*/
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
            Prozesseigenschaft pe = (Prozesseigenschaft) it.next();
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
         Werkstueck w = (Werkstueck) p.getWerkstueckeList().get(count);
         //TODO: Use for loops
         for (Iterator<Werkstueckeigenschaft> it = w.getEigenschaftenList().iterator(); it.hasNext();) {
            Werkstueckeigenschaft we = (Werkstueckeigenschaft) it.next();
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
         Vorlage v = (Vorlage) p.getVorlagenList().get(count);
         //TODO: Use for loops
         for (Iterator<Vorlageeigenschaft> it = v.getEigenschaftenList().iterator(); it.hasNext();) {
            Vorlageeigenschaft ve = (Vorlageeigenschaft) it.next();
            if (!ve.getTitel().startsWith("#"))
               gpps.add(new GoobiProcessProperty(ve.getTitel(), String.valueOf(ve.getId().intValue()), ve
                     .getWert()));
         }
      }
      return gpps;
   }

   /**
    * Diese Methode wird benötigt um Metadaten zu schreiben.
    * @param SessionID, Type, Count, Property
    * @return Status (Fehler)
    * @throws GoobiException: 1, 2, 6, 7, 254, 1501, 1502
    * ================================================================*/
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
         Werkstueck w = (Werkstueck) p.getWerkstueckeList().get(count);
         myquery = "from Werkstueckeigenschaft where werkstueck=" + w.getId().intValue();

      }

      /* --------------------------------
       * Scanvorlageneigenschaft
      * --------------------------------*/
      if (type.equals(isTemplate)) {
         if (p.getVorlagenSize() - 1 < count)
            throw new GoobiException(1500, "Template does not exist");
         Vorlage v = (Vorlage) p.getVorlagenList().get(count);
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
