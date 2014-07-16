package de.sub.goobi.helper.tasks;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
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
	private File targetFolder;
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
			new File("");
			File tempPdf = File.createTempFile(this.getProzess().getTitel(), ".pdf");
			File finalPdf = new File(this.targetFolder, this.getProzess().getTitel() + ".pdf");
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
				File imagesDir = new File(this.getProzess().getImagesTifDirectory(true));
				File[] meta = imagesDir.listFiles(filter);
				ArrayList<String> filenames = new ArrayList<String>();
				for (File data : meta) {
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
			logger.debug("Retrieving: " + goobiContentServerUrl.toString());
			method = new GetMethod(goobiContentServerUrl.toString());
			try {
			method.getParams().setParameter("http.socket.timeout", contentServerTimeOut);
			int statusCode = httpclient.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				logger.error("HttpStatus nicht ok", null);
				logger.debug("Response is:\n" + method.getResponseBodyAsString());
				return;
			}

			InputStream inStream = method.getResponseBodyAsStream();
			BufferedInputStream bis = new BufferedInputStream(inStream);
			FileOutputStream fos = new FileOutputStream(tempPdf);
			byte[] bytes = new byte[8192];
			int count = bis.read(bytes);
			while ((count != -1) && (count <= 8192)) {
				fos.write(bytes, 0, count);
				count = bis.read(bytes);
			}
			if (count != -1) {
				fos.write(bytes, 0, count);
			}
			fos.close();
			bis.close();
			setStatusProgress(80);
			} finally {
				method.releaseConnection();
			}
			/* --------------------------------
			 * copy pdf from temp to final destination
			 * --------------------------------*/
			logger.debug("pdf file created: " + tempPdf.getAbsolutePath() + "; now copy it to " + finalPdf.getAbsolutePath());
			Helper.copyFile(tempPdf, finalPdf);
			logger.debug("pdf copied to " + finalPdf.getAbsolutePath() + "; now start cleaning up");
			tempPdf.delete();
			if (this.metsURL != null) {
				File tempMets = new File(this.metsURL.toString());
				tempMets.delete();
			}
		} catch (Exception e) {
			logger.error("Error while creating pdf for " + this.getProzess().getTitel(), e);
			setStatusMessage("error " + e.getClass().getSimpleName() + " while pdf creation: " + e.getMessage());
			setStatusProgress(-1);

			/* --------------------------------
			 * report Error to User as Error-Log
			 * --------------------------------*/
			Writer output = null;
			String text = "error while pdf creation: " + e.getMessage();
			File file = new File(this.targetFolder, this.getProzess().getTitel() + ".PDF-ERROR.log");
			try {
				output = new BufferedWriter(new FileWriter(file));
				output.write(text);
				output.close();
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
	public void setTargetFolder(File targetFolder) {
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
