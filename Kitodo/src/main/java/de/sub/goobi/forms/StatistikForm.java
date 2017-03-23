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

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;

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
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

public class StatistikForm {
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger myLogger = Logger.getLogger(StatistikForm.class);
    Calendar cal = new GregorianCalendar();
    int n = 200;

    /**
     * Get amount list literature together.
     *
     * @return Anzahl aller Literatureinträge Integer
     */
    public Integer getAnzahlLiteraturGesamt() {
        return Integer.valueOf(0);
    }

    /**
     * The function getAnzahlBenutzer() counts the number of user accounts in
     * the goobi.production environment. Since user accounts are not hard
     * deleted from the database when the delete button is pressed a where
     * clause is used in the SQL statement to exclude the deleted accounts from
     * the sum.
     *
     * @return the count of valid user accounts
     * @throws DAOException
     *             if the current session can't be retrieved or an exception is
     *             thrown while performing the rollback.
     */

    public Long getAnzahlBenutzer() {
        try {
            return serviceManager.getUserService().count("from User where visible is null");
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerBeimEinlesen", e.getMessage());
            return null;
        }
    }

    /**
     * Get amount of user groups.
     *
     * @return Anzahl der Benutzer
     */
    public Long getAnzahlBenutzergruppen() {
        try {
            return serviceManager.getUserService().count("from UserGroup");
        } catch (DAOException e) {
            Helper.setMeldung(null, "fehlerBeimEinlesen", e.getMessage());
            return null;
        }
    }

    /**
     * Get amount of processes.
     *
     * @return amount of processes
     */
    public Long getAnzahlProzesse() {
        try {
            return serviceManager.getProcessService().count("from Process");
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerBeimEinlesen", e.getMessage());
            return null;
        }
    }

    /**
     * Get amount of tasks.
     *
     * @return amount of tasks
     */
    public Long getAnzahlSchritte() {
        try {
            return serviceManager.getTaskService().count("from Task");
        } catch (DAOException e) {
            myLogger.error("Hibernate error", e);
            Helper.setFehlerMeldung("fehlerBeimEinlesen", e);
            return Long.valueOf(-1);
        }
    }

    /**
     * Get amount of templates.
     *
     * @return amount of templates
     */
    public Long getAnzahlVorlagen() {
        Session session = Helper.getHibernateSession();
        return (Long) session.createQuery("select count(*) " + "from Template").uniqueResult();
    }

    /**
     * Get amount of workpieces.
     *
     * @return amount of workpieces
     */
    public Long getAnzahlWerkstuecke() {
        Session session = Helper.getHibernateSession();
        return (Long) session.createQuery("select count(*) " + "from Workpiece").uniqueResult();
    }

    /**
     * Get dummy.
     *
     * @return Dummy-Rückgabe
     */
    public int getDummy() {
        this.n++;
        return new Random().nextInt(this.n);
    }

    public int getAnzahlAktuelleSchritte() {
        return getAnzahlAktuelleSchritte(false, false);
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
            Criteria crit = session.createCriteria(Task.class);

            /* Liste der IDs */
            List<Integer> trefferListe = new ArrayList<Integer>();

            /*
             * die Treffer der Benutzergruppen
             */
            Criteria critGruppen = session.createCriteria(Task.class);
            if (!inOffen && !inBearbeitet) {
                critGruppen.add(Restrictions.or(Restrictions.eq("processingStatus", Integer.valueOf(1)),
                        Restrictions.like("processingStatus", Integer.valueOf(2))));
            }
            if (inOffen) {
                critGruppen.add(Restrictions.eq("processingStatus", Integer.valueOf(1)));
            }
            if (inBearbeitet) {
                critGruppen.add(Restrictions.eq("processingStatus", Integer.valueOf(2)));
            }

            /* nur Prozesse, die keine Vorlagen sind */
            critGruppen.createCriteria("process", "proz");
            critGruppen.add(Restrictions.eq("proz.template", Boolean.FALSE));

            /*
             * nur Schritte, wo Benutzergruppen des aktuellen Benutzers
             * eingetragen sind
             */
            critGruppen.createCriteria("userGroups", "gruppen").createCriteria("users", "gruppennutzer");
            critGruppen.add(Restrictions.eq("gruppennutzer.id", login.getMyBenutzer().getId()));

            /* die Treffer sammeln */
            for (Iterator<Task> iter = critGruppen.list().iterator(); iter.hasNext();) {
                Task step = iter.next();
                trefferListe.add(step.getId());
            }

            /*
             * Treffer der Benutzer
             */
            Criteria critBenutzer = session.createCriteria(Task.class);
            if (!inOffen && !inBearbeitet) {
                critBenutzer.add(Restrictions.or(Restrictions.eq("processingStatus", Integer.valueOf(1)),
                        Restrictions.like("processingStatus", Integer.valueOf(2))));
            }
            if (inOffen) {
                critBenutzer.add(Restrictions.eq("processingStatus", Integer.valueOf(1)));
            }
            if (inBearbeitet) {
                critBenutzer.add(Restrictions.eq("processingStatus", Integer.valueOf(2)));
            }

            /* nur Prozesse, die keine Vorlagen sind */
            critBenutzer.createCriteria("process", "proz");
            critBenutzer.add(Restrictions.eq("proz.template", Boolean.FALSE));

            /* nur Schritte, wo der aktuelle Benutzer eingetragen ist */
            critBenutzer.createCriteria("user", "nutzer");
            critBenutzer.add(Restrictions.eq("nutzer.id", login.getMyBenutzer().getId()));

            /* die Treffer sammeln */
            for (Iterator<Task> iter = critBenutzer.list().iterator(); iter.hasNext();) {
                Task step = iter.next();
                trefferListe.add(step.getId());
            }

            /*
             * nun nur die Treffer übernehmen, die in der Liste sind
             */
            crit.add(Restrictions.in("id", trefferListe));
            return crit.list().size();

        } catch (HibernateException he) {
            Helper.setFehlerMeldung("fehlerBeimEinlesen", he.getMessage());
            return 0;
        }
    }

    public int getAnzahlAktuelleSchritteOffen() {
        return getAnzahlAktuelleSchritte(true, false);
    }

    public int getAnzahlAktuelleSchritteBearbeitung() {
        return getAnzahlAktuelleSchritte(false, true);
    }

    public boolean getShowStatistics() {
        return ConfigMain.getBooleanParameter("showStatisticsOnStartPage", true);
    }
}
