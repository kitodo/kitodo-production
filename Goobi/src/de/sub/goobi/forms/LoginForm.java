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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.ldap.Ldap;
import de.sub.goobi.metadaten.MetadatenSperrung;
import de.sub.goobi.persistence.BenutzerDAO;

public class LoginForm {
    private String login;
    private String passwort;
    private Benutzer myBenutzer;
    private Benutzer tempBenutzer;
    private boolean schonEingeloggt = false;
    private String passwortAendernAlt;
    private String passwortAendernNeu1;
    private String passwortAendernNeu2;

    public String Ausloggen() {
        if (this.myBenutzer != null) {
            new MetadatenSperrung().alleBenutzerSperrungenAufheben(this.myBenutzer.getId());
        }
        this.myBenutzer = null;
        this.schonEingeloggt = false;
        SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
        HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        temp.sessionBenutzerAktualisieren(mySession, this.myBenutzer);
        if (mySession != null) {
            mySession.invalidate();
        }
        return "newMain";
    }

    public String Einloggen() {
        AlteBilderAufraeumen();
        this.myBenutzer = null;
        /* ohne Login gleich abbrechen */
        if (this.login == null) {
            Helper.setFehlerMeldung("login", "", Helper.getTranslation("wrongLogin"));
        } else {
            /* prüfen, ob schon ein Benutzer mit dem Login existiert */
            List<Benutzer> treffer;
            try {
                treffer = new BenutzerDAO().search("from Benutzer where login = :username", "username", this.login);
            } catch (DAOException e) {
                Helper.setFehlerMeldung("could not read database", e.getMessage());
                return "";
            }
            if (treffer != null && treffer.size() > 0) {
                /* Login vorhanden, nun passwort prüfen */
                Benutzer b = treffer.get(0);
                /* wenn der Benutzer auf inaktiv gesetzt (z.B. arbeitet er nicht mehr hier) wurde, jetzt Meldung anzeigen */
                if (!b.isIstAktiv()) {
                    Helper.setFehlerMeldung("login", "", Helper.getTranslation("loginInactive"));
                    return "";
                }
                /* wenn passwort auch richtig ist, den benutzer übernehmen */
                if (b.istPasswortKorrekt(this.passwort)) {
                    /* jetzt prüfen, ob dieser Benutzer schon in einer anderen Session eingeloggt ist */
                    SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
                    HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
                    if (!temp.BenutzerInAndererSessionAktiv(mySession, b)) {
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
        // checking if saved css stylesheet is available, if not replace it by something available
        if (this.myBenutzer != null) {
            String tempCss = this.myBenutzer.getCss();
            String newCss = new HelperForm().getCssLinkIfExists(tempCss);
            this.myBenutzer.setCss(newCss);
            return "";
        }
        return "";
    }

    public String NochmalEinloggen() {
        SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
        HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        /* in der Session den Login speichern */
        temp.sessionBenutzerAktualisieren(mySession, this.tempBenutzer);
        this.myBenutzer = this.tempBenutzer;
        this.schonEingeloggt = false;
        return "";
    }

    public String EigeneAlteSessionsAufraeumen() {
        SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
        HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        temp.alteSessionsDesSelbenBenutzersAufraeumen(mySession, this.tempBenutzer);
        /* in der Session den Login speichern */
        temp.sessionBenutzerAktualisieren(mySession, this.tempBenutzer);
        this.myBenutzer = this.tempBenutzer;
        this.schonEingeloggt = false;
        return "";
    }

    public String EinloggenAls() {
        if (getMaximaleBerechtigung() != 1) {
            return "newMain";
        }
        this.myBenutzer = null;
        Integer LoginID = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            this.myBenutzer = new BenutzerDAO().get(LoginID);
            /* in der Session den Login speichern */
            SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
            temp.sessionBenutzerAktualisieren((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false), this.myBenutzer);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not read database", e.getMessage());
            return "";
        }
        return "newMain";
    }

    /*
     * änderung des Passworts
     */

    /**
     * Bearbeitungsvorgang abbrechen
     */
    public String PasswortAendernAbbrechen() {
        return "newMain";
    }

    /**
     * neues Passwort übernehmen
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
                    Benutzer temp = new BenutzerDAO().get(this.myBenutzer.getId());
                    temp.setPasswortCrypt(this.passwortAendernNeu1);
                    new BenutzerDAO().save(temp);
                    this.myBenutzer = temp;

                    Helper.setMeldung(Helper.getTranslation("passwortGeaendert"));
                } catch (DAOException e) {
                    Helper.setFehlerMeldung("could not save", e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    Helper.setFehlerMeldung("ldap errror", e.getMessage());
                }
            }
        return "";
    }

    /**
     * Benutzerkonfiguration speichern
     */
    public String BenutzerkonfigurationSpeichern() {
        try {
            Benutzer temp = new BenutzerDAO().get(this.myBenutzer.getId());
            temp.setTabellengroesse(this.myBenutzer.getTabellengroesse());
            temp.setMetadatenSprache(this.myBenutzer.getMetadatenSprache());
            temp.setConfVorgangsdatumAnzeigen(this.myBenutzer.isConfVorgangsdatumAnzeigen());
            new BenutzerDAO().save(temp);
            this.myBenutzer = temp;
            Helper.setMeldung(null, "", Helper.getTranslation("configurationChanged"));
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not save", e.getMessage());
        }
        return "";
    }

    private void AlteBilderAufraeumen() {
        /* Pages-Verzeichnis mit den temporären Images ermitteln */
        String myPfad = ConfigMain.getTempImagesPathAsCompleteDirectory();

        /* Verzeichnis einlesen */
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg");
            }
        };
        File dir = new File(myPfad);
        String[] dateien = dir.list(filter);

        /* alle Dateien durchlaufen und die alten löschen */
        if (dateien != null) {
            for (int i = 0; i < dateien.length; i++) {
                File file = new File(myPfad + dateien[i]);
                if ((System.currentTimeMillis() - file.lastModified()) > 7200000) {
                    file.delete();
                }
            }
        }
    }

    /*
     * Getter und Setter
     */

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        if (this.login != null && !this.login.equals(login)) {
            this.schonEingeloggt = false;
        }
        this.login = login;
    }

    public String getPasswort() {
        return this.passwort;
    }

    public void setPasswort(String passwort) {
        this.passwort = passwort;
    }

    public Benutzer getMyBenutzer() {
        return this.myBenutzer;
    }

    public void setMyBenutzer(Benutzer myClass) {
        this.myBenutzer = myClass;
    }

    public int getMaximaleBerechtigung() {
        int rueckgabe = 0;
        if (this.myBenutzer != null) {
            for (Iterator<Benutzergruppe> iter = this.myBenutzer.getBenutzergruppen().iterator(); iter.hasNext();) {
                Benutzergruppe element = iter.next();
                if (element.getBerechtigung().intValue() < rueckgabe || rueckgabe == 0) {
                    rueckgabe = element.getBerechtigung().intValue();
                }
            }
        }
        return rueckgabe;
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
     * @return the home directory of the current user
     * @throws InterruptedException
     *             If the thread running the script is interrupted by another
     *             thread while it is waiting, then the wait is ended and an
     *             InterruptedException is thrown.
     * @throws IOException if an I/O error occurs.
     */
    public static String getCurrentUserHomeDir() throws IOException, InterruptedException {
        String result = "";
        LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (loginForm != null)
            result = loginForm.getMyBenutzer().getHomeDir();
        return result;
    }

}
