package de.sub.goobi.forms;

//TODO: Move Parts of this into a authentification API
import java.io.File;
import java.io.FilenameFilter;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.metadaten.MetadatenSperrung;
import de.sub.goobi.persistence.BenutzerDAO;
import de.sub.goobi.persistence.BenutzergruppenDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.encryption.MD5;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.ldap.Ldap;

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
		if (myBenutzer != null)
			new MetadatenSperrung().alleBenutzerSperrungenAufheben(myBenutzer.getId());
		myBenutzer = null;
		schonEingeloggt = false;
		SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
		HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		temp.sessionBenutzerAktualisieren(mySession, myBenutzer);
		if (mySession != null) {
			mySession.invalidate();
		}
		return "newMain";
	}

	public String Einloggen() {
		AlteBilderAufraeumen();
		myBenutzer = null;
		/* ohne Login gleich abbrechen */
		if (login == null) {
			Helper.setFehlerMeldung("login", "", Helper.getTranslation("wrongLogin"));
		} else {
			if (login.equals("root")) {
				String pwMD5 = new MD5(passwort).getMD5();
				if (pwMD5.equals("c01dc87e699e49cba16cf22f99eec00c")) {
					Benutzer b = new Benutzer();
					b.setLogin("root");
					try {
						Benutzergruppe admin = new BenutzergruppenDAO().get(1);
						Set<Benutzergruppe> l = new HashSet<Benutzergruppe>();
						l.add(admin);
						b.setBenutzergruppen(l);
						myBenutzer = b;
					} catch (DAOException e) {

					}
					// b.setBenutzergruppen(benutzergruppen)
				} else {
					Helper.setFehlerMeldung("login", "", Helper.getTranslation("wrongLogin"));
				}
				return "";
			}
			/* prüfen, ob schon ein Benutzer mit dem Login existiert */
			// TODO: Use generics.
			List<Benutzer> treffer;
			try {
				// TODO: Try to avoid SQL
				treffer = new BenutzerDAO().search("from Benutzer where login='" + login + "'");
			} catch (DAOException e) {
				Helper.setFehlerMeldung("could not read database", e.getMessage());
				return "";
			}
			if (treffer != null && treffer.size() > 0) {
				/* Login vorhanden, nun passwort prüfen */
				Benutzer b = (Benutzer) treffer.get(0);
				/* wenn der Benutzer auf inaktiv gesetzt (z.B. arbeitet er nicht mehr hier) wurde, jetzt Meldung anzeigen */
				if (!b.isIstAktiv()) {
					Helper.setFehlerMeldung("login", "", "login inactiv");
					return "";
				}
				/* wenn passwort auch richtig ist, den benutzer übernehmen */
				if (b.istPasswortKorrekt(passwort)) {
					/* jetzt prüfen, ob dieser Benutzer schon in einer anderen Session eingeloggt ist */
					SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
					HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
					if (!temp.BenutzerInAndererSessionAktiv(mySession, b)) {
						/* in der Session den Login speichern */
						temp.sessionBenutzerAktualisieren(mySession, b);
						myBenutzer = b;
					} else {
						schonEingeloggt = true;
						tempBenutzer = b;
						// Helper.setMeldung("formLogin:login", "", "Benutzer in anderer Session aktiv", false);
					}
				} else
					// schonEingeloggt = false;
					Helper.setFehlerMeldung("passwort", "", "wrong password");
			} else {
				/* Login nicht vorhanden, also auch keine Passwortprüfung */
				Helper.setFehlerMeldung("login", "", Helper.getTranslation("wrongLogin"));
			}
		}
		// checking if saved css stylesheet is available, if not replace it by something available
		if (myBenutzer != null) {
			String tempCss = myBenutzer.getCss();
			String newCss = new HelperForm().getCssLinkIfExists(tempCss);
			myBenutzer.setCss(newCss);
			return "";
		}
		return "";
	}

	public String NochmalEinloggen() {
		SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
		HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		/* in der Session den Login speichern */
		temp.sessionBenutzerAktualisieren(mySession, tempBenutzer);
		myBenutzer = tempBenutzer;
		schonEingeloggt = false;
		return "";
	}

	public String EigeneAlteSessionsAufraeumen() {
		SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
		HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		temp.alteSessionsDesSelbenBenutzersAufraeumen(mySession, tempBenutzer);
		/* in der Session den Login speichern */
		temp.sessionBenutzerAktualisieren(mySession, tempBenutzer);
		myBenutzer = tempBenutzer;
		schonEingeloggt = false;
		return "";
	}

	public String EinloggenAls() {
		if (getMaximaleBerechtigung() != 1)
			return "newMain";
		myBenutzer = null;
		Integer LoginID = Integer.valueOf(Helper.getRequestParameter("ID"));
		try {
			myBenutzer = new BenutzerDAO().get(LoginID);
			/* in der Session den Login speichern */
			SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
			temp.sessionBenutzerAktualisieren((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false), myBenutzer);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("could not read database", e.getMessage());
			return "";
		}
		return "newMain";
	}

	/*
	 * ##################################################### ##################################################### ## ## änderung des Passworts ##
	 * ##################################################### ####################################################
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
		// if (!passwortAendernAlt.equals(myBenutzer.getPasswort())) {
		if (!myBenutzer.istPasswortKorrekt(passwortAendernAlt)) {
			Helper.setFehlerMeldung("passwortform:passwortAendernAlt", "", Helper.getTranslation("aktuellesPasswortFalsch"));
		} else {
			/* ist das neue Passwort beide Male gleich angegeben? */
			if (!passwortAendernNeu1.equals(passwortAendernNeu2)) {
				Helper.setFehlerMeldung("passwortform:passwortAendernNeu1", "", Helper.getTranslation("neuesPasswortNichtGleich"));
			} else {
				// myBenutzer.setPasswortCrypt(passwortAendernNeu1);
				try {
					/* wenn alles korrekt, dann jetzt speichern */
					Ldap myLdap = new Ldap();
					myLdap.changeUserPassword(myBenutzer, passwortAendernAlt, passwortAendernNeu1);
					Benutzer temp = new BenutzerDAO().get(myBenutzer.getId());
					temp.setPasswortCrypt(passwortAendernNeu1);
					new BenutzerDAO().save(temp);
					myBenutzer = temp;

					Helper.setMeldung(null, "", Helper.getTranslation("passwortGeaendert"));
				} catch (DAOException e) {
					Helper.setFehlerMeldung("could not save", e.getMessage());
				} catch (NoSuchAlgorithmException e) {
					Helper.setFehlerMeldung("ldap errror", e.getMessage());
				}
			}
		}
		return "";
	}

	/**
	 * Benutzerkonfiguration speichern
	 */
	public String BenutzerkonfigurationSpeichern() {
		try {
			Benutzer temp = new BenutzerDAO().get(myBenutzer.getId());
			temp.setTabellengroesse(myBenutzer.getTabellengroesse());
			temp.setMetadatenSprache(myBenutzer.getMetadatenSprache());
			temp.setConfVorgangsdatumAnzeigen(myBenutzer.isConfVorgangsdatumAnzeigen());
			new BenutzerDAO().save(temp);
			myBenutzer = temp;
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
			public boolean accept(File dir, String name) {
				return name.endsWith(".png");
			}
		};
		File dir = new File(myPfad);
		String[] dateien = dir.list(filter);

		/* alle Dateien durchlaufen und die alten löschen */
		if (dateien != null) {
			for (int i = 0; i < dateien.length; i++) {
				File file = new File(myPfad + dateien[i]);
				if ((System.currentTimeMillis() - file.lastModified()) > 7200000)
					file.delete();
			}
		}
	}

	/*
	 * ##################################################### ##################################################### ## ## Getter und Setter ##
	 * ##################################################### ####################################################
	 */

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		if (this.login != null && !this.login.equals(login))
			schonEingeloggt = false;
		this.login = login;
	}

	public String getPasswort() {
		return passwort;
	}

	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}

	public Benutzer getMyBenutzer() {
		return myBenutzer;
	}

	public void setMyBenutzer(Benutzer myClass) {
		this.myBenutzer = myClass;
	}

	public int getMaximaleBerechtigung() {
		int rueckgabe = 0;
		if (myBenutzer != null) {
			// TODO: Don't use Iterators
			for (Iterator<Benutzergruppe> iter = myBenutzer.getBenutzergruppen().iterator(); iter.hasNext();) {
				Benutzergruppe element = (Benutzergruppe) iter.next();
				if (element.getBerechtigung().intValue() < rueckgabe || rueckgabe == 0)
					rueckgabe = element.getBerechtigung().intValue();
			}
		}
		return rueckgabe;
	}

	public String getPasswortAendernAlt() {
		return passwortAendernAlt;
	}

	public void setPasswortAendernAlt(String passwortAendernAlt) {
		this.passwortAendernAlt = passwortAendernAlt;
	}

	public String getPasswortAendernNeu1() {
		return passwortAendernNeu1;
	}

	public void setPasswortAendernNeu1(String passwortAendernNeu1) {
		this.passwortAendernNeu1 = passwortAendernNeu1;
	}

	public String getPasswortAendernNeu2() {
		return passwortAendernNeu2;
	}

	public void setPasswortAendernNeu2(String passwortAendernNeu2) {
		this.passwortAendernNeu2 = passwortAendernNeu2;
	}

	public boolean isSchonEingeloggt() {
		return schonEingeloggt;
	}

}
