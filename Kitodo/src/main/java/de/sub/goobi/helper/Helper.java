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

package de.sub.goobi.helper;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.forms.AktuelleSchritteForm;
import de.sub.goobi.helper.enums.ReportLevel;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.mq.WebServiceResult;
import org.kitodo.config.Parameters;
import org.kitodo.data.database.helper.HibernateHelper;

/**
 * Extends Helper from Kitodo Data Management module.
 */
public class Helper extends HibernateHelper implements Observer {

    private static Map<String, String> activeMQReporting = null;
    private static final Logger logger = LogManager.getLogger(Helper.class);
    private static final long serialVersionUID = -7449236652821237059L;
    private static Map<Locale, ResourceBundle> commonMessages = null;
    private static Map<Locale, ResourceBundle> localMessages = null;
    private static String compoundMessage;

    /**
     * Determine a specific parameter of the request.
     *
     * @return parameter als String
     */
    @SuppressWarnings("rawtypes")
    public static String getRequestParameter(String parameter) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map requestParams = context.getExternalContext().getRequestParameterMap();
        return (String) requestParams.get(parameter);
    }

    /**
     * Set error message for user.
     *
     * @param message
     *            for user
     */
    public static void setErrorMessage(String message) {
        setMessage(null, message, "", false);
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
        setMessage(null, message, description != null ? description : "", false);
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
        setMessage(control, message, description != null ? description : "", false);
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
     * This method also accepts logger and exception instances to automatically
     * log the exceptions message or stackTrace values to the given logger.
     * </p>
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
            setErrorMessage(title);
        } else {
            setErrorMessage(title, exception.getMessage());
        }
    }

    /**
     * Set error message to message tag with given name 'title'. Substitute all
     * placeholders in message tag with elements of given array 'parameters'.
     *
     * <p>
     * This method also accepts logger and exception instances to automatically
     * log the exceptions message or stackTrace values to the given logger.
     * </p>
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
     * instances to automatically log the exceptions message or stackTrace
     * values to the given logger.
     * </p>
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
        if (message == null) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            message = sw.toString();
        }
        return message;
    }

    /**
     * Set message for user.
     * 
     * @param message
     *            for user
     */
    public static void setMessage(String message) {
        setMessage(null, message, "", true);
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
        setMessage(null, message, description, true);
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
        setMessage(control, message, description, true);
    }

    /**
     * Dem aktuellen Formular eine Fehlermeldung für ein bestimmtes Control
     * übergeben.
     */
    private static void setMessage(String control, String message, String description, boolean onlyInfo) {
        // Never forget: Strings are immutable
        message = message.replaceAll("<", "&lt;");
        message = message.replaceAll(">", "&gt;");
        description = description.replaceAll("<", "&lt;");
        description = description.replaceAll(">", "&gt;");

        String msg = getTranslation(message);
        String descript = getTranslation(description);

        compoundMessage = msg.replaceFirst(":\\s*$", "") + ": " + descript;
        if (activeMQReporting != null) {
            new WebServiceResult(activeMQReporting.get("queueName"), activeMQReporting.get("id"),
                    onlyInfo ? ReportLevel.INFO : ReportLevel.ERROR, compoundMessage).send();
        }

        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.addMessage(control,
                new FacesMessage(onlyInfo ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR, msg, descript));
        } else {
            // wenn kein Kontext da ist, dann die Meldungen in Log
            logger.log(onlyInfo ? Level.INFO : Level.ERROR, compoundMessage);
        }
    }

    /**
     * Returns a Map holding all translations that are configured in the front
     * end of a given resource key.
     *
     * @param key
     *            resource key to get translations for
     * @return a map with all language id strings and the corresponding resource
     */
    public static Map<String, String> getAllStrings(String key) {
        Map<String, String> result = new HashMap<>((int) Math.ceil(commonMessages.entrySet().size() / 0.75));
        @SuppressWarnings("unchecked")
        Iterator<Locale> languages = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
        while (languages.hasNext()) {
            Locale language = languages.next();
            result.put(language.getLanguage(), getString(language, key));
        }
        return result;
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
        if (commonMessages == null || commonMessages.size() <= 1) {
            loadMsgs();
        }

        if (localMessages.containsKey(language)) {
            ResourceBundle languageLocal = localMessages.get(language);
            if (languageLocal.containsKey(key)) {
                return languageLocal.getString(key);
            }
            String lowKey = key.toLowerCase();
            if (languageLocal.containsKey(lowKey)) {
                return languageLocal.getString(lowKey);
            }
        }
        try {
            return commonMessages.get(language).getString(key);
        } catch (RuntimeException irrelevant) {
            return key;
        }
    }

    /**
     * Get date as formatted String.
     *
     * @param date
     *            Date object
     * @return String
     */
    public static String getDateAsFormattedString(Date date) {
        if (date == null) {
            return "-";
        } else {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.format(date);
        }
    }

    /**
     * The procedure removeManagedBean() removes a managed bean from the faces
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

    private static void loadMsgs() {
        commonMessages = new HashMap<>();
        localMessages = new HashMap<>();
        if (FacesContext.getCurrentInstance() != null) {
            @SuppressWarnings("unchecked")
            Iterator<Locale> polyglot = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
            while (polyglot.hasNext()) {
                Locale language = polyglot.next();
                commonMessages.put(language, ResourceBundle.getBundle("messages.messages", language));
                File file = new File(
                        ConfigCore.getParameter(Parameters.LOCAL_MESSAGES, Parameters.DefaultValues.LOCAL_MESSAGES));
                if (file.exists()) {
                    // Load local message bundle from file system only if file exists;
                    // if value not exists in bundle, use default bundle from classpath

                    try {
                        final URL resourceURL = file.toURI().toURL();
                        URLClassLoader urlLoader = AccessController
                                .doPrivileged((PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(new URL[] {resourceURL }));
                        ResourceBundle localBundle = ResourceBundle.getBundle("messages", language, urlLoader);
                        if (localBundle != null) {
                            localMessages.put(language, localBundle);
                        }

                    } catch (RuntimeException | MalformedURLException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        } else {
            Locale defaultLocale = new Locale("EN");
            commonMessages.put(defaultLocale, ResourceBundle.getBundle("messages.messages", defaultLocale));
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

        if (FacesContext.getCurrentInstance() != null) {
            Locale desiredLanguage = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            if (desiredLanguage != null) {
                return getString(desiredLanguage, title);
            }
        }
        return getString(Locale.ENGLISH, title);
    }

    public static String getTranslation(String inParameter, String inDefaultIfNull) {
        String result = getTranslation(inParameter);
        return result != null && !result.equals(inParameter) ? result : inDefaultIfNull;
    }

    /**
     * Get translation.
     *
     * @param title
     *            String
     * @param parameterList
     *            list of Strings
     * @return translated String
     */
    public static String getTranslation(String title, List<String> parameterList) {
        String value = getTranslation(title);

        if (value != null && parameterList != null && !parameterList.isEmpty()) {
            int parameterCount = 0;
            for (String parameter : parameterList) {
                value = value.replace("{" + parameterCount + "}", parameter);
                parameterCount++;
            }
        }
        return value;
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
     * Get current task form.
     *
     * @return AktuelleSchritteForm
     */
    public static AktuelleSchritteForm getCurrentTaskForm() {
        AktuelleSchritteForm currentTaskForm = (AktuelleSchritteForm) Helper
                .getManagedBeanValue("AktuelleSchritteForm");
        if (Objects.isNull(currentTaskForm)) {
            currentTaskForm = new AktuelleSchritteForm();
        }
        return currentTaskForm;
    }

    public static final FilenameFilter imageNameFilter = (dir, name) -> {
        List<String> regexList = getImageNameRegexList();

        for (String regex : regexList) {
            if (name.matches(regex)) {
                return true;
            }
        }

        return false;
    };

    public static final FilenameFilter dataFilter = (dir, name) -> {
        List<String> regexList = getDataRegexList();

        for (String regex : regexList) {
            if (name.matches(regex)) {
                return true;
            }
        }

        return false;
    };

    private static List<String> getImageNameRegexList() {
        String prefix = ConfigCore.getParameter("ImagePrefix", "\\d{8}");

        List<String> regexList = new ArrayList<>();
        regexList.add(prefix + "\\.[Tt][Ii][Ff][Ff]?");
        regexList.add(prefix + "\\.[jJ][pP][eE]?[gG]");
        regexList.add(prefix + "\\.[jJ][pP][2]");
        regexList.add(prefix + "\\.[pP][nN][gG]");
        regexList.add(prefix + "\\.[gG][iI][fF]");
        return regexList;
    }

    private static List<String> getDataRegexList() {
            String prefix = ConfigCore.getParameter(Parameters.IMAGE_PREFIX, Parameters.DefaultValues.IMAGE_PREFIX);

        List<String> regexList = new ArrayList<>();
        regexList.add(prefix + "\\.[Tt][Ii][Ff][Ff]?");
        regexList.add(prefix + "\\.[jJ][pP][eE]?[gG]");
        regexList.add(prefix + "\\.[jJ][pP][2]");
        regexList.add(prefix + "\\.[pP][nN][gG]");
        regexList.add(prefix + "\\.[gG][iI][fF]");
        regexList.add(prefix + "\\.[pP][dD][fF]");
        regexList.add(prefix + "\\.[aA][vV][iI]");
        regexList.add(prefix + "\\.[mM][pP][gG]");
        regexList.add(prefix + "\\.[mM][pP]4");
        regexList.add(prefix + "\\.[mM][pP]3");
        regexList.add(prefix + "\\.[wW][aA][vV]");
        regexList.add(prefix + "\\.[wW][mM][vV]");
        regexList.add(prefix + "\\.[fF][lL][vV]");
        regexList.add(prefix + "\\.[oO][gG][gG]");
        regexList.add(prefix + "\\.docx");
        regexList.add(prefix + "\\.xls");
        regexList.add(prefix + "\\.xlsx");
        regexList.add(prefix + "\\.pptx");
        regexList.add(prefix + "\\.ppt");
        return regexList;
    }

    /**
     * The function getLastMessage() returns the last message processed to be
     * shown to the user. This is a last resort only to show the user why
     * perhaps something didn’t work if no error message is available otherwise.
     *
     * @return the most recent message created to be shown to the user
     */
    public static String getLastMessage() {
        return compoundMessage;
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
}
