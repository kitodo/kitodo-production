package de.sub.goobi.Forms;

import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
import org.goobi.production.flow.statistics.hibernate.UserDefinedBatchFilter;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;

import de.sub.goobi.Beans.Batch;
import de.sub.goobi.Persistence.BatchDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;

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

		Helper.getHibernateSession().clear();
		Helper.getHibernateSession().refresh(this.batch);

		// TODO
		return "BatchesAll";

	}
}
