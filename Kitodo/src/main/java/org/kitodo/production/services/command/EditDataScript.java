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

package org.kitodo.production.services.command;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.helper.VariableReplacer;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.services.ServiceManager;

public abstract class EditDataScript {

    private static final Logger logger = LogManager.getLogger(EditDataScript.class);

    /**
     * Processes the given script for the given process.
     * @param metadataFile - the file to be changed
     * @param process - the process to run the script on
     * @param script - the script to run
     */
    public void process(LegacyMetsModsDigitalDocumentHelper metadataFile, Process process, String script) {
        List<MetadataScript> scripts = parseScript(script);
        for (MetadataScript metadataScript : scripts) {
            executeScript(metadataFile, process, metadataScript);
        }
    }

    /**
     * Executes the given script on the given file for the given process.
     * @param metadataFile the file to edit
     * @param process the related process
     * @param metadataScript the script to execute
     */
    public abstract void executeScript(LegacyMetsModsDigitalDocumentHelper metadataFile, Process process,
                                       MetadataScript metadataScript);

    private List<MetadataScript> parseScript(String script) {
        String[] commands = script.split(";");
        List<MetadataScript> metadataScripts = new ArrayList<>();
        for (String command : commands) {
            metadataScripts.add(new MetadataScript(command));
        }
        return metadataScripts;
    }

    /**
     * Generates the script value when a metadata root is given.
     * @param metadataScript the script to set the value
     * @param metadataCollection the metadata collection to extract the value from
     * @param process the process to replace variables
     * @param metadataFile the metadatafile to read metadata
     */
    public void generateValueForMetadataScript(MetadataScript metadataScript, Collection<Metadata> metadataCollection, Process process, LegacyMetsModsDigitalDocumentHelper metadataFile) {
        if (metadataScript.getRoot().startsWith("@")) {
            for (Metadata metadata : metadataCollection) {
                if (metadata.getKey().equals(metadataScript.getRootName())) {
                    metadataScript.setValue(((MetadataEntry) metadata).getValue());
                }
            }
        } else if (metadataScript.getRoot().startsWith("$")) {
            LegacyPrefsHelper legacyPrefsHelper = ServiceManager.getRulesetService().getPreferences(process.getRuleset());
            VariableReplacer replacer = new VariableReplacer(metadataFile, legacyPrefsHelper,
                    process, null);

            String replaced = replacer.replace(metadataScript.getRootName());
            metadataScript.setValue(replaced);
        }
    }

    /**
     * Saves the changed workpiece and process.
     * @param workpiece the workpiece to save
     * @param process the process to save
     */
    public void saveChanges(Workpiece workpiece, Process process) {
        try (OutputStream out = ServiceManager.getFileService()
                .write(ServiceManager.getFileService().getMetadataFilePath(process))) {
            ServiceManager.getMetsService().save(workpiece, out);
            ServiceManager.getProcessService().saveToIndex(process, false);
        } catch (IOException | CustomResponseException | DataException e) {
            logger.error(e.getMessage());
        }
    }
}
