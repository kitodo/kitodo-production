package de.sub.goobi.forms;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
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
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.File;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import de.sub.goobi.beans.Docket;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.DocketDAO;
import de.sub.goobi.persistence.apache.ProcessManager;

public class DocketForm extends BasisForm {
	private static final long serialVersionUID = -445707928042517243L;
	private Docket myDocket = new Docket();
	private DocketDAO dao = new DocketDAO();
	private static final Logger logger = Logger.getLogger(DocketForm.class);

	public String Neu() {
		this.myDocket = new Docket();
		return "DocketEdit";
	}

	public String Speichern() {
		try {
			if (hasValidRulesetFilePath(myDocket, ConfigMain.getParameter("xsltFolder"))) {
				this.dao.save(myDocket);
				return "DocketList";
			} else {
				Helper.setFehlerMeldung("DocketNotFound");
				return "";
			}
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
			logger.error(e);
			return "";
		}
	}

	private boolean hasValidRulesetFilePath(Docket d, String pathToRulesets) {
		File rulesetFile = new File(pathToRulesets + d.getFile());
		return rulesetFile.exists();
	}

	public String Loeschen() {
		try {
			if (hasAssignedProcesses(myDocket)) {
				Helper.setFehlerMeldung("DocketInUse");
				return "";
			} else {
				this.dao.remove(this.myDocket);
			}
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
			return "";
		}
		return "DocketList";
	}

	private boolean hasAssignedProcesses(Docket d) {
		Integer number = ProcessManager.getNumberOfProcessesWithDocket(d.getId());
		if (number != null && number > 0) {
			return true;
		}
		return false;
	}

	public String FilterKein() {
		try {
			// HibernateUtil.clearSession();
			Session session = Helper.getHibernateSession();
			// session.flush();
			session.clear();
			Criteria crit = session.createCriteria(Docket.class);
			crit.addOrder(Order.asc("name"));
			this.page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("fehlerBeimEinlesen", he.getMessage());
			return "";
		}
		return "DocketList";
	}

	public String FilterKeinMitZurueck() {
		FilterKein();
		return this.zurueck;
	}

	/*
	 * ##################################################### ##################################################### ## ## Getter und Setter ##
	 * ##################################################### ####################################################
	 */

	public Docket getMyDocket() {
		return this.myDocket;
	}

	public void setMyDocket(Docket docket) {
		Helper.getHibernateSession().clear();
		this.myDocket = docket;
	}
}
