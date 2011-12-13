package de.sub.goobi.helper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;

import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Regelsatz;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.Export.dms.ExportDms;
import de.sub.goobi.Persistence.BenutzerDAO;
import de.sub.goobi.Persistence.BenutzergruppenDAO;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.Persistence.RegelsatzDAO;
import de.sub.goobi.Persistence.SchrittDAO;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.helper.tasks.LongRunningTaskManager;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.helper.tasks.ProcessSwapOutTask;
import de.sub.goobi.helper.tasks.TiffWriterTask;

//TODO: Delete me, this should be part of the Plugins...
//TODO: Break this up into multiple classes with a common interface
//TODO: add funny observer pattern here for more complexity
//TODO: add some general mechanism for string-output of goobi scripts in jsp

public class GoobiScript {
	HashMap<String, String> myParameters;
	private static final Logger logger = Logger.getLogger(GoobiScript.class);
	public final static String DIRECTORY_SUFFIX = "_tif";

	/**
	 * Starten des Scripts ================================================================
	 */
	public void execute(List<Prozess> inProzesse, String inScript) {
		myParameters = new HashMap<String, String>();
		/*
		 * -------------------------------- alle Suchparameter zerlegen und erfassen --------------------------------
		 */
		StrTokenizer tokenizer = new StrTokenizer(inScript, ' ', '\"');
		while (tokenizer.hasNext()) {
			String tok = tokenizer.nextToken();
			if (tok.indexOf(":") == -1)
				Helper.setFehlerMeldung("goobiScriptfield", "missing delimiter / unknown parameter: ", tok);
			else {
				String myKey = tok.substring(0, tok.indexOf(":"));
				String myValue = tok.substring(tok.indexOf(":") + 1);
				myParameters.put(myKey, myValue);
			}
		}

		/*
		 * -------------------------------- die passende Methode mit den richtigen Parametern übergeben --------------------------------
		 */
		if (myParameters.get("action") == null) {
			Helper
					.setFehlerMeldung(
							"goobiScriptfield",
							"missing action",
							" - possible: 'action:swapsteps, action:adduser, action:addusergroup, action:swapprozessesout, action:swapprozessesin, action:deleteTiffHeaderFile, action:importFromFileSystem'");
			return;
		}

		/*
		 * -------------------------------- Aufruf der richtigen Methode über den Parameter --------------------------------
		 */
		if (myParameters.get("action").equals("swapSteps")) {
			swapSteps(inProzesse);
		} else if (myParameters.get("action").equals("swapProzessesOut")) {
			swapOutProzesses(inProzesse);
		} else if (myParameters.get("action").equals("swapProzessesIn")) {
			swapInProzesses(inProzesse);
		} else if (myParameters.get("action").equals("importFromFileSystem")) {
			importFromFileSystem(inProzesse);
		} else if (myParameters.get("action").equals("addUser")) {
			adduser(inProzesse);
		} else if (myParameters.get("action").equals("tiffWriter")) {
			writeTiffHeader(inProzesse);
		} else if (myParameters.get("action").equals("addUserGroup")) {
			addusergroup(inProzesse);
		} else if (myParameters.get("action").equals("setTaskProperty")) {
			setTaskProperty(inProzesse);
		} else if (myParameters.get("action").equals("deleteStep")) {
			deleteStep(inProzesse);
		} else if (myParameters.get("action").equals("addStep")) {
			addStep(inProzesse);
		} else if (myParameters.get("action").equals("setStepNumber")) {
			setStepNumber(inProzesse);
		} else if (myParameters.get("action").equals("setStepStatus")) {
			setStepStatus(inProzesse);
		} else if (myParameters.get("action").equals("addShellScriptToStep")) {
			addShellScriptToStep(inProzesse);
		} else if (myParameters.get("action").equals("addModuleToStep")) {
			addModuleToStep(inProzesse);
		} else if (myParameters.get("action").equals("updateImagePath")) {
			updateImagePath(inProzesse);
		} else if (myParameters.get("action").equals("deleteTiffHeaderFile")) {
			deleteTiffHeaderFile(inProzesse);
		} else if (myParameters.get("action").equals("setRuleset")) {
			setRuleset(inProzesse);
		} else if (myParameters.get("action").equals("exportDms")) {
			exportDms(inProzesse, myParameters.get("exportImages"));
		} else if (myParameters.get("action").equals("doit")) { 
			exportDms(inProzesse, "false");
		}
		else {
			Helper
					.setFehlerMeldung(
							"goobiScriptfield",
							"Unbekannte Action",
							" - möglich: 'action:swapsteps, action:adduser, action:addusergroup, action:swapprozessesout, action:swapprozessesin, action:deleteTiffHeaderFile, action:importFromFileSystem'");
			return;
		}

		// /* --------------------------------
		// * Aufruf der richtigen Action-Methode über Reflektion
		// * --------------------------------*/
		// try {
		// String trallala = (String) myParameters.get("action");
		// Method method = this.getClass().getMethod(trallala, new Class[] { List.class });
		// method.invoke(this, new Object[] { inProzesse });
		// } catch (SecurityException e) {
		// e.printStackTrace();
		// } catch (NoSuchMethodException e) {
		// e.printStackTrace();
		// } catch (IllegalArgumentException e) {
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// e.printStackTrace();
		// } catch (InvocationTargetException e) {
		// e.printStackTrace();
		// }

		Helper.setMeldung("goobiScriptfield", "", "GoobiScript beendet");
	}

	/**
	 * Prozesse auslagern ================================================================
	 */
	private void swapOutProzesses(List<Prozess> inProzesse) {
		for (Prozess p : inProzesse) {

			ProcessSwapOutTask task = new ProcessSwapOutTask();
			task.initialize(p);
			LongRunningTaskManager.getInstance().addTask(task);

			// try {
			// ProcessSwapper ps = ProcessSwapper.getInstance();
			// ps.swapOut(p);
			// } catch (Exception e) {
			// Helper.setFehlerMeldung("Fehler bei Auslagerung", e);
			// break;
			// }
		}
	}

	/**
	 * Prozesse wieder einlagern ================================================================
	 */
	private void swapInProzesses(List<Prozess> inProzesse) {
		for (Prozess p : inProzesse) {

			ProcessSwapInTask task = new ProcessSwapInTask();
			task.initialize(p);
			LongRunningTaskManager.getInstance().addTask(task);

			// try {
			// ProcessSwapper ps = ProcessSwapper.getInstance();
			// ps.swapIn(p);
			// } catch (Exception e) {
			// Helper.setFehlerMeldung("Fehler bei Einlagerung", e);
			// break;
			// }
		}
	}

	/**
	 * voll allen gewählten Prozessen die Daten aus einem Verzeichnis einspielen ================================================================
	 */
	private void importFromFileSystem(List<Prozess> inProzesse) {
		/*
		 * -------------------------------- Validierung der Actionparameter --------------------------------
		 */
		if (myParameters.get("sourcefolder") == null || myParameters.get("sourcefolder").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "missing parameter: ", "sourcefolder");
			return;
		}

		File sourceFolder = new File(myParameters.get("sourcefolder"));
		if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
			Helper.setFehlerMeldung("goobiScriptfield", "Directory " + myParameters.get("sourcefolder") + " does not exisist");
			return;
		}
		try {

			for (Prozess p : inProzesse) {
				File imagesFolder = new File(p.getImagesOrigDirectory());
				if (imagesFolder.list().length > 0)
					Helper.setFehlerMeldung("goobiScriptfield", "", "The process " + p.getTitel() + " [" + p.getId().intValue()
							+ "] has allready data in image folder");
				else {
					File sourceFolderProzess = new File(sourceFolder, p.getTitel());
					if (!sourceFolderProzess.exists() || !sourceFolder.isDirectory()) {
						Helper.setFehlerMeldung("goobiScriptfield", "", "The directory for process " + p.getTitel() + " [" + p.getId().intValue()
								+ "] is not existing");
					} else {
						CopyFile.copyDirectory(sourceFolderProzess, imagesFolder);
						Helper.setMeldung("goobiScriptfield", "", "The directory for process " + p.getTitel() + " [" + p.getId().intValue()
								+ "] is copied");
					}
					Helper.setMeldung("goobiScriptfield", "", "The process " + p.getTitel() + " [" + p.getId().intValue() + "] is copied");
				}
			}
		} catch (Exception e) {
			Helper.setFehlerMeldung(e);
			logger.error(e);
		}
	}

	/**
	 * Regelsatz setzen ================================================================
	 */
	private void setRuleset(List<Prozess> inProzesse) {
		/*
		 * -------------------------------- Validierung der Actionparameter --------------------------------
		 */
		if (myParameters.get("ruleset") == null || myParameters.get("ruleset").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "ruleset");
			return;
		}

		try {
			RegelsatzDAO rdao = new RegelsatzDAO();
			ProzessDAO pdao = new ProzessDAO();
			List<Regelsatz> rulesets = rdao.search("from Regelsatz where titel='" + myParameters.get("ruleset") + "'");
			if (rulesets == null || rulesets.size() == 0) {
				Helper.setFehlerMeldung("goobiScriptfield", "Could not found ruleset: ", "ruleset");
				return;
			}
			Regelsatz regelsatz = (Regelsatz) rulesets.get(0);

			for (Prozess p : inProzesse) {
				p.setRegelsatz(regelsatz);
				pdao.save(p);
			}
		} catch (Exception e) {
			Helper.setFehlerMeldung(e);
			logger.error(e);
		}
	}

	/**
	 * Tauschen zweier Schritte gegeneinander ================================================================
	 */
	private void swapSteps(List<Prozess> inProzesse) {
		/*
		 * -------------------------------- Validierung der Actionparameter --------------------------------
		 */
		if (myParameters.get("swap1nr") == null || myParameters.get("swap1nr").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap1nr");
			return;
		}
		if (myParameters.get("swap2nr") == null || myParameters.get("swap2nr").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap2nr");
			return;
		}
		if (myParameters.get("swap1title") == null || myParameters.get("swap1title").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap1title");
			return;
		}
		if (myParameters.get("swap2title") == null || myParameters.get("swap2title").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap2title");
			return;
		}
		int reihenfolge1;
		int reihenfolge2;
		try {
			reihenfolge1 = Integer.parseInt(myParameters.get("swap1nr"));
			reihenfolge2 = Integer.parseInt(myParameters.get("swap2nr"));
		} catch (NumberFormatException e1) {
			Helper.setFehlerMeldung("goobiScriptfield", "Invalid order number used: ", myParameters.get("swap1nr") + " - "
					+ myParameters.get("swap2nr"));
			return;
		}

		/*
		 * -------------------------------- Durchführung der Action --------------------------------
		 */
		SchrittDAO sdao = new SchrittDAO();
		for (Prozess proz : inProzesse) {
			/*
			 * -------------------------------- Swapsteps --------------------------------
			 */
			Schritt s1 = null;
			Schritt s2 = null;
			for (Iterator<Schritt> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
				Schritt s = (Schritt) iterator.next();
				if (s.getTitel().equals(myParameters.get("swap1title")) && s.getReihenfolge().intValue() == reihenfolge1)
					s1 = s;
				if (s.getTitel().equals(myParameters.get("swap2title")) && s.getReihenfolge().intValue() == reihenfolge2)
					s2 = s;
			}
			if (s1 != null && s2 != null) {
				StepStatus statustemp = s1.getBearbeitungsstatusEnum();
				s1.setBearbeitungsstatusEnum(s2.getBearbeitungsstatusEnum());
				s2.setBearbeitungsstatusEnum(statustemp);
				s1.setReihenfolge(Integer.valueOf(reihenfolge2));
				s2.setReihenfolge(Integer.valueOf(reihenfolge1));
				try {
					sdao.save(s1);
					sdao.save(s2);
				} catch (DAOException e) {
					Helper.setFehlerMeldung("goobiScriptfield", "Error on save while swapping steps in process: ", proz.getTitel() + " - "
							+ s1.getTitel() + " : " + s2.getTitel());
					logger.error("Error on save while swapping process: " + proz.getTitel() + " - " + s1.getTitel() + " : " + s2.getTitel(), e);
				}

				Helper.setMeldung("goobiScriptfield", "Swapped steps in: ", proz.getTitel());
			}

		}
		Helper.setMeldung("goobiScriptfield", "swapsteps finished: ");
	}

	/**
	 * Schritte löschen ================================================================
	 */
	private void deleteStep(List<Prozess> inProzesse) {
		/*
		 * -------------------------------- Validierung der Actionparameter --------------------------------
		 */
		if (myParameters.get("steptitle") == null || myParameters.get("steptitle").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
			return;
		}

		/*
		 * -------------------------------- Durchführung der Action --------------------------------
		 */
		ProzessDAO sdao = new ProzessDAO();
		for (Prozess proz : inProzesse) {
			if (proz.getSchritte() != null) {
				for (Iterator<Schritt> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
					Schritt s = (Schritt) iterator.next();
					if (s.getTitel().equals(myParameters.get("steptitle"))) {
						proz.getSchritte().remove(s);
						try {
							sdao.save(proz);
						} catch (DAOException e) {
							Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
							logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
						}
						Helper.setMeldung("goobiScriptfield", "Removed step from process: ", proz.getTitel());
						break;
					}
				}
			}
		}
		Helper.setMeldung("goobiScriptfield", "deleteStep finished: ");
	}

	/**
	 * Schritte hinzufuegen ================================================================
	 */
	private void addStep(List<Prozess> inProzesse) {
		/*
		 * -------------------------------- Validierung der Actionparameter --------------------------------
		 */
		if (myParameters.get("steptitle") == null || myParameters.get("steptitle").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
			return;
		}
		if (myParameters.get("number") == null || myParameters.get("number").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "number");
			return;
		}

		if (!StringUtils.isNumeric(myParameters.get("number"))) {
			Helper.setFehlerMeldung("goobiScriptfield", "Wrong number parameter", "(only numbers allowed)");
			return;
		}

		/*
		 * -------------------------------- Durchführung der Action --------------------------------
		 */
		ProzessDAO sdao = new ProzessDAO();
		for (Prozess proz : inProzesse) {
			Schritt s = new Schritt();
			s.setTitel(myParameters.get("steptitle"));
			s.setReihenfolge(Integer.parseInt(myParameters.get("number")));
			s.setProzess(proz);
			if (proz.getSchritte() == null) {
				proz.setSchritte(new HashSet<Schritt>());
			}
			proz.getSchritte().add(s);
			try {
				sdao.save(proz);
			} catch (DAOException e) {
				Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
				logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
			}
			Helper.setMeldung("goobiScriptfield", "Added step to process: ", proz.getTitel());
		}
		Helper.setMeldung("goobiScriptfield", "addStep finished: ");
	}

	/**
	 * ShellScript an Schritt hängen ================================================================
	 */
	private void addShellScriptToStep(List<Prozess> inProzesse) {
		/*
		 * -------------------------------- Validierung der Actionparameter --------------------------------
		 */
		if (myParameters.get("steptitle") == null || myParameters.get("steptitle").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "steptitle");
			return;
		}

		if (myParameters.get("script") == null || myParameters.get("script").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "script");
			return;
		}

		/*
		 * -------------------------------- Durchführung der Action --------------------------------
		 */
		ProzessDAO sdao = new ProzessDAO();
		for (Prozess proz : inProzesse) {
			if (proz.getSchritte() != null) {
				for (Iterator<Schritt> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
					Schritt s = (Schritt) iterator.next();
					if (s.getTitel().equals(myParameters.get("steptitle"))) {
						s.setTypAutomatischScriptpfad(myParameters.get("script"));
						try {
							sdao.save(proz);
						} catch (DAOException e) {
							Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
							logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
						}
						Helper.setMeldung("goobiScriptfield", "Added script to step: ", proz.getTitel());
						break;
					}
				}
			}
		}
		Helper.setMeldung("goobiScriptfield", "addShellScriptToStep finished: ");
	}

	/**
	 * ShellScript an Schritt hängen ================================================================
	 */
	private void addModuleToStep(List<Prozess> inProzesse) {
		/*
		 * -------------------------------- Validierung der Actionparameter --------------------------------
		 */
		if (myParameters.get("steptitle") == null || myParameters.get("steptitle").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
			return;
		}

		if (myParameters.get("module") == null || myParameters.get("module").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "module");
			return;
		}

		/*
		 * -------------------------------- Durchführung der Action --------------------------------
		 */
		ProzessDAO sdao = new ProzessDAO();
		for (Prozess proz : inProzesse) {
			if (proz.getSchritte() != null) {
				for (Iterator<Schritt> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
					Schritt s = (Schritt) iterator.next();
					if (s.getTitel().equals(myParameters.get("steptitle"))) {
						s.setTypModulName(myParameters.get("module"));
						try {
							sdao.save(proz);
						} catch (DAOException e) {
							Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
							logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
						}
						Helper.setMeldung("goobiScriptfield", "Added module to step: ", proz.getTitel());
						break;
					}
				}
			}
		}
		Helper.setMeldung("goobiScriptfield", "addModuleToStep finished: ");
	}

	/**
	 * Flag von Schritten setzen ================================================================
	 */
	private void setTaskProperty(List<Prozess> inProzesse) {
		/*
		 * -------------------------------- Validierung der Actionparameter --------------------------------
		 */
		if (myParameters.get("steptitle") == null || myParameters.get("steptitle").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
			return;
		}

		if (myParameters.get("property") == null || myParameters.get("property").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "property");
			return;
		}

		if (myParameters.get("value") == null || myParameters.get("value").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "value");
			return;
		}

		String property = myParameters.get("property");
		String value = myParameters.get("value");

		if (!property.equals("metadata") && !property.equals("readimages") && !property.equals("writeimages") && !property.equals("validate")
				&& !property.equals("exportdms")) {
			Helper.setFehlerMeldung("goobiScriptfield",
					"wrong parameter 'property'; possible values: metadata, readimages, writeimages, validate, exportdms");
			return;
		}

		if (!value.equals("true") && !value.equals("false")) {
			Helper.setFehlerMeldung("goobiScriptfield", "wrong parameter 'value'; possible values: true, false");
			return;
		}

		/*
		 * -------------------------------- Durchführung der Action --------------------------------
		 */
		ProzessDAO sdao = new ProzessDAO();
		for (Prozess proz : inProzesse) {
			if (proz.getSchritte() != null) {
				for (Iterator<Schritt> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
					Schritt s = (Schritt) iterator.next();
					if (s.getTitel().equals(myParameters.get("steptitle"))) {

						if (property.equals("metadata"))
							s.setTypMetadaten(Boolean.parseBoolean(value));
						if (property.equals("readimages"))
							s.setTypImagesLesen(Boolean.parseBoolean(value));
						if (property.equals("writeimages"))
							s.setTypImagesSchreiben(Boolean.parseBoolean(value));
						if (property.equals("validate"))
							s.setTypBeimAbschliessenVerifizieren(Boolean.parseBoolean(value));
						if (property.equals("exportdms"))
							s.setTypExportDMS(Boolean.parseBoolean(value));

						try {
							sdao.save(proz);
						} catch (DAOException e) {
							Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
							logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
						}
						Helper.setMeldung("goobiScriptfield", "Error while saving process: ", proz.getTitel());
						break;
					}
				}
			}
		}
		Helper.setMeldung("goobiScriptfield", "setTaskProperty abgeschlossen: ");
	}

	/**
	 * Schritte auf bestimmten Status setzen ================================================================
	 */
	private void setStepStatus(List<Prozess> inProzesse) {
		/*
		 * -------------------------------- Validierung der Actionparameter --------------------------------
		 */
		if (myParameters.get("steptitle") == null || myParameters.get("steptitle").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
			return;
		}

		if (myParameters.get("status") == null || myParameters.get("status").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "status");
			return;
		}

		if (!myParameters.get("status").equals("0") && !myParameters.get("status").equals("1") && !myParameters.get("status").equals("2")
				&& !myParameters.get("status").equals("3")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Wrong status parameter: status ", "(possible: 0=closed, 1=open, 2=in work, 3=finished");
			return;
		}

		/*
		 * -------------------------------- Durchführung der Action --------------------------------
		 */
		SchrittDAO sdao = new SchrittDAO();
		for (Prozess proz : inProzesse) {
			for (Iterator<Schritt> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
				Schritt s = (Schritt) iterator.next();
				if (s.getTitel().equals(myParameters.get("steptitle"))) {
					s.setBearbeitungsstatusAsString(myParameters.get("status"));
					try {
						sdao.save(s);
					} catch (DAOException e) {
						Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
						logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
					}
					Helper.setMeldung("goobiScriptfield", "stepstatus setted in process: ", proz.getTitel());
					break;
				}
			}
		}
		Helper.setMeldung("goobiScriptfield", "setStepStatus finished: ");
	}

	/**
	 * Schritte auf bestimmten Reihenfolge setzen ================================================================
	 */
	private void setStepNumber(List<Prozess> inProzesse) {
		/*
		 * -------------------------------- Validierung der Actionparameter --------------------------------
		 */
		if (myParameters.get("steptitle") == null || myParameters.get("steptitle").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
			return;
		}

		if (myParameters.get("number") == null || myParameters.get("number").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "number");
			return;
		}

		if (!StringUtils.isNumeric(myParameters.get("number"))) {
			Helper.setFehlerMeldung("goobiScriptfield", "Wrong number parameter", "(only numbers allowed)");
			return;
		}

		/*
		 * -------------------------------- Durchführung der Action --------------------------------
		 */
		SchrittDAO sdao = new SchrittDAO();
		for (Prozess proz : inProzesse) {
			for (Iterator<Schritt> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
				Schritt s = (Schritt) iterator.next();
				if (s.getTitel().equals(myParameters.get("steptitle"))) {
					s.setReihenfolge(Integer.parseInt(myParameters.get("number")));
					try {
						sdao.save(s);
					} catch (DAOException e) {
						Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
						logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
					}
					Helper.setMeldung("goobiScriptfield", "step order changed in process: ", proz.getTitel());
					break;
				}
			}
		}
		Helper.setMeldung("goobiScriptfield", "setStepNumber finished ");
	}

	/**
	 * Benutzer zu Schritt hinzufügen ================================================================
	 */
	private void adduser(List<Prozess> inProzesse) {
		/*
		 * -------------------------------- Validierung der Actionparameter --------------------------------
		 */
		if (myParameters.get("steptitle") == null || myParameters.get("steptitle").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
			return;
		}
		if (myParameters.get("username") == null || myParameters.get("username").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "username");
			return;
		}
		/* prüfen, ob ein solcher Benutzer existiert */
		Benutzer myUser = null;
		try {
			List<Benutzer> treffer = new BenutzerDAO().search("from Benutzer where login='" + myParameters.get("username") + "'");
			if (treffer != null && treffer.size() > 0)
				myUser = (Benutzer) treffer.get(0);
			else {
				Helper.setFehlerMeldung("goobiScriptfield", "Unknown user: ", myParameters.get("username"));
				return;
			}
		} catch (DAOException e) {
			Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript.adduser", e);
			logger.error("goobiScriptfield" + "Error in GoobiScript.adduser: ", e);
			return;
		}

		/*
		 * -------------------------------- Durchführung der Action --------------------------------
		 */
		SchrittDAO sdao = new SchrittDAO();
		for (Prozess proz : inProzesse) {
			for (Iterator<Schritt> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
				Schritt s = (Schritt) iterator.next();
				if (s.getTitel().equals(myParameters.get("steptitle"))) {
					Set<Benutzer> myBenutzer = s.getBenutzer();
					if (myBenutzer == null) {
						myBenutzer = new HashSet<Benutzer>();
						s.setBenutzer(myBenutzer);
					}
					if (!myBenutzer.contains(myUser)) {
						myBenutzer.add(myUser);
						try {
							sdao.save(s);
						} catch (DAOException e) {
							Helper.setFehlerMeldung("goobiScriptfield", "Error while saving - " + proz.getTitel(), e);
							logger.error("goobiScriptfield" + "Error while saving - " + proz.getTitel(), e);
							return;
						}
					}
				}
			}
			Helper.setMeldung("goobiScriptfield", "Added user to step: ", proz.getTitel());
		}
		Helper.setMeldung("goobiScriptfield", "", "adduser finished.");
	}

	/**
	 * Benutzergruppe zu Schritt hinzufügen ================================================================
	 */
	private void addusergroup(List<Prozess> inProzesse) {
		/*
		 * -------------------------------- Validierung der Actionparameter --------------------------------
		 */
		if (myParameters.get("steptitle") == null || myParameters.get("steptitle").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
			return;
		}
		if (myParameters.get("group") == null || myParameters.get("group").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "group");
			return;
		}
		/* prüfen, ob ein solcher Benutzer existiert */
		Benutzergruppe myGroup = null;
		try {
			List<Benutzergruppe> treffer = new BenutzergruppenDAO().search("from Benutzergruppe where titel='" + myParameters.get("group") + "'");
			if (treffer != null && treffer.size() > 0)
				myGroup = (Benutzergruppe) treffer.get(0);
			else {
				Helper.setFehlerMeldung("goobiScriptfield", "Unknown group: ", myParameters.get("group"));
				return;
			}
		} catch (DAOException e) {
			Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript.addusergroup", e);
			return;
		}

		/*
		 * -------------------------------- Durchführung der Action --------------------------------
		 */
		SchrittDAO sdao = new SchrittDAO();
		for (Prozess proz : inProzesse) {
			for (Iterator<Schritt> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
				Schritt s = (Schritt) iterator.next();
				if (s.getTitel().equals(myParameters.get("steptitle"))) {
					Set<Benutzergruppe> myBenutzergruppe = s.getBenutzergruppen();
					if (myBenutzergruppe == null) {
						myBenutzergruppe = new HashSet<Benutzergruppe>();
						s.setBenutzergruppen(myBenutzergruppe);
					}
					if (!myBenutzergruppe.contains(myGroup)) {
						myBenutzergruppe.add(myGroup);
						try {
							sdao.save(s);
						} catch (DAOException e) {
							Helper.setFehlerMeldung("goobiScriptfield", "Error while saving - " + proz.getTitel(), e);
							return;
						}
					}
				}
			}
			Helper.setMeldung("goobiScriptfield", "added usergroup to step: ", proz.getTitel());
		}
		Helper.setMeldung("goobiScriptfield", "", "addusergroup finished");
	}

	/**
	 * TiffHeader von den Prozessen löschen ================================================================
	 */
	public void deleteTiffHeaderFile(List<Prozess> inProzesse) {
		for (Prozess proz : inProzesse) {
			try {
				File tiffheaderfile = new File(proz.getImagesDirectory() + "tiffwriter.conf");
				if (tiffheaderfile.exists())
					tiffheaderfile.delete();
				Helper.setMeldung("goobiScriptfield", "TiffHeaderFile deleted: ", proz.getTitel());
			} catch (Exception e) {
				Helper.setFehlerMeldung("goobiScriptfield", "Error while deleting TiffHeader", e);
			}
		}
		Helper.setMeldung("goobiScriptfield", "", "deleteTiffHeaderFile finished");
	}

	/**
	 * TiffHeader von den Prozessen neu schreiben ================================================================
	 */
	private void writeTiffHeader(List<Prozess> inProzesse) {
		for (Iterator<Prozess> iter = inProzesse.iterator(); iter.hasNext();) {
			Prozess proz = iter.next();
			TiffWriterTask task = new TiffWriterTask();
			task.initialize(proz);
			LongRunningTaskManager.getInstance().addTask(task);
		}
	}

	/**
	 * Imagepfad in den Metadaten neu setzen (evtl. vorhandene zunächst löschen) ================================================================
	 */
	public void updateImagePath(List<Prozess> inProzesse) {
		for (Prozess proz : inProzesse) {
			try {

				Fileformat myRdf = proz.readMetadataFile();
				UghHelper ughhelp = new UghHelper();
				MetadataType mdt = ughhelp.getMetadataType(proz, "pathimagefiles");
				List<? extends ugh.dl.Metadata> alleImagepfade = myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadataByType(mdt);
				if (alleImagepfade.size() > 0) {
					for (Metadata md : alleImagepfade) {
						myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(md);
					}
				}
				Metadata newmd = new Metadata(mdt);
				if (SystemUtils.IS_OS_WINDOWS) {
					newmd.setValue("file:/" + proz.getImagesDirectory() + proz.getTitel() + DIRECTORY_SUFFIX);
				} else {
					newmd.setValue("file://" + proz.getImagesDirectory() + proz.getTitel() + DIRECTORY_SUFFIX);
				}
				myRdf.getDigitalDocument().getPhysicalDocStruct().addMetadata(newmd);
				proz.writeMetadataFile(myRdf);
				Helper.setMeldung("goobiScriptfield", "ImagePath updated: ", proz.getTitel());

			} catch (ugh.exceptions.DocStructHasNoTypeException e) {
				Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());
			} catch (UghHelperException e) {
				Helper.setFehlerMeldung("UghHelperException", e.getMessage());
			} catch (MetadataTypeNotAllowedException e) {
				Helper.setFehlerMeldung("MetadataTypeNotAllowedException", e.getMessage());

			} catch (Exception e) {
				Helper.setFehlerMeldung("goobiScriptfield", "Error while updating imagepath", e);
			}

		}
		Helper.setMeldung("goobiScriptfield", "", "updateImagePath finished");

	}

	private void exportDms(List<Prozess> processes, String exportImages) {
		ExportDms dms;
		if (exportImages.equals("false")) {
			dms = new ExportDms(false);
		} else {
			dms = new ExportDms(true);
		}
		for (Prozess prozess : processes) {
			try {
				dms.startExport(prozess);
			} catch (DocStructHasNoTypeException e) {
				logger.error("DocStructHasNoTypeException", e);
			} catch (PreferencesException e) {
				logger.error("PreferencesException", e);
			} catch (WriteException e) {
				logger.error("WriteException", e);
			} catch (MetadataTypeNotAllowedException e) {
				logger.error("MetadataTypeNotAllowedException", e);
			} catch (ReadException e) {
				logger.error("ReadException", e);
			} catch (TypeNotAllowedForParentException e) {
				logger.error("TypeNotAllowedForParentException", e);
			} catch (IOException e) {
				logger.error("IOException", e);
			} catch (InterruptedException e) {
				logger.error("InterruptedException", e);
			} catch (ExportFileException e) {
				logger.error("ExportFileException", e);
			} catch (UghHelperException e) {
				logger.error("UghHelperException", e);
			} catch (SwapException e) {
				logger.error("SwapException", e);
			} catch (DAOException e) {
				logger.error("DAOException", e);
			}
		}
	}
}
