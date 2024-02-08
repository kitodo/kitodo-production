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
import java.io.StringWriter;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.enums.MessageLevel;
import org.kitodo.production.enums.ReportLevel;
import org.kitodo.production.helper.messages.Error;
import org.kitodo.production.helper.messages.Message;
import org.kitodo.production.interfaces.activemq.WebServiceResult;

public class Helper {

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
    public static void setErrorMessage(String title, Object... parameters) {
        if (Objects.nonNull(parameters) && parameters.length > 0) {
            setErrorMessage(getTranslation(title,
                Arrays.stream(parameters).map(Objects::toString).toArray(String[]::new)));
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

    /**
     * Set error message with empty description, e.g. only with title. That means no compound message is created.
     * This is a convenience function for calling "setMessage" with parameters "level" = "MessageLevel.ERROR" and
     * "createCompoundMessage" = "false".
     *
     * @param message message String to be displayed.
     */
    public static void setErrorMessagesWithoutDescription(String message) {
        setMessage(null, message, "", MessageLevel.ERROR, false);
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

    private static void setMessage(String control, String message, String description, MessageLevel level) {
        setMessage(control, message, description, level, true);
    }

    /**
     * Transfer an error message for a specific control to the current form.
     */
    private static void setMessage(String control, String message, String description, MessageLevel level,
                                   boolean createCompoundMessage) {
        String msg = getTranslation(Objects.toString(message));
        String descript = getTranslation(Objects.toString(description));
        String detail = descript;

        String compoundMessage = msg.replaceFirst(":\\s*$", "");
        if (createCompoundMessage) {
            if (StringUtils.isNotEmpty(descript)) {
                compoundMessage += ": " + descript;
            }
            detail = null;
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
                        ? FacesMessage.SEVERITY_WARN : FacesMessage.SEVERITY_INFO, compoundMessage, detail));
        } else {
            // wenn kein Kontext da ist, dann die Meldungen in Log
            logger.log(MessageLevel.ERROR.equals(level) ? Level.ERROR : MessageLevel.WARN.equals(level) ? Level.WARN : Level.INFO,
                    compoundMessage);
        }
    }

    /**
     * Set message with empty description, e.g. only with title. That means no compound message is created.
     * This is a convenience function for calling "setMessage" with parameters "level" = "MessageLevel.INFO" and
     * "createCompoundMessage" = "false".
     *
     * @param message message String to be displayed.
     */
    public static void setMessageWithoutDescription(String message) {
        setMessage(null, message, "", MessageLevel.INFO, false);
    }

    /**
     * Get String.
     *
     * @param locale
     *            Locale object
     * @param key
     *            String
     * @return String
     */
    public static String getString(Locale locale, String key) {
        if ((Objects.isNull(commonMessages) || commonMessages.size() <= 1)
                && (Objects.isNull(errorMessages) || errorMessages.size() <= 1)) {
            loadMessages();
        }

        List<Map<Locale, ResourceBundle>> messages = new ArrayList<>();
        messages.add(commonMessages);
        messages.add(errorMessages);
        for (Map<Locale, ResourceBundle> message : messages) {
            // support locale with and without country to load message
            Optional<Locale> optionalLocale = message.keySet().stream()
                    .filter(messageKeyLocale -> messageKeyLocale.getLanguage().equals(locale.getLanguage()))
                    .findFirst();
            if (optionalLocale.isPresent()) {
                String foundMessage = getTranslatedMessage(message, optionalLocale.get(), key);
                if (StringUtils.isNotBlank(foundMessage)) {
                    return foundMessage;
                }
            }
        }
        return key;
    }

    private static String getTranslatedMessage(Map<Locale, ResourceBundle> messages, Locale locale, String key) {
        ResourceBundle languageLocal = messages.get(locale);
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
     * Parse date string to date. 
     * 
     * <p>Reverse operation of `Helper.getDateAsFormattedString`.</p>
     * 
     * @param date the date as string formatted in "yyyy-MM-dd HH:mm:ss"
     * @return the date or null if it can not be parsed
     */
    public static Date parseDateFromFormattedString(String date) {
        if (Objects.isNull(date) || date.equals("")) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime localDate = LocalDateTime.parse(date, formatter);
            Instant instant = localDate.toInstant(ZoneOffset.UTC);
            return Date.from(instant);
        } catch (DateTimeParseException e) {
            logger.info("invalid date format (yyyy-MM-dd HH:mm:ss) for date string: '" + date + "'");
            return null;
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
     * @param insertions
     *            Strings
     * @return translated String
     */
    public static String getTranslation(String title, String... insertions) {
        String pattern = getString(desiredLanguage(), title);
        String message = pattern;
        try {
            message = MessageFormat.format(pattern, (Object[]) insertions);
        } catch (IllegalArgumentException e) {
            logger.catching(Level.WARN, e);
        }
        return appendUnusedInsertions(message, insertions);
    }

    /**
     * Appends insertions that were not used. There are reasons why insertions
     * are not used: if the key is not found in the messages, or if a curly
     * bracket with the corresponding number is missing therein. Since this
     * function is used in error messages, which could be difficult to
     * reproduce, a loss of the additional information should be avoided.
     */
    private static String appendUnusedInsertions(String message, String... insertions) {
        StringBuilder messageBuilder = new StringBuilder(message);
        for (String insertion : insertions) {
            String separator = ": ";
            insertion = Objects.toString(insertion);
            if (!messageBuilder.toString().contains(insertion)) {
                messageBuilder.append(separator).append(insertion);
            }
        }
        message = messageBuilder.toString();
        return message;
    }

    private static Locale desiredLanguage() {
        if (Objects.nonNull(FacesContext.getCurrentInstance())) {
            Locale desiredLanguage = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            if (Objects.nonNull(desiredLanguage)) {
                return desiredLanguage;
            }
        }
        return Locale.ENGLISH;
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
