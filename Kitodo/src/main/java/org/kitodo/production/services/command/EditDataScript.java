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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.IncludedStructuralElement;
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
     * gets the corresponding metadata as collection.
     * @param allIncludedStructuralElements the structural Elements to check.
     * @return a list of metadata of a structural element selected from the list.
     */
    public Collection<Metadata> getMetadataCollection(List<IncludedStructuralElement> allIncludedStructuralElements) {
        Collection<Metadata> metadataCollection = Collections.emptyList();
        if (!allIncludedStructuralElements.isEmpty()) {
            for (IncludedStructuralElement allIncludedStructuralElement : allIncludedStructuralElements) {
                if (!allIncludedStructuralElement.getMetadata().isEmpty()) {
                    metadataCollection = allIncludedStructuralElement.getMetadata();
                    break;
                }
            }
        }
        return metadataCollection;
    }

    /**
     * Executes the given script on the given file for the given process.
     * @param metadataFile the file to edit
     * @param process the related process
     * @param metadataScript the script to execute
     */
    public abstract void executeScript(LegacyMetsModsDigitalDocumentHelper metadataFile, Process process,
                                       MetadataScript metadataScript);

    /**
     * Parses the given input to MetadataScripts.
     * @param script the input, entered in frontend.
     * @return the correlating MetadataScripts.
     */
    public List<MetadataScript> parseScript(String script) {
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
    public void generateValueForMetadataScript(MetadataScript metadataScript, Collection<Metadata> metadataCollection,
            Process process, LegacyMetsModsDigitalDocumentHelper metadataFile) {
        if (metadataScript.getValues().isEmpty() && Objects.nonNull(metadataScript.getRoot())) {
            if (metadataScript.getRoot().startsWith("@")) {
                for (Metadata metadata : metadataCollection) {
                    if (metadata.getKey().equals(metadataScript.getRootName())) {
                        metadataScript.getValues().add(((MetadataEntry) metadata).getValue());
                    }
                }
            } else if (metadataScript.getRoot().startsWith("$")) {
                LegacyPrefsHelper legacyPrefsHelper = ServiceManager.getRulesetService()
                        .getPreferences(process.getRuleset());
                VariableReplacer replacer = new VariableReplacer(metadataFile, legacyPrefsHelper, process, null);

                String replaced = replacer.replace(metadataScript.getRootName());
                metadataScript.getValues().add(replaced);
            }
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

    /**
     * Generates the value of the MetadatScript and from the parentProcess.
     * @param metadataScript the script to generate the value for
     * @param parentProcess the process to take the value from
     */
    public void generateValueFromParent(MetadataScript metadataScript, Process parentProcess) throws IOException {
        LegacyMetsModsDigitalDocumentHelper metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(parentProcess);
        Workpiece workpiece = metadataFile.getWorkpiece();
        List<IncludedStructuralElement> allIncludedStructuralElements = workpiece.getAllIncludedStructuralElements();

        Collection<Metadata> metadataCollection = getMetadataCollection(allIncludedStructuralElements);
        generateValueForMetadataScript(metadataScript, metadataCollection, parentProcess, metadataFile);
    }
}
