package de.sub.goobi.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;

import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Prozesseigenschaft;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Export.dms.ExportDms;
import de.sub.goobi.Metadaten.MetadatenImagesHelper;
import de.sub.goobi.Metadaten.MetadatenVerifizierung;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;

public class BatchHelper {

	private List<Schritt> steps;
	private ProzessDAO pdao = new ProzessDAO();
	private static final Logger logger = Logger.getLogger(BatchHelper.class);
	private Schritt currentStep;

	public BatchHelper(List<Schritt> steps) {
		this.steps = steps;
		for (Schritt s : steps) {

			this.processNameList.add(s.getProzess().getTitel());
		}
		this.currentStep = steps.get(0);
		this.processName = this.currentStep.getProzess().getTitel();
		loadProcessProperties(this.currentStep);
	}

	public List<Schritt> getSteps() {
		return this.steps;
	}

	public void setSteps(List<Schritt> steps) {
		this.steps = steps;
	}

	public Schritt getCurrentStep() {
		return this.currentStep;
	}

	public void setCurrentStep(Schritt currentStep) {
		this.currentStep = currentStep;
	}

	/*
	 * properties
	 */

	private List<ProcessProperty> processPropertyList;

	private ProcessProperty processProperty;

	public ProcessProperty getProcessProperty() {
		return this.processProperty;
	}

	public void setProcessProperty(ProcessProperty processProperty) {
		this.processProperty = processProperty;
	}

	public List<ProcessProperty> getProcessProperties() {
		return this.processPropertyList;
	}

	private List<String> processNameList = new ArrayList<String>();

	public List<String> getProcessNameList() {
		return this.processNameList;
	}

	public void setProcessNameList(List<String> processNameList) {
		this.processNameList = processNameList;
	}

	private String processName = "";

	public String getProcessName() {
		return this.processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
		for (Schritt s : this.steps) {
			if (s.getProzess().getTitel().equals(processName)) {
				this.currentStep = s;
				loadProcessProperties(this.currentStep);
				break;
			}
		}
	}

	public void saveCurrentProperty() {
		if (!this.processProperty.isValid()) {
			Helper.setFehlerMeldung("Property " + this.processProperty.getName() + " is not valid");
			return;
		}
		this.processProperty.transfer();

		Prozess p = this.currentStep.getProzess();
		List<Prozesseigenschaft> props = p.getEigenschaftenList();
		for (Prozesseigenschaft pe : props) {
			if (pe.getTitel() == null) {
				p.getEigenschaften().remove(pe);
			}
		}
		if (!this.processProperty.getProzesseigenschaft().getProzess().getEigenschaften().contains(this.processProperty.getProzesseigenschaft())) {
			this.processProperty.getProzesseigenschaft().getProzess().getEigenschaften().add(this.processProperty.getProzesseigenschaft());
		}
		try {
			this.pdao.save(this.currentStep.getProzess());
			Helper.setMeldung("Properties saved");
		} catch (DAOException e) {
			logger.error(e);
			Helper.setFehlerMeldung("Properties could not be saved");
		}
	}

	public void saveCurrentPropertyForAll() {
		if (!this.processProperty.isValid()) {
			Helper.setFehlerMeldung("Property " + this.processProperty.getName() + " is not valid");
			return;
		}
		this.processProperty.transfer();

		Prozesseigenschaft pe = new Prozesseigenschaft();
		pe.setTitel(this.processProperty.getName());
		pe.setWert(this.processProperty.getValue());
		pe.setContainer(this.processProperty.getContainer());

		for (Schritt s : this.steps) {
			Prozess process = s.getProzess();
			if (!s.equals(this.currentStep)) {

				if (pe.getTitel() != null) {
					boolean match = false;

					for (Prozesseigenschaft processPe : process.getEigenschaftenList()) {
						if (processPe.getTitel() != null) {
							if (pe.getTitel().equals(processPe.getTitel()) && pe.getContainer() == processPe.getContainer()) {
								processPe.setWert(pe.getWert());
								match = true;
								break;
							}
						}
					}
					if (!match) {
						Prozesseigenschaft p = new Prozesseigenschaft();
						p.setTitel(pe.getTitel());
						p.setWert(pe.getWert());
						p.setContainer(pe.getContainer());
						p.setType(pe.getType());
						p.setProzess(process);
						process.getEigenschaften().add(p);
					}
				}
			} else {
				if (!process.getEigenschaftenList().contains(this.processProperty.getProzesseigenschaft())) {
					process.getEigenschaften().add(this.processProperty.getProzesseigenschaft());
				}
			}

			List<Prozesseigenschaft> props = process.getEigenschaftenList();
			for (Prozesseigenschaft peig : props) {
				if (peig.getTitel() == null) {
					process.getEigenschaften().remove(peig);
				}
			}

			try {
				this.pdao.save(process);
				Helper.setMeldung("Properties saved");
			} catch (DAOException e) {
				logger.error(e);
				Helper.setFehlerMeldung("Properties for process " + process.getTitel() + " could not be saved");
			}
		}
	}

	private void loadProcessProperties(Schritt s) {
		this.processPropertyList = PropertyParser.getPropertiesForStep(s);
		for (ProcessProperty pt : this.processPropertyList) {
			if (!this.containers.contains(pt.getContainer())) {
				this.containers.add(pt.getContainer());
			}
		}
		Collections.sort(this.containers);
	}


//	public void saveProcessProperties() {
//		boolean valid = true;
//		for (IProperty p : this.processPropertyList) {
//			if (!p.isValid()) {
//				Helper.setFehlerMeldung("Property " + p.getName() + " is not valid");
//				valid = false;
//			}
//		}
//
//		if (valid) {
//			List<Prozesseigenschaft> peList = new ArrayList<Prozesseigenschaft>();
//
//			for (ProcessProperty p : this.processPropertyList) {
//				Prozesseigenschaft pe = new Prozesseigenschaft();
//				pe.setTitel(p.getName());
//				pe.setWert(p.getValue());
//				pe.setContainer(p.getContainer());
//				peList.add(pe);
//				p.transfer();
//			}
//
//			for (Schritt s : this.steps) {
//
//				Prozess process = s.getProzess();
//				for (Prozesseigenschaft pe : peList) {
//					if (pe.getTitel() != null) {
//						boolean match = false;
//
//						for (Prozesseigenschaft processPe : process.getEigenschaftenList()) {
//							if (processPe.getTitel() != null) {
//								if (pe.getTitel().equals(processPe.getTitel()) && pe.getContainer() == processPe.getContainer()) {
//									processPe.setWert(pe.getWert());
//									match = true;
//									break;
//								}
//							}
//						}
//						if (!match) {
//							Prozesseigenschaft p = new Prozesseigenschaft();
//							p.setTitel(pe.getTitel());
//							p.setWert(pe.getWert());
//							p.setContainer(pe.getContainer());
//							p.setType(pe.getType());
//							p.setProzess(process);
//							process.getEigenschaften().add(p);
//						}
//					}
//
//					try {
//						this.pdao.save(process);
//						Helper.setMeldung("Properties saved");
//					} catch (DAOException e) {
//						logger.error(e);
//						Helper.setFehlerMeldung("Properties for process " + process.getTitel() + " could not be saved");
//					}
//				}
//			}
//
//		}
//
//	}

	// private void saveWithoutValidation() {
	// List<Prozesseigenschaft> peList = new ArrayList<Prozesseigenschaft>();
	// for (ProcessProperty p : this.processPropertyList) {
	// p.transfer();
	// peList.add(p.getProzesseigenschaft());
	// }
	//
	// for (Schritt s : this.steps) {
	// Prozess process = s.getProzess();
	// for (Prozesseigenschaft pe : peList) {
	// boolean match = false;
	// for (Prozesseigenschaft processPe : process.getEigenschaftenList()) {
	// if (pe.getTitel().equals(processPe.getTitel())) {
	// processPe.setWert(pe.getWert());
	// match = true;
	// break;
	// }
	// }
	// if (!match) {
	// Prozesseigenschaft p = new Prozesseigenschaft();
	// p.setTitel(pe.getTitel());
	// p.setWert(pe.getWert());
	// p.setContainer(pe.getContainer());
	// p.setType(pe.getType());
	// process.getEigenschaften().add(p);
	// }
	//
	// }
	// try {
	// this.pdao.save(process);
	// } catch (DAOException e) {
	// logger.error(e);
	// Helper.setFehlerMeldung("Properties for process " + process.getTitel() + " could not be saved");
	// }
	// }
	//
	// }

	private List<Integer> containers = new ArrayList<Integer>();

	// public String duplicateContainer() {
	// Integer currentContainer = this.processProperty.getContainer();
	// List<ProcessProperty> plist = new ArrayList<ProcessProperty>();
	// // search for all properties in container
	// for (ProcessProperty pt : this.processPropertyList) {
	// if (pt.getContainer() == currentContainer) {
	// plist.add(pt);
	// }
	// }
	//
	// // find new unused container number
	// boolean search = true;
	// int newContainerNumber = 1;
	// while (search) {
	// if (!this.containers.contains(newContainerNumber)) {
	// search = false;
	// } else {
	// newContainerNumber++;
	// }
	// }
	// // clone properties
	// for (ProcessProperty pt : plist) {
	// ProcessProperty newProp = pt.getClone(newContainerNumber);
	// this.processPropertyList.add(newProp);
	// saveWithoutValidation();
	// }
	//
	// return "";
	// }

	public List<Integer> getContainers() {
		return this.containers;
	}

	public List<ProcessProperty> getSortedProperties() {
		Comparator<ProcessProperty> comp = new ProcessProperty.CompareProperties();
		Collections.sort(this.processPropertyList, comp);
		return this.processPropertyList;
	}

	// kein delete im batch möglich, da keine eindeutige Zuordnung möglich ist, wenn mehr als eine eigenschaft mit dem selben Namen vorhanden ist

	// public void deleteProperty() {
	// this.processPropertyList.remove(this.processProperty);

	// if (this.processProperty.getProzesseigenschaft().getId() != null) {
	// this.mySchritt.getProzess().removeProperty(this.processProperty.getProzesseigenschaft());
	// }
	// saveWithoutValidation();
	// loadProcessProperties(steps.get(0));
	// }
	//
	// public void duplicateProperty() {
	// ProcessProperty pt = this.processProperty.getClone(0);
	// this.processPropertyList.add(pt);
	// saveWithoutValidation();
	// }

	/*
	 * actions
	 */

	private String script;
	private WebDav myDav = new WebDav();

	public String getScript() {
		return this.script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public void executeScript() {
		for (Schritt step : this.steps) {

			if (step.getAllScripts().containsKey(this.script)) {

				String scriptPath = step.getAllScripts().get(this.script);
				try {
					new HelperSchritte().executeScript(step, scriptPath, false);
				} catch (SwapException e) {
					logger.error(e);
				}
			}
		}

	}

	public void ExportDMS() {
		for (Schritt step : this.steps) {
			ExportDms export = new ExportDms();
			try {
				export.startExport(step.getProzess());
			} catch (Exception e) {
				Helper.setFehlerMeldung("Error on export", e.getMessage());
				logger.error(e);
			}
		}
	}

	public String BatchDurchBenutzerZurueckgeben() {

		for (Schritt s : this.steps) {

			this.myDav.UploadFromHome(s.getProzess());
			s.setBearbeitungsstatusEnum(StepStatus.OPEN);
			if (s.isCorrectionStep()) {
				s.setBearbeitungsbeginn(null);
			}
			s.setEditTypeEnum(StepEditType.MANUAL_MULTI);
			HelperSchritte.updateEditing(s);

			try {
				this.pdao.save(s.getProzess());
			} catch (DAOException e) {
			}
		}
		return "AktuelleSchritteAlle";
	}

	public String BatchDurchBenutzerAbschliessen() {
		for (ProcessProperty pp : this.processPropertyList) {
			this.processProperty = pp;
			saveCurrentPropertyForAll();
		}
		for (Schritt s : this.steps) {

			if (s.isTypImagesSchreiben()) {
				try {
					s.getProzess().setSortHelperImages(FileUtils.getNumberOfFiles(new File(s.getProzess().getImagesOrigDirectory())));
					HistoryAnalyserJob.updateHistory(s.getProzess());
				} catch (Exception e) {
					Helper.setFehlerMeldung("Error while calculation of storage and images", e);
				}
			}

			if (s.isTypBeimAbschliessenVerifizieren()) {
				if (s.isTypMetadaten() && ConfigMain.getBooleanParameter("useMetadatenvalidierung")) {
					MetadatenVerifizierung mv = new MetadatenVerifizierung();
					mv.setAutoSave(true);
					if (!mv.validate(s.getProzess())) {
						return "";
					}
				}
				if (s.isTypImagesSchreiben()) {
					MetadatenImagesHelper mih = new MetadatenImagesHelper(null, null);
					try {
						if (!mih.checkIfImagesValid(s.getProzess(), s.getProzess().getImagesOrigDirectory())) {
							return "";
						}
					} catch (Exception e) {
						Helper.setFehlerMeldung("Error on image validation: ", e);
					}
				}
			}

			this.myDav.UploadFromHome(s.getProzess());
			s.setEditTypeEnum(StepEditType.MANUAL_MULTI);
			new HelperSchritte().SchrittAbschliessen(s, false);
		}
		return "AktuelleSchritteAlle";
	}

	public List<String> getScriptnames() {
		List<String> answer = new ArrayList<String>();
		answer.addAll(getCurrentStep().getAllScripts().keySet());
		return answer;
	}

	/*
	 * Error management
	 */

	// public String ReportProblem() {
	// BatchDisplayItem bdi = this.batch.getCurrentStep();
	// for (Prozess p : this.batch.getBatchList()) {
	// if (p.getId() == this.process) {
	// Schritt currentStep = p.getFirstOpenStep();
	// if (currentStep.getTitel().equals(bdi.getStepTitle()) && currentStep.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)
	// && currentStep.getBearbeitungsbenutzer().equals(this.batch.getUser())) {
	//
	// myLogger.debug("mySchritt.ID: " + currentStep.getId().intValue());
	// myLogger.debug("Korrekturschritt.ID: " + this.myProblemID.intValue());
	// this.myDav.UploadFromHome(p);
	// Date myDate = new Date();
	// currentStep.setBearbeitungsstatusEnum(StepStatus.LOCKED);
	// currentStep.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
	// HelperSchritte.updateEditing(currentStep);
	// currentStep.setBearbeitungsbeginn(null);
	//
	// try {
	// SchrittDAO dao = new SchrittDAO();
	// Schritt temp = dao.get(this.myProblemID);
	// temp.setBearbeitungsstatusEnum(StepStatus.OPEN);
	// // if (temp.getPrioritaet().intValue() == 0)
	// temp.setCorrectionStep();
	// temp.setBearbeitungsende(null);
	// Schritteigenschaft se = new Schritteigenschaft();
	// Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
	//
	// se.setTitel(Helper.getTranslation("Korrektur notwendig"));
	// se.setWert("[" + this.formatter.format(new Date()) + ", " + ben.getNachVorname() + "] " + this.problemMessage);
	// se.setType(PropertyType.messageError);
	// se.setCreationDate(myDate);
	// se.setSchritt(temp);
	// temp.getEigenschaften().add(se);
	// dao.save(temp);
	// currentStep
	// .getProzess()
	// .getHistory()
	// .add(new HistoryEvent(myDate, temp.getReihenfolge().doubleValue(), temp.getTitel(), HistoryEventType.stepError, temp
	// .getProzess()));
	// /*
	// * alle Schritte zwischen dem aktuellen und dem
	// * Korrekturschritt wieder schliessen
	// */
	// @SuppressWarnings("unchecked")
	// List<Schritt> alleSchritteDazwischen = Helper.getHibernateSession().createCriteria(Schritt.class)
	// .add(Restrictions.le("reihenfolge", currentStep.getReihenfolge()))
	// .add(Restrictions.gt("reihenfolge", temp.getReihenfolge())).addOrder(Order.asc("reihenfolge"))
	// .createCriteria("prozess").add(Restrictions.idEq(currentStep.getProzess().getId())).list();
	// for (Iterator<Schritt> iter = alleSchritteDazwischen.iterator(); iter.hasNext();) {
	// Schritt step = iter.next();
	// step.setBearbeitungsstatusEnum(StepStatus.LOCKED);
	// // if (step.getPrioritaet().intValue() == 0)
	// step.setCorrectionStep();
	// step.setBearbeitungsende(null);
	// Schritteigenschaft seg = new Schritteigenschaft();
	// seg.setTitel(Helper.getTranslation("Korrektur notwendig"));
	// seg.setWert(Helper.getTranslation("KorrekturFuer") + temp.getTitel() + ": " + this.problemMessage);
	// seg.setSchritt(step);
	// seg.setType(PropertyType.messageImportant);
	// seg.setCreationDate(new Date());
	// step.getEigenschaften().add(seg);
	// dao.save(step);
	// }
	//
	// /*
	// * den Prozess aktualisieren, so dass der
	// * Sortierungshelper gespeichert wird
	// */
	// new ProzessDAO().save(currentStep.getProzess());
	// } catch (DAOException e) {
	// }
	//
	// this.problemMessage = "";
	// this.myProblemID = 0;
	// }
	// }
	// }
	// return FilterAlleStart();
	//
	// }
	//
	// @SuppressWarnings("unchecked")
	// public List<Schritt> getPreviousStepsForProblemReporting() {
	// List<Schritt> alleVorherigenSchritte = null;
	// BatchDisplayItem bdi = this.batch.getCurrentStep();
	// for (Prozess p : this.batch.getBatchList()) {
	// if (p.getId() == this.process) {
	// Schritt currentStep = p.getFirstOpenStep();
	// if (currentStep.getTitel().equals(bdi.getStepTitle()) && currentStep.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)
	// && currentStep.getBearbeitungsbenutzer().equals(this.batch.getUser())) {
	// alleVorherigenSchritte = Helper.getHibernateSession().createCriteria(Schritt.class)
	// .add(Restrictions.lt("reihenfolge", currentStep.getReihenfolge())).addOrder(Order.asc("reihenfolge"))
	// .createCriteria("prozess").add(Restrictions.idEq(this.process)).list();
	// }
	// }
	// }
	//
	// return alleVorherigenSchritte;
	// }
	//
	// @SuppressWarnings("unchecked")
	// public List<Schritt> getNextStepsForProblemSolution() {
	// List<Schritt> alleNachfolgendenSchritte = null;
	// // BatchDisplayItem bdi = this.batch.getCurrentStep();
	// for (Prozess p : this.batch.getBatchList()) {
	// if (p.getId() == this.process) {
	// Schritt currentStep = p.getFirstOpenStep();
	// alleNachfolgendenSchritte = Helper.getHibernateSession().createCriteria(Schritt.class)
	// .add(Restrictions.ge("reihenfolge", currentStep.getReihenfolge())).add(Restrictions.eq("prioritaet", 10))
	// .addOrder(Order.asc("reihenfolge")).createCriteria("prozess").add(Restrictions.idEq(this.process)).list();
	// }
	// }
	// return alleNachfolgendenSchritte;
	// }
	//
	// @SuppressWarnings("unchecked")
	// public String SolveProblem() {
	// BatchDisplayItem bdi = this.batch.getCurrentStep();
	// for (Prozess p : this.batch.getBatchList()) {
	// if (p.getId() == this.process) {
	// Schritt currentStep = p.getFirstOpenStep();
	// if (currentStep.getTitel().equals(bdi.getStepTitle()) && currentStep.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)
	// && currentStep.getBearbeitungsbenutzer().equals(this.batch.getUser())) {
	//
	// Date now = new Date();
	// this.myDav.UploadFromHome(p);
	// currentStep.setBearbeitungsstatusEnum(StepStatus.DONE);
	// currentStep.setBearbeitungsende(now);
	// currentStep.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
	// HelperSchritte.updateEditing(currentStep);
	//
	// try {
	// SchrittDAO dao = new SchrittDAO();
	// Schritt temp = dao.get(this.mySolutionID);
	//
	// /*
	// * alle Schritte zwischen dem aktuellen und dem
	// * Korrekturschritt wieder schliessen
	// */
	// List<Schritt> alleSchritteDazwischen = Helper.getHibernateSession().createCriteria(Schritt.class)
	// .add(Restrictions.ge("reihenfolge", currentStep.getReihenfolge()))
	// .add(Restrictions.le("reihenfolge", temp.getReihenfolge())).addOrder(Order.asc("reihenfolge"))
	// .createCriteria("prozess").add(Restrictions.idEq(p.getId())).list();
	// for (Iterator<Schritt> iter = alleSchritteDazwischen.iterator(); iter.hasNext();) {
	// Schritt step = iter.next();
	// step.setBearbeitungsstatusEnum(StepStatus.DONE);
	// step.setBearbeitungsende(now);
	// step.setPrioritaet(Integer.valueOf(0));
	// if (step.getId().intValue() == temp.getId().intValue()) {
	// step.setBearbeitungsstatusEnum(StepStatus.OPEN);
	// step.setCorrectionStep();
	// step.setBearbeitungsende(null);
	// // step.setBearbeitungsbeginn(null);
	// step.setBearbeitungszeitpunkt(now);
	// }
	// Schritteigenschaft seg = new Schritteigenschaft();
	// seg.setTitel(Helper.getTranslation("Korrektur durchgefuehrt"));
	// Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
	// seg.setWert("[" + this.formatter.format(new Date()) + ", " + ben.getNachVorname() + "] "
	// + Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitel() + ": " + this.solutionMessage);
	// seg.setSchritt(step);
	// seg.setType(PropertyType.messageImportant);
	// seg.setCreationDate(new Date());
	// step.getEigenschaften().add(seg);
	// dao.save(step);
	// }
	//
	// /*
	// * den Prozess aktualisieren, so dass der
	// * Sortierungshelper gespeichert wird
	// */
	// new ProzessDAO().save(p);
	// } catch (DAOException e) {
	// }
	//
	// this.solutionMessage = "";
	// this.mySolutionID = 0;
	// }
	// }
	// }
	// return FilterAlleStart();
	// }

	private Integer myProblemID;
	private Integer mySolutionID;
	private String problemMessage;
	private String solutionMessage;

	public String getProblemMessage() {
		return this.problemMessage;
	}

	public void setProblemMessage(String problemMessage) {
		this.problemMessage = problemMessage;
	}

	public Integer getMyProblemID() {
		return this.myProblemID;
	}

	public void setMyProblemID(Integer myProblemID) {
		this.myProblemID = myProblemID;
	}

	public String getSolutionMessage() {
		return this.solutionMessage;
	}

	public void setSolutionMessage(String solutionMessage) {
		this.solutionMessage = solutionMessage;
	}

	public Integer getMySolutionID() {
		return this.mySolutionID;
	}

	public void setMySolutionID(Integer mySolutionID) {
		this.mySolutionID = mySolutionID;
	}

	
	

	/**
	 * sets new value for wiki field
	 * 
	 * @param inString
	 */
	
	private String addToWikiField = "";
	
	public void setWikiField(String inString) {
		this.currentStep.getProzess().setWikifield(inString);
	}
	public String getWikiField() {
		return this.currentStep.getProzess().getWikifield();

	}

	public String getAddToWikiField() {
		return this.addToWikiField;
	}

	public void setAddToWikiField(String addToWikiField) {
		this.addToWikiField = addToWikiField;
	}

	public void addToWikiField() {
		Benutzer user = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
		String message = this.addToWikiField + " (" + user.getNachVorname() + ")";
		this.currentStep.getProzess().setWikifield(this.currentStep.getProzess().getWikifield() + WikiFieldHelper.getWikiMessage("user", message));
		this.addToWikiField = "";
		try {
			this.pdao.save(this.currentStep.getProzess());
		} catch (DAOException e) {
			logger.error(e);
		}
	}
	
}
