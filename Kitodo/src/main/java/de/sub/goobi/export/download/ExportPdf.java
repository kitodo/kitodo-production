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

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.helper.tasks.CreatePdfFromServletThread;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.metadaten.MetadatenVerifizierung;

import java.io.BufferedWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.TreeSet;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.methods.GetMethod;

import org.goobi.io.FileListFilter;
import org.goobi.io.SafeFile;

import ugh.dl.Fileformat;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

public class ExportPdf extends ExportMets {

	private static final String AND_TARGET_FILE_NAME_IS = "&targetFileName=";
	private static final String PDF_EXTENSION = ".pdf";

	@Override
	public boolean startExport(Prozess myProzess, String inZielVerzeichnis) throws IOException, InterruptedException,
			PreferencesException, WriteException, DocStructHasNoTypeException, MetadataTypeNotAllowedException,
			ExportFileException, UghHelperException, ReadException, SwapException, DAOException,
			TypeNotAllowedForParentException {

		/*
		 * Read Document
		 */
		Fileformat gdzfile = myProzess.readMetadataFile();
		String zielVerzeichnis = prepareUserDirectory(inZielVerzeichnis);
		this.myPrefs = myProzess.getRegelsatz().getPreferences();

		/*
		 * irst of all write mets-file in images-Folder of process
		 */
		SafeFile metsTempFile = SafeFile.createTempFile(myProzess.getTitel(), ".xml");
		writeMetsFile(myProzess, metsTempFile.toString(), gdzfile, true);
		Helper.setMeldung(null, myProzess.getTitel() + ": ", "mets file created");
		Helper.setMeldung(null, myProzess.getTitel() + ": ", "start pdf generation now");

		if (myLogger.isDebugEnabled()) {
			myLogger.debug("METS file created: " + metsTempFile);
		}

		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
		String fullpath = req.getRequestURL().toString();
		String servletpath = context.getExternalContext().getRequestServletPath();
		String myBasisUrl = fullpath.substring(0, fullpath.indexOf(servletpath));

		if (!ConfigMain.getBooleanParameter("pdfAsDownload")) {
			/*
			 * use contentserver api for creation of pdf-file
			 */
			CreatePdfFromServletThread pdf = new CreatePdfFromServletThread();
			pdf.setMetsURL(metsTempFile.toURI().toURL());
			pdf.setTargetFolder(new SafeFile(zielVerzeichnis));
			pdf.setInternalServletPath(myBasisUrl);
			if (myLogger.isDebugEnabled()) {
				myLogger.debug("Taget directory: " + zielVerzeichnis);
				myLogger.debug("Using ContentServer2 base URL: " + myBasisUrl);
			}
			pdf.initialize(myProzess);
			pdf.start();
		} else {

			GetMethod method = null;
			try {
				/*
				 * define path for mets and pdfs
				 */
				URL goobiContentServerUrl = null;
				String contentServerUrl = ConfigMain.getParameter("goobiContentServerUrl");
				Integer contentServerTimeOut = ConfigMain.getIntParameter("goobiContentServerTimeOut", 60000);

				/*
				 * using mets file
				 */

				if (new MetadatenVerifizierung().validate(myProzess) && metsTempFile.toURI().toURL() != null) {
					/* if no contentserverurl defined use internal goobiContentServerServlet */
					if (contentServerUrl == null || contentServerUrl.length() == 0) {
						contentServerUrl = myBasisUrl + "/gcs/gcs?action=pdf&metsFile=";
					}
					goobiContentServerUrl = new URL(contentServerUrl + metsTempFile.toURI().toURL()
							+ AND_TARGET_FILE_NAME_IS + myProzess.getTitel() + PDF_EXTENSION);
					/*
					 *  mets data does not exist or is invalid
					 */

				} else {
					if (contentServerUrl == null || contentServerUrl.length() == 0) {
						contentServerUrl = myBasisUrl + "/cs/cs?action=pdf&images=";
					}
					FilenameFilter filter = new FileListFilter("\\d*\\.tif");
					SafeFile imagesDir = new SafeFile(myProzess.getImagesTifDirectory(true));
					SafeFile[] meta = imagesDir.listFiles(filter);
					int capacity = contentServerUrl.length() + (meta.length - 1) + AND_TARGET_FILE_NAME_IS.length()
							+ myProzess.getTitel().length() + PDF_EXTENSION.length();
					TreeSet<String> filenames = new TreeSet<String>(new MetadatenHelper(null, null));
					for (SafeFile data : meta) {
						String file = data.toURI().toURL().toString();
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
					url.append(myProzess.getTitel());
					url.append(PDF_EXTENSION);
					goobiContentServerUrl = new URL(url.toString());
				}

				/*
				 * get pdf from servlet and forward response to file
				 */

				method = new GetMethod(goobiContentServerUrl.toString());
				method.getParams().setParameter("http.socket.timeout", contentServerTimeOut);

				if (!context.getResponseComplete()) {
					HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
					String fileName = myProzess.getTitel() + PDF_EXTENSION;
					ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();
					String contentType = servletContext.getMimeType(fileName);
					response.setContentType(contentType);
					response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
					response.sendRedirect(goobiContentServerUrl.toString());
					context.responseComplete();
				}
				if (metsTempFile.toURI().toURL() != null) {
					SafeFile tempMets = new SafeFile(metsTempFile.toURI().toURL().toString());
					tempMets.delete();
				}
			} catch (Exception e) {

				/*
				 * report Error to User as Error-Log
				 */
				String text = "error while pdf creation: " + e.getMessage();
				SafeFile file = new SafeFile(zielVerzeichnis, myProzess.getTitel() + ".PDF-ERROR.log");
				try (BufferedWriter output = new BufferedWriter(file.createFileWriter())) {
					output.write(text);
				} catch (IOException e1) {
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
