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

import de.sub.goobi.forms.ModuleServerForm;
import de.sub.goobi.helper.Helper;

import de.unigoettingen.goobi.module.api.dataprovider.process.data.DataImpl;
import de.unigoettingen.goobi.module.api.exception.GoobiException;
import de.unigoettingen.goobi.module.api.types.GoobiProcessProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.ProcessProperty;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.TemplateProperty;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.beans.WorkpieceProperty;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ProcessService;

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

	private ProcessService processService = new ProcessService();

   /**
    * Diese Methode wird benötigt um Metadaten zu schreiben.
    * @param sessionId
    * @param type
    * @param count
    * @param pp
    * @return Status (Fehler)
    * @throws GoobiException: 1, 2, 6, 7, 254, 1500, 1501, 1502
    */
   @Override
public int add(String sessionId, String type, int count, HashMap pp) throws GoobiException {
      super.add(sessionId, type, count, pp);
      Process p = ModuleServerForm.getProcessFromShortSession(sessionId);
      GoobiProcessProperty gpp = new GoobiProcessProperty(pp);
      if (gpp.getName().startsWith("#"))
         throw new GoobiException(5, "Parameter not allowed");

      /*
       * Prozesseigenschaft
       */
      if (type.equals("") || type.equals(isProcess)) {
         if (processService.getPropertiesInitialized(p) == null)
            p.setProperties(new ArrayList<ProcessProperty>());
         ProcessProperty pe = new ProcessProperty();
         pe.setProcess(p);
         pe.setTitle(gpp.getName());
         pe.setValue(gpp.getValue());
         processService.getPropertiesInitialized(p).add(pe);
      }

      /*
       * Werkstückeigenschaft
       */
      if (type.equals(isWorkpiece)) {
         /* wenn auf Werkstück zugegriffen werden soll, was nicht existiert, raus */
         if (processService.getWorkpiecesSize(p) - 1 < count)
            throw new GoobiException(1500, "Workpiece does not exist");
         Workpiece w = p.getWorkpieces().get(count);
         if (w.getProperties() == null)
            w.setProperties(new ArrayList<WorkpieceProperty>());
         WorkpieceProperty we = new WorkpieceProperty();
         we.setWorkpiece(w);
         we.setTitle(gpp.getName());
         we.setValue(gpp.getValue());
         w.getProperties().add(we);
      }

      /*
       * Scanvorlageneigenschaft
       */
      if (type.equals(isTemplate)) {
         /* wenn auf Scanvorlage zugegriffen werden soll, die nicht existiert, raus */
         if (processService.getTemplatesSize(p) - 1 < count)
            throw new GoobiException(1500, "Template does not exist");
         Template v = p.getTemplates().get(count);
         if (v.getProperties() == null)
            v.setProperties(new ArrayList<TemplateProperty>());
         TemplateProperty ve = new TemplateProperty();
         ve.setTemplate(v);
         ve.setTitle(gpp.getName());
         ve.setValue(gpp.getValue());
         v.getProperties().add(ve);
      }

      try {
         processService.save(p);
      } catch (DAOException e) {
         throw new GoobiException(1400, "******** wrapped DAOException ********: " + e.getMessage() + "\n"
				 + Helper.getStacktraceAsString(e));
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
    */
   @Override
public HashMap<String, String> getData(String sessionId, String type, int count) throws GoobiException {
      super.getData(sessionId, type, count);

      Process p = ModuleServerForm.getProcessFromShortSession(sessionId);
      HashMap<String, String> rueckgabe = new HashMap<String, String>();
      /*
       * feste Prozesseigenschaften
       */
      if (type.equals("") || type.equals(isProcess)) {
         rueckgabe.put("id", String.valueOf(p.getId().intValue()));
         rueckgabe.put("title", p.getTitle());
         if (p.getOutputName() != null)
            rueckgabe.put("outputname", p.getOutputName());
         rueckgabe.put("project", p.getProject().getTitle());
      }

      /*
       * feste Werkstückeigenschaften
       */
      if (type.equals(isWorkpiece)) {
         /* wenn auf Werkstück zugegriffen werden soll, was nicht existiert, raus */
         if (processService.getWorkpiecesSize(p) - 1 < count)
            throw new GoobiException(1500, "Workpiece does not exist");
         Workpiece w = p.getWorkpieces().get(count);
         rueckgabe.put("id", String.valueOf(w.getId().intValue()));
      }

      /*
       * feste Scanvorlageneigenschaften
       */
      if (type.equals(isTemplate)) {
         /* wenn auf Scanvorlage zugegriffen werden soll, die nicht existiert, raus */
         if (processService.getTemplatesSize(p) - 1 < count)
            throw new GoobiException(1500, "Template does not exist");
         Template v = p.getTemplates().get(count);
         rueckgabe.put("id", String.valueOf(v.getId().intValue()));
         rueckgabe.put("origin", (v.getOrigin() == null?"":v.getOrigin()));
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
    */
   @Override
public ArrayList<GoobiProcessProperty> getProperties(String sessionId, String type, int count)
         throws GoobiException {
      super.getProperties(sessionId, type, count);
      ArrayList<GoobiProcessProperty> gpps = new ArrayList<GoobiProcessProperty>();
      Process p = ModuleServerForm.getProcessFromShortSession(sessionId);
      /*
       * Prozesseigenschaften
       */
      if (type.equals("") || type.equals(isProcess)) {
    	  //TODO: Use for loops
         for (Iterator<ProcessProperty> it = p.getProperties().iterator(); it.hasNext();) {
            ProcessProperty pe = it.next();
            if (!pe.getTitle().startsWith("#"))
               gpps.add(new GoobiProcessProperty(pe.getTitle(), String.valueOf(pe.getId().intValue()), pe.getValue()));
         }
      }

      /*
       * Werkstückeigenschaften
       */
      if (type.equals(isWorkpiece)) {
         /* wenn auf Werkstück zugegriffen werden soll, was nicht existiert, raus */
         if (processService.getWorkpiecesSize(p) - 1 < count)
            throw new GoobiException(1500, "Workpiece does not exist");
         Workpiece w = p.getWorkpieces().get(count);
         //TODO: Use for loops
         for (Iterator<WorkpieceProperty> it = w.getProperties().iterator(); it.hasNext();) {
            WorkpieceProperty we = it.next();
            if (!we.getTitle().startsWith("#"))
               gpps.add(new GoobiProcessProperty(we.getTitle(), String.valueOf(we.getId().intValue()), we.getValue()));
         }
      }

      /*
       * Scanvorlageneigenschaften
       */
      if (type.equals(isTemplate)) {
         /* wenn auf Scanvorlage zugegriffen werden soll, die nicht existiert, raus */
         if (processService.getTemplatesSize(p) - 1 < count)
            throw new GoobiException(1500, "Template does not exist");
         Template v = p.getTemplates().get(count);
         //TODO: Use for loops
         for (Iterator<TemplateProperty> it = v.getProperties().iterator(); it.hasNext();) {
            TemplateProperty ve = it.next();
            if (!ve.getTitle().startsWith("#"))
               gpps.add(new GoobiProcessProperty(ve.getTitle(), String.valueOf(ve.getId().intValue()), ve.getValue()));
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
    */
   @Override
public int set(String sessionId, String type, int count, HashMap pp) throws GoobiException {
      super.set(sessionId, type, count, pp);
      Process p = ModuleServerForm.getProcessFromShortSession(sessionId);
      GoobiProcessProperty gpp = new GoobiProcessProperty(pp);
      if (gpp.getName().startsWith("#"))
         throw new GoobiException(5, "Parameter not allowed");
      /*
       * Prozesseigenschaft
       */
      String myquery = "from Prozesseigenschaft where prozess=" + p.getId().intValue();
      /*
       * Werkstückeigenschaft
       */
      if (type.equals(isWorkpiece)) {
         if (processService.getWorkpiecesSize(p) - 1 < count)
            throw new GoobiException(1500, "Workpiece does not exist");
         Workpiece w = p.getWorkpieces().get(count);
         myquery = "from Werkstueckeigenschaft where werkstueck=" + w.getId();
      }

      /*
       * Scanvorlageneigenschaft
       */
      if (type.equals(isTemplate)) {
         if (processService.getTemplatesSize(p) - 1 < count)
            throw new GoobiException(1500, "Template does not exist");
         Template v = p.getTemplates().get(count);
         myquery = "from Vorlageeigenschaft where vorlage=" + v.getId();
      }
      myquery += " and titel='" + gpp.getName() + "' and id=" + gpp.getId();

      try {
    	 //TODO: Use generics
         List hits = processService.search(myquery);
         if (hits.size() > 0) {
            if (type.equals("") || type.equals(isProcess)) {
               ProcessProperty pe = (ProcessProperty) hits.get(0);
               pe.setValue(gpp.getValue());
            }
            if (type.equals(isWorkpiece)) {
               WorkpieceProperty we = (WorkpieceProperty) hits.get(0);
               we.setValue(gpp.getValue());
            }
            if (type.equals(isTemplate)) {
               TemplateProperty ve = (TemplateProperty) hits.get(0);
               ve.setValue(gpp.getValue());
            }
            processService.save(p);
         } else {
            throw new GoobiException(1500, "Property " + gpp.getName() + " with id " + gpp.getId()
                  + " does not exist");
         }
      } catch (DAOException e) {
         throw new GoobiException(1400, "******** wrapped DAOException ********: " + e.getMessage() + "\n"
				 + Helper.getStacktraceAsString(e));
      }
      return 0;
   }

}
