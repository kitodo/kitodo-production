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

package de.sub.goobi.export.download;

import de.sub.goobi.helper.tasks.CreatePdfFromServletThread;

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
import org.kitodo.config.DefaultValues;
import org.kitodo.config.Parameters;
import org.kitodo.data.database.beans.Process;
import org.kitodo.helper.Helper;
import org.kitodo.metadata.comparator.MetadataImageComparator;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class ExportPdf extends ExportMets {
    private static final Logger logger = LogManager.getLogger(ExportPdf.class);
    private final ServiceManager serviceManager = new ServiceManager();
    private static final String AND_TARGET_FILE_NAME_IS = "&targetFileName=";
    private static final String PDF_EXTENSION = ".pdf";
    private final FileService fileService = serviceManager.getFileService();

    @Override
    public boolean startExport(Process process, URI userHome)
            throws ReadException, IOException, PreferencesException, WriteException, JAXBException {

        // Read Document
        FileformatInterface gdzfile = serviceManager.getProcessService().readMetadataFile(process);
        prepareUserDirectory(userHome);
        this.myPrefs = serviceManager.getRulesetService().getPreferences(process.getRuleset());

        // first of all write mets-file in images-Folder of process
        URI targetFileName = fileService.createResource(process.getTitle() + ".xml");
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

        if (!ConfigCore.getBooleanParameter(Parameters.PDF_AS_DOWNLOAD)) {
            useContentServerForPdfCreation(metaFile, userHome, process, basisUrl);
        } else {

            GetMethod method = null;
            try {
                // define path for mets and pdfs
                Integer contentServerTimeOut = ConfigCore.getIntParameter(Parameters.KITODO_CONTENT_SERVER_TIMEOUT,
                    DefaultValues.KITODO_CONTENT_SERVER_TIMEOUT);
                URL kitodoContentServerUrl = getKitodoContentServerURL(metaFile, process, basisUrl);

                // get pdf from servlet and forward response to file
                method = new GetMethod(kitodoContentServerUrl.toString());
                method.getParams().setParameter("http.socket.timeout", contentServerTimeOut);

                if (!context.getResponseComplete()) {
                    completeResponse(context, kitodoContentServerUrl, process);
                }
                fileService.delete(metaFile);
            } catch (RuntimeException e) {
                String text = "error while pdf creation: " + e.getMessage();
                URI uri = userHome.resolve(process.getTitle() + ".PDF-ERROR.log");
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
        String contentServerUrl = ConfigCore.getParameter(Parameters.KITODO_CONTENT_SERVER_URL);

        // using mets file
        if (serviceManager.getMetadataValidationService().validate(process)) {
            // if no contentServerUrl defined use internal
            // goobiContentServerServlet
            if (contentServerUrl == null || contentServerUrl.length() == 0) {
                contentServerUrl = basisUrl + "/gcs/gcs?action=pdf&metsFile=";
            }
            return new URL(
                    contentServerUrl + metaFile.toURL() + AND_TARGET_FILE_NAME_IS + process.getTitle() + PDF_EXTENSION);
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
        URI imagesDir = serviceManager.getProcessService().getImagesTifDirectory(true, process);
        List<URI> meta = fileService.getSubUris(filter, imagesDir);
        int capacity = contentServerUrl.length() + (meta.size() - 1) + AND_TARGET_FILE_NAME_IS.length()
                + process.getTitle().length() + PDF_EXTENSION.length();
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
        url.append(process.getTitle());
        url.append(PDF_EXTENSION);
        return url.toString();
    }

    private void completeResponse(FacesContext context, URL kitodoContentServerUrl, Process process)
            throws IOException {
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        String fileName = process.getTitle() + PDF_EXTENSION;
        ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();
        String contentType = servletContext.getMimeType(fileName);
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        response.sendRedirect(kitodoContentServerUrl.toString());
        context.responseComplete();
    }
}
