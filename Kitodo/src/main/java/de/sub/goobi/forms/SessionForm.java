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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.User;
import org.kitodo.services.ServiceManager;

/**
 * Die Klasse SessionForm für den überblick über die aktuell offenen Sessions
 * 
 * @author Steffen Hankiewicz
 * @version 1.00 - 16.01.2005
 */
public class SessionForm {
    @SuppressWarnings("rawtypes")
    private List alleSessions = new ArrayList();
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private String aktuelleZeit = this.formatter.format(new Date());
    private String bitteAusloggen = "";
    private static final Logger logger = LogManager.getLogger(SessionForm.class);
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Get active sessions.
     *
     * @return int
     */
    public int getAktiveSessions() {
        if (this.alleSessions == null) {
            return 0;
        } else {
            return this.alleSessions.size();
        }
    }

    public String getAktuelleZeit() {
        return this.aktuelleZeit;
    }

    /**
     * Get all sessions.
     *
     * @return List
     */
    @SuppressWarnings("rawtypes")
    public List getAlleSessions() {
        try {
            return this.alleSessions;
        } catch (RuntimeException e) {
            return null;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    private void sessionAdd(HttpSession insession) {
        HashMap map = new HashMap();
        map.put("id", insession.getId());
        map.put("created", this.formatter.format(new Date()));
        map.put("last", this.formatter.format(new Date()));
        map.put("last2", System.currentTimeMillis());
        map.put("user", " - ");
        map.put("userid", 0);
        map.put("session", insession);
        map.put("browserIcon", "none.gif");
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

            String address = request.getRemoteAddr();
            if (address != null && address.startsWith("127.0.0.1")) {
                address = request.getHeader("x-forwarded-for");
                if (address == null) {
                    address = "127.0.0.1";
                }
            }
            map.put("address", address);

            String mybrowser = request.getHeader("User-Agent");
            if (mybrowser == null) {
                mybrowser = "-";
            }
            map.put("browser", mybrowser);
            if (mybrowser.contains("Netscape")) {
                map.put("browserIcon", "netscape.gif");
            } else if (mybrowser.contains("Konqueror")) {
                map.put("browserIcon", "konqueror.gif");
            } else if (mybrowser.contains("Opera") || mybrowser.contains("OPR")) {
                map.put("browserIcon", "opera.gif");
            } else if (mybrowser.contains("Safari")) {
                map.put("browserIcon", "safari.gif");
            } else if (mybrowser.contains("MSIE") || mybrowser.contains("Trident")) {
                map.put("browserIcon", "ie.png");
            } else if (mybrowser.contains("Firefox")) {
                map.put("browserIcon", "firefox.png");
            } else if (mybrowser.contains("Gecko")) {
                map.put("browserIcon", "mozilla.png");
            }
        }
        this.alleSessions.add(map);
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    private void sessionsAufraeumen(int time) {
        for (HashMap session : (List<HashMap>) new ArrayList(this.alleSessions)) {
            long difference = System.currentTimeMillis() - ((Long) session.get("last2")).longValue();
            if (difference / 1000 > time || session.get("address") == null || (session.get("user").equals("- ausgeloggt - "))) {
                this.alleSessions.remove(session);
            }
        }
    }

    /**
     * Update session.
     *
     * @param insession
     *            HttpSession object
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    public void sessionAktualisieren(HttpSession insession) {
        boolean gefunden = false;
        this.aktuelleZeit = this.formatter.format(new Date());
        for (HashMap session : (List<HashMap>) this.alleSessions) {
            if (session.get("id").equals(insession.getId())) {
                session.put("last", this.formatter.format(new Date()));
                session.put("last2", System.currentTimeMillis());
                gefunden = true;
                break;
            }
        }
        if (!gefunden) {
            sessionAdd(insession);
        }
        sessionsAufraeumen(insession.getMaxInactiveInterval());
    }

    /**
     * Update user session.
     *
     * @param insession
     *            HttpSession object
     * @param inBenutzer
     *            User object
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    public void sessionBenutzerAktualisieren(HttpSession insession, User inBenutzer) {
        // logger.debug("sessionBenutzerAktualisieren-start");
        for (HashMap session : (List<HashMap>) this.alleSessions) {
            if (session.get("id").equals(insession.getId())) {
                if (inBenutzer != null) {
                    insession.setAttribute("User", serviceManager.getUserService().getFullName(inBenutzer));
                    session.put("user", serviceManager.getUserService().getFullName(inBenutzer));
                    session.put("userid", inBenutzer.getId());
                    insession.setMaxInactiveInterval(serviceManager.getUserService().getSessionTimeout(inBenutzer));
                } else {
                    session.put("user", "- ausgeloggt - ");
                    session.put("userid", 0);
                }
                break;
            }
        }
    }

    /**
     * prüfen, ob der Benutzer in einer anderen Session aktiv ist.
     */
    @SuppressWarnings("rawtypes")
    public boolean userActiveInOtherSession(HttpSession insession, User inBenutzer) {
        boolean rueckgabe = false;
        for (HashMap session : (List<HashMap>) this.alleSessions) {
            boolean sessiongleich = session.get("id").equals(insession.getId());
            boolean nutzergleich = inBenutzer.getId().intValue() == ((Integer) session.get("userid")).intValue();
            if (!sessiongleich && nutzergleich) {
                rueckgabe = true;
                break;
            }
        }
        return rueckgabe;
    }

    /**
     * Java doc.
     *
     * @param inSession
     *            HttpSession object
     * @param inBenutzer
     *            User object
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    public void alteSessionsDesSelbenBenutzersAufraeumen(HttpSession inSession, User inBenutzer) {
        List alleSessionKopie = new ArrayList(this.alleSessions);
        for (HashMap sessionCopy : (List<HashMap>) alleSessionKopie) {
            boolean sessiongleich = sessionCopy.get("id").equals(inSession.getId());
            boolean nutzergleich = inBenutzer.getId().intValue() == ((Integer) sessionCopy.get("userid")).intValue();
            if (!sessiongleich && nutzergleich) {
                HttpSession tempSession = (HttpSession) sessionCopy.get("session");
                try {
                    if (tempSession != null) {
                        tempSession.invalidate();
                    }
                } catch (RuntimeException e) {
                    logger.error(e);
                }
                this.alleSessions.remove(sessionCopy);
            }
        }
    }

    public String getBitteAusloggen() {
        return this.bitteAusloggen;
    }

    public void setBitteAusloggen(String bitteAusloggen) {
        this.bitteAusloggen = bitteAusloggen;
    }
}
