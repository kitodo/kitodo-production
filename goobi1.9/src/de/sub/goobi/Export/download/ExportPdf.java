package de.sub.goobi.Export.download;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.methods.GetMethod;

import ugh.dl.Fileformat;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Metadaten.MetadatenHelper;
import de.sub.goobi.Metadaten.MetadatenVerifizierung;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FileUtils;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.helper.tasks.CreatePdfFromServletThread;

public class ExportPdf extends ExportMets {

	@Override
	public boolean startExport(Prozess myProzess, String inZielVerzeichnis) throws IOException, InterruptedException, PreferencesException,
			WriteException, DocStructHasNoTypeException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException,
			SwapException, DAOException, TypeNotAllowedForParentException {

		/*
		 * -------------------------------- Read Document --------------------------------
		 */
		Fileformat gdzfile = myProzess.readMetadataFile();
		String zielVerzeichnis = prepareUserDirectory(inZielVerzeichnis);
		this.myPrefs = myProzess.getRegelsatz().getPreferences();

		/*
		 * -------------------------------- first of all write mets-file in images-Folder of process --------------------------------
		 */
		new File("");
		File metsTempFile = File.createTempFile(myProzess.getTitel(), ".xml");
		writeMetsFile(myProzess, metsTempFile.toString(), gdzfile, true);
		Helper.setMeldung(null, myProzess.getTitel() + ": ", "mets file created");
		Helper.setMeldung(null, myProzess.getTitel() + ": ", "start pdf generation now");

		myLogger.debug("METS file created: " + metsTempFile);

		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
		String fullpath = req.getRequestURL().toString();
		String servletpath = context.getExternalContext().getRequestServletPath();
		String myBasisUrl = fullpath.substring(0, fullpath.indexOf(servletpath));

		if (!ConfigMain.getBooleanParameter("pdfAsDownload")) {
			/*
			 * -------------------------------- use contentserver api for creation of pdf-file --------------------------------
			 */
			CreatePdfFromServletThread pdf = new CreatePdfFromServletThread();
			pdf.setMetsURL(metsTempFile.toURI().toURL());
			pdf.setTargetFolder(new File(zielVerzeichnis));
			pdf.setInternalServletPath(myBasisUrl);
			myLogger.debug("Taget directory: " + zielVerzeichnis);
			myLogger.debug("Using ContentServer2 base URL: " + myBasisUrl);
			pdf.initialize(myProzess);
			pdf.start();
		} else {

			GetMethod method = null;
			try {
				/*
				 * -------------------------------- define path for mets and pdfs --------------------------------
				 */
				URL goobiContentServerUrl = null;
				String contentServerUrl = ConfigMain.getParameter("goobiContentServerUrl");
				Integer contentServerTimeOut = ConfigMain.getIntParameter("goobiContentServerTimeOut", 60000);

				/*
				 * -------------------------------- using mets file --------------------------------
				 */

				if (new MetadatenVerifizierung().validate(myProzess) && metsTempFile.toURI().toURL() != null) {
					/* if no contentserverurl defined use internal goobiContentServerServlet */
					if (contentServerUrl == null || contentServerUrl.length() == 0) {
						contentServerUrl = myBasisUrl + "/gcs/gcs?action=pdf&metsFile=";
					}
					goobiContentServerUrl = new URL(contentServerUrl + metsTempFile.toURI().toURL() + "&targetFileName=" + myProzess.getTitel() + ".pdf");
					/*
					 * -------------------------------- mets data does not exist or is invalid --------------------------------
					 */

				} else {
					if (contentServerUrl == null || contentServerUrl.length() == 0) {
						contentServerUrl = myBasisUrl + "/cs/cs?action=pdf&images=";
					}
					String url = "";
					FilenameFilter filter = new FileUtils.FileListFilter("\\d*\\.tif");
					File imagesDir = new File(myProzess.getImagesTifDirectory(true));
					File[] meta = imagesDir.listFiles(filter);
					ArrayList<String> filenames = new ArrayList<String>();
					for (File data : meta) {
						String file = "";
						file += data.toURI().toURL();
						filenames.add(file);
					}
					Collections.sort(filenames, new MetadatenHelper(null, null));
					for (String f : filenames) {
						url = url + f + "$";
					}
					String imageString = url.substring(0, url.length() - 1);
					String targetFileName = "&targetFileName=" + myProzess.getTitel() + ".pdf";
					goobiContentServerUrl = new URL(contentServerUrl + imageString + targetFileName);
					
				}

				/*
				 * -------------------------------- get pdf from servlet and forward response to file --------------------------------
				 */

				method = new GetMethod(goobiContentServerUrl.toString());
				method.getParams().setParameter("http.socket.timeout", contentServerTimeOut);

				if (!context.getResponseComplete()) {
					HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
					String fileName = myProzess.getTitel() + ".pdf";
					ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();
					String contentType = servletContext.getMimeType(fileName);
					response.setContentType(contentType);
					response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
					response.sendRedirect(goobiContentServerUrl.toString());
					context.responseComplete();
				}
				if (metsTempFile.toURI().toURL() != null) {
					File tempMets = new File(metsTempFile.toURI().toURL().toString());
					tempMets.delete();
				}
			} catch (Exception e) {

				/*
				 * -------------------------------- report Error to User as Error-Log --------------------------------
				 */
				Writer output = null;
				String text = "error while pdf creation: " + e.getMessage();
				File file = new File(zielVerzeichnis, myProzess.getTitel() + ".PDF-ERROR.log");
				try {
					output = new BufferedWriter(new FileWriter(file));
					output.write(text);
					output.close();
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
