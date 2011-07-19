package de.sub.goobi.helper.tasks;

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

import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Metadaten.MetadatenHelper;
import de.sub.goobi.Metadaten.MetadatenImagesHelper;
import de.sub.goobi.Metadaten.MetadatenVerifizierung;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;

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

	@Override
	public void initialize(Prozess inProzess) {
		super.initialize(inProzess);
		setTitle("Create PDF: " + inProzess.getTitel());
	}

	/**
	 * Aufruf als Thread
	 * ================================================================
	 */
	public void run() {
		setStatusProgress(30);
		if (this.getProzess() == null || targetFolder == null || internalServletPath == null) {
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
			File finalPdf = new File(targetFolder, this.getProzess().getTitel() + ".pdf");
			Integer contentServerTimeOut = ConfigMain.getIntParameter("goobiContentServerTimeOut", 60000);
			
			/* --------------------------------
			 * using mets file
			 * --------------------------------*/

			
			if (new MetadatenVerifizierung().validate(this.getProzess()) && metsURL != null) {
				/* if no contentserverurl defined use internal goobiContentServerServlet */
					if (contentServerUrl == null || contentServerUrl.length() == 0) {
						contentServerUrl = internalServletPath + "/gcs/gcs?action=pdf&metsFile=";
					}
				goobiContentServerUrl = new URL(contentServerUrl + metsURL);		
			
				/* --------------------------------
				 * mets data does not exist or is invalid
				 * --------------------------------*/
				
			} else {
				if (contentServerUrl == null || contentServerUrl.length() == 0) {
					contentServerUrl = internalServletPath + "/cs/cs?action=pdf&images=";
				}
				String url = "";
				FilenameFilter filter = MetadatenImagesHelper.filter;
				File imagesDir = new File(this.getProzess().getImagesTifDirectory());
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
			while (count != -1 && count <= 8192) {
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
			if (metsURL != null) {
				File tempMets = new File(metsURL.toString());
				tempMets.delete();
			}
			//TODO: Don't catch Exception (the super class)
		} catch (Exception e) {
			logger.error("Error while creating pdf for " + this.getProzess().getTitel(), e);
			setStatusMessage("error " + e.getClass().getSimpleName() + " while pdf creation: " + e.getMessage());
			setStatusProgress(-1);

			/* --------------------------------
			 * report Error to User as Error-Log
			 * --------------------------------*/
			Writer output = null;
			String text = "error while pdf creation: " + e.getMessage();
			File file = new File(targetFolder, this.getProzess().getTitel() + ".PDF-ERROR.log");
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
	 * Setter for tempFolder
	 * 
	 * @param tempFolder
	 *            the tempFolder to set
	 **************************************************************************************/
/*	public void setTempFolder(File tempFolder) {
		this.tempFolder = tempFolder;
	}
*/
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
		return metsURL;
	}

	public void setMetsURL(URL metsURL) {
		this.metsURL = metsURL;
	}

}
