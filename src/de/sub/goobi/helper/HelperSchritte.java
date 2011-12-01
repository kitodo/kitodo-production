package de.sub.goobi.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import ugh.dl.DigitalDocument;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.HistoryEvent;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.Persistence.SchrittDAO;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;

//TODO: Check if some methods can declared static
public class HelperSchritte {
	SchrittDAO dao = new SchrittDAO();
	// Helper help = new Helper();
	private static final Logger logger = Logger.getLogger(HelperSchritte.class);

	public final static String DIRECTORY_PREFIX = "orig_";

	/**
	 * Schritt abschliessen und dabei parallele Schritte berücksichtigen ================================================================
	 */
	@SuppressWarnings("unchecked")
	public void SchrittAbschliessen(Schritt inSchritt, boolean automatic) {
		inSchritt.setBearbeitungsstatusEnum(StepStatus.DONE);
		HelperSchritte.updateEditing(inSchritt);
		Date myDate = new Date();
		inSchritt.setBearbeitungsende(myDate);
		List<Schritt> automatischeSchritte = new ArrayList<Schritt>();

		inSchritt.getProzess().getHistory().add(
				new HistoryEvent(myDate, inSchritt.getReihenfolge().doubleValue(), inSchritt.getTitel(), HistoryEventType.stepDone, inSchritt
						.getProzess()));
		Session session = Helper.getHibernateSession();
		/* prüfen, ob es Schritte gibt, die parallel stattfinden aber noch nicht abgeschlossen sind */
		int offeneSchritteGleicherReihenfolge = session.createCriteria(Schritt.class).add(Restrictions.eq("reihenfolge", inSchritt.getReihenfolge()))
				.add(Restrictions.ne("bearbeitungsstatus", 3)).add(Restrictions.ne("id", inSchritt.getId())).createCriteria("prozess").add(
						Restrictions.idEq(inSchritt.getProzess().getId())).list().size();

		// if (offeneSchritteGleicherReihenfolge != 0) {
		// List bla = Helper.getHibernateSession().createCriteria(Schritt.class).add(
		// Restrictions.eq("reihenfolge", inSchritt.getReihenfolge())).add(
		// Restrictions.ne("bearbeitungsstatus", 3)).add(Restrictions.ne("id", inSchritt.getId()))
		// .createCriteria("prozess").add(Restrictions.idEq(inSchritt.getProzess().getId())).list();
		// for (Iterator it = bla.iterator(); it.hasNext();) {
		// Schritt s = (Schritt) it.next();
		// }
		// }

		/* wenn keine offenen parallelschritte vorhanden sind, die nächsten Schritte aktivieren */
		if (offeneSchritteGleicherReihenfolge == 0) {

			List<Schritt> allehoeherenSchritte = session.createCriteria(Schritt.class)
					.add(Restrictions.gt("reihenfolge", inSchritt.getReihenfolge())).addOrder(Order.asc("reihenfolge")).createCriteria("prozess")
					.add(Restrictions.idEq(inSchritt.getProzess().getId())).list();
			int reihenfolge = 0;
			// TODO: Don't use iterators, use for loops instead
			for (Iterator<Schritt> iter = allehoeherenSchritte.iterator(); iter.hasNext();) {
				Schritt myStep = iter.next();
				if (reihenfolge == 0)
					reihenfolge = myStep.getReihenfolge().intValue();

				if (reihenfolge == myStep.getReihenfolge().intValue() && myStep.getBearbeitungsstatusEnum() == StepStatus.LOCKED) {
					/*
					 * den Schritt aktivieren, wenn es kein vollautomatischer ist
					 */

					myStep.setBearbeitungsstatusEnum(StepStatus.OPEN);
					myStep.setBearbeitungszeitpunkt(myDate);
					myStep.setEditTypeEnum(StepEditType.AUTOMATIC);

					myStep.getProzess().getHistory().add(
							new HistoryEvent(myDate, myStep.getReihenfolge().doubleValue(), myStep.getTitel(), HistoryEventType.stepOpen, myStep
									.getProzess()));
					/* wenn es ein automatischer Schritt mit Script ist */
					if (myStep.isTypAutomatisch() && !myStep.getAllScriptPaths().isEmpty()) {
						automatischeSchritte.add(myStep);
					}
					System.out.println("opened: " + myStep.getTitel());
				} else
					break;
			}
		}

		try {
			/* den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird */
			new ProzessDAO().save(inSchritt.getProzess());
			session.evict(inSchritt.getProzess());
		} catch (DAOException e) {
		}

		if (automatic) {
			try {
				session.close();
			} catch (Exception e) {
			}
		}
		/*
		 * -------------------------------- zum Schluss alle automatischen Schritte nehmen und deren Automatik ausführen
		 * --------------------------------
		 */
		// TODO: Don't use iterators, use for loops instead
		for (Iterator<Schritt> iter = automatischeSchritte.iterator(); iter.hasNext();) {
			Schritt myStep = iter.next();
			ScriptThread myThread = new ScriptThread(myStep);
			myThread.start();
		}
	}

	/**
	 * alle Scripte ausführen
	 * 
	 */
	public void executeAllScripts(Schritt mySchritt, boolean fullautomatic) throws SwapException, DAOException {
		ArrayList<String> paths = mySchritt.getAllScriptPaths();
		ArrayList<String> scriptpaths = new ArrayList<String>();
		int count = 1;
		for (String script : paths) {
			if (script == null || script.equals(" ") || script.length() != 0) {
				scriptpaths.add(script);
			}
		}
		if (scriptpaths.size() != 0) {
			int size = scriptpaths.size();
			for (String script : scriptpaths) {
				if (script != null && !script.equals(" ") && script.length() != 0) {
					if (fullautomatic && (count == size)) {
						executeScript(mySchritt, script, true);
					} else {
						executeScript(mySchritt, script, false);
					}
				}
				count++;
			}
		}
	}

	/**
	 * Script des Schrittes ausführen ================================================================
	 * 
	 * @throws DAOException
	 * @throws SwapException
	 */
	public void executeScript(Schritt mySchritt, String script, boolean fullautomatic) throws SwapException {

		if (script == null || script.length() == 0)
			return;

		if (script.equals("copyOrig"))
			try {
				copyOrig(mySchritt, fullautomatic);
			} catch (DAOException e1) {
				logger.error(e1);
			}
		else {
			script = script.replace("{", "(").replace("}", ")");
			DigitalDocument dd = null;
			try {
				dd = mySchritt.getProzess().readMetadataFile().getDigitalDocument();
			} catch (PreferencesException e2) {
				logger.error(e2);
			} catch (ReadException e2) {
				logger.error(e2);
			} catch (WriteException e2) {
				logger.error(e2);
			} catch (IOException e2) {
				logger.error(e2);
			} catch (InterruptedException e2) {
				logger.error(e2);
			} catch (Exception e2) {
				logger.error(e2);
			}
			VariableReplacer replacer = new VariableReplacer(dd, mySchritt.getProzess().getRegelsatz().getPreferences(), mySchritt.getProzess(),
					mySchritt);
			script = replacer.replace(script);
			String stepId = String.valueOf(mySchritt.getId());
			script = script.replace("(stepid)", stepId);

			try {
				logger.info("Calling the shell: " + script);
				int rueckgabe = Helper.callShell2(script);
				if (fullautomatic) {
					if (rueckgabe == 0)
						closeStep(mySchritt, fullautomatic);
					else
						abortStep(mySchritt, fullautomatic);
				}
			} catch (IOException e) {
				Helper.setFehlerMeldung("IOException: ", e.getMessage());
			} catch (InterruptedException e) {
				Helper.setFehlerMeldung("InterruptedException: ", e.getMessage());
			}

		}

	}

	// TODO: Make this more generic, prefix should be configurable, this should be a workflow step
	/**
	 * von dem Images-Ordner eine Kopie machen und als Orig-Ordner speichern ================================================================
	 * 
	 * @throws DAOException
	 * @throws SwapException
	 */
	private void copyOrig(Schritt mySchritt, boolean fullautomatic) throws SwapException, DAOException {
		/* Aktion ausführen, bei Fehlern auf offen setzen */
		try {
			// File ausgang = new File(mySchritt.getProzess().getTifPfad());
			File ausgang = new File(mySchritt.getProzess().getImagesDirectory());
			File ziel = new File(ausgang.getParent() + File.separator + DIRECTORY_PREFIX + ausgang.getName());
			CopyFile.copyDirectory(ausgang, ziel);
		} catch (IOException e) {
			Helper.setFehlerMeldung("IOException: ", e.getMessage());
			if (fullautomatic)
				abortStep(mySchritt, true);
			return;
		} catch (InterruptedException e) {
			Helper.setFehlerMeldung("InterruptedException: ", e.getMessage());
			if (fullautomatic)
				abortStep(mySchritt, false);
			return;
		}
		if (fullautomatic) {
			closeStep(mySchritt, true);
		}
	}

	/**
	 * den Schritt abbrechen und zurück auf "offen" setzen only called by fullautomatic steps
	 * ================================================================
	 */
	private void abortStep(Schritt mySchritt, boolean automatic) {
		// try {
		// /* bei einem Fehler den Schritt zurücksetzen */
		// Schritt temp = dao.get(mySchritt.getId());
		// temp.setEditTypeEnum(StepEditType.AUTOMATIC);
		// temp.setBearbeitungsstatusEnum(StepStatus.OPEN);
		// dao.save(temp);
		// } catch (DAOException d) {
		// d.printStackTrace();
		// }
		mySchritt.setEditTypeEnum(StepEditType.AUTOMATIC);
		mySchritt.setBearbeitungsstatusEnum(StepStatus.OPEN);
		try {
			dao.save(mySchritt);
		} catch (DAOException e) {
			logger.error(e);
		}
	}

	/**
	 * den Schritt erfolgreich beenden only called by fullautomatic steps ================================================================
	 */
	private void closeStep(Schritt mySchritt, boolean automatic) {
		try {
			Schritt temp = dao.get(mySchritt.getId());
			temp.setEditTypeEnum(StepEditType.AUTOMATIC);
			temp.setBearbeitungsstatusEnum(StepStatus.DONE);
			dao.save(temp);
			SchrittAbschliessen(temp, automatic);
		} catch (DAOException e) {
			logger.error(e);
		}
		mySchritt.setEditTypeEnum(StepEditType.AUTOMATIC);
		// mySchritt.setBearbeitungsstatusEnum(StepStatus.DONE);
		SchrittAbschliessen(mySchritt, automatic);
	}

	/**
	 * set values of given step:
	 * 
	 * editing user to currently logged in user and date to current date
	 */
	public static void updateEditing(Schritt mySchritt) {
		mySchritt.setBearbeitungszeitpunkt(new Date());
		Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
		if (ben != null)
			mySchritt.setBearbeitungsbenutzer(ben);

	}
}
