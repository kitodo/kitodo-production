package de.sub.goobi.Forms;

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
import org.hibernate.criterion.Expression;

import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Persistence.BenutzerDAO; //import de.sub.goobi.Persistence.HelperDAO;
import de.sub.goobi.Persistence.BaseDAO;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.Persistence.SchrittDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;

//TODO: Correct the comments

public class StatistikForm {
	private static final Logger myLogger = Logger.getLogger(StatistikForm.class);
	Calendar cal = new GregorianCalendar();
	int n = 200;

	

	/**
	 * @return Anzahl aller Literatureintr�ge
	 * @throws DAOException
	 */
	public Integer getAnzahlLiteraturGesamt() {
		return Integer.valueOf(0);
	}

	

	/**
	 * @return Anzahl der Benutzer
	 * @throws DAOException
	 */
	public Long getAnzahlBenutzer() {
		try {
			return new BenutzerDAO().count("from Benutzer");
		} catch (DAOException e) {

			new Helper().setFehlerMeldung("fehlerBeimEinlesen", e.getMessage());
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
			new Helper().setMeldung(null, "fehlerBeimEinlesen", e.getMessage());
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
			new Helper().setFehlerMeldung("fehlerBeimEinlesen", e.getMessage());
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
			new Helper().setFehlerMeldung("fehlerBeimEinlesen", e);
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
		n++;
		return new Random().nextInt(n);
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

	

	private int getAnzahlAktuelleSchritte(boolean inOffen, boolean inBearbeitet) {
		/* aktuellen Benutzer ermitteln */
		LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		if (login.getMyBenutzer() == null)
			return 0;

		try {
			//	  HibernateUtil.clearSession();
			Session session = Helper.getHibernateSession();
			Criteria crit = session.createCriteria(Schritt.class);

			/* Liste der IDs */
			List trefferListe = new ArrayList();

			/* --------------------------------
			 * die Treffer der Benutzergruppen
			 * --------------------------------*/
			Criteria critGruppen = session.createCriteria(Schritt.class);
			if (!inOffen && !inBearbeitet)
				critGruppen.add(Expression.or(Expression.eq("bearbeitungsstatus", Integer.valueOf(1)), Expression.like("bearbeitungsstatus", Integer
						.valueOf(2))));
			if (inOffen)
				critGruppen.add(Expression.eq("bearbeitungsstatus", Integer.valueOf(1)));
			if (inBearbeitet)
				critGruppen.add(Expression.eq("bearbeitungsstatus", Integer.valueOf(2)));

			/* nur Prozesse, die keine Vorlagen sind */
			critGruppen.createCriteria("prozess", "proz");
			critGruppen.add(Expression.eq("proz.istTemplate", Boolean.valueOf(false)));

			/* nur Schritte, wo Benutzergruppen des aktuellen Benutzers eingetragen sind */
			critGruppen.createCriteria("benutzergruppen", "gruppen").createCriteria("benutzer", "gruppennutzer");
			critGruppen.add(Expression.eq("gruppennutzer.id", login.getMyBenutzer().getId()));

			/* die Treffer sammeln */
			//TODO: Don't use Iterators
			for (Iterator iter = critGruppen.list().iterator(); iter.hasNext();) {
				Schritt step = (Schritt) iter.next();
				trefferListe.add(step.getId());
			}

			/* --------------------------------
			 * Treffer der Benutzer
			 * --------------------------------*/
			Criteria critBenutzer = session.createCriteria(Schritt.class);
			if (!inOffen && !inBearbeitet)
				critBenutzer.add(Expression.or(Expression.eq("bearbeitungsstatus", Integer.valueOf(1)), Expression.like("bearbeitungsstatus", Integer
						.valueOf(2))));
			if (inOffen)
				critBenutzer.add(Expression.eq("bearbeitungsstatus", Integer.valueOf(1)));
			if (inBearbeitet)
				critBenutzer.add(Expression.eq("bearbeitungsstatus", Integer.valueOf(2)));

			/* nur Prozesse, die keine Vorlagen sind */
			critBenutzer.createCriteria("prozess", "proz");
			critBenutzer.add(Expression.eq("proz.istTemplate", Boolean.valueOf(false)));

			/* nur Schritte, wo der aktuelle Benutzer eingetragen ist */
			critBenutzer.createCriteria("benutzer", "nutzer");
			critBenutzer.add(Expression.eq("nutzer.id", login.getMyBenutzer().getId()));

			/* die Treffer sammeln */
			//TODO: Don't use Iterators
			for (Iterator iter = critBenutzer.list().iterator(); iter.hasNext();) {
				Schritt step = (Schritt) iter.next();
				trefferListe.add(step.getId());
			}

			/* --------------------------------
			 * nun nur die Treffer übernehmen, die in der Liste sind
			 * --------------------------------*/
			crit.add(Expression.in("id", trefferListe));
			return crit.list().size();

		} catch (HibernateException he) {
			new Helper().setFehlerMeldung("fehlerBeimEinlesen", he.getMessage());
			return 0;
		}
	}
}
