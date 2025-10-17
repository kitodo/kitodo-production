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

package org.kitodo.export;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class ExportMets {
    private final FileService fileService = ServiceManager.getFileService();
    protected LegacyPrefsHelper myPrefs;

    private static final Logger logger = LogManager.getLogger(ExportMets.class);

    /**
     * The field exportDmsTask holds an optional task instance. Its progress and
     * its errors will be passed to the task manager screen (if available) for
     * visualization.
     */
    protected EmptyTask exportDmsTask = null;

    /**
     * DMS-Export in das Benutzer-Homeverzeichnis.
     *
     * @param process
     *            Process object
     */
    public boolean startExport(Process process) throws DAOException, IOException {
        User user = ServiceManager.getUserService().getAuthenticatedUser();
        URI userHome = ServiceManager.getUserService().getHomeDirectory(user);
        boolean exportSuccessful = startExport(process, userHome);
        if (exportSuccessful) {
            if (Objects.nonNull(process.getParent())) {
                startExport(process.getParent());
            }
        }
        return exportSuccessful;
    }

    /**
     * DMS-Export an eine gewünschte Stelle.
     *
     * @param process
     *            Process object
     * @param userHome
     *            String
     */
    public boolean startExport(Process process, URI userHome) throws IOException, DAOException {

        /*
         * Read Document
         */
        this.myPrefs = ServiceManager.getRulesetService().getPreferences(process.getRuleset());
        String atsPpnBand = Helper.getNormalizedTitle(process.getTitle());
        LegacyMetsModsDigitalDocumentHelper gdzfile = ServiceManager.getProcessService().readMetadataFile(process);

        if (ServiceManager.getProcessService().handleExceptionsForConfiguration(gdzfile, process)) {
            return false;
        }

        prepareUserDirectory(userHome);

        String targetFileName = atsPpnBand + "_mets.xml";
        URI metaFile = userHome.resolve(userHome.getRawPath() + "/" + targetFileName);
        return writeMetsFile(process, metaFile, gdzfile);
    }

    /**
     * prepare user directory.
     *
     * @param targetFolder
     *            the folder to prove and maybe create it
     */
    protected void prepareUserDirectory(URI targetFolder) {
        User user = ServiceManager.getUserService().getAuthenticatedUser();
        try {
            fileService.createDirectoryForUser(targetFolder, user.getLogin());
        } catch (IOException | RuntimeException e) {
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setException(e);
            }
            Helper.setErrorMessage("Export canceled, could not create destination directory: " + targetFolder, logger,
                e);
        }
    }

    /**
     * write MetsFile to given Path.
     *
     * @param process
     *            the Process to use
     * @param metaFile
     *            the meta file which should be written
     * @param gdzfile
     *            the FileFormat-Object to use for Mets-Writing
     * @return true or false
     */
    protected boolean writeMetsFile(Process process, URI metaFile, LegacyMetsModsDigitalDocumentHelper gdzfile)
            throws IOException, DAOException {

        Workpiece workpiece = gdzfile.getWorkpiece();
        try {
            ServiceManager.getSchemaService().tempConvert(workpiece, process);
        } catch (URISyntaxException e) {
            if (Objects.nonNull(exportDmsTask)) {
                exportDmsTask.setException(e);
            }
            Helper.setErrorMessage("Writing Mets file failed!", e.getLocalizedMessage(), logger, e);
            return false;
        }
        /*
         * We write to the user’s home directory or to the hotfolder here, not
         * to a content repository, therefore no use of file service.
         */
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ServiceManager.getMetsService().save(workpiece, out);
            byte[] xmlBytes = out.toByteArray();
            File debugFolder = ConfigCore.getKitodoDebugDirectory();
            if (Objects.nonNull(debugFolder)) {
                FileUtils.writeByteArrayToFile(new File(debugFolder, "preExport.xml"), xmlBytes);
            }
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xmlBytes)) {
                StreamSource source = new StreamSource(byteArrayInputStream);
                try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(metaFile)))) {
                    URI xslFile = XsltHelper.getXsltFileFromConfig(process);
                    if (!Files.exists(Paths.get(xslFile))) {
                        String message = Helper.getTranslation("xsltFileNotFound", xslFile.toString());
                        throw new FileNotFoundException(message);
                    }
                    byte[] transformedBytes = XsltHelper.transformXmlByXslt(source, xslFile).toByteArray();
                    bufferedOutputStream.write(transformedBytes);
                    if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.EXPORT_ENRICH_LABELS)) {
                        updateInternalLabelsIfNeeded(ServiceManager.getFileService().getMetadataFilePath(process),
                                transformedBytes, process);
                    }
                } catch (FileNotFoundException | TransformerException e) {
                    if (Objects.nonNull(exportDmsTask)) {
                        exportDmsTask.setException(e);
                    }
                    Helper.setErrorMessage("Writing Mets file failed!", e.getLocalizedMessage(), logger, e);
                    return false;
                }
            }
        }

        Helper.setMessage(process.getTitle() + ": ", "exportFinished");
        return true;
    }

    /**
     * Extract LABEL/ORDERLABEL from export and update the internal meta.xml
     * only if values actually changed.
     */
    private void updateInternalLabelsIfNeeded(URI metaFile, byte[] xmlBytes, Process exportProcess) {
        Map<String, String> labels = extractLabels(xmlBytes);
        String newLabel = labels.get("LABEL");
        String newOrderLabel = labels.get("ORDERLABEL");

        if (Objects.isNull(newLabel) && Objects.isNull(newOrderLabel)) {
            logger.debug("No LABEL/ORDERLABEL found in exported METS for {}", metaFile);
            return;
        }
        try {
            Workpiece freshWorkpiece = ServiceManager.getMetsService().loadWorkpiece(metaFile);
            LogicalDivision logicalStructure = freshWorkpiece.getLogicalStructure();
            // Update only if a new non-null value differs from what is stored.
            boolean labelChanged = Objects.nonNull(newLabel)
                    && !Objects.equals(logicalStructure.getLabel(), newLabel);
            boolean orderLabelChanged = Objects.nonNull(newOrderLabel)
                    && !Objects.equals(logicalStructure.getOrderlabel(), newOrderLabel);

            if (labelChanged || orderLabelChanged) {
                if (labelChanged) {
                    logicalStructure.setLabel(newLabel);
                }
                if (orderLabelChanged) {
                    logicalStructure.setOrderlabel(newOrderLabel);
                }
                ServiceManager.getFileService().createBackupFile(exportProcess);
                ServiceManager.getMetsService().saveWorkpiece(freshWorkpiece, metaFile);
                logger.info("Updated LABEL/ORDERLABEL for {} (LABEL='{}', ORDERLABEL='{}')",
                        Paths.get(metaFile.getPath()).getFileName(), newLabel, newOrderLabel);
            } else {
                logger.debug("LABEL/ORDERLABEL unchanged for {}", metaFile);
            }
        } catch (IOException e) {
            Helper.setErrorMessage("Updating LABEL/ORDERLABEL in METS file failed!", e.getLocalizedMessage(), logger, e);
        }
    }


    private Map<String, String> extractLabels(byte[] xmlBytes) {
        Map<String, String> labels = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xmlBytes));
            XPath xpath = XPathFactory.newInstance().newXPath();

            // Select the first <div> inside structMap[@TYPE='LOGICAL'] that has a DMDID attribute.
            // This ensures we skip wrapper nodes for parents
            String xpathExpr = "/*[local-name()='mets']"
                    + "/*[local-name()='structMap' and @TYPE='LOGICAL']"
                    + "//*[local-name()='div' and @DMDID][1]";

            Element logicalDiv = (Element) xpath.evaluate(xpathExpr, doc, XPathConstants.NODE);

            if (Objects.nonNull(logicalDiv)) {
                String label = StringUtils.trimToNull(logicalDiv.getAttribute("LABEL"));
                String orderLabel = StringUtils.trimToNull(logicalDiv.getAttribute("ORDERLABEL"));
                if (Objects.nonNull(label)) {
                    labels.put("LABEL", label);
                }
                if (Objects.nonNull(orderLabel)) {
                    labels.put("ORDERLABEL", orderLabel);
                }
            }
        } catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
            Helper.setErrorMessage(
                    "Parsing Mets file failed!", e.getLocalizedMessage(), logger, e);
        }
        return labels;
    }
}
