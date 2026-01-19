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
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.exceptions.KitodoScriptExecutionException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.VariableReplacer;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.services.ServiceManager;
import org.xml.sax.SAXException;

public abstract class EditDataScript {

    private static final Logger logger = LogManager.getLogger(EditDataScript.class);

    /**
     * Processes the given script for the given process.
     * @param metadataFile - the file to be changed
     * @param process - the process to run the script on
     * @param script - the script to run
     */
    public void process(LegacyMetsModsDigitalDocumentHelper metadataFile, Process process, String script)
            throws KitodoScriptExecutionException {
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
                                       MetadataScript metadataScript) throws KitodoScriptExecutionException;

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
     * Generates the script value when a metadata source is given.
     * @param metadataScript the script to set the value
     * @param metadataCollection the metadata collection to extract the value from
     * @param process the process to replace variables
     * @param metadataFile the metadatafile to read metadata
     */
    public void generateValueForMetadataScript(MetadataScript metadataScript, Collection<Metadata> metadataCollection,
            Process process, LegacyMetsModsDigitalDocumentHelper metadataFile) {
        if (metadataScript.getValues().isEmpty() && Objects.nonNull(metadataScript.getValueSource())
                || Objects.nonNull(metadataScript.getVariable())) {
            if (StringUtils.isNotBlank(metadataScript.getValueSource())) {
                for (Metadata metadata : metadataCollection) {
                    if (metadata.getKey().equals(metadataScript.getValueSource())) {
                        metadataScript.getValues().add(((MetadataEntry) metadata).getValue());
                    }
                }
            } else if (StringUtils.isNotBlank(metadataScript.getVariable())) {
                VariableReplacer replacer = new VariableReplacer(metadataFile.getWorkpiece(), process, null);

                String replaced = replacer.replace(metadataScript.getVariable());
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
        try {
            ServiceManager.getFileService().createBackupFile(process);
            try (OutputStream out = ServiceManager.getFileService()
                    .write(ServiceManager.getProcessService().getMetadataFileUri(process))) {
                ServiceManager.getMetsService().save(workpiece, out);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            return;
        }
        try {
            ServiceManager.getProcessService().save(process);
        } catch (DAOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Generates the value of the MetadatScript and from the parentProcess.
     * @param metadataScript the script to generate the value for
     * @param parentProcess the process to take the value from
     * @throws IOException when reading metadata file fails
     * @throws SAXException when reading metadata file fails
     * @throws FileStructureValidationException when validating the metadata file fails
     */
    public void generateValueFromParent(MetadataScript metadataScript, Process parentProcess) throws IOException,
            SAXException, FileStructureValidationException {
        LegacyMetsModsDigitalDocumentHelper metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(parentProcess);
        Workpiece workpiece = metadataFile.getWorkpiece();

        Collection<Metadata> metadataCollection = workpiece.getLogicalStructure().getMetadata();
        generateValueForMetadataScript(metadataScript, metadataCollection, parentProcess, metadataFile);
    }

    /**
     * Gets the metadataCollection where the metadata should be edited.
     * May be specified by type in the script.
     * @param metadataScript the metadataScript
     * @param workpiece the workpiece to get the collection from.
     * @return the metadataCollection.
     */
    public Collection<Metadata> getMetadataCollection(MetadataScript metadataScript, Workpiece workpiece)
            throws KitodoScriptExecutionException {
        Collection<Metadata> metadataCollection;

        if (Objects.nonNull(metadataScript.getTypeTarget())) {
            LogicalDivision structuralElement = getLogicalDivisionWithType(metadataScript.getTypeTarget(),
                workpiece.getLogicalStructure());
            if (Objects.nonNull(structuralElement)) {
                metadataCollection = structuralElement.getMetadata();
            } else {
                throw new KitodoScriptExecutionException(
                        Helper.getTranslation("kitodoScript.noStructureOfTypeFound",
                                metadataScript.getTypeTarget()));
            }
        } else {
            metadataCollection = workpiece.getLogicalStructure().getMetadata();
        }
        return metadataCollection;
    }

    private LogicalDivision getLogicalDivisionWithType(String typeTarget, LogicalDivision structuralElement) {
        if (typeTarget.equals(structuralElement.getType())) {
            return structuralElement;
        } else {
            for (LogicalDivision structuralElementChild : structuralElement.getChildren()) {
                LogicalDivision structuralElementWithType = getLogicalDivisionWithType(typeTarget,
                    structuralElementChild);
                if (Objects.nonNull(structuralElementWithType)) {
                    return structuralElementWithType;
                }
            }
        }
        return null;
    }
}
