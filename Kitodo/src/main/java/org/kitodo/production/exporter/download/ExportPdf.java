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

package org.kitodo.production.exporter.download;

import java.io.BufferedWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.TreeSet;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.filemanagement.filters.FileNameMatchesFilter;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.tasks.CreatePdfFromServletThread;
import org.kitodo.production.metadata.comparator.MetadataImageComparator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

public class ExportPdf extends ExportMets {
    private static final Logger logger = LogManager.getLogger(ExportPdf.class);
    private static final String AND_TARGET_FILE_NAME_IS = "&targetFileName=";
    private static final String PDF_EXTENSION = ".pdf";
    private final FileService fileService = ServiceManager.getFileService();

    @Override
    public boolean startExport(Process process, URI userHome)
            throws ReadException, IOException, PreferencesException, WriteException, JAXBException {
        String normalizedTitle = ServiceManager.getProcessService().getNormalizedTitle(process.getTitle());

        // Read Document
        FileformatInterface gdzfile = ServiceManager.getProcessService().readMetadataFile(process);
        prepareUserDirectory(userHome);
        this.myPrefs = ServiceManager.getRulesetService().getPreferences(process.getRuleset());

        // first of all write mets-file in images-Folder of process
        URI targetFileName = fileService.createResource(normalizedTitle + ".xml");
        URI metaFile = userHome.resolve(targetFileName);
        writeMetsFile(process, metaFile, gdzfile, true);
        Helper.setMessage(process.getTitle() + ": ", "mets file created");
        Helper.setMessage(process.getTitle() + ": ", "start pdf generation now");

        logger.debug("METS file created: " + targetFileName);

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
        String fullPath = req.getRequestURL().toString();
        String servletPath = context.getExternalContext().getRequestServletPath();
        String basisUrl = fullPath.substring(0, fullPath.indexOf(servletPath));

        if (!ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.PDF_AS_DOWNLOAD)) {
            useContentServerForPdfCreation(metaFile, userHome, process, basisUrl);
        } else {

            GetMethod method = null;
            try {
                // define path for mets and pdfs
                Integer contentServerTimeOut = ConfigCore
                        .getIntParameterOrDefaultValue(ParameterCore.KITODO_CONTENT_SERVER_TIMEOUT);
                URL kitodoContentServerUrl = getKitodoContentServerURL(metaFile, process, basisUrl);

                // get pdf from servlet and forward response to file
                method = new GetMethod(kitodoContentServerUrl.toString());
                method.getParams().setParameter("http.socket.timeout", contentServerTimeOut);

                if (!context.getResponseComplete()) {
                    completeResponse(context, kitodoContentServerUrl, normalizedTitle);
                }
                fileService.delete(metaFile);
            } catch (RuntimeException e) {
                String text = "error while pdf creation: " + e.getMessage();
                URI uri = userHome.resolve(normalizedTitle + ".PDF-ERROR.log");
                try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(fileService.write(uri)))) {
                    output.write(text);
                } catch (IOException e1) {
                    logger.error(e1);
                }
                return false;
            } finally {
                if (method != null) {
                    method.releaseConnection();
                }
            }
        }
        return true;
    }

    private URL getKitodoContentServerURL(URI metaFile, Process process, String basisUrl) throws IOException {
        String contentServerUrl = ConfigCore.getParameter(ParameterCore.KITODO_CONTENT_SERVER_URL);

        // using mets file
        if (ServiceManager.getMetadataValidationService().validate(process)) {
            // if no contentServerUrl defined use internal
            // goobiContentServerServlet
            if (contentServerUrl == null || contentServerUrl.length() == 0) {
                contentServerUrl = basisUrl + "/gcs/gcs?action=pdf&metsFile=";
            }
            return new URL(contentServerUrl + metaFile.toURL() + AND_TARGET_FILE_NAME_IS
                    + ServiceManager.getProcessService().getNormalizedTitle(process.getTitle()) + PDF_EXTENSION);
            // mets data does not exist or is invalid
        } else {
            if (contentServerUrl == null || contentServerUrl.length() == 0) {
                contentServerUrl = basisUrl + "/cs/cs?action=pdf&images=";
            }
            return new URL(prepareKitodoContentServerURL(process, contentServerUrl));
        }
    }

    private void useContentServerForPdfCreation(URI metaFile, URI userHome, Process process, String basisUrl)
            throws MalformedURLException {
        CreatePdfFromServletThread pdf = new CreatePdfFromServletThread();
        pdf.setMetsURL(metaFile.toURL());
        pdf.setTargetFolder(userHome);
        pdf.setInternalServletPath(basisUrl);
        logger.debug("Target directory: " + userHome);
        logger.debug("Using ContentServer2 base URL: " + basisUrl);
        pdf.initialize(process);
        pdf.start();
    }

    private String prepareKitodoContentServerURL(Process process, String contentServerUrl) throws IOException {
        FilenameFilter filter = new FileNameMatchesFilter("\\d*\\.tif");
        String normalizedTitle = ServiceManager.getProcessService().getNormalizedTitle(process.getTitle());
        URI imagesDir = ServiceManager.getProcessService().getImagesTifDirectory(true, process.getId(),
            process.getTitle(), process.getProcessBaseUri());
        List<URI> meta = fileService.getSubUris(filter, imagesDir);
        int capacity = contentServerUrl.length() + (meta.size() - 1) + AND_TARGET_FILE_NAME_IS.length()
                + normalizedTitle.length() + PDF_EXTENSION.length();
        TreeSet<String> fileNames = new TreeSet<>(new MetadataImageComparator());
        String basePath = ConfigCore.getKitodoDataDirectory();
        for (URI data : meta) {
            String file = basePath + data.getRawPath();
            fileNames.add(file);
            capacity += file.length();
        }
        StringBuilder url = new StringBuilder(capacity);
        url.append(contentServerUrl);
        boolean subsequent = false;
        for (String fileName : fileNames) {
            if (subsequent) {
                url.append('$');
            } else {
                subsequent = true;
            }
            url.append(fileName);
        }
        url.append(AND_TARGET_FILE_NAME_IS);
        url.append(normalizedTitle);
        url.append(PDF_EXTENSION);
        return url.toString();
    }

    private void completeResponse(FacesContext context, URL kitodoContentServerUrl, String normalizedTitle)
            throws IOException {
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        String fileName = normalizedTitle + PDF_EXTENSION;
        ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();
        String contentType = servletContext.getMimeType(fileName);
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        response.sendRedirect(kitodoContentServerUrl.toString());
        context.responseComplete();
    }
}
