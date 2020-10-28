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

package org.kitodo.production.helper;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.enums.MessageLevel;
import org.kitodo.production.enums.ReportLevel;
import org.kitodo.production.helper.messages.Error;
import org.kitodo.production.helper.messages.Message;
import org.kitodo.production.interfaces.activemq.WebServiceResult;

/**
 * Extends Helper from Kitodo Data Management module.
 */
public class Helper implements Observer, Serializable {

    private static Map<String, String> activeMQReporting = null;
    private static final Logger logger = LogManager.getLogger(Helper.class);
    private static Map<Locale, ResourceBundle> commonMessages = null;
    private static Map<Locale, ResourceBundle> errorMessages = null;

    /**
     * Determine a specific parameter of the request.
     *
     * @return parameter als String
     */
    @SuppressWarnings("rawtypes")
    public static String getRequestParameter(String parameter) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (Objects.nonNull(context)) {
            Map requestParams = context.getExternalContext().getRequestParameterMap();
            return (String) requestParams.get(parameter);
        }
        return null;
    }

    /**
     * Set error message for user.
     *
     * @param message
     *            for user
     */
    public static void setErrorMessage(String message) {
        setMessage(null, message, "", MessageLevel.ERROR);
    }

    /**
     * Set error message and description for user.
     *
     * @param message
     *            for user
     * @param description
     *            additional information to message
     */
    public static void setErrorMessage(String message, String description) {
        setMessage(null, message, description, MessageLevel.ERROR);
    }

    /**
     * Set error message and description for user.
     *
     * @param control
     *            what is it - no documentation for clientId in FacesContext
     * @param message
     *            for user
     * @param description
     *            additional information to message
     */
    public static void setErrorMessage(String control, String message, String description) {
        setMessage(control, message, description, MessageLevel.ERROR);
    }

    /**
     * Set error message for user with usage of exception.
     *
     * @param e
     *            thrown exception
     */
    public static void setErrorMessage(Exception e) {
        setErrorMessage("Error (" + e.getClass().getName() + "): ", getExceptionMessage(e));
    }

    /**
     * Set error message for user with usage of exception.
     *
     * @param control
     *            what is it - no documentation for clientId in FacesContext
     * @param message
     *            for user
     * @param e
     *            thrown exception
     */
    public static void setErrorMessage(String control, String message, Exception e) {
        setErrorMessage(control, message + " (" + e.getClass().getSimpleName() + "): ", getExceptionMessage(e));
    }

    /**
     * Set error message to message tag with given name 'title'. Substitute all
     * placeholders in message tag with elements of given array 'parameters'.
     *
     * @param title
     *            name of the message tag set as error message
     * @param parameters
     *            list of parameters used for string substitution in message tag
     */
    public static void setErrorMessage(String title, final Object[] parameters) {
        if (Objects.nonNull(parameters) && parameters.length > 0) {
            setErrorMessage(MessageFormat.format(getTranslation(title), parameters));
        } else {
            setErrorMessage(getTranslation(title));
        }
    }

    /**
     * Set error message to message tag with given name 'title'.
     *
     * <p>
     * This method also accepts logger and exception instances to automatically log
     * the exceptions message or stackTrace values to the given logger.
     *
     * @param title
     *            name of the message tag set as error message
     * @param logger
     *            Logger instance for error logging
     * @param exception
     *            Exception instance for error logging
     */
    public static void setErrorMessage(String title, Logger logger, Exception exception) {
        logger.error(title, exception);
        if (Objects.isNull(exception.getMessage()) || exception.getMessage().equals(title)) {
            setErrorMessage(getRootCause(exception));
        } else {
            setErrorMessage(title, exception.getMessage());
        }
    }

    private static String getRootCause(Throwable problem) {
        Throwable cause = problem.getCause();
        String className = problem.getClass().getSimpleName();
        if (Objects.nonNull(cause)) {
            return className + " / " + getRootCause(cause);
        } else {
            String message = problem.getLocalizedMessage();
            return StringUtils.isEmpty(message) ? className : className + ": " + message;
        }
    }

    /**
     * Set error message to message tag with given name 'title'. Substitute all
     * placeholders in message tag with elements of given array 'parameters'.
     *
     * <p>
     * This method also accepts logger and exception instances to automatically log
     * the exceptions message or stackTrace values to the given logger.
     *
     * @param title
     *            name of the message tag set as error message
     * @param parameters
     *            list of parameters used for string substitution in message tag
     * @param logger
     *            Logger instance for error logging
     * @param exception
     *            Exception instance for error logging
     */
    public static void setErrorMessage(String title, final Object[] parameters, Logger logger, Exception exception) {
        logger.error(title, exception);
        setErrorMessage(title, parameters);
    }

    /**
     * Set error message to message tag with given name 'title'.
     *
     * <p>
     * This method also accepts a description text and logger and exception
     * instances to automatically log the exceptions message or stackTrace values to
     * the given logger.
     *
     * @param title
     *            name of the message tag set as error message
     * @param description
     *            description text that will be displayed in the faces message
     * @param logger
     *            Logger instance for error logging
     * @param exception
     *            Exception instance for error logging
     */
    public static void setErrorMessage(String title, String description, Logger logger, Exception exception) {
        logger.error(title, exception);
        setErrorMessage(title, description);
    }

    private static String getExceptionMessage(Throwable e) {
        String message = e.getMessage();
        if (Objects.isNull(message)) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            message = sw.toString();
        }
        return message;
    }

    /**
     * Set a message with warning level.
     *
     * @param message
     *            Message displayed to the user
     */
    public static void setWarnMessage(String message) {
        setMessage(null, message, "", MessageLevel.WARN);
    }

    /**
     * Set message for user.
     *
     * @param message
     *            for user
     */
    public static void setMessage(String message) {
        setMessage(null, message, "", MessageLevel.INFO);
    }

    /**
     * Set message and description for user.
     *
     * @param message
     *            for user
     * @param description
     *            additional information to message
     */
    public static void setMessage(String message, String description) {
        setMessage(null, message, description, MessageLevel.INFO);
    }

    /**
     * Set message and description for user.
     *
     * @param control
     *            what is it - no documentation for clientId in FacesContext
     * @param message
     *            for user
     * @param description
     *            additional information to message
     */
    public static void setMessage(String control, String message, String description) {
        setMessage(control, message, description, MessageLevel.INFO);
    }

    /**
     * Transfer an error message for a specific control to the current form.
     */
    private static void setMessage(String control, String message, String description, MessageLevel level) {
        String msg = getTranslation(Objects.toString(message));
        String descript = getTranslation(Objects.toString(description));

        String compoundMessage = msg.replaceFirst(":\\s*$", "");
        if (StringUtils.isNotEmpty(descript)) {
            compoundMessage += ": " + descript;
        }
        if (Objects.nonNull(activeMQReporting)) {
            new WebServiceResult(activeMQReporting.get("queueName"), activeMQReporting.get("id"),
                    MessageLevel.ERROR.equals(level) ? ReportLevel.ERROR :
                            MessageLevel.WARN.equals(level) ? ReportLevel.WARN : ReportLevel.INFO, compoundMessage).send();
        }

        FacesContext context = FacesContext.getCurrentInstance();
        if (Objects.nonNull(context)) {
            context.addMessage(control,
                new FacesMessage(MessageLevel.ERROR.equals(level) ? FacesMessage.SEVERITY_ERROR : MessageLevel.WARN.equals(level)
                        ? FacesMessage.SEVERITY_WARN : FacesMessage.SEVERITY_INFO, null, compoundMessage));
        } else {
            // wenn kein Kontext da ist, dann die Meldungen in Log
            logger.log(MessageLevel.ERROR.equals(level) ? Level.ERROR : MessageLevel.WARN.equals(level) ? Level.WARN : Level.INFO,
                    compoundMessage);
        }
    }

    /**
     * Get String.
     *
     * @param language
     *            Locale object
     * @param key
     *            String
     * @return String
     */
    public static String getString(Locale language, String key) {
        if ((Objects.isNull(commonMessages) || commonMessages.size() <= 1)
                && (Objects.isNull(errorMessages) || errorMessages.size() <= 1)) {
            loadMessages();
        }

        List<Map<Locale, ResourceBundle>> messages = new ArrayList<>();
        messages.add(commonMessages);
        messages.add(errorMessages);

        for (Map<Locale, ResourceBundle> message : messages) {
            if (message.containsKey(language)) {
                String foundMessage = getTranslatedMessage(message, language, key);
                if (!Objects.equals(foundMessage, "")) {
                    return foundMessage;
                }
            }
        }
        return key;
    }

    private static String getTranslatedMessage(Map<Locale, ResourceBundle> messages, Locale language, String key) {
        ResourceBundle languageLocal = messages.get(language);
        if (languageLocal.containsKey(key)) {
            return languageLocal.getString(key);
        }
        String lowKey = key.toLowerCase();
        if (languageLocal.containsKey(lowKey)) {
            return languageLocal.getString(lowKey);
        }
        return "";
    }

    /**
     * Get date as formatted String.
     *
     * @param date
     *            Date object
     * @return String
     */
    public static String getDateAsFormattedString(Date date) {
        if (Objects.isNull(date)) {
            return "-";
        } else {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.format(date);
        }
    }

    /**
     * Removes a managed bean from the faces
     * context by name. If nothing such is available, nothing happens.
     *
     * @param name
     *            managed bean to remove
     */
    public static void removeManagedBean(String name) {
        try {
            @SuppressWarnings("rawtypes")
            Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            sessionMap.remove(name);
        } catch (RuntimeException nothingToDo) {
            logger.error(nothingToDo);
        }
    }

    private static void loadMessages() {
        commonMessages = new HashMap<>();
        errorMessages = new HashMap<>();
        if (Objects.nonNull(FacesContext.getCurrentInstance())) {
            Iterator<Locale> polyglot = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
            while (polyglot.hasNext()) {
                Locale language = polyglot.next();
                commonMessages.put(language, Message.getResourceBundle("messages.messages", "messages", language));
                errorMessages.put(language, Error.getResourceBundle("messages.errors", "errors", language));
            }
        } else {
            Locale defaultLocale = new Locale("EN");
            commonMessages.put(defaultLocale, Message.getResourceBundle("messages.messages", "messages", defaultLocale));
            errorMessages.put(defaultLocale, Error.getResourceBundle("messages.errors", "errors", defaultLocale));
        }
    }

    /**
     * Get translation.
     *
     * @param title
     *            String
     * @return translated String
     */
    public static String getTranslation(String title) {
        // running instance of ResourceBundle doesn't respond on user language
        // changes, workaround by instantiating it every time

        if (Objects.nonNull(FacesContext.getCurrentInstance())) {
            Locale desiredLanguage = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            if (Objects.nonNull(desiredLanguage)) {
                return getString(desiredLanguage, title);
            }
        }
        return getString(Locale.ENGLISH, title);
    }

    public static String getTranslation(String inParameter, String inDefaultIfNull) {
        String result = getTranslation(inParameter);
        return Objects.nonNull(result) && !result.equals(inParameter) ? result : inDefaultIfNull;
    }

    /**
     * Get translation.
     *
     * @param title
     *            String
     * @param parameters
     *            list of Strings
     * @return translated String
     */
    public static String getTranslation(String title, List<String> parameters) {
        return MessageFormat.format(getTranslation(title), parameters.toArray());
    }

    /**
     * for easy access of the implemented Interface Observer.
     *
     * @return Observer -> can be added to an Observable
     */
    public Observer createObserver() {
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof String)) {
            Helper.setErrorMessage("User notification failed by object: '" + arg.toString()
                    + "' which isn't an expected String Object. This error is caused by an implementation of "
                    + "the Observer Interface in Helper");
        } else {
            Helper.setErrorMessage((String) arg);
        }
    }

    /**
     * Set activeMQReporting.
     *
     * @param activeMQReporting
     *            as Map of Strings
     */
    public static void setActiveMQReporting(Map<String, String> activeMQReporting) {
        Helper.activeMQReporting = activeMQReporting;
    }

    /**
     * Get title without white spaces.
     *
     * @param title
     *            of object
     * @return title with '__' instead of ' '
     */
    public static String getNormalizedTitle(String title) {
        return title.replace(" ", "__");
    }

    /**
     * Generate random string.
     *
     * @param length
     *            of random string to be created
     * @return random string
     */
    public static String generateRandomString(int length) {
        final String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(AB.charAt(random.nextInt(AB.length())));
        }
        return sb.toString();
    }
}
