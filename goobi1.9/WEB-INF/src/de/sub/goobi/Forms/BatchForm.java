package de.sub.goobi.Forms;

import java.io.File;
import java.text.SimpleDateFormat;
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

import de.sub.goobi.Beans.Batch;
import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.HistoryEvent;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Export.dms.ExportDms;
import de.sub.goobi.Metadaten.MetadatenImagesHelper;
import de.sub.goobi.Metadaten.MetadatenVerifizierung;
import de.sub.goobi.Persistence.BatchDAO;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FileUtils;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.WebDav;
import de.sub.goobi.helper.enums.HistoryEventType;
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

	public BatchForm() {
		FilterAlleStart();
	}

	public String FilterAlleStart() {
		if (this.page != null && this.page.getTotalResults() != 0) {
			BatchDAO dao = new BatchDAO();
			for (Iterator<Batch> iter = this.page.getListReload().iterator(); iter.hasNext();) {
				Batch batch = iter.next();
				dao.refresh(batch);
			}
		}
		try {

			this.myFilteredDataSource = new UserDefinedBatchFilter(this.filter);

			Criteria crit = this.myFilteredDataSource.getCriteria();

			// sortList(crit);
			this.page = new Page(crit, 0);
			// calcHomeImages();
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("error on reading database", he.getMessage());
			return "";
		}
		return "BatchesAll";
	}

	public Batch getBatch() {
		return this.batch;
	}

	public void setBatch(Batch batch) {
		this.batch = batch;
	}

	public String BatchDurchBenutzerUebernehmen() {

		WebDav myDav = new WebDav();
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
					/*
					 * den Prozess aktualisieren, so dass der Sortierungshelper
					 * gespeichert wird
					 */
					pdao.save(currentStep.getProzess());
				} catch (DAOException e) {
					Helper.setFehlerMeldung(Helper.getTranslation("stepSaveError"), e);
					myLogger.error("step couldn't get saved", e);
				}
				/*
				 * wenn es ein Image-Schritt ist, dann gleich die Images ins
				 * Home
				 */

				if (currentStep.isTypImagesLesen() || currentStep.isTypImagesSchreiben()) {
					try {
						new File(currentStep.getProzess().getImagesOrigDirectory());
					} catch (Exception e1) {

					}
					HelperSchritte.updateEditing(currentStep);
					myDav.DownloadToHome(currentStep.getProzess(), currentStep.getId().intValue(), !currentStep.isTypImagesSchreiben());

				}
			}
			Benutzer user = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
			if (user != null) {
				this.batch.setUser(user);
			}
			// calcHomeImages();
		}

		// sttb_8_cod_ms_hist_lit__48x_21
		// zeitdesaf_PPN602167531_0093
		// kleiuniv_PPN517154005
		// von_gognw_PPN627001874
		// strugrun_PPN634347969
		// patede_PPN635662876

		return "BatchesEdit";
	}

	public String BatchDurchBenutzerZurueckgeben() {
		WebDav myDav = new WebDav();
		ProzessDAO pdao = new ProzessDAO();
		BatchDisplayItem bdi = this.batch.getCurrentStep();
		for (Prozess p : this.batch.getBatchList()) {
			Schritt currentStep = p.getFirstOpenStep();
			if (currentStep.getTitel().equals(bdi.getStepTitle()) && currentStep.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)
					&& currentStep.getBearbeitungsbenutzer().equals(this.batch.getUser())) {

				myDav.UploadFromHome(currentStep.getProzess());
				currentStep.setBearbeitungsstatusEnum(StepStatus.OPEN);
				// mySchritt.setBearbeitungsbenutzer(null);
				// if we have a correction-step here then never remove startdate
				if (currentStep.isCorrectionStep()) {
					currentStep.setBearbeitungsbeginn(null);
				}
				currentStep.setEditTypeEnum(StepEditType.MANUAL_MULTI);
				HelperSchritte.updateEditing(currentStep);

				try {
					/*
					 * den Prozess aktualisieren, so dass der Sortierungshelper
					 * gespeichert wird
					 */
					pdao.save(currentStep.getProzess());
				} catch (DAOException e) {
				}
			}
		}
		// calcHomeImages();
		return "BatchesAll";
	}

	public String BatchDurchBenutzerAbschliessen() {
		WebDav myDav = new WebDav();
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

				/*
				 * -------------------------------- wenn das Resultat des
				 * Arbeitsschrittes zunÃ¤chst verifiziert werden soll, dann ggf.
				 * das Abschliessen abbrechen --------------------------------
				 */
				if (currentStep.isTypBeimAbschliessenVerifizieren()) {
					/* Metadatenvalidierung */
					if (currentStep.isTypMetadaten() && ConfigMain.getBooleanParameter("useMetadatenvalidierung")) {
						MetadatenVerifizierung mv = new MetadatenVerifizierung();
						mv.setAutoSave(true);
						if (!mv.validate(currentStep.getProzess())) {
							return "";
						}
					}

					/* Imagevalidierung */
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
							Helper.setFehlerMeldung(Helper.getTranslation("Eigenschaft") + " " + prop.getTitel() + " " + Helper.getTranslation("bei Vorgang ") + p.getTitel()
									+ " " + Helper.getTranslation("requiredValue"));
							return "";
						}
					}
				}

				/*
				 * wenn das Ergebnis der Verifizierung ok ist, dann weiter,
				 * ansonsten schon vorher draussen
				 */
				myDav.UploadFromHome(currentStep.getProzess());
				currentStep.setEditTypeEnum(StepEditType.MANUAL_MULTI);
				new HelperSchritte().SchrittAbschliessen(currentStep, false);
			}
		}
		return FilterAlleStart();
	}

	public String getScriptName() {
		return this.script;
	}

	public void setScriptName(String scriptName) {
		this.script = scriptName;
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

}
