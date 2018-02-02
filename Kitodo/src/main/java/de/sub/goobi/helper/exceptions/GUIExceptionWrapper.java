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

package de.sub.goobi.helper.exceptions;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.util.ArrayList;
import java.util.Date;

/**
 * This class provides the tools it takes to generate a configurable Error
 * message for Errors which are unexpected An example for the area in
 * GoobiProperties.config is given after the class declaration in the source
 * code.
 *
 * <p>
 * Besides building up the information in the constructor the other important
 * method is getLocalizedMessage(), which provides the build up message in html.
 * </p>
 *
 * @author Wulf
 * @version 12/10/2009
 *
 *          Variables in Messages Bundle: errEmailBody -> message in the email
 *          before the stack trace errEmailMessage -> message displayed if email
 *          is enabled in GoobiConfig: err_userHandling=true err_fallBackMessage
 *          -> messgae displayed if feature is turned off in GoobiConfig
 *          errLinkText -> message in which the link from GoobiConfig:
 *          err_linkToPage= err_noMailService -> message if email is disabled in
 *          GoobiConfig: err_emailEnabled=false errSubjectLine -> message in
 *          Subject Line of email
 *
 */
public class GUIExceptionWrapper extends Exception {

    private static final long serialVersionUID = 1L;

    private String fallBackErrorMessage = Helper.getTranslation("err_fallBackMessage");

    private String userSeenErrorMessage = "";

    private String additionalMessage = "";

    private String errLinkText = "";

    private String errEmailBody = "";
    private String errEmailMessage = "";
    private String errSubjectLine = "";

    private ArrayList<String> emailAddresses = new ArrayList<>();

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
     * Exception Class catching unhandled exceptions to wrap it for GUI.
     *
     * @param message
     *            additional info, like which class called this constructor
     * @param cause
     *            last Exception cought with this wrapper
     */
    public GUIExceptionWrapper(String message, Throwable cause) {
        this(cause);
        this.additionalMessage = message + "<br/>";
        init();
    }

    private void init() {

        try {
            if (ConfigCore.getBooleanParameter("err_userHandling")) {
                this.errLinkText = Helper.getTranslation("err_linkText");
                this.errLinkText = this.errLinkText.replace("{0}",
                        ConfigCore.getParameter("err_linkToPage", "./start.jsf"));

                if (ConfigCore.getBooleanParameter("err_emailEnabled")) {

                    this.errEmailMessage = Helper.getTranslation("err_emailMessage");
                    this.errSubjectLine = Helper.getTranslation("err_subjectLine");
                    this.errEmailBody = Helper.getTranslation("err_emailBody");

                    Integer emailCounter = 0;
                    String email = "";

                    // indefinite emails can be added
                    while (!email.equals("end")) {
                        emailCounter++;
                        email = ConfigCore.getParameter("err_emailAddress" + emailCounter.toString(), "end");
                        if (!email.equals("end")) {
                            this.emailAddresses.add(email);
                        }
                    }

                } else {
                    // no email service enabled, build standard message
                    this.errEmailMessage = Helper.getTranslation("err_noMailService");

                }
            } else {
                this.internalErrorMsg = this.internalErrorMsg + "Feature turned off:<br/><br/>";
                this.userSeenErrorMessage = this.fallBackErrorMessage;
            }

        } catch (Exception e) {
            this.internalErrorMsg = this.internalErrorMsg + "Error on loading Config items:<br/>" + e.getMessage()
                    + "<br/><br/>";
            this.userSeenErrorMessage = this.fallBackErrorMessage;

        }
    }

    /**
     * this method overwrites supers method of the same name. It provides the
     * output of collected error data and shapes it into html format for display
     * in browsers.
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
        String linkPart = this.errLinkText + lineFeed;
        String emailPart;

        // only elaborate email part if
        if (this.emailAddresses.size() > 0) {
            emailPart = this.errEmailMessage.replace("{0}",
                    mailtoLinkHrefMailTo + getAddresses() + mailtoLinkSubject + this.errSubjectLine + mailtoLinkBody
                            + this.errEmailBody + htmlLineFeed + htmlLineFeed + htmlLineFeed + getContextInfo()
                            + htmlLineFeed + getStackTrace(this.getCause().getStackTrace()));

        } else {
            // if no address a general text will be provided by this class
            emailPart = Helper.getTranslation("err_noMailService");
        }

        this.userSeenErrorMessage = this.internalErrorMsg + linkPart + htmlLineFeed + emailPart;

        return this.userSeenErrorMessage;
    }

    /**
     * Get address.
     *
     * @return collected addresses as a string to be used after &lt;a
     *         href="mailto:"&gt;
     */
    private String getAddresses() {
        StringBuilder addresses = new StringBuilder();
        for (String emailAddy : this.emailAddresses) {
            addresses.append(emailAddy);
            addresses.append(",%20");
        }
        return addresses.toString();
    }

    /**
     * Get stack trace.
     *
     * @param stackTrace
     *            StackTraceElement object
     * @return stack trace as String
     */
    private String getStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder stackTraceReturn = new StringBuilder();
        String tempTraceReturn = "";
        Integer counter = 0;
        for (StackTraceElement itStackTrace : stackTrace) {
            // only taking those elements from the stack trace, which contain
            // goobi and the top level element
            if (counter++ == 1 || itStackTrace.toString().toLowerCase().contains("goobi")) {
                stackTraceReturn.append("<br/>");
                stackTraceReturn.append(itStackTrace.toString());
                tempTraceReturn = "";
            } else {
                if (tempTraceReturn.length() < 1) {
                    stackTraceReturn.append("<br/> ---- skipping non goobi class(es) .");
                } else {
                    stackTraceReturn.append(" .");
                }
                tempTraceReturn = "<br/>" + itStackTrace.toString();
            }

            if (stackTraceReturn.length() > 1000) {
                return stackTraceReturn + tempTraceReturn
                        + "<br/><br/>	---- truncated rest of stack trace to avoid overflow ---- ";
            }
        }
        return stackTraceReturn + tempTraceReturn + "<br/><br/>	---- bottom of stack trace ---- ";
    }

    /**
     * Get context info.
     *
     * @return the Class of the initial Exception if possible
     */
    private String getContextInfo() {
        String getContextInfo = "";
        getContextInfo = getContextInfo + "ThrowingClass=" + this.additionalMessage;
        getContextInfo = getContextInfo + "Time=" + new Date().toString() + "<br/>";
        getContextInfo = getContextInfo + "Cause=" + super.getCause() + "<br/>";
        return getContextInfo;
    }

}
