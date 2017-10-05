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
import java.util.List;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

/**
 * Namenraum Process.Data Adressierung von Prozessmetadaten Prozessmetadaten
 * werden über eine Kombination aus SessionID, Typ, Nummer und Name
 * angesprochen. · Der Typ ist dabei ein String um z.B. zwischen Scanvorlage
 * („TEMPLATE“) und Werkstück („WORKPIECE“) unterscheiden zu können. · Die
 * Nummer ist dabei die Nummer von z.B. Scanvorlage oder Werkstück. Für Daten
 * des Prozesses ist der Typ entweder „PROCESS“ oder leer, die Nummer wird
 * ignoriert.
 *
 * <p>
 * Für zukünftige Versionen bleiben Feldnamen mit dem Präfix „#“ reserviert, sie
 * dürfen durch die API nicht ausgelesen oder geschrieben werden. Für die
 * Adressierung und den Austausch von einzelnen Prozesseigenschaften wird die
 * Datenstruktur „Process Property“ verwendet. Dabei wird die Struktur abhängig
 * vom Kontext interpretiert: · Die Methode „add“ ignoriert das Feld „id“. · Die
 * Methode „set“ kann das Feld „name“ ignorieren oder es zur Validierung
 * einsetzen.
 * </p>
 *
 * @author Steffen Hankiewicz
 */
public class ExtendedDataImpl extends DataImpl {
    private static final String isProcess = "PROCESS";
    private static final String isWorkpiece = "WORKPIECE";
    private static final String isTemplate = "TEMPLATE";
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Diese Methode wird benötigt um Metadaten zu schreiben.
     *
     * @param sessionId
     *            String
     * @param type
     *            String
     * @param count
     *            int
     * @param pp
     *            HashMap
     * @return Status (Fehler)
     * @throws GoobiException:
     *             1, 2, 6, 7, 254, 1500, 1501, 1502
     */
    @Override
    public int add(String sessionId, String type, int count, HashMap pp) throws GoobiException {
        super.add(sessionId, type, count, pp);
        Process p = ModuleServerForm.getProcessFromShortSession(sessionId);
        GoobiProcessProperty gpp = new GoobiProcessProperty(pp);
        if (gpp.getName().startsWith("#")) {
            throw new GoobiException(5, "Parameter not allowed");
        }

        /*
         * Prozesseigenschaft
         */
        if (type.equals("") || type.equals(isProcess)) {
            if (p.getProperties() == null) {
                p.setProperties(new ArrayList<>());
            }
            Property processProperty = new Property();
            processProperty.getProcesses().add(p);
            processProperty.setTitle(gpp.getName());
            processProperty.setValue(gpp.getValue());
            p.getProperties().add(processProperty);
        }

        /*
         * Werkstückeigenschaft
         */
        if (type.equals(isWorkpiece)) {
            /*
             * wenn auf Werkstück zugegriffen werden soll, was nicht existiert,
             * raus
             */
            if (serviceManager.getProcessService().getWorkpiecesSize(p) - 1 < count) {
                throw new GoobiException(1500, "Workpiece does not exist");
            }
            Workpiece w = p.getWorkpieces().get(count);
            if (w.getProperties() == null) {
                w.setProperties(new ArrayList<>());
            }
            Property workpieceProperty = new Property();
            workpieceProperty.getWorkpieces().add(w);
            workpieceProperty.setTitle(gpp.getName());
            workpieceProperty.setValue(gpp.getValue());
            w.getProperties().add(workpieceProperty);
        }

        /*
         * Scanvorlageneigenschaft
         */
        if (type.equals(isTemplate)) {
            /*
             * wenn auf Scanvorlage zugegriffen werden soll, die nicht
             * existiert, raus
             */
            if (serviceManager.getProcessService().getTemplatesSize(p) - 1 < count) {
                throw new GoobiException(1500, "Template does not exist");
            }
            Template v = p.getTemplates().get(count);
            if (v.getProperties() == null) {
                v.setProperties(new ArrayList<>());
            }
            Property templateProperty = new Property();
            templateProperty.getTemplates().add(v);
            templateProperty.setTitle(gpp.getName());
            templateProperty.setValue(gpp.getValue());
            v.getProperties().add(templateProperty);
        }

        try {
            serviceManager.getProcessService().save(p);
        } catch (DataException e) {
            throw new GoobiException(1400, "******** wrapped DataException ********: " + e.getMessage() + "\n"
                    + Helper.getStacktraceAsString(e));
        }
        return 0;
    }

    /**
     * Diese Methode wird benötigt um feste Eigenschaften von Metadaten
     * auszulesen.
     *
     * @param sessionId
     *            String
     * @param type
     *            String
     * @param count
     *            int
     * @return Liste von Namen – Wert Paaren
     * @throws GoobiException:
     *             1, 2, 6, 254, 1500, 1501, 1502
     */
    @Override
    public HashMap<String, String> getData(String sessionId, String type, int count) throws GoobiException {
        super.getData(sessionId, type, count);

        Process p = ModuleServerForm.getProcessFromShortSession(sessionId);
        HashMap<String, String> rueckgabe = new HashMap<>();
        /*
         * feste Prozesseigenschaften
         */
        if (type.equals("") || type.equals(isProcess)) {
            rueckgabe.put("id", String.valueOf(p.getId().intValue()));
            rueckgabe.put("title", p.getTitle());
            if (p.getOutputName() != null) {
                rueckgabe.put("outputname", p.getOutputName());
            }
            rueckgabe.put("project", p.getProject().getTitle());
        }

        /*
         * feste Werkstückeigenschaften
         */
        if (type.equals(isWorkpiece)) {
            /*
             * wenn auf Werkstück zugegriffen werden soll, was nicht existiert,
             * raus
             */
            if (serviceManager.getProcessService().getWorkpiecesSize(p) - 1 < count) {
                throw new GoobiException(1500, "Workpiece does not exist");
            }
            Workpiece w = p.getWorkpieces().get(count);
            rueckgabe.put("id", String.valueOf(w.getId().intValue()));
        }

        /*
         * feste Scanvorlageneigenschaften
         */
        if (type.equals(isTemplate)) {
            /*
             * wenn auf Scanvorlage zugegriffen werden soll, die nicht
             * existiert, raus
             */
            if (serviceManager.getProcessService().getTemplatesSize(p) - 1 < count) {
                throw new GoobiException(1500, "Template does not exist");
            }
            Template v = p.getTemplates().get(count);
            rueckgabe.put("id", String.valueOf(v.getId().intValue()));
            rueckgabe.put("origin", (v.getOrigin() == null ? "" : v.getOrigin()));
        }
        return rueckgabe;
    }

    /**
     * Diese Methode wird benötigt um Eigenschaften von Metadaten auszulesen.
     *
     * @param sessionId
     *            String
     * @param type
     *            String
     * @param count
     *            int
     * @return Liste von Namen – Wert Paaren
     * @throws GoobiException:
     *             1, 2, 6, 254, 1501, 1502
     */
    @Override
    public ArrayList<GoobiProcessProperty> getProperties(String sessionId, String type, int count)
            throws GoobiException {
        super.getProperties(sessionId, type, count);
        ArrayList<GoobiProcessProperty> gpps = new ArrayList<>();
        Process p = ModuleServerForm.getProcessFromShortSession(sessionId);
        /*
         * Prozesseigenschaften
         */
        if (type.equals("") || type.equals(isProcess)) {
            for (Property processProperty : p.getProperties()) {
                if (!processProperty.getTitle().startsWith("#")) {
                    gpps.add(new GoobiProcessProperty(processProperty.getTitle(),
                            String.valueOf(processProperty.getId().intValue()), processProperty.getValue()));
                }
            }
        }

        /*
         * Werkstückeigenschaften
         */
        if (type.equals(isWorkpiece)) {
            /*
             * wenn auf Werkstück zugegriffen werden soll, was nicht existiert,
             * raus
             */
            if (serviceManager.getProcessService().getWorkpiecesSize(p) - 1 < count) {
                throw new GoobiException(1500, "Workpiece does not exist");
            }
            Workpiece w = p.getWorkpieces().get(count);
            for (Property workpieceProperty : w.getProperties()) {
                if (!workpieceProperty.getTitle().startsWith("#")) {
                    gpps.add(new GoobiProcessProperty(workpieceProperty.getTitle(),
                            String.valueOf(workpieceProperty.getId().intValue()), workpieceProperty.getValue()));
                }
            }
        }

        /*
         * Scanvorlageneigenschaften
         */
        if (type.equals(isTemplate)) {
            /*
             * wenn auf Scanvorlage zugegriffen werden soll, die nicht
             * existiert, raus
             */
            if (serviceManager.getProcessService().getTemplatesSize(p) - 1 < count) {
                throw new GoobiException(1500, "Template does not exist");
            }
            Template v = p.getTemplates().get(count);
            for (Property templateProperty : v.getProperties()) {
                if (!templateProperty.getTitle().startsWith("#")) {
                    gpps.add(new GoobiProcessProperty(templateProperty.getTitle(),
                            String.valueOf(templateProperty.getId().intValue()), templateProperty.getValue()));
                }
            }
        }
        return gpps;
    }

    /**
     * Diese Methode wird benötigt um Metadaten zu schreiben.
     *
     * @param sessionId
     *            String
     * @param type
     *            String
     * @param count
     *            int
     * @param pp
     *            HashMap
     * @return Status (Fehler)
     * @throws GoobiException:
     *             1, 2, 6, 7, 254, 1501, 1502
     */
    @Override
    public int set(String sessionId, String type, int count, HashMap pp) throws GoobiException {
        super.set(sessionId, type, count, pp);
        Process p = ModuleServerForm.getProcessFromShortSession(sessionId);
        GoobiProcessProperty gpp = new GoobiProcessProperty(pp);
        if (gpp.getName().startsWith("#")) {
            throw new GoobiException(5, "Parameter not allowed");
        }
        /*
         * Prozesseigenschaft
         */
        String myquery = "from ProcessProperty where process=" + p.getId();
        /*
         * Werkstückeigenschaft
         */
        if (type.equals(isWorkpiece)) {
            if (serviceManager.getProcessService().getWorkpiecesSize(p) - 1 < count) {
                throw new GoobiException(1500, "Workpiece does not exist");
            }
            Workpiece w = p.getWorkpieces().get(count);
            myquery = "from WorkpieceProperty where workpiece=" + w.getId();
        }

        /*
         * Scanvorlageneigenschaft
         */
        if (type.equals(isTemplate)) {
            if (serviceManager.getProcessService().getTemplatesSize(p) - 1 < count) {
                throw new GoobiException(1500, "Template does not exist");
            }
            Template v = p.getTemplates().get(count);
            myquery = "from TemplateProperty where template=" + v.getId();
        }
        myquery += " and title='" + gpp.getName() + "' and id=" + gpp.getId();

        try {
            // TODO: Use generics
            List hits = serviceManager.getProcessService().getByQuery(myquery);
            if (hits.size() > 0) {
                if (type.equals("") || type.equals(isProcess)) {
                    Property processProperty = (Property) hits.get(0);
                    processProperty.setValue(gpp.getValue());
                }
                if (type.equals(isWorkpiece)) {
                    Property workpieceProperty = (Property) hits.get(0);
                    workpieceProperty.setValue(gpp.getValue());
                }
                if (type.equals(isTemplate)) {
                    Property templateProperty = (Property) hits.get(0);
                    templateProperty.setValue(gpp.getValue());
                }
                serviceManager.getProcessService().save(p);
            } else {
                throw new GoobiException(1500,
                        "Property " + gpp.getName() + " with id " + gpp.getId() + " does not exist");
            }
        } catch (DataException e) {
            throw new GoobiException(1400, "******** wrapped DataException ********: " + e.getMessage() + "\n"
                    + Helper.getStacktraceAsString(e));
        }
        return 0;
    }

}
