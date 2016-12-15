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

package de.sub.goobi.helper.tasks;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import org.goobi.io.SafeFile;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.metadaten.MetadatenVerifizierung;

/*************************************************************************************
 * Creation of PDF-Files as long running task for GoobiContentServerServlet
 * First of all the variables have to be set via the setters after that you can
 * initialize and run it
 * 
 * @author Steffen Hankiewicz
 * @version 12.02.2009
 *************************************************************************************/
public class CreatePdfFromServletThread extends LongRunningTask {
	private static final Logger logger = Logger.getLogger(CreatePdfFromServletThread.class);
	private SafeFile targetFolder;
	private String internalServletPath;
	private URL metsURL;

	public CreatePdfFromServletThread() {
	}

	/**
	 * The clone constructor creates a new instance of this object. This is
	 * necessary for Threads that have terminated in order to render to run them
	 * again possible.
	 * 
	 * @param master
	 *            copy master to create a clone of
	 */
	public CreatePdfFromServletThread(CreatePdfFromServletThread master) {
		super(master);
	}

	@Override
	public void initialize(Prozess inProzess) {
		super.initialize(inProzess);
		setTitle("Create PDF: " + inProzess.getTitel());
	}

	/**
	 * Aufruf als Thread
	 * ================================================================
	 */
	@Override
	public void run() {
		setStatusProgress(30);
		if ((this.getProzess() == null) || (this.targetFolder == null) || (this.internalServletPath == null)) {
			setStatusMessage("parameters for temporary and final folder and internal servlet path not defined");
			setStatusProgress(-1);
			return;
		}
		GetMethod method = null;
		try {
			/* --------------------------------
			 * define path for mets and pdfs
			 * --------------------------------*/
			URL goobiContentServerUrl = null;
			String contentServerUrl = ConfigMain.getParameter("goobiContentServerUrl");
			new SafeFile("");
			SafeFile tempPdf = SafeFile.createTempFile(this.getProzess().getTitel(), ".pdf");
			SafeFile finalPdf = new SafeFile(this.targetFolder, this.getProzess().getTitel() + ".pdf");
			Integer contentServerTimeOut = ConfigMain.getIntParameter("goobiContentServerTimeOut", 60000);
			
			/* --------------------------------
			 * using mets file
			 * --------------------------------*/

			
			if (new MetadatenVerifizierung().validate(this.getProzess()) && (this.metsURL != null)) {
				/* if no contentserverurl defined use internal goobiContentServerServlet */
					if ((contentServerUrl == null) || (contentServerUrl.length() == 0)) {
						contentServerUrl = this.internalServletPath + "/gcs/gcs?action=pdf&metsFile=";
					}
				goobiContentServerUrl = new URL(contentServerUrl + this.metsURL);		
			
				/* --------------------------------
				 * mets data does not exist or is invalid
				 * --------------------------------*/
				
			} else {
				if ((contentServerUrl == null) || (contentServerUrl.length() == 0)) {
					contentServerUrl = this.internalServletPath + "/cs/cs?action=pdf&images=";
				}
				String url = "";
				FilenameFilter filter = Helper.imageNameFilter;
				SafeFile imagesDir = new SafeFile(this.getProzess().getImagesTifDirectory(true));
				SafeFile[] meta = imagesDir.listFiles(filter);
				ArrayList<String> filenames = new ArrayList<String>();
				for (SafeFile data : meta) {
					String file = "";
					file +=data.toURI().toURL();
					filenames.add(file);
				}
				Collections.sort(filenames, new MetadatenHelper(null, null));
				for (String f : filenames) {
					url = url + f + "$";
				}
				String imageString = url.substring(0, url.length()-1);
				String targetFileName = "&targetFileName=" + this.getProzess().getTitel()+".pdf";	
				goobiContentServerUrl = new URL(contentServerUrl + imageString + targetFileName);
			}
			
			/* --------------------------------
			 * get pdf from servlet and forward response to file 
			 * --------------------------------*/

			HttpClient httpclient = new HttpClient();
			if(logger.isDebugEnabled()){
				logger.debug("Retrieving: " + goobiContentServerUrl.toString());
			}
			method = new GetMethod(goobiContentServerUrl.toString());
			try {
			method.getParams().setParameter("http.socket.timeout", contentServerTimeOut);
			int statusCode = httpclient.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				logger.error("HttpStatus nicht ok", null);
				if(logger.isDebugEnabled()){
					logger.debug("Response is:\n" + method.getResponseBodyAsString());
				}
				return;
			}

			InputStream inStream = method.getResponseBodyAsStream();
			try (
				BufferedInputStream bis = new BufferedInputStream(inStream);
				FileOutputStream fos = tempPdf.createFileOutputStream();
			) {
				byte[] bytes = new byte[8192];
				int count = bis.read(bytes);
				while ((count != -1) && (count <= 8192)) {
					fos.write(bytes, 0, count);
					count = bis.read(bytes);
				}
				if (count != -1) {
					fos.write(bytes, 0, count);
				}
			}
			setStatusProgress(80);
			} finally {
				method.releaseConnection();
			}
			/* --------------------------------
			 * copy pdf from temp to final destination
			 * --------------------------------*/
			if(logger.isDebugEnabled()){
				logger.debug("pdf file created: " + tempPdf.getAbsolutePath() + "; now copy it to " + finalPdf.getAbsolutePath());
			}
			tempPdf.copyFile(finalPdf, false);
			if(logger.isDebugEnabled()){
				logger.debug("pdf copied to " + finalPdf.getAbsolutePath() + "; now start cleaning up");
			}
			tempPdf.delete();
			if (this.metsURL != null) {
				SafeFile tempMets = new SafeFile(this.metsURL.toString());
				tempMets.delete();
			}
		} catch (Exception e) {
			logger.error("Error while creating pdf for " + this.getProzess().getTitel(), e);
			setStatusMessage("error " + e.getClass().getSimpleName() + " while pdf creation: " + e.getMessage());
			setStatusProgress(-1);

			/* --------------------------------
			 * report Error to User as Error-Log
			 * --------------------------------*/
			String text = "error while pdf creation: " + e.getMessage();
			SafeFile file = new SafeFile(this.targetFolder, this.getProzess().getTitel() + ".PDF-ERROR.log");
			try (BufferedWriter output = new BufferedWriter(file.createFileWriter())) {
				output.write(text);
			} catch (IOException e1) {
				logger.error("Error while reporting error to user in file " + file.getAbsolutePath(), e);
			}
			return;
		} finally {
			if (method != null) {
				method.releaseConnection();
			}

		}
		setStatusMessage("done");
		setStatusProgress(100);
	}


	/**************************************************************************************
	 * Setter for targetFolder
	 * 
	 * @param targetFolder
	 *            the targetFolder to set
	 **************************************************************************************/
	public void setTargetFolder(SafeFile targetFolder) {
		this.targetFolder = targetFolder;
	}

	/**************************************************************************************
	 * Setter for internalServletPath
	 * 
	 * @param internalServletPath
	 *            the internalServletPath to set
	 **************************************************************************************/
	public void setInternalServletPath(String internalServletPath) {
		this.internalServletPath = internalServletPath;
	}

	public URL getMetsURL() {
		return this.metsURL;
	}

	public void setMetsURL(URL metsURL) {
		this.metsURL = metsURL;
	}

	/**
	 * Calls the clone constructor to create a not yet executed instance of this
	 * thread object. This is necessary for threads that have terminated in
	 * order to render possible to restart them.
	 * 
	 * @return a not-yet-executed replacement of this thread
	 * @see de.sub.goobi.helper.tasks.EmptyTask#replace()
	 */
	@Override
	public CreatePdfFromServletThread replace() {
		return new CreatePdfFromServletThread(this);
	}

	/**
	 * Returns the display name of the task to show to the user.
	 * 
	 * @see de.sub.goobi.helper.tasks.INameableTask#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return Helper.getTranslation("CreatePdfFromServletThread");
	}
}
