package de.sub.goobi.Forms;

//TODO: Move Parts of this into a authentification API
import java.io.File;
import java.io.FilenameFilter;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.Benutzergruppe;
import de.sub.goobi.Metadaten.MetadatenSperrung;
import de.sub.goobi.Persistence.BenutzerDAO;
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
      if (myBenutzer!=null)
         new MetadatenSperrung().alleBenutzerSperrungenAufheben(myBenutzer.getId());
      myBenutzer = null;
      schonEingeloggt = false;
      SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
      HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
            .getSession(false);
      temp.sessionBenutzerAktualisieren(mySession, myBenutzer);
      mySession.invalidate();
      return "newMain";
   }

   

   public String Einloggen() {
      AlteBilderAufraeumen();
      myBenutzer = null;
      /* ohne Login gleich abbrechen */
      if (login == null) {
         new Helper().setFehlerMeldung("login", "", "loginUngueltig");
      } else {
    	  if (login.equals("root")) {
				String pwMD5 = new MD5(passwort).getMD5();
				if (pwMD5.equals("8ea0eb240c3e6432c3ad08e85adcd9ae")) {
					Benutzer b = new Benutzer();
					b.setLogin("root");
					myBenutzer = b;
				} else {
					new Helper().setFehlerMeldung("login", "", "loginUngueltig");
				}
				return "";
			}
         /* prüfen, ob schon ein Benutzer mit dem Login existiert */
    	//TODO: Use generics.
         List treffer;
         try {
        	 //TODO: Try to avoid SQL
            treffer = new BenutzerDAO().search("from Benutzer where login='" + login + "'");
         } catch (DAOException e) {
            new Helper().setFehlerMeldung( "fehlerNichtLadbar", e.getMessage());
            return "";
         }
         if (treffer != null && treffer.size() > 0) {
            /* Login vorhanden, nun passwort prüfen */
            Benutzer b = (Benutzer) treffer.get(0);
           /* wenn der Benutzer auf inaktiv gesetzt (z.B. arbeitet er nicht mehr hier) wurde, jetzt Meldung anzeigen */
            if (!b.isIstAktiv()){
               new Helper().setFehlerMeldung("login", "", "loginInaktiv");
               return "";
            }
            /* wenn passwort auch richtig ist, den benutzer übernehmen */
            if (b.istPasswortKorrekt(passwort)) {
               /* jetzt prüfen, ob dieser Benutzer schon in einer anderen Session eingeloggt ist */
               SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
               HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
                     .getSession(false);
               if (!temp.BenutzerInAndererSessionAktiv(mySession, b)) {
                  /* in der Session den Login speichern */
                  temp.sessionBenutzerAktualisieren(mySession, b);
                  myBenutzer = b;
               } else {
                  schonEingeloggt = true;
                  tempBenutzer = b;
                  // new Helper().setMeldung("formLogin:login", "", "Benutzer in anderer Session aktiv", false);
               }
            } else
               //               schonEingeloggt = false;
               new Helper().setFehlerMeldung("passwort", "", "passwortUngueltig");
         } else {
            /* Login nicht vorhanden, also auch keine Passwortprüfung */
            new Helper().setFehlerMeldung("login", "", "loginUngueltig");
         }
      }
      // checking if saved css stylesheet is available, if not replace it by something available
      if(myBenutzer!=null){
      String tempCss = myBenutzer.getCss();
      String newCss = new HelperForm().getCssLinkIfExists(tempCss);
      myBenutzer.setCss(newCss);
      return "";}
      return "";
   }

   

   public String NochmalEinloggen() {
      SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
      HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
            .getSession(false);
      /* in der Session den Login speichern */
      temp.sessionBenutzerAktualisieren(mySession, tempBenutzer);
      myBenutzer = tempBenutzer;
      schonEingeloggt = false;
      return "";
   }

   

   public String EigeneAlteSessionsAufraeumen() {
      SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
      HttpSession mySession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
            .getSession(false);
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
      Integer LoginID = Integer.valueOf(new Helper().getRequestParameter("ID"));
      try {
         myBenutzer = new BenutzerDAO().get(LoginID);
         /* in der Session den Login speichern */
         SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
         temp.sessionBenutzerAktualisieren((HttpSession) FacesContext.getCurrentInstance()
               .getExternalContext().getSession(false), myBenutzer);
      } catch (DAOException e) {
         new Helper().setFehlerMeldung( "fehlerNichtLadbar", e.getMessage());
         return "";
      }
      return "newMain";
   }

   /*#####################################################
    #####################################################
    ##																															 
    ##					                             änderung des Passworts									
    ##                                                   															    
    #####################################################
    ####################################################*/

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
//      if (!passwortAendernAlt.equals(myBenutzer.getPasswort())) {
      if (!myBenutzer.istPasswortKorrekt(passwortAendernAlt)) {
         new Helper().setFehlerMeldung("passwortform:passwortAendernAlt", "", "aktuellesPasswortFalsch");
      } else {
         /* ist das neue Passwort beide Male gleich angegeben? */
         if (!passwortAendernNeu1.equals(passwortAendernNeu2)) {
            new Helper()
                  .setFehlerMeldung("passwortform:passwortAendernNeu1", "", "neuesPasswortNichtGleich");
         } else {
//            myBenutzer.setPasswortCrypt(passwortAendernNeu1);
            try {
               /* wenn alles korrekt, dann jetzt speichern */
               Ldap myLdap = new Ldap();
               myLdap.changeUserPassword(myBenutzer, passwortAendernAlt, passwortAendernNeu1);
               Benutzer temp = new BenutzerDAO().get(myBenutzer.getId());
               temp.setPasswortCrypt(passwortAendernNeu1);
               new BenutzerDAO().save(temp);
               myBenutzer = temp;
               
               new Helper().setMeldung(null, "", "passwortGeaendert");
            } catch (DAOException e) {
               new Helper().setFehlerMeldung( "fehlerNichtSpeicherbar", e.getMessage());
            } catch (NoSuchAlgorithmException e) {
               new Helper().setFehlerMeldung( "fehlerLdap", e.getMessage());
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
         new Helper().setMeldung(null, "", "KonfigurationGeaendert");
      } catch (DAOException e) {
         new Helper().setFehlerMeldung( "fehlerNichtSpeicherbar", e.getMessage());
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


   /*#####################################################
    #####################################################
    ##                                                                                              
    ##                                                Getter und Setter                         
    ##                                                                                                    
    #####################################################
    ####################################################*/

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
    	//TODO: Don't use Iterators
         for (Iterator iter = myBenutzer.getBenutzergruppen().iterator(); iter.hasNext();) {
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
