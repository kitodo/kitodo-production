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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.tasks.CreatePdfFromServletThread;
import de.sub.goobi.metadaten.MetadatenHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.methods.GetMethod;
import org.kitodo.api.filemanagement.filters.FileNameMatchesFilter;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

public class ExportPdf extends ExportMets {
    private final ServiceManager serviceManager = new ServiceManager();
    private static final String AND_TARGET_FILE_NAME_IS = "&targetFileName=";
    private static final String PDF_EXTENSION = ".pdf";
    private final FileService fileService = serviceManager.getFileService();

    @Override
    public boolean startExport(Process myProcess, URI inZielVerzeichnis)
            throws ReadException, IOException, PreferencesException, TypeNotAllowedForParentException, WriteException {

        /*
         * Read Document
         */
        FileformatInterface gdzfile = serviceManager.getProcessService().readMetadataFile(myProcess);
        prepareUserDirectory(inZielVerzeichnis);
        this.myPrefs = serviceManager.getRulesetService().getPreferences(myProcess.getRuleset());

        /*
         * first of all write mets-file in images-Folder of process
         */
        URI metsTempFile = fileService.createResource(myProcess.getTitle() + ".xml");
        writeMetsFile(myProcess, metsTempFile, gdzfile, true);
        Helper.setMeldung(null, myProcess.getTitle() + ": ", "mets file created");
        Helper.setMeldung(null, myProcess.getTitle() + ": ", "start pdf generation now");

        if (logger.isDebugEnabled()) {
            logger.debug("METS file created: " + metsTempFile);
        }

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
        String fullpath = req.getRequestURL().toString();
        String servletpath = context.getExternalContext().getRequestServletPath();
        String myBasisUrl = fullpath.substring(0, fullpath.indexOf(servletpath));

        if (!ConfigCore.getBooleanParameter("pdfAsDownload")) {
            /*
             * use contentserver api for creation of pdf-file
             */
            CreatePdfFromServletThread pdf = new CreatePdfFromServletThread();
            pdf.setMetsURL(metsTempFile.toURL());
            pdf.setTargetFolder(inZielVerzeichnis);
            pdf.setInternalServletPath(myBasisUrl);
            if (logger.isDebugEnabled()) {
                logger.debug("Taget directory: " + inZielVerzeichnis);
                logger.debug("Using ContentServer2 base URL: " + myBasisUrl);
            }
            pdf.initialize(myProcess);
            pdf.start();
        } else {

            GetMethod method = null;
            try {
                /*
                 * define path for mets and pdfs
                 */
                URL kitodoContentServerUrl = null;
                String contentServerUrl = ConfigCore.getParameter("kitodoContentServerUrl");
                Integer contentServerTimeOut = ConfigCore.getIntParameter("kitodoContentServerTimeOut", 60000);

                /*
                 * using mets file
                 */

                // TODO:second condition is always true if reached
                if (serviceManager.getMetadataValidationService().validate(myProcess) && metsTempFile.toURL() != null) {
                    /*
                     * if no contentserverurl defined use internal
                     * goobiContentServerServlet
                     */
                    if (contentServerUrl == null || contentServerUrl.length() == 0) {
                        contentServerUrl = myBasisUrl + "/gcs/gcs?action=pdf&metsFile=";
                    }
                    kitodoContentServerUrl = new URL(contentServerUrl + metsTempFile.toURL() + AND_TARGET_FILE_NAME_IS
                            + myProcess.getTitle() + PDF_EXTENSION);
                    /*
                     * mets data does not exist or is invalid
                     */

                } else {
                    if (contentServerUrl == null || contentServerUrl.length() == 0) {
                        contentServerUrl = myBasisUrl + "/cs/cs?action=pdf&images=";
                    }
                    FilenameFilter filter = new FileNameMatchesFilter("\\d*\\.tif");
                    URI imagesDir = serviceManager.getProcessService().getImagesTifDirectory(true, myProcess);
                    ArrayList<URI> meta = fileService.getSubUris(filter, imagesDir);
                    int capacity = contentServerUrl.length() + (meta.size() - 1) + AND_TARGET_FILE_NAME_IS.length()
                            + myProcess.getTitle().length() + PDF_EXTENSION.length();
                    TreeSet<String> filenames = new TreeSet<>(new MetadatenHelper(null, null));
                    for (URI data : meta) {
                        String file = data.toURL().toString();
                        filenames.add(file);
                        capacity += file.length();
                    }
                    StringBuilder url = new StringBuilder(capacity);
                    url.append(contentServerUrl);
                    boolean subsequent = false;
                    for (String f : filenames) {
                        if (subsequent) {
                            url.append('$');
                        } else {
                            subsequent = true;
                        }
                        url.append(f);
                    }
                    url.append(AND_TARGET_FILE_NAME_IS);
                    url.append(myProcess.getTitle());
                    url.append(PDF_EXTENSION);
                    kitodoContentServerUrl = new URL(url.toString());
                }

                /*
                 * get pdf from servlet and forward response to file
                 */
                method = new GetMethod(kitodoContentServerUrl.toString());
                method.getParams().setParameter("http.socket.timeout", contentServerTimeOut);

                if (!context.getResponseComplete()) {
                    HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
                    String fileName = myProcess.getTitle() + PDF_EXTENSION;
                    ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();
                    String contentType = servletContext.getMimeType(fileName);
                    response.setContentType(contentType);
                    response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
                    response.sendRedirect(kitodoContentServerUrl.toString());
                    context.responseComplete();
                }
                if (metsTempFile.toURL() != null) {
                    File tempMets = new File(metsTempFile.toURL().toString());
                    tempMets.delete();
                }
            } catch (Exception e) {

                /*
                 * report Error to User as Error-Log
                 */
                String text = "error while pdf creation: " + e.getMessage();
                URI uri = inZielVerzeichnis.resolve(myProcess.getTitle() + ".PDF-ERROR.log");
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
}
