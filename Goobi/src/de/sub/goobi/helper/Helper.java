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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
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

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.goobi.mq.WebServiceResult;
import org.goobi.production.constants.Parameters;
import org.hibernate.Session;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.forms.SpracheForm;
import de.sub.goobi.helper.enums.ReportLevel;
import de.sub.goobi.persistence.HibernateUtilOld;

public class Helper implements Serializable, Observer {

    /**
     * Always treat de-serialization as a full-blown constructor, by validating the final state of the de-serialized object.
     */
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {

    }

    /**
     * This is the default implementation of writeObject. Customise if necessary.
     */
    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {

    }

    private static final Logger logger = Logger.getLogger(Helper.class);
    private static final long serialVersionUID = -7449236652821237059L;

    private String myMetadatenVerzeichnis;
    private String myConfigVerzeichnis;
    private static Map<Locale, ResourceBundle> commonMessages = null;
    private static Map<Locale, ResourceBundle> localMessages = null;

    public static Map<String, String> activeMQReporting = null;
    private static String compoundMessage;

    /**
     * Ermitteln eines bestimmten Parameters des Requests
     *
     * @return Parameter als String
     */
    @SuppressWarnings("rawtypes")
    public static String getRequestParameter(String Parameter) {
        /* einen bestimmten übergebenen Parameter ermitteln */
        FacesContext context = FacesContext.getCurrentInstance();
        Map requestParams = context.getExternalContext().getRequestParameterMap();
        String myParameter = (String) requestParams.get(Parameter);
        if (myParameter == null) {
            myParameter = "";
        }
        return myParameter;
    }

    public String getGoobiDataDirectory() {
        if (this.myMetadatenVerzeichnis == null) {
            this.myMetadatenVerzeichnis = ConfigMain.getParameter("MetadatenVerzeichnis");
        }
        return this.myMetadatenVerzeichnis;
    }

    public String getGoobiConfigDirectory() {
        if (this.myConfigVerzeichnis == null) {
            this.myConfigVerzeichnis = ConfigMain.getParameter(Parameters.CONFIG_DIR);
        }
        return this.myConfigVerzeichnis;
    }

    public static String getStacktraceAsString(Exception inException) {
        StringWriter sw = new StringWriter();
        inException.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static void setFehlerMeldung(String meldung) {
        setMeldung(null, meldung, "", false);
    }

    public static void setFehlerMeldung(String meldung, String beschreibung) {
        setMeldung(null, meldung, beschreibung != null ? beschreibung : "", false);
    }

    public static void setFehlerMeldung(String control, String meldung, String beschreibung) {
        setMeldung(control, meldung, beschreibung != null ? beschreibung : "", false);
    }

    public static void setFehlerMeldung(Exception e) {
        setFehlerMeldung("Error (" + e.getClass().getName() + "): ", getExceptionMessage(e));
    }

    public static void setFehlerMeldung(String meldung, Exception e) {
        setFehlerMeldung(meldung, '(' + e.getClass().getSimpleName() + ": " + getExceptionMessage(e) + ')');
    }

    public static void setFehlerMeldung(String control, String meldung, Exception e) {
        setFehlerMeldung(control, meldung + " (" + e.getClass().getSimpleName() + "): ", getExceptionMessage(e));
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

    public static void setMeldung(String meldung) {
        setMeldung(null, meldung, "", true);
    }

    public static void setMeldung(String meldung, String beschreibung) {
        setMeldung(null, meldung, beschreibung, true);
    }

    public static void setMeldung(String control, String meldung, String beschreibung) {
        setMeldung(control, meldung, beschreibung, true);
    }

    /**
     * Dem aktuellen Formular eine Fehlermeldung für ein bestimmtes Control übergeben
     */
    private static void setMeldung(String control, String meldung, String beschreibung, boolean nurInfo) {
        FacesContext context = FacesContext.getCurrentInstance();

        // Never forget: Strings are immutable
        meldung = meldung.replaceAll("<", "&lt;");
        meldung = meldung.replaceAll(">", "&gt;");
        beschreibung = beschreibung.replaceAll("<", "&lt;");
        beschreibung = beschreibung.replaceAll(">", "&gt;");

        String msg = "";
        String beschr = "";
        Locale language = Locale.ENGLISH;
        SpracheForm sf = (SpracheForm) Helper.getManagedBeanValue("#{SpracheForm}");
        if (sf != null) {
            language = sf.getLocale();
        }

        try {
            msg = getString(language, meldung);
        } catch (RuntimeException e) {
            msg = meldung;
        }
        try {
            beschr = getString(language, beschreibung);
        } catch (RuntimeException e) {
            beschr = beschreibung;
        }

        compoundMessage = msg.replaceFirst(":\\s*$", "") + ": " + beschr;
        if (activeMQReporting != null) {
            new WebServiceResult(activeMQReporting.get("queueName"), activeMQReporting.get("id"), nurInfo ? ReportLevel.INFO : ReportLevel.ERROR,
                    compoundMessage).send();
        }
        if (context != null) {
            context.addMessage(control, new FacesMessage(nurInfo ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR, msg, beschr));
        } else {
            // wenn kein Kontext da ist, dann die Meldungen in Log
            logger.log(nurInfo ? Level.INFO : Level.ERROR, compoundMessage);

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
    public static HashMap<String, String> getAllStrings(String key) {
        HashMap<String, String> result = new HashMap<String, String>(Util.hashCapacityFor(commonMessages.entrySet()));
        @SuppressWarnings("unchecked")
        Iterator<Locale> languages = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
        while (languages.hasNext()) {
            Locale language = languages.next();
            result.put(language.getLanguage(), getString(language, key));
        }
        return result;
    }

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

    public static String getDateAsFormattedString(Date inDate) {
        if (inDate == null) {
            return "-";
        } else {
            return DateFormat.getDateInstance().format(inDate) + " " + DateFormat.getTimeInstance(DateFormat.MEDIUM).format(inDate);
        }
    }

    public static Object getManagedBeanValue(String expr) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return null;
        } else {
            Object value = null;
            Application application = context.getApplication();
            if (application != null) {
                ValueBinding vb = application.createValueBinding(expr);
                if (vb != null) {
                    try {
                    value = vb.getValue(context);
                    } catch (PropertyNotFoundException e) {
                        logger.error(e);
                    } catch (EvaluationException e) {
                        logger.error(e);
                    }
                }
            }
            return value;
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
            if (sessionMap.containsKey(name)) {
                sessionMap.remove(name);
            }
        } catch (Exception nothingToDo) {
        }
    }

    public static Session getHibernateSession() {
        Session sess;
        try {
            sess = (Session) getManagedBeanValue("#{HibernateSessionLong.session}");
            if (sess == null) {
                sess = HibernateUtilOld.getSession();
            }
        } catch (Exception e) {
            sess = HibernateUtilOld.getSession();
        }
        if (!sess.isOpen()) {
            sess = HibernateUtilOld.getSession();
        }
        return sess;
    }

    private static void loadMsgs() {
        commonMessages = new HashMap<Locale, ResourceBundle>();
        localMessages = new HashMap<Locale, ResourceBundle>();
        if (FacesContext.getCurrentInstance() != null) {
            @SuppressWarnings("unchecked")
            Iterator<Locale> polyglot = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
            while (polyglot.hasNext()) {
                Locale language = polyglot.next();
                commonMessages.put(language, ResourceBundle.getBundle("messages.messages", language));
                File file = new File(ConfigMain.getParameter("localMessages", "/usr/local/goobi/messages/"));
                if (file.exists()) {
                    // Load local message bundle from file system only if file exists;
                    // if value not exists in bundle, use default bundle from classpath

                    try {
                        final URL resourceURL = file.toURI().toURL();
                        URLClassLoader urlLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                            @Override
                            public URLClassLoader run() {
                                return new URLClassLoader(new URL[] { resourceURL });
                            }
                        });
                        ResourceBundle localBundle = ResourceBundle.getBundle("messages", language, urlLoader);
                        if (localBundle != null) {
                            localMessages.put(language, localBundle);
                        }

                    } catch (Exception e) {
                    }
                }
            }
        } else {
            Locale defaullLocale = new Locale("EN");
            commonMessages.put(defaullLocale, ResourceBundle.getBundle("messages.messages", defaullLocale));
        }
    }

    public static String getTranslation(String dbTitel) {
        // running instance of ResourceBundle doesn't respond on user language
        // changes, workaround by instanciating it every time

        Locale desiredLanguage = null;
        try {
            desiredLanguage = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        } catch (NullPointerException skip) {
        }
        if (desiredLanguage != null) {
            return getString(new Locale(desiredLanguage.getLanguage()), dbTitel);
        } else {
            return getString(Locale.ENGLISH, dbTitel);
        }
    }

    public static String getTranslation(String inParameter, String inDefaultIfNull) {
        String result = getTranslation(inParameter);
        return result != null && !result.equals(inParameter) ? result : inDefaultIfNull;
    }

    public static String getTranslation(String dbTitel, List<String> parameterList) {
        String value = "";
        Locale desiredLanguage = null;
        try {
            desiredLanguage = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        } catch (NullPointerException skip) {
        }
        if (desiredLanguage != null) {
            value = getString(new Locale(desiredLanguage.getLanguage()), dbTitel);
        } else {
            value = getString(Locale.ENGLISH, dbTitel);
        }
        if (value != null && parameterList != null && parameterList.size() > 0) {
            int parameterCount = 0;
            for (String parameter : parameterList) {
                if (!Objects.equals(parameter, null)) {
                    value = value.replace("{" + parameterCount + "}", parameter);
                    parameterCount++;
                }
            }
        }

        return value;
    }

    /**
     * for easy access of the implemented Interface Observer
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
            Helper.setFehlerMeldung("Usernotification failed by object: '" + arg.toString()
                    + "' which isn't an expected String Object. This error is caused by an implementation of the Observer Interface in Helper");
        } else {
            Helper.setFehlerMeldung((String) arg);
        }
    }

    public static String getBaseUrl() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
        String fullpath = req.getRequestURL().toString();
        String servletpath = context.getExternalContext().getRequestServletPath();
        return fullpath.substring(0, fullpath.indexOf(servletpath));
    }

    public static Benutzer getCurrentUser() {
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        return login != null ? login.getMyBenutzer() : null;
    }

    public static final FilenameFilter imageNameFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            boolean fileOk = false;
            String prefix = ConfigMain.getParameter("ImagePrefix", "\\d{8}");

            if (name.matches(prefix + "\\.[Tt][Ii][Ff][Ff]?")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[jJ][pP][eE]?[gG]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[jJ][pP][2]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[pP][nN][gG]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[gG][iI][fF]")) {
                fileOk = true;
            }
            return fileOk;
        }
    };

    public static final FilenameFilter dataFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            boolean fileOk = false;
            String prefix = ConfigMain.getParameter("ImagePrefix", "\\d{8}");
            if (name.matches(prefix + "\\.[Tt][Ii][Ff][Ff]?")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[jJ][pP][eE]?[gG]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[jJ][pP][2]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[pP][nN][gG]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[gG][iI][fF]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[pP][dD][fF]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[aA][vV][iI]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[mM][pP][gG]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[mM][pP]4")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[mM][pP]3")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[wW][aA][vV]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[wW][mM][vV]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[fF][lL][vV]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.[oO][gG][gG]")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.docx")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.doc")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.xls")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.xlsx")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.pptx")) {
                fileOk = true;
            } else if (name.matches(prefix + "\\.ppt")) {
                fileOk = true;
            }
            return fileOk;
        }
    };

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
}
