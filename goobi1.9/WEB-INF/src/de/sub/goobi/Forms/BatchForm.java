package de.sub.goobi.Forms;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.production.api.property.xmlbasedprovider.impl.PropertyTemplate;
import org.goobi.production.flow.helper.BatchDisplayItem;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
import org.goobi.production.flow.statistics.hibernate.UserDefinedBatchFilter;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.Beans.Batch;
import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.HistoryEvent;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Beans.Schritteigenschaft;
import de.sub.goobi.Export.dms.ExportDms;
import de.sub.goobi.Metadaten.MetadatenImagesHelper;
import de.sub.goobi.Metadaten.MetadatenVerifizierung;
import de.sub.goobi.Persistence.BatchDAO;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.Persistence.SchrittDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FileUtils;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.WebDav;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;

public class BatchForm extends BasisForm {

	private static final long serialVersionUID = 6628126856923665087L;
	private static final Logger myLogger = Logger.getLogger(BatchForm.class);
	private Batch batch = new Batch();
	private Integer myProblemID;
	private Integer mySolutionID;
	private String problemMessage;
	private String solutionMessage;
	private IEvaluableFilter myFilteredDataSource;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String script;
	private Integer process = null;
	WebDav myDav = new WebDav();

	public BatchForm() {
		FilterAlleStart();
	}

	public String FilterAlleStart() {
		// refreshing list
		if (this.page != null && this.page.getTotalResults() != 0) {
			BatchDAO dao = new BatchDAO();
			for (@SuppressWarnings("unchecked")
			Iterator<Batch> iter = this.page.getListReload().iterator(); iter.hasNext();) {
				Batch batch = iter.next();
				dao.refresh(batch);
			}
		}
		try {
			this.myFilteredDataSource = new UserDefinedBatchFilter(this.filter);
			Criteria crit = this.myFilteredDataSource.getCriteria();
			sortList(crit);
			this.page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("error on reading database", he.getMessage());
			return "";
		}
		return "BatchesAll";
	}

	private void sortList(Criteria crit) {
		// TODO
	}

	

	public String BatchDurchBenutzerUebernehmen() {

		ProzessDAO pdao = new ProzessDAO();
		Helper.getHibernateSession().clear();
		Helper.getHibernateSession().refresh(this.batch);

		BatchDisplayItem bdi = this.batch.getCurrentStep();
		for (Prozess p : this.batch.getBatchList()) {
			Schritt currentStep = p.getFirstOpenStep();
			if (currentStep.getTitel().equals(bdi.getStepTitle()) && currentStep.getBearbeitungsstatusEnum().equals(StepStatus.OPEN)) {
				currentStep.setBearbeitungsstatusEnum(StepStatus.INWORK);
				currentStep.setEditTypeEnum(StepEditType.MANUAL_MULTI);
				HelperSchritte.updateEditing(currentStep);
				if (currentStep.getBearbeitungsbeginn() == null) {
					Date myDate = new Date();
					currentStep.setBearbeitungsbeginn(myDate);
				}
				currentStep
						.getProzess()
						.getHistory()
						.add(new HistoryEvent(currentStep.getBearbeitungsbeginn(), currentStep.getReihenfolge().doubleValue(),
								currentStep.getTitel(), HistoryEventType.stepInWork, currentStep.getProzess()));
				try {
					pdao.save(currentStep.getProzess());
				} catch (DAOException e) {
					Helper.setFehlerMeldung(Helper.getTranslation("stepSaveError"), e);
					myLogger.error("step couldn't get saved", e);
				}
				if (currentStep.isTypImagesLesen() || currentStep.isTypImagesSchreiben()) {
					try {
						new File(currentStep.getProzess().getImagesOrigDirectory());
					} catch (Exception e1) {

					}
					HelperSchritte.updateEditing(currentStep);
					this.myDav.DownloadToHome(currentStep.getProzess(), currentStep.getId().intValue(), !currentStep.isTypImagesSchreiben());

				}
			}
			Benutzer user = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
			if (user != null) {
				this.batch.setUser(user);
			}
		}
		return "BatchesEdit";
	}

	public String BatchDurchBenutzerZurueckgeben() {
		ProzessDAO pdao = new ProzessDAO();
		BatchDisplayItem bdi = this.batch.getCurrentStep();
		for (Prozess p : this.batch.getBatchList()) {
			Schritt currentStep = p.getFirstOpenStep();
			if (currentStep.getTitel().equals(bdi.getStepTitle()) && currentStep.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)
					&& currentStep.getBearbeitungsbenutzer().equals(this.batch.getUser())) {

				this.myDav.UploadFromHome(currentStep.getProzess());
				currentStep.setBearbeitungsstatusEnum(StepStatus.OPEN);
				if (currentStep.isCorrectionStep()) {
					currentStep.setBearbeitungsbeginn(null);
				}
				currentStep.setEditTypeEnum(StepEditType.MANUAL_MULTI);
				HelperSchritte.updateEditing(currentStep);

				try {
					pdao.save(currentStep.getProzess());
				} catch (DAOException e) {
				}
			}
		}
		return "BatchesAll";
	}

	public String BatchDurchBenutzerAbschliessen() {
		BatchDisplayItem bdi = this.batch.getCurrentStep();
		for (Prozess p : this.batch.getBatchList()) {
			Schritt currentStep = p.getFirstOpenStep();
			if (currentStep.getTitel().equals(bdi.getStepTitle()) && currentStep.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)
					&& currentStep.getBearbeitungsbenutzer().equals(this.batch.getUser())) {

				if (currentStep.isTypImagesSchreiben()) {
					try {
						currentStep.getProzess().setSortHelperImages(
								FileUtils.getNumberOfFiles(new File(currentStep.getProzess().getImagesOrigDirectory())));
						HistoryAnalyserJob.updateHistory(currentStep.getProzess());
					} catch (Exception e) {
						Helper.setFehlerMeldung("Error while calculation of storage and images", e);
					}
				}

				if (currentStep.isTypBeimAbschliessenVerifizieren()) {
					if (currentStep.isTypMetadaten() && ConfigMain.getBooleanParameter("useMetadatenvalidierung")) {
						MetadatenVerifizierung mv = new MetadatenVerifizierung();
						mv.setAutoSave(true);
						if (!mv.validate(currentStep.getProzess())) {
							return "";
						}
					}
					if (currentStep.isTypImagesSchreiben()) {
						MetadatenImagesHelper mih = new MetadatenImagesHelper(null, null);
						try {
							if (!mih.checkIfImagesValid(currentStep.getProzess(), currentStep.getProzess().getImagesOrigDirectory())) {
								return "";
							}
						} catch (Exception e) {
							Helper.setFehlerMeldung("Error on image validation: ", e);
						}
					}
				}
				List<PropertyTemplate> propList = currentStep.getDisplayProperties().getPropertyTemplatesAsList();
				if (propList.size() > 0) {
					for (PropertyTemplate prop : propList) {
						if (prop.isIstObligatorisch() && (prop.getWert() == null || prop.getWert().equals(""))) {
							Helper.setFehlerMeldung(Helper.getTranslation("Eigenschaft") + " " + prop.getTitel() + " "
									+ Helper.getTranslation("bei Vorgang ") + p.getTitel() + " " + Helper.getTranslation("requiredValue"));
							return "";
						}
					}
				}

				this.myDav.UploadFromHome(currentStep.getProzess());
				currentStep.setEditTypeEnum(StepEditType.MANUAL_MULTI);
				new HelperSchritte().SchrittAbschliessen(currentStep, false);
			}
		}
		return FilterAlleStart();
	}



	public void executeScript() {
		BatchDisplayItem bdi = this.batch.getCurrentStep();
		for (Prozess p : this.batch.getBatchList()) {
			Schritt currentStep = p.getFirstOpenStep();
			if (currentStep.getTitel().equals(bdi.getStepTitle()) && currentStep.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)
					&& currentStep.getBearbeitungsbenutzer().equals(this.batch.getUser())) {
				if (currentStep.getAllScripts().containsKey(this.script)) {
					String scriptPath = currentStep.getAllScripts().get(this.script);
					try {
						new HelperSchritte().executeScript(currentStep, scriptPath, false);
					} catch (SwapException e) {
						myLogger.error(e);
					}
				}
			}
		}
	}

	public void ExportDMS() {
		BatchDisplayItem bdi = this.batch.getCurrentStep();
		for (Prozess p : this.batch.getBatchList()) {
			Schritt currentStep = p.getFirstOpenStep();
			if (currentStep.getTitel().equals(bdi.getStepTitle()) && currentStep.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)
					&& currentStep.getBearbeitungsbenutzer().equals(this.batch.getUser())) {
				ExportDms export = new ExportDms();
				try {
					export.startExport(p);
				} catch (Exception e) {
					Helper.setFehlerMeldung("Error on export", e.getMessage());
					myLogger.error(e);
				}
			}
		}
	}

	public List<Prozess> getAllActiveProcesses() {
		List<Prozess> answer = new ArrayList<Prozess>();
		BatchDisplayItem bdi = this.batch.getCurrentStep();
		for (Prozess p : this.batch.getBatchList()) {
			Schritt currentStep = p.getFirstOpenStep();
			if (currentStep.getTitel().equals(bdi.getStepTitle()) && currentStep.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)
					&& currentStep.getBearbeitungsbenutzer().equals(this.batch.getUser())) {
				answer.add(p);
			}
		}
		return answer;
	}



	public void setProcess(Integer title) {
		this.process = title;
	}

	public Integer getProcess() {
		return this.process;
	}

	public String ReportProblem() {
		BatchDisplayItem bdi = this.batch.getCurrentStep();
		for (Prozess p : this.batch.getBatchList()) {
			if (p.getId() == this.process) {
				Schritt currentStep = p.getFirstOpenStep();
				if (currentStep.getTitel().equals(bdi.getStepTitle()) && currentStep.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)
						&& currentStep.getBearbeitungsbenutzer().equals(this.batch.getUser())) {

					myLogger.debug("mySchritt.ID: " + currentStep.getId().intValue());
					myLogger.debug("Korrekturschritt.ID: " + this.myProblemID.intValue());
					this.myDav.UploadFromHome(p);
					Date myDate = new Date();
					currentStep.setBearbeitungsstatusEnum(StepStatus.LOCKED);
					currentStep.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
					HelperSchritte.updateEditing(currentStep);
					currentStep.setBearbeitungsbeginn(null);

					try {
						SchrittDAO dao = new SchrittDAO();
						Schritt temp = dao.get(this.myProblemID);
						temp.setBearbeitungsstatusEnum(StepStatus.OPEN);
						// if (temp.getPrioritaet().intValue() == 0)
						temp.setCorrectionStep();
						temp.setBearbeitungsende(null);
						Schritteigenschaft se = new Schritteigenschaft();
						Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");

						se.setTitel(Helper.getTranslation("Korrektur notwendig"));
						se.setWert("[" + this.formatter.format(new Date()) + ", " + ben.getNachVorname() + "] " + this.problemMessage);
						se.setType(PropertyType.messageError);
						se.setCreationDate(myDate);
						se.setSchritt(temp);
						temp.getEigenschaften().add(se);
						dao.save(temp);
						currentStep
								.getProzess()
								.getHistory()
								.add(new HistoryEvent(myDate, temp.getReihenfolge().doubleValue(), temp.getTitel(), HistoryEventType.stepError, temp
										.getProzess()));
						/*
						 * alle Schritte zwischen dem aktuellen und dem
						 * Korrekturschritt wieder schliessen
						 */
						@SuppressWarnings("unchecked")
						List<Schritt> alleSchritteDazwischen = Helper.getHibernateSession().createCriteria(Schritt.class)
								.add(Restrictions.le("reihenfolge", currentStep.getReihenfolge()))
								.add(Restrictions.gt("reihenfolge", temp.getReihenfolge())).addOrder(Order.asc("reihenfolge"))
								.createCriteria("prozess").add(Restrictions.idEq(currentStep.getProzess().getId())).list();
						for (Iterator<Schritt> iter = alleSchritteDazwischen.iterator(); iter.hasNext();) {
							Schritt step = iter.next();
							step.setBearbeitungsstatusEnum(StepStatus.LOCKED);
							// if (step.getPrioritaet().intValue() == 0)
							step.setCorrectionStep();
							step.setBearbeitungsende(null);
							Schritteigenschaft seg = new Schritteigenschaft();
							seg.setTitel(Helper.getTranslation("Korrektur notwendig"));
							seg.setWert(Helper.getTranslation("KorrekturFuer") + temp.getTitel() + ": " + this.problemMessage);
							seg.setSchritt(step);
							seg.setType(PropertyType.messageImportant);
							seg.setCreationDate(new Date());
							step.getEigenschaften().add(seg);
							dao.save(step);
						}

						/*
						 * den Prozess aktualisieren, so dass der
						 * Sortierungshelper gespeichert wird
						 */
						new ProzessDAO().save(currentStep.getProzess());
					} catch (DAOException e) {
					}

					this.problemMessage = "";
					this.myProblemID = 0;
				}
			}
		}
		return FilterAlleStart();

	}

	@SuppressWarnings("unchecked")
	public List<Schritt> getPreviousStepsForProblemReporting() {
		List<Schritt> alleVorherigenSchritte = null;
		BatchDisplayItem bdi = this.batch.getCurrentStep();
		for (Prozess p : this.batch.getBatchList()) {
			if (p.getId() == this.process) {
				Schritt currentStep = p.getFirstOpenStep();
				if (currentStep.getTitel().equals(bdi.getStepTitle()) && currentStep.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)
						&& currentStep.getBearbeitungsbenutzer().equals(this.batch.getUser())) {
					alleVorherigenSchritte = Helper.getHibernateSession().createCriteria(Schritt.class)
							.add(Restrictions.lt("reihenfolge", currentStep.getReihenfolge())).addOrder(Order.asc("reihenfolge"))
							.createCriteria("prozess").add(Restrictions.idEq(this.process)).list();
				}
			}
		}

		return alleVorherigenSchritte;
	}

	@SuppressWarnings("unchecked")
	public List<Schritt> getNextStepsForProblemSolution() {
		List<Schritt> alleNachfolgendenSchritte = null;
//		BatchDisplayItem bdi = this.batch.getCurrentStep();
		for (Prozess p : this.batch.getBatchList()) {
			if (p.getId() == this.process) {
				Schritt currentStep = p.getFirstOpenStep();
				alleNachfolgendenSchritte = Helper.getHibernateSession().createCriteria(Schritt.class)
						.add(Restrictions.ge("reihenfolge", currentStep.getReihenfolge())).add(Restrictions.eq("prioritaet", 10))
						.addOrder(Order.asc("reihenfolge")).createCriteria("prozess").add(Restrictions.idEq(this.process)).list();
			}
		}
		return alleNachfolgendenSchritte;
	}

	@SuppressWarnings("unchecked")
	public String SolveProblem() {
		BatchDisplayItem bdi = this.batch.getCurrentStep();
		for (Prozess p : this.batch.getBatchList()) {
			if (p.getId() == this.process) {
				Schritt currentStep = p.getFirstOpenStep();
				if (currentStep.getTitel().equals(bdi.getStepTitle()) && currentStep.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)
						&& currentStep.getBearbeitungsbenutzer().equals(this.batch.getUser())) {

					Date now = new Date();
					this.myDav.UploadFromHome(p);
					currentStep.setBearbeitungsstatusEnum(StepStatus.DONE);
					currentStep.setBearbeitungsende(now);
					currentStep.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
					HelperSchritte.updateEditing(currentStep);

					try {
						SchrittDAO dao = new SchrittDAO();
						Schritt temp = dao.get(this.mySolutionID);

						/*
						 * alle Schritte zwischen dem aktuellen und dem
						 * Korrekturschritt wieder schliessen
						 */
						List<Schritt> alleSchritteDazwischen = Helper.getHibernateSession().createCriteria(Schritt.class)
								.add(Restrictions.ge("reihenfolge", currentStep.getReihenfolge()))
								.add(Restrictions.le("reihenfolge", temp.getReihenfolge())).addOrder(Order.asc("reihenfolge"))
								.createCriteria("prozess").add(Restrictions.idEq(p.getId())).list();
						for (Iterator<Schritt> iter = alleSchritteDazwischen.iterator(); iter.hasNext();) {
							Schritt step = iter.next();
							step.setBearbeitungsstatusEnum(StepStatus.DONE);
							step.setBearbeitungsende(now);
							step.setPrioritaet(Integer.valueOf(0));
							if (step.getId().intValue() == temp.getId().intValue()) {
								step.setBearbeitungsstatusEnum(StepStatus.OPEN);
								step.setCorrectionStep();
								step.setBearbeitungsende(null);
								// step.setBearbeitungsbeginn(null);
								step.setBearbeitungszeitpunkt(now);
							}
							Schritteigenschaft seg = new Schritteigenschaft();
							seg.setTitel(Helper.getTranslation("Korrektur durchgefuehrt"));
							Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
							seg.setWert("[" + this.formatter.format(new Date()) + ", " + ben.getNachVorname() + "] "
									+ Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitel() + ": " + this.solutionMessage);
							seg.setSchritt(step);
							seg.setType(PropertyType.messageImportant);
							seg.setCreationDate(new Date());
							step.getEigenschaften().add(seg);
							dao.save(step);
						}

						/*
						 * den Prozess aktualisieren, so dass der
						 * Sortierungshelper gespeichert wird
						 */
						new ProzessDAO().save(p);
					} catch (DAOException e) {
					}

					this.solutionMessage = "";
					this.mySolutionID = 0;
				}
			}
		}
		return FilterAlleStart();
	}

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
	public Batch getBatch() {
		return this.batch;
	}

	public void setBatch(Batch batch) {
		this.batch = batch;
	}
	
	public String getScriptName() {
		return this.script;
	}

	public void setScriptName(String scriptName) {
		this.script = scriptName;
	}
}
