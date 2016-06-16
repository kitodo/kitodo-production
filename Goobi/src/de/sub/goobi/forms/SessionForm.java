package de.sub.goobi.forms;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import de.sub.goobi.beans.Benutzer;

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

	@SuppressWarnings("rawtypes")
	public List getAlleSessions() {
		try {
			return this.alleSessions;
		} catch (RuntimeException e) {
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void sessionAdd(HttpSession insession) {
		HashMap map = new HashMap();
		map.put("id", insession.getId());
		map.put("created", this.formatter.format(new Date()));
		map.put("last", this.formatter.format(new Date()));
		map.put("last2", Long.valueOf(System.currentTimeMillis()));
		map.put("user", " - ");
		map.put("userid", Integer.valueOf(0));
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
			if (mybrowser.indexOf("Gecko") > 0) {
				map.put("browserIcon", "mozilla.png");
			}
			if (mybrowser.indexOf("Firefox") > 0) {
				map.put("browserIcon", "firefox.png");
			}
			if (mybrowser.indexOf("MSIE") > 0) {
				map.put("browserIcon", "ie.png");
			}
			if (mybrowser.indexOf("Opera") > 0) {
				map.put("browserIcon", "opera.gif");
			}
			if (mybrowser.indexOf("Safari") > 0) {
				map.put("browserIcon", "safari.gif");
			}
			if (mybrowser.indexOf("Konqueror") > 0) {
				map.put("browserIcon", "konqueror.gif");
			}
			if (mybrowser.indexOf("Netscape") > 0) {
				map.put("browserIcon", "netscape.gif");
			}
		}
		this.alleSessions.add(map);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void sessionsAufraeumen(int time) {
		List temp = new ArrayList(this.alleSessions);
		for (Iterator iter = temp.iterator(); iter.hasNext();) {
			HashMap map = (HashMap) iter.next();
			long differenz = System.currentTimeMillis() - ((Long) map.get("last2")).longValue();
			if (differenz / 1000 > time || map.get("address") == null || (map.get("user").equals("- ausgeloggt - "))) {
				this.alleSessions.remove(map);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void sessionAktualisieren(HttpSession insession) {
		boolean gefunden = false;
		this.aktuelleZeit = this.formatter.format(new Date());
		for (Iterator iter = this.alleSessions.iterator(); iter.hasNext();) {
			HashMap map = (HashMap) iter.next();
			if (map.get("id").equals(insession.getId())) {
				map.put("last", this.formatter.format(new Date()));
				map.put("last2", Long.valueOf(System.currentTimeMillis()));
				gefunden = true;
				break;
			}
		}
		if (!gefunden) {
			sessionAdd(insession);
		}
		sessionsAufraeumen(insession.getMaxInactiveInterval());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void sessionBenutzerAktualisieren(HttpSession insession, Benutzer inBenutzer) {
		// logger.debug("sessionBenutzerAktualisieren-start");
		for (Iterator iter = this.alleSessions.iterator(); iter.hasNext();) {
			HashMap map = (HashMap) iter.next();
			if (map.get("id").equals(insession.getId())) {
				if (inBenutzer != null) {
					insession.setAttribute("User", inBenutzer.getNachVorname());
					map.put("user", inBenutzer.getNachVorname());
					map.put("userid", inBenutzer.getId());
					insession.setMaxInactiveInterval(inBenutzer.getSessiontimeout());
				} else {
					map.put("user", "- ausgeloggt - ");
					map.put("userid", Integer.valueOf(0));
				}
				break;
			}
		}
	}

	/* prüfen, ob der Benutzer in einer anderen Session aktiv ist */
	@SuppressWarnings("rawtypes")
	public boolean BenutzerInAndererSessionAktiv(HttpSession insession, Benutzer inBenutzer) {
		boolean rueckgabe = false;
		for (Iterator iter = this.alleSessions.iterator(); iter.hasNext();) {
			HashMap map = (HashMap) iter.next();
			boolean sessiongleich = map.get("id").equals(insession.getId());
			boolean nutzergleich = inBenutzer.getId().intValue() == ((Integer) map.get("userid")).intValue();
			if (!sessiongleich && nutzergleich) {
				rueckgabe = true;
				break;
			}
		}
		return rueckgabe;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void alteSessionsDesSelbenBenutzersAufraeumen(HttpSession inSession, Benutzer inBenutzer) {
		List alleSessionKopie = new ArrayList(this.alleSessions);
		for (Iterator iter = alleSessionKopie.iterator(); iter.hasNext();) {
			HashMap map = (HashMap) iter.next();
			boolean sessiongleich = map.get("id").equals(inSession.getId());
			boolean nutzergleich = inBenutzer.getId().intValue() == ((Integer) map.get("userid")).intValue();
			if (!sessiongleich && nutzergleich) {
				HttpSession tempSession = (HttpSession) map.get("session");
				try {
					if (tempSession != null) {
						tempSession.invalidate();
					}
				} catch (RuntimeException e) {
				}
				this.alleSessions.remove(map);
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
