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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.forms.ModuleServerForm;
import de.sub.goobi.helper.Helper;
import de.unigoettingen.goobi.module.api.dataprovider.process.ProcessImpl;
import de.unigoettingen.goobi.module.api.exception.GoobiException;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;

/**
 * Das ist die Implementierung von ProcessInterface. Wird auf Kitodo-Seiten
 * Ausgeführt Ist auch vorläufer für GoobiEngine Erweitert um die individuellen
 * Api-Aufrufe
 *
 * @author Igor Toker
 */
public class ExtendedProzessImpl extends ProcessImpl {
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Diese Methode wird benötigt um die mit der Session ID verbundene Prozess
     * ID zu erhalten. Die Implementierung dieser Methode ist optional.
     *
     * @param sessionID
     *            String
     * @return ProzessID
     * @throws GoobiException:
     *             1, 2, 4, 5, 6, 254, 1400
     */
    @Override
    public String get(String sessionID) throws GoobiException {
        super.get(sessionID);
        return String.valueOf(ModuleServerForm.getProcessFromShortSession(sessionID).getId().intValue());
    }

    /**
     * Diese Methode wird benötigt um die Volltextdatei zu erhalten.
     *
     * @param sessionId
     *            String
     * @return Pfad zur XML Datei (String)
     * @throws GoobiException:
     *             1, 2, 4, 5, 6, 254, 1400, 1401
     */
    @Override
    public String getFullTextFile(String sessionId) throws GoobiException {
        super.getFullTextFile(sessionId);
        return serviceManager.getProcessService()
                    .getFulltextFilePath(ModuleServerForm.getProcessFromShortSession(sessionId));
    }

    /**
     * Diese Methode wird benötigt um das relative Arbeisverzeichnis zu
     * erhalten.
     *
     * @param sessionId
     *            String
     * @return Arbeitsverzeichnis (String)
     * @throws GoobiException:
     *             1, 2, 4, 5, 6, 254, 1400, 1401
     */
    @Override
    public String getImageDir(String sessionId) throws GoobiException {
        super.getImageDir(sessionId);
        return serviceManager.getFileService()
                    .getImagesDirectory(ModuleServerForm.getProcessFromShortSession(sessionId)).toString();
    }

    /**
     * Diese Methode wird benötigt um die Metadatendatei zu erhalten.
     *
     * @param sessionId
     *            String
     * @return Pfad zur XML Datei (String)
     * @throws GoobiException:
     *             1, 2, 4, 5, 6, 254, 1400, 1401
     */
    @Override
    public String getMetadataFile(String sessionId) throws GoobiException {
        super.getMetadataFile(sessionId);
        return serviceManager.getFileService()
                    .getMetadataFilePath(ModuleServerForm.getProcessFromShortSession(sessionId)).toString();
    }

    /**
     * Diese Methode wird benutzt um die Parameter für den Aufruf des Moduls zu
     * bekommen. Die Implementierung dieser Methode ist optional.
     *
     * @param sessionId
     *            String
     * @return Parameter Struktur
     * @throws GoobiException:
     *             1, 2, 4, 5, 6, 254
     */
    @Override
    public HashMap<String, URI> getParams(String sessionId) throws GoobiException {
        super.getParams(sessionId);
        HashMap<String, URI> myMap = new HashMap<>();
        Process p = ModuleServerForm.getProcessFromShortSession(sessionId);
        try {
            myMap.put("ruleset", serviceManager.getFileService()
                .createResource(ConfigCore.getParameter("RegelsaetzeVerzeichnis") + p.getRuleset().getFile()));
            myMap.put("tifdirectory", serviceManager.getProcessService().getImagesTifDirectory(false, p, null));
        } catch (IOException e) {
            throw new GoobiException(1300, "******** wrapped IOException ********: " + e.getMessage() + "\n"
                    + Helper.getStacktraceAsString(e));
        }
        return myMap;
    }

    /**
     * Diese Methode liefert das Projekt eines Prozesses. Die Implementierung
     * dieser Methode ist optional.
     *
     * @param sessionId
     *            String
     * @return Projekttitel (String)
     * @throws GoobiException:
     *             1, 2, 4, 5, 6, 254, 1400
     */
    @Override
    public String getProject(String sessionId) throws GoobiException {
        super.getProject(sessionId);
        return ModuleServerForm.getProcessFromShortSession(sessionId).getProject().getTitle();
    }

    /**
     * Diese Methode liefert den Titel eines Prozesses. Die Implementierung
     * dieser Methode ist optional.
     *
     * @param sessionId
     *            String
     * @return Prozesstitel (String)
     * @throws GoobiException:
     *             1, 2, 4, 5, 6, 254, 1400
     */
    @Override
    public String getTitle(String sessionId) throws GoobiException {
        super.getTitle(sessionId);
        return ModuleServerForm.getProcessFromShortSession(sessionId).getTitle();
    }

}
