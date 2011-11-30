package de.sub.goobi.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.properties.IProperty;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;

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
		this.setSteps(steps);
		this.currentStep = steps.get(0);
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

	private void loadProcessProperties(Schritt s) {
		this.processPropertyList = PropertyParser.getPropertiesForStep(s);
		for (ProcessProperty pt : this.processPropertyList) {
			if (!this.containers.contains(pt.getContainer())) {
				this.containers.add(pt.getContainer());
			}
		}
		Collections.sort(this.containers);
	}

	// TODO validierung nur bei abgeben, nicht bei normalen speichern
	public void saveProcessProperties() {
		boolean valid = true;
		for (IProperty p : this.processPropertyList) {
			if (!p.isValid()) {
				Helper.setFehlerMeldung("Property " + p.getName() + " is not valid");
				valid = false;
			}
		}
		

		if (valid) {
			List<Prozesseigenschaft> peList = new ArrayList<Prozesseigenschaft>();

			
			for (ProcessProperty p : this.processPropertyList) {
				Prozesseigenschaft pe = new Prozesseigenschaft();
				pe.setTitel(p.getName());
				pe.setWert(p.getValue());
				pe.setContainer(p.getContainer());
				peList.add(pe);
				p.transfer();
			}

			for (Schritt s : this.steps) {

				Prozess process = s.getProzess();
				for (Prozesseigenschaft pe : peList) {
					if (pe.getTitel() != null) {
						boolean match = false;

						for (Prozesseigenschaft processPe : process.getEigenschaftenList()) {
							if (processPe.getTitel() != null) {
								if (pe.getTitel().equals(processPe.getTitel())) {
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

					try {
						this.pdao.save(process);
					} catch (DAOException e) {
						logger.error(e);
						Helper.setFehlerMeldung("Properties for process " + process.getTitel() + " could not be saved");
					}
				}
			}

		}

	}

//	private void saveWithoutValidation() {
//		List<Prozesseigenschaft> peList = new ArrayList<Prozesseigenschaft>();
//		for (ProcessProperty p : this.processPropertyList) {
//			p.transfer();
//			peList.add(p.getProzesseigenschaft());
//		}
//
//		for (Schritt s : this.steps) {
//			Prozess process = s.getProzess();
//			for (Prozesseigenschaft pe : peList) {
//				boolean match = false;
//				for (Prozesseigenschaft processPe : process.getEigenschaftenList()) {
//					if (pe.getTitel().equals(processPe.getTitel())) {
//						processPe.setWert(pe.getWert());
//						match = true;
//						break;
//					}
//				}
//				if (!match) {
//					Prozesseigenschaft p = new Prozesseigenschaft();
//					p.setTitel(pe.getTitel());
//					p.setWert(pe.getWert());
//					p.setContainer(pe.getContainer());
//					p.setType(pe.getType());
//					process.getEigenschaften().add(p);
//				}
//
//			}
//			try {
//				this.pdao.save(process);
//			} catch (DAOException e) {
//				logger.error(e);
//				Helper.setFehlerMeldung("Properties for process " + process.getTitel() + " could not be saved");
//			}
//		}
//
//	}

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
		saveProcessProperties();
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
}
