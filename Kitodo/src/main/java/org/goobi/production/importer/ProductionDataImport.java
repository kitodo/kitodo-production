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

package org.goobi.production.importer;

import com.thoughtworks.xstream.XStream;

import de.sub.goobi.config.ConfigMain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.ProcessProperty;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.TemplateProperty;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.beans.WorkpieceProperty;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.HistoryType;
import org.kitodo.data.database.helper.enums.PropertyType;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.HibernateUtilOld;
import org.kitodo.services.ProcessService;
import org.kitodo.services.ProjectService;
import org.kitodo.services.RulesetService;
import org.kitodo.services.UserGroupService;

/**
 * 
 * @author Robert Sehr
 * 
 */
public class ProductionDataImport {
	
	// TODO Namen mit Rolfs Liste abgleichen

	private static final Logger logger = Logger.getLogger(ProductionDataImport.class);
	private final static String conflictFilename = "propertiesWithoutProcess.xml";
	private String filename;
	private ArrayList<ImportConflicts> conflicts = new ArrayList<ImportConflicts>();
	private List<Project> projectList = new ArrayList<Project>();
	private Project altdaten = null;
	private ProcessService processService = new ProcessService();
	private ProjectService projectService = new ProjectService();
	private RulesetService rulesetService = new RulesetService();
	private UserGroupService userGroupService = new UserGroupService();
	private Session session;

	private ProductionDataImport() throws IOException {
		session = HibernateUtilOld.getSessionFactory().openSession();
		altdaten = generateProject();
		try {
			projectService.save(altdaten);
			projectList = projectService.search("from Project");
		} catch (DAOException e) {
			System.exit(1);
		}
	}

	/**
	 * Diese Methode darf nur ein Mal durchlaufen, sonst werden zu viele Properties hinzugefügt!!!!
	 *
	 * @throws ConfigurationException
	 * @throws DAOException
	 * @throws FileNotFoundException
	 */

	public static void main(String[] args) throws HibernateException, SQLException, ConfigurationException, DAOException,
			FileNotFoundException, IOException {
		new ProductionDataImport().importData();

	}

	@SuppressWarnings("unchecked")
	private void importData() throws DAOException, HibernateException, ConfigurationException, SQLException,
			FileNotFoundException {
		filename = ConfigMain.getParameter("tempfolder") + "produktionsDb.xml";
		// load data from xml
		logger.debug("Load Production Data from xml.");
		ArrayList<ProductionData> dataList = load(filename);
		if(logger.isDebugEnabled()){
			logger.debug("Got " + dataList.size() + " items");
		}

		Process template = new Process();
		template.setProject(altdaten);
		template.setTitle("Altdatenvorlage");
		template.setTemplate(true);

		// gdz Regelsatz
		Ruleset ruleset = rulesetService.find(Integer.valueOf(17));

		template.setRuleset(ruleset);

		session.save(template);
		List<Task> step = getSteps(template);
		template.setTasks(step);
		for (Task s : step) {
			session.save(s);
		}
		session.save(template);
		int i = 0;
		int newProj = 0;
		int oldProj = 0;
		for (ProductionData pd : dataList) {
			String ppn = pd.getWERKPPNDIGITAL();
			if (ppn != null && ppn.length() > 0) {
				// get ppn.
				int ppnIndex = ppn.indexOf("PPN");
				if (ppnIndex == -1) {
					ppnIndex = ppn.indexOf("ppn");
				}
				if (ppnIndex != -1) {
					ppn = ppn.substring(ppnIndex + 3);
					logger.debug(ppn);

					// get all werkstueckeigenschaften for this ppn
					ArrayList<String> ppnlist = new ArrayList<String>();
					ppnlist.add(ppn);
					ppnlist.add("PPN" + ppn);
					Criteria crit = session.createCriteria(WorkpieceProperty.class).add(Restrictions.in("wert", ppnlist));
					ArrayList<WorkpieceProperty> weList = new ArrayList<WorkpieceProperty>();
					weList.addAll(crit.list());
					// add properties to existing prozess
					boolean added = false;
					if (weList.size() > 0) {
						WorkpieceProperty we = weList.get(0);
						Workpiece w = we.getWorkpiece();
						if (w != null) {
							Process p = w.getProcess();
							if (p != null) {
								if(logger.isDebugEnabled()){
									logger.debug("Add new Properties for Process : " + p.getTitle());
								}
								addNewPropertiesForExistingProcesses(session, p.getId(), pd);
								added = true;
								oldProj++;
							}
						}
					}
					// generate new properties for new process
					if (!added) {
						logger.debug("Add new Properties for new process");
						generateNewPropertiesForNewProzess(session, ruleset, altdaten, pd);
						newProj++;
					}
				}
			}
			if (++i % 40 == 0) {
				session.flush();
				session.clear();
			}
		}
		if (conflicts != null) {
			XStream xstream = new XStream();
			xstream.setMode(XStream.NO_REFERENCES);
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(new File(ConfigMain.getParameter("tempfolder") + conflictFilename)),
					StandardCharsets.UTF_8);
			xstream.toXML(conflicts, fw);
		}
		if(logger.isDebugEnabled()){
			logger.debug("Neue Prozesse: " + newProj);
			logger.debug("Zu " + oldProj + " existierenden Prozessen wurden Properties hinzugefügt");
		}
	}

	private void generateNewPropertiesForNewProzess(Session session, Ruleset ruleset, Project project, ProductionData pd)
			throws HibernateException, SQLException {

		// generate new Process

		Process prozess = new Process();

		prozess.setProject(getProjekt(pd));
		String title = pd.getWERKATS() + "_" + pd.getWERKPPNDIGITAL();
		title = title.replaceAll("\\W", "");
		prozess.setTitle(title);
		prozess.setTemplate(false);
		prozess.setRuleset(ruleset);
		Workpiece werk = new Workpiece();
		werk.setProcess(prozess);

		WorkpieceProperty we = new WorkpieceProperty();
		we.setWorkpiece(werk);
		we.setTitle("PPN");
		String ppn = pd.getWERKPPNDIGITAL();

		// get ppn.
		int ppnIndex = ppn.indexOf("PPN");
		if (ppnIndex == -1) {
			ppnIndex = ppn.indexOf("ppn");
		}
		if (ppnIndex != -1) {
			ppn = ppn.substring(ppnIndex + 3);
		}

		we.setValue(ppn);
		if (werk.getProperties() == null) {
			werk.setProperties(new ArrayList<WorkpieceProperty>());
		}
		werk.getProperties().add(we);

		Template v = new Template();
		v.setProcess(prozess);

		if (prozess.getWorkpieces() == null) {
			ArrayList<Workpiece> werkstueckeSet = new ArrayList<>();
			prozess.setWorkpieces(werkstueckeSet);
		}
		if (prozess.getTemplates() == null) {
			ArrayList<Template> vorlagenSet = new ArrayList<>();
			prozess.setTemplates(vorlagenSet);
		}
		prozess.getWorkpieces().add(werk);
		prozess.getTemplates().add(v);

		session.save(werk);
		session.save(we);
		session.save(v);
		session.save(prozess);
		List<Task> step = getSteps(prozess);
		prozess.setTasks(step);
		for (Task s : step) {
			session.save(s);
		}

		session.save(prozess);
		try {
			getNewPropertiesForNewProcesses(session, prozess, pd);
		} catch (ConfigurationException e) {
			logger.error(e);
		}

	}

	private void generateWerkProperty(Session session, Workpiece w, String name, String value, PropertyType type, Integer position, boolean required) {
		if (value != null) {
			WorkpieceProperty property = new WorkpieceProperty();
			property.setObligatory(required);
			property.setTitle(name);
			property.setWorkpiece(w);
			property.setValue(value);
			property.setType(type);
			w.getProperties().add(property);
			session.saveOrUpdate(w);
			session.saveOrUpdate(property);
		}
	}

	private void generateVorlageProperty(Session session, Template s, String name, String value, PropertyType type, Integer position, boolean required) {
		if (value != null) {
			TemplateProperty property = new TemplateProperty();
			property.setObligatory(required);
			property.setTitle(name);
			property.setTemplate(s);
			property.setValue(value);
			property.setType(type);
			s.getProperties().add(property);
			session.saveOrUpdate(s);
			session.saveOrUpdate(property);
		}
	}

	private void generateProzessProperty(Session session, Process s, String name, String value, PropertyType type, Integer position, boolean required) {
		if (value != null) {
			ProcessProperty property = new ProcessProperty();
			property.setObligatory(required);
			property.setTitle(name);
			property.setProcess(s);
			property.setValue(value);
			property.setType(type);
			s.getProperties().add(property);
			session.saveOrUpdate(s);
			session.saveOrUpdate(property);
		}
	}

	private void getNewPropertiesForNewProcesses(Session session, Process prozess, ProductionData pd) throws HibernateException, SQLException,
			ConfigurationException {


		// Generate Properties
		prozess.setCreationDate(pd.getDATUMAUFNAHMEWERK());
		generateProzessProperty(session, prozess, "ImportMarker", "created", PropertyType.String, 0, false);
		// ATS
		generateProzessProperty(session, prozess, "ATS", pd.getWERKATS(), PropertyType.String, 0, false);
		// Auftragsnummer
		generateProzessProperty(session, prozess, "Auftragsnummer", pd.getAUFTRAGSNUMMER(), PropertyType.String, 0, false);
		// BEMERKUNG
		generateProzessProperty(session, prozess, "Kommentar", pd.getBEMERKUNG(), PropertyType.String, 0, false);
		// KOMMENTAR
		generateProzessProperty(session, prozess, "Bemerkung", pd.getKOMMENTAR(), PropertyType.String, 0, false);

		// FEHLERKOMMENTAR
		generateProzessProperty(session, prozess, "FehlerKommentar", pd.getFEHLERKOMMENTAR(), PropertyType.String, 0, false);

		// AUFTRAGGEBER
		generateProzessProperty(session, prozess, "Auftraggeber", String.valueOf(pd.getAUFTRAGGEBER()), PropertyType.Integer, 0, false);
		if (prozess.getProject().getTitle().equals("DigiWunschBuch")) {

			// BEMERKUNG2
			// TODO besseren Namen finden
			generateProzessProperty(session, prozess, "Bemerkung2", String.valueOf(pd.getBEMERKUNG2()), PropertyType.Integer, 0, false);

			// XSLSHEET
			// TODO besseren Namen finden
			generateProzessProperty(session, prozess, "XslSheet", String.valueOf(pd.getXSLSHEET()), PropertyType.Integer, 0, false);

			// SponsorNaming
			generateProzessProperty(session, prozess, "Patennennung", String.valueOf(pd.getPatennennung()), PropertyType.Integer, 0, false);

			// Patenname
			generateProzessProperty(session, prozess, "Patenname", pd.getPatenname(), PropertyType.String, 0, false);

			// StempelGesetzt
			generateProzessProperty(session, prozess, "Stempel gesetzt", String.valueOf(pd.getStempelGesetzt()), PropertyType.Integer, 0, false);

			// xmlTag
			generateProzessProperty(session, prozess, "xml-Tag", String.valueOf(pd.getXmlTag()), PropertyType.Integer, 0, false);

			// otrsID
			generateProzessProperty(session, prozess, "OTRS-ID", pd.getOtrsID(), PropertyType.String, 0, false);

			// versandErfolgt
			generateProzessProperty(session, prozess, "Versand", String.valueOf(pd.getVersandErfolgt()), PropertyType.Integer, 0, false);

			// pdfErstellt
			generateProzessProperty(session, prozess, "PDF erstellt", String.valueOf(pd.getPdfErstellt()), PropertyType.Integer, 0, false);
		}

		Workpiece werkstueck = prozess.getWorkpieces().get(0);
		String ppn = pd.getWERKPPNDIGITAL();
		if (ppn != null) {
			if (ppn.startsWith("ppn") || ppn.startsWith("PPN")) {
				ppn = ppn.substring(3);
			}
			generateWerkProperty(session, werkstueck, "PPN digital f-Satz", ppn, PropertyType.String, 0, false);
		}

		Template v = prozess.getTemplates().get(0);
		ppn = pd.getWERKPPNANALOG();
		if (ppn != null) {
			if (ppn.startsWith("ppn") || ppn.startsWith("PPN")) {
				ppn = ppn.substring(3);
			}
			generateVorlageProperty(session, v, "PPN analog f-Satz", ppn, PropertyType.String, 0, false);
			generateVorlageProperty(session, v, "Signatur", pd.getWERKSIGNATUR(), PropertyType.String, 0, false);

		}
		for (Task s : prozess.getTasks()) {
			if (s.getTitle().contains("Bibliographisch") || (s.getTitle().contains("bibliographische "))) {
				s.setProcessingEnd(pd.getDATUMAUFNAHMEWERK());
				s.setEditTypeEnum(TaskEditType.ADMIN);
				s.setProcessingStatusEnum(TaskStatus.DONE);
			}
			/********************************************
			 * step 'scannen' *
			 *******************************************/

			if (s.getTitle().contains("scan") || s.getTitle().contains("Scan")) {
				s.setProcessingEnd(pd.getWERKSCANDATUM());
				s.setEditTypeEnum(TaskEditType.ADMIN);
				s.setProcessingStatusEnum(TaskStatus.DONE);
				
				// WERKSCANSEITEN
				generateProzessProperty(session, prozess, "Seitenanzahl", String.valueOf(pd.getWERKSCANSEITEN()), PropertyType.Integer, 0, false);

				prozess.setSortHelperImages(pd.getWERKSCANSEITEN());

				// SCANNERTYP
				generateProzessProperty(session, prozess, "Scangerät", pd.getSCANNERTYP(), PropertyType.String, 0, false);

				// DRUCKQUALITAET
				generateProzessProperty(session, prozess, "Druckqualität", String.valueOf(pd.getDRUCKQUALITAET()), PropertyType.Integer, 0, false);

			}

			/********************************************
			 * step 'Qualitätskontrolle' *
			 *******************************************/
			else if (s.getTitle().contains("Qualitaetskontrolle")) {
				s.setProcessingEnd(pd.getWERKQKONTROLLDATUM());
				s.setProcessingStatusEnum(TaskStatus.DONE);
				s.setEditTypeEnum(TaskEditType.ADMIN);

				/********************************************
				 * step 'Imagenachbearbeitung' *
				 *******************************************/
			} else if (s.getTitle().contains("Imagenachbearbeitung")) {
				s.setProcessingStatusEnum(TaskStatus.DONE);
				s.setEditTypeEnum(TaskEditType.ADMIN);

			
				// BITONALIMAGENACHBEARBEITUNG
				generateProzessProperty(session, prozess, "BitonalImageNachbearbeitung", pd.getBITONALIMAGENACHBEARBEITUNG(), PropertyType.String, 0, false);

				// GRAUIMAGENACHBEARBEITUNG
				generateProzessProperty(session, prozess, "GrayscaleImageNachbearbeitung", pd.getGRAUIMAGENACHBEARBEITUNG(), PropertyType.String, 0, false);

				// FARBEIMAGENACHBEARBEITUNG
				generateProzessProperty(session, prozess, "ColorImageNachbearbeitung", pd.getFARBEIMAGENACHBEARBEITUNG(), PropertyType.String, 0, false);

				// FARBGRAUABB
				// TODO Name
				generateProzessProperty(session, prozess, "FarbGrauAbb", String.valueOf(pd.getFARBGRAUABB()), PropertyType.Integer, 0, false);

				// ImageNachbearbBitonalDatum
				generateProzessProperty(session, prozess, "DatumBitonalImageNachbearbeitung", String.valueOf(pd.getImageNachbearbBitonalDatum()),
						PropertyType.Date, 0, false);

				// ImageNachbearbBitonalPerson
				// generateProzessProperty(session, prozess, "PersonBitonalImageCorrection", pd.getImageNachbearbBitonalPerson(), PropertyType.String, 0,
				// false);

				// ImageNachbearbGrauDatum
				generateProzessProperty(session, prozess, "DatumGrauImageNachbearbeitung", String.valueOf(pd.getImageNachbearbGrauDatum()), PropertyType.Date,
						0, false);

				// ImageNachbearbGrauPerson
				// generateProzessProperty(session, prozess, "PersonGrayscaleImageCorrection", pd.getImageNachbearbGrauPerson(), PropertyType.String, 0,
				// false);

				// ImageNachbearbFarbeDatum
				generateProzessProperty(session, prozess, "DatumFarbImageNachbearbeitung", String.valueOf(pd.getImageNachbearbFarbeDatum()),
						PropertyType.Date, 0, false);

				// ImageNachbearbFarbePerson
				// generateProzessProperty(session, prozess, "PersonColorImageCorrection", pd.getImageNachbearbFarbePerson(), PropertyType.String, 0, false);

				/********************************************
				 * step 'Archiv' *
				 *******************************************/
			} else if (s.getTitle().contains("Archivierung")) {
				s.setProcessingEnd(pd.getImportDatum());
				s.setProcessingStatusEnum(TaskStatus.DONE);
				s.setEditTypeEnum(TaskEditType.ADMIN);

				// CDSICHERUNG
				generateProzessProperty(session, prozess, "CD-Sicherung-BK", pd.getCDSICHERUNG(), PropertyType.String, 0, false);

				// MAARCHIV
				generateProzessProperty(session, prozess, "MA-ArchivNr", pd.getMAARCHIV(), PropertyType.String, 0, false);

			} else if (s.getTitle().contains("Import von CD")) {
				s.setProcessingBegin(pd.getImportDatum());
				s.setProcessingStatusEnum(TaskStatus.OPEN);
			}
		}
		session.flush();
		session.clear();
	}

	private void addNewPropertiesForExistingProcesses(Session session, int pId, ProductionData pd) throws HibernateException, SQLException,
			ConfigurationException {

		// Prozess holen
		Process p = null;
		Criteria crit = session.createCriteria(Process.class).add(Restrictions.eq("id", pId));
		if (crit.list().size() > 0) {
			p = (Process) crit.list().get(0);
		}
		if (p == null)
			return;

		/*
		 * properties and attributes for processes
		 */
		generateProzessProperty(session, p, "ImportMarker", "merged", PropertyType.String, 0, false);

		// AutorTitelSchluessel
		generateProzessProperty(session, p, "ATS", pd.getWERKATS(), PropertyType.String, 0, false);

		// Auftragsnummer
		generateProzessProperty(session, p, "Auftragsnummer", pd.getAUFTRAGSNUMMER(), PropertyType.String, 0, false);

		// BEMERKUNG

		generateProzessProperty(session, p, "Bemerkung", pd.getBEMERKUNG(), PropertyType.String, 0, false);

		// KOMMENTAR
		generateProzessProperty(session, p, "Kommentar", pd.getKOMMENTAR(), PropertyType.String, 0, false);

		// FEHLERKOMMENTAR
		generateProzessProperty(session, p, "Fehlerkommentar", pd.getFEHLERKOMMENTAR(), PropertyType.String, 0, false);

		// AUFTRAGGEBER
		generateProzessProperty(session, p, "Auftraggeber", String.valueOf(pd.getAUFTRAGGEBER()), PropertyType.Integer, 0, false);

		// BEMERKUNG2
		// TODO Name
		generateProzessProperty(session, p, "Bemerkung2", String.valueOf(pd.getBEMERKUNG2()), PropertyType.Integer, 0, false);

		// XSLSHEET
		// TODO Name
		generateProzessProperty(session, p, "XslSheet", String.valueOf(pd.getXSLSHEET()), PropertyType.Integer, 0, false);

		/********************************************
		 * DigiWunschbuch Sponsor *
		 *******************************************/
		List<ProcessProperty> eig = p.getProperties();
		if (p.getProject().getTitle().equals("DigiWunschBuch")) {
			boolean sponsor = false;

			for (ProcessProperty pe : eig) {
				if (pe.getTitle().contains("Besteller") && (pe.getValue() != null)) {
					sponsor = true;
				}
			}

			if (!sponsor) {
				// SponsorNaming
				generateProzessProperty(session, p, "Patennennung", String.valueOf(pd.getPatennennung()), PropertyType.Integer, 0, false);

				// Patenname
				generateProzessProperty(session, p, "Patenname", pd.getPatenname(), PropertyType.String, 0, false);

				// StempelGesetzt
				generateProzessProperty(session, p, "Stempel gesetzt", String.valueOf(pd.getStempelGesetzt()), PropertyType.Integer, 0, false);

				// xmlTag
				generateProzessProperty(session, p, "xml-Tag", String.valueOf(pd.getXmlTag()), PropertyType.Integer, 0, false);

				// otrsID
				generateProzessProperty(session, p, "OTRS-ID", pd.getOtrsID(), PropertyType.String, 0, false);

				// versandErfolgt
				generateProzessProperty(session, p, "Versand", String.valueOf(pd.getVersandErfolgt()), PropertyType.Integer, 0, false);

				// pdfErstellt
				generateProzessProperty(session, p, "PDF erstellt", String.valueOf(pd.getPdfErstellt()), PropertyType.Integer, 0, false);
			}
		}
		List<Workpiece> wl = p.getWorkpieces();
		boolean ppndigital = false;
		boolean ppnconflict = false;
		for (Workpiece w : wl) {
			List<WorkpieceProperty> wel = w.getProperties();

			String ppn = pd.getWERKPPNDIGITAL();
			if (ppn.startsWith("ppn") || ppn.startsWith("PPN")) {
				ppn = ppn.substring(3);
			}
			for (WorkpieceProperty we : wel) {
				if (we.getTitle().contains("PPN digital")) {
					if (we.getValue() == null) {
						ppndigital = true;
						we.setValue(pd.getWERKPPNDIGITAL());
					} else if (we.getValue().contains(ppn)) {
						ppndigital = true;
					} else if (!we.getValue().contains(ppn)) {
						ppnconflict = true;
					}
				}
			}
			if (!ppndigital && ppnconflict) {
				conflicts.add(new ImportConflicts(String.valueOf(w.getId()), "PPN digital f-Satz", "", pd.getWERKPPNDIGITAL()));
			}
		}

		if (!ppndigital) {
			Workpiece newWerk = new Workpiece();
			newWerk.setProcess(p);
			session.save(newWerk);
			// PPN digital f-Satz
			newWerk.setProcess(p);

			generateWerkProperty(session, newWerk, "PPN digital f-Satz", pd.getWERKPPNDIGITAL(), PropertyType.String, 0, false);
		}
		ppnconflict = false;
		boolean signatur = false;
		boolean ppnanalog = false;
		boolean sigconflict = false;
		Template newVorlage = new Template();
		newVorlage.setProcess(p);
		String ppn = pd.getWERKPPNANALOG();
		if (ppn != null) {
			if (ppn.startsWith("ppn") || ppn.startsWith("PPN")) {
				ppn = ppn.substring(3);
			}
			for (Template v : p.getTemplates()) {
				for (TemplateProperty ve : v.getProperties()) {
					if (pd.getWERKSIGNATUR() != null) {
						if (ve.getTitle().contains("Signatur")) {
							newVorlage = v;
							if (ve.getValue() == null) {
								signatur = true;
								ve.setValue(pd.getWERKSIGNATUR());
							} else if (ve.getValue().contains(pd.getWERKSIGNATUR())) {
								signatur = true;
							} else {
								sigconflict = true;
							}
						}
					}
					if (ve.getTitle().contains("PPN analog")) {
						newVorlage = v;
						if (ve.getValue() == null) {
							ppnanalog = true;
							ve.setValue(pd.getWERKPPNANALOG());
						} else if (ve.getValue().contains(ppn)) {
							ppnanalog = true;
						} else {
							ppnconflict = true;
						}

					}

				}
				if (!ppnanalog && ppnconflict) {
					conflicts.add(new ImportConflicts(String.valueOf(v.getId()), "PPN analog", "", ppn + " or " + "PPN" + ppn));
				}
				if (!signatur && sigconflict) {
					conflicts.add(new ImportConflicts(String.valueOf(v.getId()), "Signatur", "", pd.getWERKSIGNATUR()));
				}
			}
			if (newVorlage.getId() == null) {
				session.save(newVorlage);
			}
			if (!signatur) {
				// WERKSIGNATUR

				generateVorlageProperty(session, newVorlage, "Signatur", pd.getWERKSIGNATUR(), PropertyType.String, 0, false);
			}
			if (!ppnanalog) {

				generateVorlageProperty(session, newVorlage, "PPN analog f-Satz", pd.getWERKPPNANALOG(), PropertyType.String, 0, false);

			}

		}

		List<Task> stepList = p.getTasks();

		for (Task s : stepList) {

			/********************************************
			 * step 'scannen' *
			 * TODO einzelne farbformate
			 *******************************************/
			if (s.getTitle().contains("scan") || s.getTitle().contains("Scan")) {
				boolean pages = false;
				boolean scangeraet = false;

				if (!pages) {
					// WERKSCANSEITEN
					generateProzessProperty(session, p, "Seitenanzahl", String.valueOf(pd.getWERKSCANSEITEN()), PropertyType.Integer, 0, false);

				}
				if (!scangeraet) {
					// SCANNERTYP
					generateProzessProperty(session, p, "Scangerät", pd.getSCANNERTYP(), PropertyType.String, 0, false);
				}
				// DRUCKQUALITAET
				generateProzessProperty(session, p, "Druckqualität", String.valueOf(pd.getDRUCKQUALITAET()), PropertyType.Integer, 0, false);
			}
			/********************************************
			 * step 'Qualitätskontrolle' *
			 *******************************************/
			else if (s.getTitle().contains("Qualitätskontrolle")) {
				if (s.getProcessingEnd() == null) {
					s.setProcessingEnd(pd.getWERKQKONTROLLDATUM());
				}

				/********************************************
				 * step 'Imagenachbearbeitung' *
				 *******************************************/
			} else if (s.getTitle().contains("Imagenachbearbeitung")) {

				generateProzessProperty(session, p, "BitonalImageNachbearbeitung", pd.getBITONALIMAGENACHBEARBEITUNG(), PropertyType.String, 0, false);

				// GRAUIMAGENACHBEARBEITUNG
				generateProzessProperty(session, p, "GrauImageNachbearbeitung", pd.getGRAUIMAGENACHBEARBEITUNG(), PropertyType.String, 0, false);

				// FARBEIMAGENACHBEARBEITUNG
				generateProzessProperty(session, p, "FarbeImageNachbearbeitung", pd.getFARBEIMAGENACHBEARBEITUNG(), PropertyType.String, 0, false);

				// FARBGRAUABB
				generateProzessProperty(session, p, "FarbgrauABB", String.valueOf(pd.getFARBGRAUABB()), PropertyType.Integer, 0, false);

				// ImageNachbearbBitonalDatum
				generateProzessProperty(session, p, "DatumBitonalImageNachbearbeitung", String.valueOf(pd.getImageNachbearbBitonalDatum()), PropertyType.Date,
						0, false);

				// ImageNachbearbBitonalPerson
//				generateProzessProperty(session, p, "PersonBitonalImageCorrection", pd.getImageNachbearbBitonalPerson(), PropertyType.String, 0, false);

				// ImageNachbearbGrauDatum
				generateProzessProperty(session, p, "DatumGrauImageNachbearbeitung", String.valueOf(pd.getImageNachbearbGrauDatum()), PropertyType.Date,
						0, false);

				// ImageNachbearbGrauPerson
//				generateProzessProperty(session, p, "PersonGrayscaleImageCorrection", pd.getImageNachbearbGrauPerson(), PropertyType.String, 0, false);

				// ImageNachbearbFarbeDatum
				generateProzessProperty(session, p, "DatumFarbeImageNachbearbeitung", String.valueOf(pd.getImageNachbearbFarbeDatum()), PropertyType.Date, 0,
						false);

				// ImageNachbearbFarbePerson
//				generateProzessProperty(session, p, "PersonColorImageCorrection", pd.getImageNachbearbFarbePerson(), PropertyType.String, 0, false);

				/********************************************
				 * step 'Archiv' *
				 *******************************************/
			} else if (s.getTitle().contains("Archivierung")) {
				generateProzessProperty(session, p, "CD-Sicherung-BK", pd.getCDSICHERUNG(), PropertyType.String, 0, false);

				// MAARCHIV
				generateProzessProperty(session, p, "MA-ArchivNr", pd.getMAARCHIV(), PropertyType.String, 0, false);

			}

		}

		// History s
		for (Task s : stepList) {
			if (s.getTitle().equals("Bibliographische Aufnahme")) {
				processService.getHistoryInitialized(p).add(new History(pd.getDATUMAUFNAHMEWERK(), s.getOrdering(), s.getTitle(), HistoryType.taskDone, p));
			} else if (s.getTitle().equals("scannen")) {
				processService.getHistoryInitialized(p).add(new History(pd.getWERKSCANDATUM(), pd.getWERKSCANSEITEN(), null, HistoryType.imagesMasterDiff, p));
				processService.getHistoryInitialized(p).add(new History(pd.getWERKSCANDATUM(), s.getOrdering(), s.getTitle(), HistoryType.taskDone, p));
				processService.getHistoryInitialized(p).add(new History(pd.getDATUMAUFNAHMEWERK(), s.getOrdering(), s.getTitle(), HistoryType.taskOpen, p));
			} else if (s.getTitle().equals("Qualitaetskontrolle")) {
				processService.getHistoryInitialized(p).add(new History(pd.getWERKQKONTROLLDATUM(), pd.getWERKSCANSEITEN(), null, HistoryType.imagesWorkDiff, p));
				processService.getHistoryInitialized(p).add(new History(pd.getWERKQKONTROLLDATUM(), s.getOrdering(), s.getTitle(), HistoryType.taskDone, p));
				processService.getHistoryInitialized(p).add(new History(pd.getWERKSCANDATUM(), s.getOrdering(), s.getTitle(), HistoryType.taskOpen, p));
			} else if (s.getTitle().equals("Imagenachbearbeitung")) {
				processService.getHistoryInitialized(p).add(
						new History(pd.getImageNachbearbBitonalDatum(), s.getOrdering(), s.getTitle(), HistoryType.taskDone, p));
				processService.getHistoryInitialized(p).add(new History(pd.getWERKQKONTROLLDATUM(), s.getOrdering(), s.getTitle(), HistoryType.taskOpen, p));
				try {
					processService.getHistoryInitialized(p).add(
							new History(pd.getImageNachbearbBitonalDatum(), Integer.valueOf(pd.getBITONALIMAGENACHBEARBEITUNG()), null,
									HistoryType.bitonal, p));
					processService.getHistoryInitialized(p).add(
							new History(pd.getImageNachbearbBitonalDatum(), Integer.valueOf(pd.getGRAUIMAGENACHBEARBEITUNG()), null,
									HistoryType.grayScale, p));
					processService.getHistoryInitialized(p).add(
							new History(pd.getImageNachbearbBitonalDatum(), Integer.valueOf(pd.getFARBEIMAGENACHBEARBEITUNG()), null,
									HistoryType.color, p));

				} catch (NumberFormatException e) {

				} catch (NullPointerException e) {
				}
			}
		}

		session.flush();
		session.clear();
	}

	private List<Task> getSteps(Process prozess) {
		List<Task> stepList = new ArrayList<>();
		try {
			UserGroup adm = userGroupService.find(6);
			UserGroup importGoe = userGroupService.find(15);
			Task biblio = new Task();
			biblio.setOrdering(0);
			biblio.setTitle("Bibliographische Aufnahme");
			biblio.setProcess(prozess);
			biblio.setProcessingStatusEnum(TaskStatus.DONE);
			biblio.getUserGroups().add(adm);
			Task scanning = new Task();
			scanning.setOrdering(1);
			scanning.setTitle("scannen");
			scanning.setProcess(prozess);
			scanning.setProcessingStatusEnum(TaskStatus.OPEN);
			scanning.getUserGroups().add(adm);
			Task qk = new Task();
			qk.setOrdering(2);
			qk.setTitle("Qualitaetskontrolle");
			qk.setProcess(prozess);
			qk.getUserGroups().add(adm);
			Task image = new Task();
			image.setOrdering(3);
			image.setTitle("Imagenachbearbeitung");
			image.setProcess(prozess);
			image.getUserGroups().add(adm);
			Task export = new Task();
			export.setOrdering(4);
			export.setTitle("Archivierung");
			export.setProcess(prozess);
			export.getUserGroups().add(adm);
			Task cd = new Task();
			cd.setOrdering(5);
			cd.setTitle("Import von CD");
			cd.setProcess(prozess);
			cd.getUserGroups().add(adm);

			Task importDms = new Task();
			importDms.setOrdering(6);
			importDms.setTitle("Import DMS");
			importDms.setProcess(prozess);
			importDms.getUserGroups().add(importGoe);

			Task longtimearchive = new Task();
			longtimearchive.setOrdering(7);
			longtimearchive.setTitle("Langzeitarchivierung");
			longtimearchive.setProcess(prozess);
			longtimearchive.getUserGroups().add(adm);
			stepList.add(biblio);
			stepList.add(scanning);
			stepList.add(qk);
			stepList.add(image);
			stepList.add(export);
			stepList.add(cd);
			stepList.add(importDms);
			stepList.add(longtimearchive);
		} catch (DAOException e) {
			logger.error(e);
		}
		return stepList;
	}

	private Project generateProject() {
		Project project = new Project();
		project.setTitle("Altdaten");
		project.setFileFormatDmsExport("Mets");
		project.setFileFormatInternal("Mets");
		return project;
	}

	private Project getProjekt(ProductionData pd) {
		for (Project p : projectList) {
			if (p.getTitle().equals(pd.getWERKPROJEKT())) {
				return p;
			}
		}
		return altdaten;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<ProductionData> load(String filename) {
		ArrayList<ProductionData> productionList = new ArrayList<ProductionData>();
		XStream xstream = new XStream();
		try {
			xstream.alias("ProductionData", ProductionData.class);
			productionList = (ArrayList<ProductionData>) xstream.fromXML(new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8)));
		} catch (FileNotFoundException e) {
			logger.debug(e);
		}
		return productionList;
	}
}
