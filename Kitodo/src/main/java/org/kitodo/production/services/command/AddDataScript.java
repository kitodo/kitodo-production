package org.kitodo.production.services.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.services.ServiceManager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AddDataScript {

    private static final Logger logger = LogManager.getLogger(AddDataScript.class);

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

    private void executeScript(LegacyMetsModsDigitalDocumentHelper metadataFile, Process process,
            MetadataScript metadataScript) {
        Workpiece workpiece = metadataFile.getWorkpiece();
        List<IncludedStructuralElement> allIncludedStructuralElements = workpiece.getAllIncludedStructuralElements();

        IncludedStructuralElement child = allIncludedStructuralElements.get(0);
        Collection<Metadata> metadataCollection = child.getMetadata();
        MdSec domain = null;
        for (Metadata metadata : metadataCollection) {
            domain = metadata.getDomain();
        }

        if (Objects.isNull(metadataScript.getValue())) {
            generateValueForMetadataScript(metadataScript, metadataCollection);
        }

        MetadataEntry metadataEntry = new MetadataEntry();
        metadataEntry.setKey(metadataScript.getGoal());
        metadataEntry.setValue(metadataScript.getValue());
        metadataEntry.setDomain(domain);
        metadataCollection.add(metadataEntry);

        try (OutputStream out = ServiceManager.getFileService()
                .write(ServiceManager.getFileService().getMetadataFilePath(process))) {
            ServiceManager.getMetsService().save(workpiece, out);
            ServiceManager.getProcessService().saveToIndex(process, false);
        } catch (IOException | CustomResponseException | DataException e) {
            logger.error(e.getMessage());
        }
    }

    private void generateValueForMetadataScript(MetadataScript metadataScript, Collection<Metadata> metadataCollection) {
        for (Metadata metadata : metadataCollection) {
            if (metadata.getKey().equals(metadataScript.getRootName())) {
                metadataScript.setValue(((MetadataEntry) metadata).getValue());
            }
        }
    }

    private List<MetadataScript> parseScript(String script) {
        String[] commands = script.split(";");
        List<MetadataScript> metadataScripts = new ArrayList<>();
        for (String command : commands) {
            metadataScripts.add(new MetadataScript(command));
        }
        return metadataScripts;
    }
}
