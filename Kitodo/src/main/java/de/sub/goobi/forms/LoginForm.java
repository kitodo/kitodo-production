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

package de.sub.goobi.forms;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ldap.Ldap;
import de.sub.goobi.metadaten.MetadatenSperrung;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.filemanagement.filters.FileNameEndsWithFilter;
import org.kitodo.data.database.beans.Authorization;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.security.SecurityPasswordEncoder;
import org.kitodo.services.ServiceManager;

@Named("LoginForm")
@SessionScoped
public class LoginForm implements Serializable {
    private static final long serialVersionUID = 7732045664713555233L;
    private String login;
    private String password;
    private User myBenutzer;
    private User tempBenutzer;
    private boolean schonEingeloggt = false;
    private String passwortAendernAlt;
    private String passwortAendernNeu1;
    private String passwortAendernNeu2;
    private SecurityPasswordEncoder passwordEncoder = new SecurityPasswordEncoder();
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(LoginForm.class);

    /**
     * Log out.
     *
     * @return String
     */
    public String Ausloggen() {
        if (this.myBenutzer != null) {
            new MetadatenSperrung().alleBenutzerSperrungenAufheben(this.myBenutzer.getId());
        }
        this.myBenutzer = null;
        this.schonEingeloggt = false;
        this.login = "";
        SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
        HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        temp.sessionBenutzerAktualisieren(mySession, this.myBenutzer);
        if (mySession != null) {
            mySession.invalidate();
        }
        return "/pages/Main";
    }

    /**
     * Log in.
     *
     * @return String
     */
    public String Einloggen() throws IOException {
        AlteBilderAufraeumen();
        this.myBenutzer = null;
        /* ohne Login gleich abbrechen */
        if (this.login == null) {
            Helper.setFehlerMeldung("login", "", Helper.getTranslation("wrongLogin"));
        } else {
            /* prüfen, ob schon ein Benutzer mit dem Login existiert */
            List<User> treffer;
            try {
                treffer = serviceManager.getUserService().getByQuery("from User where login = :username", "username",
                        this.login);
            } catch (DAOException e) {
                Helper.setFehlerMeldung("could not read database", e.getMessage());
                return null;
            }
            if (treffer != null && treffer.size() > 0) {
                /* Login vorhanden, nun passwort prüfen */
                User b = treffer.get(0);
                /*
                 * wenn der Benutzer auf inaktiv gesetzt (z.B. arbeitet er nicht
                 * mehr hier) wurde, jetzt Meldung anzeigen
                 */
                if (!b.isActive()) {
                    Helper.setFehlerMeldung("login", "", Helper.getTranslation("loginInactive"));
                    return null;
                }
                /* wenn passwort auch richtig ist, den benutzer übernehmen */
                if (serviceManager.getUserService().isPasswordCorrect(b, this.password)) {
                    /*
                     * jetzt prüfen, ob dieser Benutzer schon in einer anderen
                     * Session eingeloggt ist
                     */
                    SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
                    HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
                            .getSession(false);
                    if (!temp.userActiveInOtherSession(mySession, b)) {
                        /* in der Session den Login speichern */
                        temp.sessionBenutzerAktualisieren(mySession, b);
                        this.myBenutzer = b;
                    } else {
                        this.schonEingeloggt = true;
                        this.tempBenutzer = b;
                    }
                } else {
                    Helper.setFehlerMeldung("login", "", Helper.getTranslation("wrongLogin"));
                }
            } else {
                /* Login nicht vorhanden, also auch keine Passwortprüfung */
                Helper.setFehlerMeldung("login", "", Helper.getTranslation("wrongLogin"));
            }
        }
        // checking if saved css stylesheet is available, if not replace it by
        // something available
        if (this.myBenutzer != null) {
            String tempCss = this.myBenutzer.getCss();
            String newCss = new HelperForm().getCssLinkIfExists(tempCss);
            this.myBenutzer.setCss(newCss);
            return null;
        }
        return null;
    }

    /**
     * Again log in.
     *
     * @return String
     */
    public String NochmalEinloggen() {
        SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
        HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        /* in der Session den Login speichern */
        temp.sessionBenutzerAktualisieren(mySession, this.tempBenutzer);
        this.myBenutzer = this.tempBenutzer;
        this.schonEingeloggt = false;
        return null;
    }

    /**
     * Clean session.
     *
     * @return empty String
     */
    public String EigeneAlteSessionsAufraeumen() {
        SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
        HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        temp.alteSessionsDesSelbenBenutzersAufraeumen(mySession, this.tempBenutzer);
        /* in der Session den Login speichern */
        temp.sessionBenutzerAktualisieren(mySession, this.tempBenutzer);
        this.myBenutzer = this.tempBenutzer;
        this.schonEingeloggt = false;
        return null;
    }

    /**
     * Login.
     *
     * @return String
     */
    public String EinloggenAls() {
        if (getMaximaleBerechtigung() != 1) {
            return "/pages/Main";
        }
        this.myBenutzer = null;
        Integer loginId = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            this.myBenutzer = serviceManager.getUserService().getById(loginId);
            /* in der Session den Login speichern */
            SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
            temp.sessionBenutzerAktualisieren(
                    (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false),
                    this.myBenutzer);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not read database", e.getMessage());
            return null;
        }
        return "/pages/Main";
    }

    /*
     * änderung des Passworts
     */

    /**
     * neues Passwort übernehmen.
     */
    public String PasswortAendernSpeichern() {
        /* ist das aktuelle Passwort korrekt angegeben ? */
        /* ist das neue Passwort beide Male gleich angegeben? */
        if (!this.passwortAendernNeu1.equals(this.passwortAendernNeu2)) {
            Helper.setFehlerMeldung(Helper.getTranslation("neuesPasswortNichtGleich"));
        } else {
            try {
                /* wenn alles korrekt, dann jetzt speichern */
                Ldap myLdap = new Ldap();
                myLdap.changeUserPassword(this.myBenutzer, this.passwortAendernAlt, this.passwortAendernNeu1);
                User temp = serviceManager.getUserService().getById(this.myBenutzer.getId());
                temp.setPassword(passwordEncoder.encrypt(this.passwortAendernNeu1));
                serviceManager.getUserService().save(temp);
                this.myBenutzer = temp;
                Helper.setMeldung(Helper.getTranslation("passwortGeaendert"));
            } catch (DAOException e) {
                Helper.setFehlerMeldung("could not save", e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                Helper.setFehlerMeldung("ldap errror", e.getMessage());
            } catch (DataException e) {
                Helper.setFehlerMeldung("could not insert to index", e.getMessage());
            }
        }
        return null;
    }

    /**
     * Benutzerkonfiguration speichern.
     */
    public String BenutzerkonfigurationSpeichern() {
        try {
            User temp = serviceManager.getUserService().getById(this.myBenutzer.getId());
            temp.setTableSize(this.myBenutzer.getTableSize());
            temp.setMetadataLanguage(this.myBenutzer.getMetadataLanguage());
            temp.setConfigProductionDateShow(this.myBenutzer.isConfigProductionDateShow());
            temp.setCss(this.myBenutzer.getCss());
            serviceManager.getUserService().save(temp);
            this.myBenutzer = temp;
            Helper.setMeldung(null, "", Helper.getTranslation("configurationChanged"));
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not save", e.getMessage());
        } catch (DataException e) {
            Helper.setFehlerMeldung("could not insert to index", e.getMessage());
        }
        return null;
    }

    private void AlteBilderAufraeumen() throws IOException {
        /* Pages-Verzeichnis mit den temporären Images ermitteln */
        URI path = ConfigCore.getTempImagesPathAsCompleteDirectory();

        /* Verzeichnis einlesen */
        FilenameFilter filter = new FileNameEndsWithFilter(".png");
        ArrayList<URI> uris = serviceManager.getFileService().getSubUris(filter, path);

        /* alle Dateien durchlaufen und die alten löschen */
        for (URI uri : uris) {
            URI file = path.resolve(uri);
            if ((System.currentTimeMillis() - new File(file).lastModified()) > 7200000) {
                serviceManager.getFileService().delete(file);
            }
        }
    }

    /*
     * Getter und Setter
     */

    public String getLogin() {
        return this.login;
    }

    /**
     * Set login.
     *
     * @param login
     *            String
     */
    public void setLogin(String login) {
        if (this.login != null && !this.login.equals(login)) {
            this.schonEingeloggt = false;
        }
        this.login = login;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets current authenticated User.
     *
     * @return
     *      The User.
     */
    public User getMyBenutzer() {
        if (myBenutzer != null) {
            return this.myBenutzer;
        } else {
            try {
                myBenutzer = serviceManager.getUserService().getAuthenticatedUser();
                return this.myBenutzer;
            } catch (DAOException e) {
                Helper.setFehlerMeldung(e);
            }
        }
        return null;
    }

    public void setMyBenutzer(User myClass) {
        this.myBenutzer = myClass;
    }

    /**
     * Get max rights.
     *
     * @return int
     */
    public int getMaximaleBerechtigung() {
        //TODO Only to keep compatibility to old frontend pages
        //TODO delete this methode when all new frontend is ready or security tags are replaced
        if (this.myBenutzer != null) {
            for (UserGroup userGroup : this.myBenutzer.getUserGroups()) {
                if (userGroup.getAuthorizations().size() > 0) {
                    for (Authorization authorization : userGroup.getAuthorizations()) {
                        if (authorization.getTitle().equals("admin")) {
                            return 1;
                        }
                    }
                }
            }
            return 4;
        }
        return 0;

    }

    public String getPasswortAendernAlt() {
        return this.passwortAendernAlt;
    }

    public void setPasswortAendernAlt(String passwortAendernAlt) {
        this.passwortAendernAlt = passwortAendernAlt;
    }

    public String getPasswortAendernNeu1() {
        return this.passwortAendernNeu1;
    }

    public void setPasswortAendernNeu1(String passwortAendernNeu1) {
        this.passwortAendernNeu1 = passwortAendernNeu1;
    }

    public String getPasswortAendernNeu2() {
        return this.passwortAendernNeu2;
    }

    public void setPasswortAendernNeu2(String passwortAendernNeu2) {
        this.passwortAendernNeu2 = passwortAendernNeu2;
    }

    public boolean isSchonEingeloggt() {
        return this.schonEingeloggt;
    }

    /**
     * The function getUserHomeDir() returns the home directory of the currently
     * logged in user, if any, or the empty string otherwise.
     *
     * @return the home directory of the current user.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public static URI getCurrentUserHomeDir() throws IOException {
        URI result = null;
        ServiceManager serviceManager = new ServiceManager();
        LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (loginForm != null) {
            result = serviceManager.getUserService().getHomeDirectory(loginForm.getMyBenutzer());
        }
        return result;
    }
}
