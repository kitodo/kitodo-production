/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.helper.exceptions;

import java.util.ArrayList;
import java.util.Date;

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Messages;

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
 * Variables in messages Bundle:
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

	/*
	 * Example for GoobiConfig.properties
	 * 
	 * #use this to turn this feature on or off (true/false) 
	 * err_userHandling=true
	 *
	 * # page the user will be directed to continue
	 * err_linkToPage=../Main.jsf
	 * 
	 * # use this to turn the email feature on or off, user handling has to be set to true for this to have any effect
	 * err_emailEnabled=true
	 * 
	 * # an indefinate number of email adresses can be entered here as long as the pattern is met
	 * err_emailAddress1=goobi@intranda.com
	 * err_emailAddress2=mahnke@sub.uni-goettingen.de
	 * 
	 * ######################
	 * 
	 * the displayed messages are taken from the messages Package
	 * 
	 * Here the current example for messgaes_en.properties
	 * 
	 * err_emailBody=An error ocurred in Goobi. Please add to this message a short description of what you just did in Goobi  when this error occurred.
	 * err_emailMessage=If this error occurred for the first time you could <a href\="{0}"> send an email to the developers </a> to support the developement of Goobi. Please mind further requests for information within the email.
	 * err_fallBackMessage=There was an error in the programms execution. <br/> Unfortunately the more user and developer friendly feature of a link provided for the continuation of work  <br/> and the bug reporting in GoobiConfig.properties via email is disabled or not sufficiently set up. <br/> <br/> Please request your administrator to change the settings in GoobiConfig.properties for a user friendly display of this error.
	 * err_linkText=You may ignore the stack trace ignorieren and continue your work with a click <a href\="{0}"> here </a>. <br/>If this doesn't work you may hace to use the naviagation or even restart the application by logging out and back in.
	 * err_noMailService=The feature to notify the development team by email from here is disabled
	 * err_subjectLine=Goobi Application Error
	 * 
	 */

	private String fallBackErrorMessage = Messages.getString("err_fallBackMessage");

	private String userSeenErrorMessage = "";

	private String additionalMessage = "";

	// see comment above
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
		additionalMessage = message + "<br/>";
		init();
	}

	private void init() {

		try {
			if (ConfigMain.getBooleanParameter("err_userHandling")) {
				err_linkText = Messages.getString("err_linkText");
				err_linkText = err_linkText.replace("{0}", Messages.getString("err_linkToPage")).replace("err_linkToPage", "./Main.jsf");

				if (ConfigMain.getBooleanParameter("err_emailEnabled")) {

					err_emailMessage = Messages.getString("err_emailMessage");
					err_subjectLine = Messages.getString("err_subjectLine");
					err_emailBody = Messages.getString("err_emailBody");

					Integer emailCounter = Integer.valueOf(0);
					String email = "";

					// indefinate emails can be added
					while (!email.equals("end")) {
						emailCounter++;
						email = ConfigMain.getParameter("err_emailAddress" + emailCounter.toString(), "end");
						if (!email.equals("end")) {
							emailAdresses.add(email);
						}
					}

				} else {
					// no email service enabled, build standard message
					err_emailMessage = Messages.getString("err_noMailService");

				}
			} else {
				internalErrorMsg = internalErrorMsg + "Feature turned off:<br/><br/>";
				userSeenErrorMessage = fallBackErrorMessage;
			}

		} catch (Exception e) {
			internalErrorMsg = internalErrorMsg + "Error on loading Config items:<br/>" + e.getMessage() + "<br/><br/>";
			userSeenErrorMessage = fallBackErrorMessage;

		} finally {
		}
	}

	/**
	 * this method overwrites supers method of the same name. It provides the output of collected error data and shapes it inot html format for display in browsers
	 */
	public String getLocalizedMessage() {

		final String lineFeed = "\r\n";
		final String htmlLineFeed = "<br/>";

		final String mailtoLinkHrefMailTo = "mailto:";
		final String mailtoLinkSubject = "?subject=";
		final String mailtoLinkBody = "&body=";

		if (userSeenErrorMessage.length() > 0) {
			return additionalMessage + userSeenErrorMessage;
		}

		// according to config the message to the user consists of two parts
		// the part, which may contain a href web link and the part, which
		// allows a mailto: link triggered email from the user to
		// admin/developers
		String linkPart = "";
		String emailPart = "";

		linkPart = err_linkText + lineFeed;

		// only elaborate email part if
		if (emailAdresses.size() > 0) {
			emailPart = err_emailMessage.replace("{0}", 
					mailtoLinkHrefMailTo + getAdresses() + 
					mailtoLinkSubject + err_subjectLine + 
					mailtoLinkBody +  err_emailBody +
					htmlLineFeed + htmlLineFeed + 	
					htmlLineFeed + getContextInfo() + 
					htmlLineFeed + getStackTrace(this.getCause().getStackTrace()));

		} else {
			// if no adresse a general text will be provided by this class
			emailPart = Messages.getString("err_noMailService");
		}

		userSeenErrorMessage = internalErrorMsg + linkPart + htmlLineFeed
				+ emailPart;

		return userSeenErrorMessage;
	}

	/**
	 * 
	 * @return collected adresses as a string to be used after <a href="mailto:"
	 */
	private String getAdresses() {
		StringBuffer adresses = new StringBuffer();
		for (String emailAddy : emailAdresses) {
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
		getContextInfo = getContextInfo + "ThrowingClass=" + additionalMessage;		
		getContextInfo = getContextInfo + "Time=" + new Date().toString() + "<br/>";
		getContextInfo = getContextInfo + "Cause=" + super.getCause() + "<br/>";
		return getContextInfo;
	}

}
