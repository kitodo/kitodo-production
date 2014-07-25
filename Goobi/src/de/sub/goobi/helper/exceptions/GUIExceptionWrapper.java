package de.sub.goobi.helper.exceptions;
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
import java.util.ArrayList;
import java.util.Date;

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;

/**
 * This class provides the tools it takes to generate a configurable Error
 * message for Errors which are unexpected An example for the area in
 * GoobiProperties.config is given after the class declaration in the source
 * code.
 * 
 * Besides building up the information in the constructor the other important
 * method is getLocalizedMessage(), which provides the build up message in html
 * 
 * @author Wulf
 * @version 12/10/2009
 * 
 * Variables in Messages Bundle:
 * err_emailBody -> message in the email before the stack trace
 * err_emailMessage -> message displayed if email is enabled in GoobiConfig: err_userHandling=true 
 * err_fallBackMessage -> messgae displayed if feature is turned off in GoobiConfig
 * err_linkText -> message in which the link from GoobiConfig: err_linkToPage=
 * err_noMailService -> message if email is disabled in GoobiConfig: err_emailEnabled=false 
 * err_subjectLine -> message in Subject Line of email
 * 
 */
public class GUIExceptionWrapper extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	

	private String fallBackErrorMessage = Helper.getTranslation("err_fallBackMessage");

	private String userSeenErrorMessage = "";

	private String additionalMessage = "";

	private String err_linkText = "";

	private String err_emailBody = "";
	private String err_emailMessage = "";
	private String err_subjectLine = "";

	private ArrayList<String> emailAdresses = new ArrayList<String>();

	private String internalErrorMsg = "";

	// private constructor to avoid wrong construction
	@SuppressWarnings("unused")
	private GUIExceptionWrapper() {
	}

	@SuppressWarnings("unused")
	private GUIExceptionWrapper(String message) {
	}

	public GUIExceptionWrapper(Throwable cause) {
		super.initCause(cause);
	}

	/**
	 * Exception Class catching unhandled exceptions to wrap it for GUI
	 * 
	 * @param message,  additional info, like which class called this constructor
	 * @param cause, last Exception cought with this wrapper
	 */
	public GUIExceptionWrapper(String message, Throwable cause) {	
		this(cause);
		this.additionalMessage = message + "<br/>";
		init();
	}

	private void init() {

		try {
			if (ConfigMain.getBooleanParameter("err_userHandling")) {
				this.err_linkText = Helper.getTranslation("err_linkText");
				this.err_linkText = this.err_linkText.replace("{0}",
						ConfigMain.getParameter("err_linkToPage", "./Main.jsf"));

				if (ConfigMain.getBooleanParameter("err_emailEnabled")) {

					this.err_emailMessage = Helper.getTranslation("err_emailMessage");
					this.err_subjectLine = Helper.getTranslation("err_subjectLine");
					this.err_emailBody = Helper.getTranslation("err_emailBody");

					Integer emailCounter = Integer.valueOf(0);
					String email = "";

					// indefinite emails can be added
					while (!email.equals("end")) {
						emailCounter++;
						email = ConfigMain.getParameter("err_emailAddress" + emailCounter.toString(), "end");
						if (!email.equals("end")) {
							this.emailAdresses.add(email);
						}
					}

				} else {
					// no email service enabled, build standard message
					this.err_emailMessage = Helper.getTranslation("err_noMailService");

				}
			} else {
				this.internalErrorMsg = this.internalErrorMsg + "Feature turned off:<br/><br/>";
				this.userSeenErrorMessage = this.fallBackErrorMessage;
			}

		} catch (Exception e) {
			this.internalErrorMsg = this.internalErrorMsg + "Error on loading Config items:<br/>" + e.getMessage() + "<br/><br/>";
			this.userSeenErrorMessage = this.fallBackErrorMessage;

		} finally {
		}
	}

	/**
	 * this method overwrites supers method of the same name. It provides the output of collected error data and shapes it into html format for display in browsers
	 */
	@Override
	public String getLocalizedMessage() {

		final String lineFeed = "\r\n";
		final String htmlLineFeed = "<br/>";

		final String mailtoLinkHrefMailTo = "mailto:";
		final String mailtoLinkSubject = "?subject=";
		final String mailtoLinkBody = "&body=";

		if (this.userSeenErrorMessage.length() > 0) {
			return this.additionalMessage + this.userSeenErrorMessage;
		}

		// according to config the message to the user consists of two parts
		// the part, which may contain a href web link and the part, which
		// allows a mailto: link triggered email from the user to
		// admin/developers
		String linkPart = "";
		String emailPart = "";

		linkPart = this.err_linkText + lineFeed;

		// only elaborate email part if
		if (this.emailAdresses.size() > 0) {
			emailPart = this.err_emailMessage.replace("{0}", 
					mailtoLinkHrefMailTo + getAdresses() + 
					mailtoLinkSubject + this.err_subjectLine + 
					mailtoLinkBody +  this.err_emailBody +
					htmlLineFeed + htmlLineFeed + 	
					htmlLineFeed + getContextInfo() + 
					htmlLineFeed + getStackTrace(this.getCause().getStackTrace()));

		} else {
			// if no adresse a general text will be provided by this class
			emailPart = Helper.getTranslation("err_noMailService");
		}

		this.userSeenErrorMessage = this.internalErrorMsg + linkPart + htmlLineFeed
				+ emailPart;

		return this.userSeenErrorMessage;
	}

	/**
	 * 
	 * @return collected adresses as a string to be used after <a href="mailto:"
	 */
	private String getAdresses() {
		StringBuffer adresses = new StringBuffer();
		for (String emailAddy : this.emailAdresses) {
			adresses = adresses.append(emailAddy).append(",%20");
		}
		return adresses.toString();
	}

	/**
	 * 
	 * @param aThrowable
	 * @return stack trace as String
	 */
	private String getStackTrace(StackTraceElement[] stackTrace) {
		String stackTraceReturn = "";
		String tempTraceReturn = "";
		Integer counter = 0;
		for (StackTraceElement itStackTrace : stackTrace) {
			// only taking those elements from the stack trace, which contain goobi and the top level element
			if (counter++==1 || itStackTrace.toString().toLowerCase().contains("goobi")){
				stackTraceReturn = stackTraceReturn +  "<br/>" + itStackTrace.toString();
				tempTraceReturn = "";
			}else{
				if (tempTraceReturn.length()<1){
					stackTraceReturn = stackTraceReturn + "<br/> ---- skipping non goobi class(es) .";
				}else{
					stackTraceReturn = stackTraceReturn + " .";
				}
				tempTraceReturn = "<br/>" + itStackTrace.toString();
			}
			
			if (stackTraceReturn.length()>1000) {
				return stackTraceReturn + tempTraceReturn + "<br/><br/>	---- truncated rest of stack trace to avoid overflow ---- ";
			}
		}
		return stackTraceReturn + tempTraceReturn + "<br/><br/>	---- bottom of stack trace ---- ";
	}

	/**
	 * 
	 * @returns the Class of the initial Exception if possible
	 */
	private String getContextInfo(){
		String getContextInfo = "";
		getContextInfo = getContextInfo + "ThrowingClass=" + this.additionalMessage;		
		getContextInfo = getContextInfo + "Time=" + new Date().toString() + "<br/>";
		getContextInfo = getContextInfo + "Cause=" + super.getCause() + "<br/>";
		return getContextInfo;
	}

}
