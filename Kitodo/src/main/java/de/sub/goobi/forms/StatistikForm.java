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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.beans.Schritt;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.BenutzerDAO;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.persistence.SchrittDAO;

public class StatistikForm {
	private static final Logger myLogger = Logger.getLogger(StatistikForm.class);
	Calendar cal = new GregorianCalendar();
	int n = 200;

	/**
	 * @return Anzahl aller Literatureinträge
	 * @throws DAOException
	 */
	public Integer getAnzahlLiteraturGesamt() {
		return Integer.valueOf(0);
	}

	/**
	 * The function getAnzahlBenutzer() counts the number of user accounts in the goobi.production environment. Since user accounts are not hard
	 * deleted from the database when the delete button is pressed a where clause is used in the SQL statement to exclude the deleted accounts from
	 * the sum.
	 * 
	 * @return the count of valid user accounts
	 * @throws DAOException
	 *             if the current session can't be retrieved or an exception is thrown while performing the rollback.
	 */

	public Long getAnzahlBenutzer() {
		try {
			return new BenutzerDAO().count("from Benutzer where isVisible is null");
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerBeimEinlesen", e.getMessage());
			return null;
		}
	}

	/**
	 * @return Anzahl der Benutzer
	 * @throws DAOException
	 */
	public Long getAnzahlBenutzergruppen() {
		try {
			return new BenutzerDAO().count("from Benutzergruppe");
		} catch (DAOException e) {
			Helper.setMeldung(null, "fehlerBeimEinlesen", e.getMessage());
			return null;
		}
	}

	/**
	 * @return Anzahl der Benutzer
	 * @throws DAOException
	 */
	public Long getAnzahlProzesse() {
		try {
			return new ProzessDAO().count("from Prozess");
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerBeimEinlesen", e.getMessage());
			return null;
		}
	}

	/**
	 * @return Anzahl der Benutzer
	 * @throws DAOException
	 */
	public Long getAnzahlSchritte() {
		try {
			return new SchrittDAO().count("from Schritt");
		} catch (DAOException e) {
			myLogger.error("Hibernate error", e);
			Helper.setFehlerMeldung("fehlerBeimEinlesen", e);
			return Long.valueOf(-1);
		}
	}

	/**
	 * @return Anzahl der Benutzer
	 * @throws DAOException
	 */
	public Long getAnzahlVorlagen() {
		Session session = Helper.getHibernateSession();
		return (Long) session.createQuery("select count(*) " + "from Vorlage").uniqueResult();
	}

	/**
	 * @return Anzahl der Benutzer
	 * @throws DAOException
	 */
	public Long getAnzahlWerkstuecke() {
		Session session = Helper.getHibernateSession();
		return (Long) session.createQuery("select count(*) " + "from Werkstueck").uniqueResult();
	}

	/**
	 * @return Dummy-Rückgabe
	 * @throws DAOException
	 */
	public int getDummy() {
		this.n++;
		return new Random().nextInt(this.n);
	}

	public int getAnzahlAktuelleSchritte() {
		return getAnzahlAktuelleSchritte(false, false);
	}

	public int getAnzahlAktuelleSchritteOffen() {
		return getAnzahlAktuelleSchritte(true, false);
	}

	public int getAnzahlAktuelleSchritteBearbeitung() {
		return getAnzahlAktuelleSchritte(false, true);
	}

	@SuppressWarnings("unchecked")
	private int getAnzahlAktuelleSchritte(boolean inOffen, boolean inBearbeitet) {
		/* aktuellen Benutzer ermitteln */
		LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		if (login.getMyBenutzer() == null) {
			return 0;
		}

		try {
			Session session = Helper.getHibernateSession();
			Criteria crit = session.createCriteria(Schritt.class);

			/* Liste der IDs */
			List<Integer> trefferListe = new ArrayList<Integer>();

			/*
			 * -------------------------------- die Treffer der Benutzergruppen --------------------------------
			 */
			Criteria critGruppen = session.createCriteria(Schritt.class);
			if (!inOffen && !inBearbeitet) {
				critGruppen.add(Restrictions.or(Restrictions.eq("bearbeitungsstatus", Integer.valueOf(1)),
						Restrictions.like("bearbeitungsstatus", Integer.valueOf(2))));
			}
			if (inOffen) {
				critGruppen.add(Restrictions.eq("bearbeitungsstatus", Integer.valueOf(1)));
			}
			if (inBearbeitet) {
				critGruppen.add(Restrictions.eq("bearbeitungsstatus", Integer.valueOf(2)));
			}

			/* nur Prozesse, die keine Vorlagen sind */
			critGruppen.createCriteria("prozess", "proz");
			critGruppen.add(Restrictions.eq("proz.istTemplate", Boolean.FALSE));

			/* nur Schritte, wo Benutzergruppen des aktuellen Benutzers eingetragen sind */
			critGruppen.createCriteria("benutzergruppen", "gruppen").createCriteria("benutzer", "gruppennutzer");
			critGruppen.add(Restrictions.eq("gruppennutzer.id", login.getMyBenutzer().getId()));

			/* die Treffer sammeln */
			for (Iterator<Schritt> iter = critGruppen.list().iterator(); iter.hasNext();) {
				Schritt step = iter.next();
				trefferListe.add(step.getId());
			}

			/*
			 * -------------------------------- Treffer der Benutzer --------------------------------
			 */
			Criteria critBenutzer = session.createCriteria(Schritt.class);
			if (!inOffen && !inBearbeitet) {
				critBenutzer.add(Restrictions.or(Restrictions.eq("bearbeitungsstatus", Integer.valueOf(1)),
						Restrictions.like("bearbeitungsstatus", Integer.valueOf(2))));
			}
			if (inOffen) {
				critBenutzer.add(Restrictions.eq("bearbeitungsstatus", Integer.valueOf(1)));
			}
			if (inBearbeitet) {
				critBenutzer.add(Restrictions.eq("bearbeitungsstatus", Integer.valueOf(2)));
			}

			/* nur Prozesse, die keine Vorlagen sind */
			critBenutzer.createCriteria("prozess", "proz");
			critBenutzer.add(Restrictions.eq("proz.istTemplate", Boolean.FALSE));

			/* nur Schritte, wo der aktuelle Benutzer eingetragen ist */
			critBenutzer.createCriteria("benutzer", "nutzer");
			critBenutzer.add(Restrictions.eq("nutzer.id", login.getMyBenutzer().getId()));

			/* die Treffer sammeln */
			for (Iterator<Schritt> iter = critBenutzer.list().iterator(); iter.hasNext();) {
				Schritt step = iter.next();
				trefferListe.add(step.getId());
			}

			/*
			 * -------------------------------- nun nur die Treffer übernehmen, die in der Liste sind --------------------------------
			 */
			crit.add(Restrictions.in("id", trefferListe));
			return crit.list().size();

		} catch (HibernateException he) {
			Helper.setFehlerMeldung("fehlerBeimEinlesen", he.getMessage());
			return 0;
		}
	}

	public boolean getShowStatistics() {
		return ConfigMain.getBooleanParameter("showStatisticsOnStartPage", true);
	}
}
