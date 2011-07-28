package de.sub.goobi.Forms;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.goobi.production.flow.helper.BatchDisplayItem;
import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
import org.goobi.production.flow.statistics.hibernate.UserDefinedBatchFilter;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;

import de.sub.goobi.Beans.Batch;
import de.sub.goobi.Beans.HistoryEvent;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Persistence.BatchDAO;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.WebDav;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;

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
						.add(new HistoryEvent(currentStep.getBearbeitungsbeginn(), currentStep.getReihenfolge().doubleValue(), currentStep.getTitel(),
								HistoryEventType.stepInWork, currentStep.getProzess()));
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
			// calcHomeImages();
		}

//		sttb_8_cod_ms_hist_lit__48x_21
//		zeitdesaf_PPN602167531_0093
//		kleiuniv_PPN517154005
//		von_gognw_PPN627001874
//		strugrun_PPN634347969
//		patede_PPN635662876
		
		// TODO
		return "BatchesAll";

	}
}
