package de.sub.goobi.Forms;

//TODO: Use generics.
//TODO: Remove RusDML garbage
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.goobi.production.api.property.xmlbasedprovider.impl.PropertyTemplate;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
import org.goobi.production.flow.statistics.hibernate.UserDefinedStepFilter;
import org.goobi.production.search.lucene.LuceneStepFilter;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import Messages.Messages;
import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.HistoryEvent;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Beans.Schritteigenschaft;
import de.sub.goobi.Export.dms.ExportDms;
import de.sub.goobi.Export.download.TiffHeader;
import de.sub.goobi.Metadaten.MetadatenImagesHelper;
import de.sub.goobi.Metadaten.MetadatenSperrung;
import de.sub.goobi.Metadaten.MetadatenVerifizierung;
import de.sub.goobi.Persistence.HibernateUtil;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.Persistence.SchrittDAO;
import de.sub.goobi.Persistence.SimpleDAO;
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
import de.unigoettingen.goobi.module.api.exception.GoobiException;

public class AktuelleSchritteForm extends BasisForm {
	private static final long serialVersionUID = 5841566727939692509L;
	private static final Logger myLogger = Logger.getLogger(AktuelleSchritteForm.class);
	private Prozess myProzess = new Prozess();
	private Schritt mySchritt = new Schritt();
	private Integer myProblemID;
	private Integer mySolutionID;
	private String problemMessage;
	private String solutionMessage;

	private String modusBearbeiten = "";
	private Schritteigenschaft mySchrittEigenschaft;
	private WebDav myDav = new WebDav();
	private int gesamtAnzahlImages = 0;
	private int pageAnzahlImages = 0;
	private boolean nurOffeneSchritte = false;
	private boolean nurEigeneSchritte = false;
	private HashMap<String, Boolean> anzeigeAnpassen;
	private IEvaluableFilter myFilteredDataSource;
	private String scriptPath;

	private Boolean flagWait = false;

	public AktuelleSchritteForm() {
		anzeigeAnpassen = new HashMap<String, Boolean>();
		anzeigeAnpassen.put("lockings", false);
		anzeigeAnpassen.put("selectionBoxes", false);
		anzeigeAnpassen.put("processId", false);
		anzeigeAnpassen.put("modules", false);
		/*
		 * --------------------- Vorgangsdatum generell anzeigen? -------------------
		 */
		LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		if (login.getMyBenutzer() != null)
			anzeigeAnpassen.put("processDate", login.getMyBenutzer().isConfVorgangsdatumAnzeigen());
		else
			anzeigeAnpassen.put("processDate", false);
	}

	/*
	 * ##################################################### ##################################################### ## ## Filter ##
	 * ##################################################### ####################################################
	 */

	/**
	 * Anzeige der Schritte
	 */
	@SuppressWarnings("unchecked")
	public String FilterAlleStart() {
		if (page != null && page.getTotalResults() != 0) {
			SchrittDAO dao = new SchrittDAO();
			for (Iterator<Schritt> iter = page.getListReload().iterator(); iter.hasNext();) {
				Schritt step = (Schritt) iter.next();
				dao.refresh(step);
			}
		}
		try {
			if (filter.toLowerCase().startsWith("lucene")) {
				myFilteredDataSource = new LuceneStepFilter();
				myFilteredDataSource.getObservable().addObserver(new Helper().createObserver());
				((UserDefinedStepFilter) myFilteredDataSource).setFilterModes(nurOffeneSchritte, nurEigeneSchritte);
				myFilteredDataSource.setFilter(filter.substring("lucene".length()));
			} else {
				HibernateUtil.clearSession();
				myFilteredDataSource = new UserDefinedStepFilter();
				myFilteredDataSource.getObservable().addObserver(new Helper().createObserver());
				((UserDefinedStepFilter) myFilteredDataSource).setFilterModes(nurOffeneSchritte, nurEigeneSchritte);
				myFilteredDataSource.setFilter(filter);
			}

			// // HibernateUtil.clearSession();
			// myFilteredDataSource = new UserDefinedStepFilter();
			// myFilteredDataSource.getObservable().addObserver(Helper.createObserver());
			//
			// ((UserDefinedStepFilter) myFilteredDataSource).setFilterModes(nurOffeneSchritte, nurEigeneSchritte);
			// myFilteredDataSource.setFilter(filter);

			Criteria crit = myFilteredDataSource.getCriteria();

			sortList(crit);
			page = new Page(crit, 0);
			// calcHomeImages();
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("error on reading database", he.getMessage());
			return "";
		}
		return "AktuelleSchritteAlle";
	}

	private void sortList(Criteria inCrit) {
		inCrit.addOrder(Order.desc("prioritaet"));

		Order order = Order.asc("proc.titel");
		if (sortierung.equals("schrittAsc"))
			order = Order.asc("titel");
		if (sortierung.equals("schrittDesc"))
			order = Order.desc("titel");
		if (sortierung.equals("prozessAsc"))
			order = Order.asc("proc.titel");
		if (sortierung.equals("prozessDesc"))
			order = Order.desc("proc.titel");
		if (sortierung.equals("prozessdateAsc"))
			order = Order.asc("proc.erstellungsdatum");
		if (sortierung.equals("prozessdateDesc"))
			order = Order.desc("proc.erstellungsdatum");
		if (sortierung.equals("projektAsc"))
			order = Order.asc("proj.titel");
		if (sortierung.equals("projektDesc"))
			order = Order.desc("proj.titel");
		if (sortierung.equals("modulesAsc"))
			order = Order.asc("typModulName");
		if (sortierung.equals("modulesDesc"))
			order = Order.desc("typModulName");
		if (sortierung.equals("statusAsc"))
			order = Order.asc("bearbeitungsstatus");
		if (sortierung.equals("statusDesc"))
			order = Order.desc("bearbeitungsstatus");

		inCrit.addOrder(order);
	}

	/*
	 * ##################################################### ##################################################### ## ## Bearbeitung des Schritts
	 * übernehmen oder abschliessen ## ##################################################### ####################################################
	 */

	public String SchrittDurchBenutzerUebernehmen() {
		synchronized (flagWait) {

			if (!flagWait) {
				flagWait = true;

				Helper.getHibernateSession().clear();
				Helper.getHibernateSession().refresh(mySchritt);

				if (mySchritt.getBearbeitungsstatusEnum() != StepStatus.OPEN) {
					Helper.setFehlerMeldung("stepInWorkError");
					flagWait = false;
					return "";
				}

				else {
					mySchritt.setBearbeitungsstatusEnum(StepStatus.INWORK);
					mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
					HelperSchritte.updateEditing(mySchritt);
					if (mySchritt.getBearbeitungsbeginn() == null) {
						Date myDate = new Date();
						mySchritt.setBearbeitungsbeginn(myDate);
					}
					mySchritt.getProzess().getHistory().add(
							new HistoryEvent(mySchritt.getBearbeitungsbeginn(), mySchritt.getReihenfolge().doubleValue(), mySchritt.getTitel(),
									HistoryEventType.stepInWork, mySchritt.getProzess()));
					try {
						/*
						 * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
						 */
						new ProzessDAO().save(mySchritt.getProzess());
					} catch (DAOException e) {
						Helper.setFehlerMeldung(Helper.getTranslation("stepSaveError"), e);
						myLogger.error("step couldn't get saved", e);
					} finally {
						flagWait = false;
					}
					/*
					 * wenn es ein Image-Schritt ist, dann gleich die Images ins Home
					 */

					if (mySchritt.isTypImagesLesen() || mySchritt.isTypImagesSchreiben())
						DownloadToHome();
				}
				// calcHomeImages();
			} else {
				Helper.setFehlerMeldung("stepInWorkError");
				return "";
			}
			flagWait = false;
		}
		return "AktuelleSchritteBearbeiten";

	}

	public void saveProperties() {
		try {
			/* den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird */

			for (PropertyTemplate pt : mySchritt.getDisplayProperties().getPropertyTemplatesAsList()) {
				mySchritt.getEigenschaften().add((Schritteigenschaft) pt.getProperty());
				((Schritteigenschaft) pt.getProperty()).setSchritt(mySchritt);
			}
			new SimpleDAO().save(mySchritt);
			mySchritt.refreshProperties();
		} catch (DAOException e) {
			myLogger.error("stupid dao-exception occured", e);
		}
	}

	public String SchrittDurchBenutzerZurueckgeben() {
		myDav.UploadFromHome(mySchritt.getProzess());
		mySchritt.setBearbeitungsstatusEnum(StepStatus.OPEN);
		// mySchritt.setBearbeitungsbenutzer(null);
		// if we have a correction-step here then never remove startdate
		if (mySchritt.isCorrectionStep()) {
			mySchritt.setBearbeitungsbeginn(null);
		}
		mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
		HelperSchritte.updateEditing(mySchritt);

		try {
			/* den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird */
			new ProzessDAO().save(mySchritt.getProzess());
		} catch (DAOException e) {
		}
		// calcHomeImages();
		return "AktuelleSchritteAlle";
	}

	public String SchrittDurchBenutzerAbschliessen() {
		/*
		 * -------------------------------- if step allows writing of images, then count all images here --------------------------------
		 */
		if (mySchritt.isTypImagesSchreiben()) {
			try {
				mySchritt.getProzess().setSortHelperImages(FileUtils.getNumberOfFiles(new File(mySchritt.getProzess().getImagesOrigDirectory())));
				HistoryAnalyserJob.updateHistory(mySchritt.getProzess());
			} catch (Exception e) {
				Helper.setFehlerMeldung("Error while calculation of storage and images", e);
			}
		}

		/*
		 * -------------------------------- wenn das Resultat des Arbeitsschrittes zunÃ¤chst verifiziert werden soll, dann ggf. das Abschliessen
		 * abbrechen --------------------------------
		 */
		if (mySchritt.isTypBeimAbschliessenVerifizieren()) {
			/* Metadatenvalidierung */
			if (mySchritt.isTypMetadaten() && ConfigMain.getBooleanParameter("useMetadatenvalidierung")) {
				MetadatenVerifizierung mv = new MetadatenVerifizierung();
				mv.setAutoSave(true);
				if (!mv.validate(mySchritt.getProzess()))
					return "";
			}

			/* Imagevalidierung */
			if (mySchritt.isTypImagesSchreiben()) {
				MetadatenImagesHelper mih = new MetadatenImagesHelper(null, null);
				try {
					if (!mih.checkIfImagesValid(mySchritt.getProzess(), mySchritt.getProzess().getImagesOrigDirectory()))
						return "";
				} catch (Exception e) {
					Helper.setFehlerMeldung("Error on image validation: ", e);
				}
			}
		}
		List<PropertyTemplate> propList = mySchritt.getDisplayProperties().getPropertyTemplatesAsList();
		if (propList.size() > 0) {
			for (PropertyTemplate prop : propList) {
				if (prop.isIstObligatorisch() && (prop.getWert() == null || prop.getWert().equals(""))) {
					Helper.setFehlerMeldung(Helper.getTranslation("Eigenschaft") + " " + prop.getTitel() + " "
							+ Helper.getTranslation("requiredValue"));
					return "";
				}
			}
		}

		/* wenn das Ergebnis der Verifizierung ok ist, dann weiter, ansonsten schon vorher draussen */
		myDav.UploadFromHome(mySchritt.getProzess());
		mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
		new HelperSchritte().SchrittAbschliessen(mySchritt, false);
		return FilterAlleStart();
	}

	/*
	 * ##################################################### ##################################################### ## ## Eigenschaften bearbeiten ##
	 * ##################################################### ####################################################
	 */

	public String SchrittEigenschaftNeu() {
		mySchritt.setBearbeitungszeitpunkt(new Date());
		mySchrittEigenschaft = new Schritteigenschaft();
		return "";
	}

	public String SperrungAufheben() {
		MetadatenSperrung.UnlockProcess(mySchritt.getProzess().getId());
		return "";
	}

	public String SchrittEigenschaftUebernehmen() {
		mySchritt.setBearbeitungszeitpunkt(new Date());
		mySchritt.getEigenschaften().add(mySchrittEigenschaft);
		mySchrittEigenschaft.setSchritt(mySchritt);
		try {
			new SchrittDAO().save(mySchritt);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("error on saving step property", e);
		}
		mySchritt.refreshProperties();
		return "";
	}

	/*
	 * ##################################################### ##################################################### ## ## Korrekturmeldung an vorherige
	 * Schritte ## ##################################################### ####################################################
	 */

	@SuppressWarnings("unchecked")
	public List<Schritt> getPreviousStepsForProblemReporting() {
		List<Schritt> alleVorherigenSchritte = Helper.getHibernateSession().createCriteria(Schritt.class).add(
				Restrictions.lt("reihenfolge", mySchritt.getReihenfolge())).addOrder(Order.asc("reihenfolge")).createCriteria("prozess").add(
				Restrictions.idEq(mySchritt.getProzess().getId())).list();
		return alleVorherigenSchritte;
	}

	@SuppressWarnings("unchecked")
	public String ReportProblem() {
		myLogger.debug("mySchritt.ID: " + mySchritt.getId().intValue());
		myLogger.debug("Korrekturschritt.ID: " + myProblemID.intValue());
		myDav.UploadFromHome(mySchritt.getProzess());
		Date myDate = new Date();
		mySchritt.setBearbeitungsstatusEnum(StepStatus.LOCKED);
		mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
		HelperSchritte.updateEditing(mySchritt);
		mySchritt.setBearbeitungsbeginn(null);

		try {
			SchrittDAO dao = new SchrittDAO();
			Schritt temp = dao.get(myProblemID);
			temp.setBearbeitungsstatusEnum(StepStatus.OPEN);
			// if (temp.getPrioritaet().intValue() == 0)
			temp.setCorrectionStep();
			temp.setBearbeitungsende(null);
			Schritteigenschaft se = new Schritteigenschaft();
			se.setTitel("Korrektur notwendig");
			se.setWert(problemMessage);
			se.setType(PropertyType.messageError);
			se.setCreationDate(myDate);
			se.setSchritt(temp);
			temp.getEigenschaften().add(se);
			dao.save(temp);
			mySchritt.getProzess().getHistory().add(
					new HistoryEvent(myDate, temp.getReihenfolge().doubleValue(), temp.getTitel(), HistoryEventType.stepError, temp.getProzess()));
			/* alle Schritte zwischen dem aktuellen und dem Korrekturschritt wieder schliessen */
			// TODO: Use generics
			List<Schritt> alleSchritteDazwischen = Helper.getHibernateSession().createCriteria(Schritt.class).add(
					Restrictions.le("reihenfolge", mySchritt.getReihenfolge())).add(Restrictions.gt("reihenfolge", temp.getReihenfolge())).addOrder(
					Order.asc("reihenfolge")).createCriteria("prozess").add(Restrictions.idEq(mySchritt.getProzess().getId())).list();
			for (Iterator<Schritt> iter = alleSchritteDazwischen.iterator(); iter.hasNext();) {
				Schritt step = (Schritt) iter.next();
				step.setBearbeitungsstatusEnum(StepStatus.LOCKED);
				// if (step.getPrioritaet().intValue() == 0)
				step.setCorrectionStep();
				step.setBearbeitungsende(null);
				Schritteigenschaft seg = new Schritteigenschaft();
				seg.setTitel("Korrektur notwendig");
				seg.setWert(Messages.getString("KorrekturFuer") + temp.getTitel() + ": " + problemMessage);
				seg.setSchritt(step);
				seg.setType(PropertyType.messageImportant);
				seg.setCreationDate(new Date());
				step.getEigenschaften().add(seg);
				dao.save(step);
			}

			/* den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird */
			new ProzessDAO().save(mySchritt.getProzess());
		} catch (DAOException e) {
		}

		problemMessage = "";
		myProblemID = 0;
		return FilterAlleStart();
	}

	/*
	 * ##################################################### ##################################################### ## ## Problem-behoben-Meldung an
	 * nachfolgende Schritte ## ##################################################### ####################################################
	 */

	@SuppressWarnings("unchecked")
	public List<Schritt> getNextStepsForProblemSolution() {
		List<Schritt> alleNachfolgendenSchritte = Helper.getHibernateSession().createCriteria(Schritt.class).add(
				Restrictions.ge("reihenfolge", mySchritt.getReihenfolge())).add(Restrictions.eq("prioritaet", 10)).addOrder(Order.asc("reihenfolge"))
				.createCriteria("prozess").add(Restrictions.idEq(mySchritt.getProzess().getId())).list();
		return alleNachfolgendenSchritte;
	}

	@SuppressWarnings("unchecked")
	public String SolveProblem() {
		Date now = new Date();
		myDav.UploadFromHome(mySchritt.getProzess());
		mySchritt.setBearbeitungsstatusEnum(StepStatus.DONE);
		mySchritt.setBearbeitungsende(now);
		mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
		HelperSchritte.updateEditing(mySchritt);

		try {
			SchrittDAO dao = new SchrittDAO();
			Schritt temp = dao.get(mySolutionID);

			/* alle Schritte zwischen dem aktuellen und dem Korrekturschritt wieder schliessen */
			List<Schritt> alleSchritteDazwischen = Helper.getHibernateSession().createCriteria(Schritt.class).add(
					Restrictions.ge("reihenfolge", mySchritt.getReihenfolge())).add(Restrictions.le("reihenfolge", temp.getReihenfolge())).addOrder(
					Order.asc("reihenfolge")).createCriteria("prozess").add(Restrictions.idEq(mySchritt.getProzess().getId())).list();
			for (Iterator<Schritt> iter = alleSchritteDazwischen.iterator(); iter.hasNext();) {
				Schritt step = (Schritt) iter.next();
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
				seg.setTitel("Korrektur durchgefuehrt");
				seg.setWert(Messages.getString("KorrekturloesungFuer") + temp.getTitel() + ": " + solutionMessage);
				seg.setSchritt(step);
				seg.setType(PropertyType.messageImportant);
				seg.setCreationDate(new Date());
				step.getEigenschaften().add(seg);
				dao.save(step);
			}

			/* den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird */
			new ProzessDAO().save(mySchritt.getProzess());
		} catch (DAOException e) {
		}

		solutionMessage = "";
		mySolutionID = 0;
		return FilterAlleStart();
	}

	/*
	 * ##################################################### ##################################################### ## ## Upload und Download der
	 * Images ## ##################################################### ####################################################
	 */

	public String UploadFromHome() {
		HelperSchritte.updateEditing(mySchritt);
		myDav.UploadFromHome(mySchritt.getProzess());
		Helper.setMeldung(null, "Removed directory from user home", mySchritt.getProzess().getTitel());
		return "";
	}

	public String DownloadToHome() {
		try {
			File orig = new File(mySchritt.getProzess().getImagesOrigDirectory());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
		
		}
		HelperSchritte.updateEditing(mySchritt);
		myDav.DownloadToHome(mySchritt.getProzess(), mySchritt.getId().intValue(), !mySchritt.isTypImagesSchreiben());

		return "";
	}

	// TODO: Use generics
	@SuppressWarnings("unchecked")
	public String UploadFromHomeAlle() throws NumberFormatException, DAOException {
		List<String> fertigListe = myDav.UploadFromHomeAlle("fertig/");
		List<String> geprueft = new ArrayList<String>();
		/*
		 * -------------------------------- die hochgeladenen Prozess-IDs durchlaufen und auf abgeschlossen setzen --------------------------------
		 */
		if (fertigListe != null && fertigListe.size() > 0 && nurOffeneSchritte) {
			nurOffeneSchritte = false;
			FilterAlleStart();
		}
		for (Iterator<String> iter = fertigListe.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			String myID = element.substring(element.indexOf("[") + 1, element.indexOf("]")).trim();

			for (Iterator<Schritt> iterator = page.getCompleteList().iterator(); iterator.hasNext();) {
				Schritt step = (Schritt) iterator.next();
				/* nur wenn der Schritt bereits im Bearbeitungsmodus ist, abschliessen */
				if (step.getProzess().getId().intValue() == Integer.parseInt(myID) && step.getBearbeitungsstatusEnum() == StepStatus.INWORK) {
					mySchritt = step;
					if (SchrittDurchBenutzerAbschliessen() != "")
						geprueft.add(element);
					mySchritt.setEditTypeEnum(StepEditType.MANUAL_MULTI);
				}
			}
		}

		myDav.removeFromHomeAlle(geprueft, "fertig/");
		Helper.setMeldung(null, "removed " + geprueft.size() + " directories from user home:", "fertig/");
		return "";
	}

	@SuppressWarnings("unchecked")
	public String DownloadToHomePage() {
		ProzessDAO dao = new ProzessDAO();
		for (Iterator<Schritt> iter = page.getListReload().iterator(); iter.hasNext();) {
			Schritt step = (Schritt) iter.next();
			if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN) {
				step.setBearbeitungsstatusEnum(StepStatus.INWORK);
				step.setEditTypeEnum(StepEditType.MANUAL_MULTI);
				HelperSchritte.updateEditing(step);
				step.setBearbeitungsbeginn(new Date());
				Prozess proz = step.getProzess();
				try {
					dao.save(proz);
				} catch (DAOException e) {
					Helper.setMeldung("fehlerNichtSpeicherbar" + proz.getTitel());
				}
				myDav.DownloadToHome(proz, step.getId().intValue(), false);
			}
		}
		// calcHomeImages();
		Helper.setMeldung(null, "Created directies in user home", "");
		return "";
	}

	@SuppressWarnings("unchecked")
	public String DownloadToHomeHits() {
		ProzessDAO dao = new ProzessDAO();
		for (Iterator<Schritt> iter = page.getCompleteList().iterator(); iter.hasNext();) {
			Schritt step = (Schritt) iter.next();
			if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN) {
				step.setBearbeitungsstatusEnum(StepStatus.INWORK);
				step.setEditTypeEnum(StepEditType.MANUAL_MULTI);
				HelperSchritte.updateEditing(step);
				step.setBearbeitungsbeginn(new Date());
				Prozess proz = step.getProzess();
				try {
					dao.save(proz);
				} catch (DAOException e) {
					Helper.setMeldung("fehlerNichtSpeicherbar" + proz.getTitel());
				}
				myDav.DownloadToHome(proz, step.getId().intValue(), false);
			}
		}
		// calcHomeImages();
		Helper.setMeldung(null, "Created directories in user home", "");
		return "";
	}

	public String getScriptPath() {

		return scriptPath;
	}

	public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}

	public void executeScript() {
	
			try {
				new HelperSchritte().executeScript(mySchritt, scriptPath, false);
			} catch (SwapException e) {
				myLogger.error(e);
			}
		
	}

	/**
	 * call module for this step ================================================================
	 * 
	 * @throws IOException
	 */
	public void executeModule() {
		Helper.setMeldung("call module");
		ModuleServerForm msf = (ModuleServerForm) Helper.getManagedBeanValue("#{ModuleServerForm}");
		String url = null;
		try {
			url = msf.startShortSession(mySchritt);
			Helper.setMeldung(url);
		} catch (GoobiException e) {
			Helper.setFehlerMeldung("GoobiException: " + e.getMessage());
			return;
		} catch (XmlRpcException e) {
			Helper.setMeldung("XmlRpcException: " + e.getMessage());
			return;
		}
		Helper.setMeldung("module called");
		if (url != null && url.length() > 0) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			if (!facesContext.getResponseComplete()) {
				HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
				try {
					response.sendRedirect(url);
				} catch (IOException e) {
					Helper.setFehlerMeldung("IOException: " + e.getMessage());
				}
				facesContext.responseComplete();
			}
		}
	}

	public int getHomeBaende() {
		// return myDav.getAnzahlBaende("");
		return 0;
	}

	public int getAllImages() {
		return gesamtAnzahlImages;
	}

	public int getPageImages() {
		return pageAnzahlImages;
	}

	@SuppressWarnings("unchecked")
	public void calcHomeImages() {
		gesamtAnzahlImages = 0;
		pageAnzahlImages = 0;
		Benutzer aktuellerBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
		if (aktuellerBenutzer != null && aktuellerBenutzer.isMitMassendownload()) {
			// TODO: Try to avoid Iterators, use for loops instead
			for (Iterator<Schritt> iter = page.getCompleteList().iterator(); iter.hasNext();) {
				Schritt step = (Schritt) iter.next();
				try {
					if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN) {
						// gesamtAnzahlImages += myDav.getAnzahlImages(step.getProzess().getImagesOrigDirectory());
						// TODO: Remove hard coded extension
						gesamtAnzahlImages += FileUtils.getNumberOfFiles(step.getProzess().getImagesOrigDirectory());
					}
				} catch (Exception e) {
					myLogger.error(e);
				}
			}

			// for (Iterator iter = page.getList().iterator(); iter.hasNext();) {
			// Schritt step = (Schritt) iter.next();
			// try {
			// if (step.getBearbeitungsstatus().intValue() == 1)
			// pageAnzahlImages += myDav.getAnzahlImages(step.getProzess().getOrigPfad());
			// } catch (IOException e) {
			// myLogger.error(e);
			// } catch (InterruptedException e) {
			// myLogger.error(e);

			// }
			// }
		}
	}

	/*
	 * ##################################################### ##################################################### ## ## Getter und Setter ##
	 * ##################################################### ####################################################
	 */

	public Prozess getMyProzess() {
		return myProzess;
	}

	public void setMyProzess(Prozess myProzess) {
		this.myProzess = myProzess;
	}

	public Schritt getMySchritt() {
		try {
			schrittPerParameterLaden();
		} catch (NumberFormatException e) {
			myLogger.error(e);
		} catch (DAOException e) {
			myLogger.error(e);
		}
		return mySchritt;
	}

	public void setMySchritt(Schritt mySchritt) {
		modusBearbeiten = "";
		this.mySchritt = mySchritt;
	}

	public String getModusBearbeiten() {
		return modusBearbeiten;
	}

	public void setModusBearbeiten(String modusBearbeiten) {
		this.modusBearbeiten = modusBearbeiten;
	}

	public Integer getMyProblemID() {
		return myProblemID;
	}

	public void setMyProblemID(Integer myProblemID) {
		this.myProblemID = myProblemID;
	}

	public Integer getMySolutionID() {
		return mySolutionID;
	}

	public void setMySolutionID(Integer mySolutionID) {
		this.mySolutionID = mySolutionID;
	}

	public String getProblemMessage() {
		return problemMessage;
	}

	public void setProblemMessage(String problemMessage) {
		this.problemMessage = problemMessage;
	}

	public String getSolutionMessage() {
		return solutionMessage;
	}

	public void setSolutionMessage(String solutionMessage) {
		this.solutionMessage = solutionMessage;
	}

	public Schritteigenschaft getMySchrittEigenschaft() {
		return mySchrittEigenschaft;
	}

	public void setMySchrittEigenschaft(Schritteigenschaft mySchrittEigenschaft) {
		this.mySchrittEigenschaft = mySchrittEigenschaft;
	}

	/*
	 * ##################################################### ##################################################### ## ## Parameter per Get Ã¼bergeben
	 * bekommen und entsprechen den passenden Schritt laden ## #####################################################
	 * ####################################################
	 */

	/**
	 * prüfen, ob per Parameter vielleicht zunÃ¤chst ein anderer geladen werden soll
	 * 
	 * @throws DAOException
	 *             , NumberFormatException
	 */
	private void schrittPerParameterLaden() throws DAOException, NumberFormatException {
		String param = Helper.getRequestParameter("myid");
		if (param != null && !param.equals("")) {
			/*
			 * wenn bisher noch keine aktuellen Schritte ermittelt wurden, dann dies jetzt nachholen, damit die Liste vollstÃ¤ndig ist
			 */
			if (page == null && (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}") != null)
				FilterAlleStart();
			Integer inParam = Integer.valueOf(param);
			if (mySchritt == null || mySchritt.getId() == null || !mySchritt.getId().equals(inParam))
				mySchritt = new SchrittDAO().get(inParam);
		}
	}


	/*
	 * ========================================================
	 * 
	 * Auswahl mittels Selectboxen
	 * 
	 * ========================================================
	 */

	// TODO: Try to avoid Iterators, usr for loops instead
	@SuppressWarnings("unchecked")
	public void SelectionAll() {
		for (Iterator<Schritt> iter = page.getList().iterator(); iter.hasNext();) {
			Schritt s = (Schritt) iter.next();
			s.setSelected(true);
		}
	}

	// TODO: Try to avoid Iterators, usr for loops instead
	@SuppressWarnings("unchecked")
	public void SelectionNone() {
		for (Iterator<Schritt> iter = page.getList().iterator(); iter.hasNext();) {
			Schritt s = (Schritt) iter.next();
			s.setSelected(false);
		}
	}

	/*
	 * ##################################################### ##################################################### ## ## Downloads ##
	 * ##################################################### ####################################################
	 */

	public void DownloadTiffHeader() throws IOException {
		TiffHeader tiff = new TiffHeader(mySchritt.getProzess());
		tiff.ExportStart();
	}

	/*
	 * public void DownloadRusExport() throws IOException, ReadException, InterruptedException, PreferencesException, SwapException, DAOException,
	 * WriteException { RusslandExport rus = new RusslandExport(mySchritt.getProzess()); rus.ExportStart(); }
	 */

	public void ExportDMS() {
		ExportDms export = new ExportDms();
		try {
			export.startExport(mySchritt.getProzess());
		} catch (Exception e) {
			Helper.setFehlerMeldung("Error on export", e.getMessage());
			myLogger.error(e);
		}
	}

	public boolean isNurOffeneSchritte() {
		return nurOffeneSchritte;
	}

	public void setNurOffeneSchritte(boolean nurOffeneSchritte) {
		this.nurOffeneSchritte = nurOffeneSchritte;
	}

	public boolean isNurEigeneSchritte() {
		return nurEigeneSchritte;
	}

	public void setNurEigeneSchritte(boolean nurEigeneSchritte) {
		this.nurEigeneSchritte = nurEigeneSchritte;
	}

	public HashMap<String, Boolean> getAnzeigeAnpassen() {
		return anzeigeAnpassen;
	}

	public void setAnzeigeAnpassen(HashMap<String, Boolean> anzeigeAnpassen) {
		this.anzeigeAnpassen = anzeigeAnpassen;
	}

	/**
	 * @return values for wiki field
	 */
	public String getWikiField() {
		return mySchritt.getProzess().getWikifield();

	}

	/**
	 * sets new value for wiki field
	 * 
	 * @param inString
	 */
	public void setWikiField(String inString) {
		mySchritt.getProzess().setWikifield(inString);
		// try {
		// new ProzessDAO().save(mySchritt.getProzess());
		// } catch (DAOException e) {
		// myLogger.error(e);
		// }
	}
}
