package org.goobi.production.importer;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - http://gdz.sub.uni-goettingen.de - http://www.intranda.com
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.thoughtworks.xstream.XStream;

import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.beans.HistoryEvent;
import de.sub.goobi.beans.Projekt;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Prozesseigenschaft;
import de.sub.goobi.beans.Regelsatz;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.beans.Schritteigenschaft;
import de.sub.goobi.beans.Vorlage;
import de.sub.goobi.beans.Vorlageeigenschaft;
import de.sub.goobi.beans.Werkstueck;
import de.sub.goobi.beans.Werkstueckeigenschaft;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.BenutzergruppenDAO;
import de.sub.goobi.persistence.HibernateUtilOld;
import de.sub.goobi.persistence.ProjektDAO;
import de.sub.goobi.persistence.RegelsatzDAO;

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
	private List<Projekt> projectList = new ArrayList<Projekt>();
	private Projekt altdaten = null;
	private Session session;

	private ProductionDataImport() {
		session = HibernateUtilOld.getSessionFactory().openSession();
		altdaten = generateProject();
		try {
			new ProjektDAO().save(altdaten);
			projectList = new ProjektDAO().search("from Projekt");
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
	 * @throws UnsupportedEncodingException
	 */

	public static void main(String[] args) throws HibernateException, SQLException, ConfigurationException, DAOException,
			UnsupportedEncodingException, FileNotFoundException {
		new ProductionDataImport().importData();

	}

	@SuppressWarnings("unchecked")
	private void importData() throws DAOException, HibernateException, ConfigurationException, SQLException, UnsupportedEncodingException,
			FileNotFoundException {
		filename = ConfigMain.getParameter("tempfolder") + "produktionsDb.xml";
		// load data from xml
		logger.debug("Load Production Data from xml.");
		ArrayList<ProductionData> dataList = load(filename);
		logger.debug("Got " + dataList.size() + " items");
		// Session session = HibernateUtilOld.getSessionFactory().openSession();

		// Session session = Helper.getHibernateSession();

		Prozess template = new Prozess();
		template.setProjekt(altdaten);
		template.setTitel("Altdatenvorlage");
		template.setIstTemplate(true);

		// gdz Regelsatz
		Regelsatz ruleset = new RegelsatzDAO().get(Integer.valueOf(17));

		template.setRegelsatz(ruleset);

		// session.save(altdaten);
		session.save(template);
		Set<Schritt> step = getSteps(template);
		template.setSchritte(step);
		for (Schritt s : step) {
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
					Criteria crit = session.createCriteria(Werkstueckeigenschaft.class).add(Restrictions.in("wert", ppnlist));
					ArrayList<Werkstueckeigenschaft> weList = new ArrayList<Werkstueckeigenschaft>();
					weList.addAll(crit.list());
					// -------------------------------------------------------------------------------
					// add properties to existing prozess
					// -------------------------------------------------------------------------------
					boolean added = false;
					if (weList.size() > 0) {
						Werkstueckeigenschaft we = weList.get(0);
						Werkstueck w = we.getWerkstueck();
						if (w != null) {
							Prozess p = w.getProzess();
							if (p != null) {
								logger.debug("Add new Properties for Process : " + p.getTitel());
								addNewPropertiesForExistingProcesses(session, p.getId(), pd);
								added = true;
								oldProj++;
							}
						}
					}
					// -------------------------------------------------------------------------------
					// generate new properties for new process
					// -------------------------------------------------------------------------------
					if (!added) {
						logger.debug("Add new Properties for new process");
						generateNewPropertiesForNewProzess(session, ruleset, altdaten, pd);
						newProj++;
					}
				}
			}
			if (++i % 40 == 0) {
				// logger.debug("clear cache");
				session.flush();
				session.clear();
			}
		}
		if (conflicts != null) {
			XStream xstream = new XStream();
			xstream.setMode(XStream.NO_REFERENCES);
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(new File(ConfigMain.getParameter("tempfolder") + conflictFilename)),
					"UTF8");
			xstream.toXML(conflicts, fw);
		}
		logger.debug("Neue Prozesse: " + newProj);
		logger.debug("Zu " + oldProj + " existierenden Prozessen wurden Properties hinzugefügt");
	}

	private void generateNewPropertiesForNewProzess(Session session, Regelsatz ruleset, Projekt project, ProductionData pd)
			throws HibernateException, SQLException {
		// Session session = HibernateUtilOld.getSessionFactory().openSession();

		// generate new Process

		Prozess prozess = new Prozess();

		prozess.setProjekt(getProjekt(pd));
		String title = pd.getWERKATS() + "_" + pd.getWERKPPNDIGITAL();
		title = title.replaceAll("\\W", "");
		prozess.setTitel(title);
//		if (prozess.getTitel().contains(" ")) {
//			prozess.setTitel(prozess.getTitel().replaceAll(" ", ""));
//		}
		prozess.setIstTemplate(false);
		prozess.setRegelsatz(ruleset);
		Werkstueck werk = new Werkstueck();
		werk.setProzess(prozess);

		Werkstueckeigenschaft we = new Werkstueckeigenschaft();
		we.setWerkstueck(werk);
		we.setTitel("PPN");
		String ppn = pd.getWERKPPNDIGITAL();

		// get ppn.
		int ppnIndex = ppn.indexOf("PPN");
		if (ppnIndex == -1) {
			ppnIndex = ppn.indexOf("ppn");
		}
		if (ppnIndex != -1) {
			ppn = ppn.substring(ppnIndex + 3);
		}

		we.setWert(ppn);
		if (werk.getEigenschaften() == null) {
			werk.setEigenschaften(new HashSet<Werkstueckeigenschaft>());
		}
		werk.getEigenschaften().add(we);

		Vorlage v = new Vorlage();
		v.setProzess(prozess);

		if (prozess.getWerkstuecke() == null) {
			HashSet<Werkstueck> werkstueckeSet = new HashSet<Werkstueck>();
			prozess.setWerkstuecke(werkstueckeSet);
		}
		if (prozess.getVorlagen() == null) {
			HashSet<Vorlage> vorlagenSet = new HashSet<Vorlage>();
			prozess.setVorlagen(vorlagenSet);
		}
		prozess.getWerkstuecke().add(werk);
		prozess.getVorlagen().add(v);

		session.save(werk);
		session.save(we);
		session.save(v);
		session.save(prozess);
		Set<Schritt> step = getSteps(prozess);
		prozess.setSchritte(step);
		for (Schritt s : step) {
			session.save(s);
		}
		// prozess.setSchritte(getSteps(prozess));

		session.save(prozess);
		try {
			getNewPropertiesForNewProcesses(session, prozess, pd);
		} catch (ConfigurationException e) {
			logger.error(e);
		}

	}

	private void generateStepProperty(Session session, Schritt s, String name, String value, PropertyType type, Integer position, boolean required) {
		if (value != null) {
			Schritteigenschaft property = new Schritteigenschaft();
			property.setCreationDate(new Date());
			property.setIstObligatorisch(required);
			property.setTitel(name);
			property.setSchritt(s);
			property.setWert(value);
			property.setType(type);
			s.getEigenschaftenList().add(property);
			session.saveOrUpdate(s);
			session.saveOrUpdate(property);
		}
	}

	private void generateWerkProperty(Session session, Werkstueck w, String name, String value, PropertyType type, Integer position, boolean required) {
		if (value != null) {
			Werkstueckeigenschaft property = new Werkstueckeigenschaft();
			property.setCreationDate(new Date());
			property.setIstObligatorisch(required);
			property.setTitel(name);
			property.setWerkstueck(w);
			property.setWert(value);
			property.setType(type);
			w.getEigenschaftenList().add(property);
			session.saveOrUpdate(w);
			session.saveOrUpdate(property);
		}
	}

	private void generateVorlageProperty(Session session, Vorlage s, String name, String value, PropertyType type, Integer position, boolean required) {
		if (value != null) {
			Vorlageeigenschaft property = new Vorlageeigenschaft();
			property.setCreationDate(new Date());
			property.setIstObligatorisch(required);
			property.setTitel(name);
			property.setVorlage(s);
			property.setWert(value);
			property.setType(type);
			s.getEigenschaftenList().add(property);
			session.saveOrUpdate(s);
			session.saveOrUpdate(property);
		}
	}

	private void generateProzessProperty(Session session, Prozess s, String name, String value, PropertyType type, Integer position, boolean required) {
		if (value != null) {
			Prozesseigenschaft property = new Prozesseigenschaft();
			property.setCreationDate(new Date());
			property.setIstObligatorisch(required);
			property.setTitel(name);
			property.setProzess(s);
			property.setWert(value);
			property.setType(type);
			s.getEigenschaftenList().add(property);
			session.saveOrUpdate(s);
			session.saveOrUpdate(property);
		}
	}

	private void getNewPropertiesForNewProcesses(Session session, Prozess prozess, ProductionData pd) throws HibernateException, SQLException,
			ConfigurationException {

		// ArrayList<HibernateGoobiProperty> newPropertiesList = new ArrayList<HibernateGoobiProperty>();

		// Generate Properties
		prozess.setErstellungsdatum(pd.getDATUMAUFNAHMEWERK());
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
		if (prozess.getProjekt().getTitel().equals("DigiWunschBuch")) {

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

		Werkstueck werkstueck = prozess.getWerkstueckeList().get(0);
		// w.setProzess(p);
		String ppn = pd.getWERKPPNDIGITAL();
		if (ppn != null) {
			if (ppn.startsWith("ppn") || ppn.startsWith("PPN")) {
				ppn = ppn.substring(3);
			}

			// session.save(w);
			// PPN digital f-Satz
			// List<IGoobiProperty> prps = new
			// ArrayList<IGoobiProperty>();
			generateWerkProperty(session, werkstueck, "PPN digital f-Satz", ppn, PropertyType.String, 0, false);
		}

		Vorlage v = prozess.getVorlagenList().get(0);
		ppn = pd.getWERKPPNANALOG();
		if (ppn != null) {
			if (ppn.startsWith("ppn") || ppn.startsWith("PPN")) {
				ppn = ppn.substring(3);
			}
			// List<IGoobiProperty> proplist = new
			// ArrayList<IGoobiProperty>();
			generateVorlageProperty(session, v, "PPN analog f-Satz", ppn, PropertyType.String, 0, false);
			// proplist.add(propPPNA);
			generateVorlageProperty(session, v, "Signatur", pd.getWERKSIGNATUR(), PropertyType.String, 0, false);

		}
		// p.setProperties(newProcessProperties);
		for (Schritt s : prozess.getSchritte()) {
			if (s.getTitel().contains("Bibliographisch") || (s.getTitel().contains("bibliographische "))) {
				s.setBearbeitungsende(pd.getDATUMAUFNAHMEWERK());
				s.setEditTypeEnum(StepEditType.ADMIN);
				s.setBearbeitungsstatusEnum(StepStatus.DONE);
			}
			/********************************************
			 * step 'scannen' *
			 *******************************************/

			if (s.getTitel().contains("scan") || s.getTitel().contains("Scan")) {
				s.setBearbeitungsende(pd.getWERKSCANDATUM());
				s.setEditTypeEnum(StepEditType.ADMIN);
				s.setBearbeitungsstatusEnum(StepStatus.DONE);
				// List<IGoobiProperty> proplist = new
				// ArrayList<IGoobiProperty>();

				// WERKSCANSEITEN
				generateStepProperty(session, s, "Seitenanzahl", String.valueOf(pd.getWERKSCANSEITEN()), PropertyType.Integer, 0, false);

				prozess.setSortHelperImages(pd.getWERKSCANSEITEN());

				// SCANNERTYP
				generateStepProperty(session, s, "Scangerät", pd.getSCANNERTYP(), PropertyType.String, 0, false);

				// DRUCKQUALITAET
				generateStepProperty(session, s, "Druckqualität", String.valueOf(pd.getDRUCKQUALITAET()), PropertyType.Integer, 0, false);

			}

			/********************************************
			 * step 'Qualitätskontrolle' *
			 *******************************************/
			else if (s.getTitel().contains("Qualitaetskontrolle")) {
				s.setBearbeitungsende(pd.getWERKQKONTROLLDATUM());
				s.setBearbeitungsstatusEnum(StepStatus.DONE);
				s.setEditTypeEnum(StepEditType.ADMIN);

				/********************************************
				 * step 'Imagenachbearbeitung' *
				 *******************************************/
			} else if (s.getTitel().contains("Imagenachbearbeitung")) {
				s.setBearbeitungsstatusEnum(StepStatus.DONE);
				s.setEditTypeEnum(StepEditType.ADMIN);

				// List<IGoobiProperty> proplist = new
				// ArrayList<IGoobiProperty>();
				// BITONALIMAGENACHBEARBEITUNG
				generateStepProperty(session, s, "BitonalImageNachbearbeitung", pd.getBITONALIMAGENACHBEARBEITUNG(), PropertyType.String, 0, false);

				// GRAUIMAGENACHBEARBEITUNG
				generateStepProperty(session, s, "GrayscaleImageNachbearbeitung", pd.getGRAUIMAGENACHBEARBEITUNG(), PropertyType.String, 0, false);

				// FARBEIMAGENACHBEARBEITUNG
				generateStepProperty(session, s, "ColorImageNachbearbeitung", pd.getFARBEIMAGENACHBEARBEITUNG(), PropertyType.String, 0, false);

				// FARBGRAUABB
				// TODO Name
				generateStepProperty(session, s, "FarbGrauAbb", String.valueOf(pd.getFARBGRAUABB()), PropertyType.Integer, 0, false);

				// ImageNachbearbBitonalDatum
				generateStepProperty(session, s, "DatumBitonalImageNachbearbeitung", String.valueOf(pd.getImageNachbearbBitonalDatum()),
						PropertyType.Date, 0, false);

				// ImageNachbearbBitonalPerson
				// generateStepProperty(session, s, "PersonBitonalImageCorrection", pd.getImageNachbearbBitonalPerson(), PropertyType.String, 0,
				// false);

				// ImageNachbearbGrauDatum
				generateStepProperty(session, s, "DatumGrauImageNachbearbeitung", String.valueOf(pd.getImageNachbearbGrauDatum()), PropertyType.Date,
						0, false);

				// ImageNachbearbGrauPerson
				// generateStepProperty(session, s, "PersonGrayscaleImageCorrection", pd.getImageNachbearbGrauPerson(), PropertyType.String, 0,
				// false);

				// ImageNachbearbFarbeDatum
				generateStepProperty(session, s, "DatumFarbImageNachbearbeitung", String.valueOf(pd.getImageNachbearbFarbeDatum()),
						PropertyType.Date, 0, false);

				// ImageNachbearbFarbePerson
				// generateStepProperty(session, s, "PersonColorImageCorrection", pd.getImageNachbearbFarbePerson(), PropertyType.String, 0, false);

				/********************************************
				 * step 'Archiv' *
				 *******************************************/
			} else if (s.getTitel().contains("Archivierung")) {
				s.setBearbeitungsende(pd.getImportDatum());
				s.setBearbeitungsstatusEnum(StepStatus.DONE);
				s.setEditTypeEnum(StepEditType.ADMIN);

				// CDSICHERUNG
				generateStepProperty(session, s, "CD-Sicherung-BK", pd.getCDSICHERUNG(), PropertyType.String, 0, false);

				// MAARCHIV
				generateStepProperty(session, s, "MA-ArchivNr", pd.getMAARCHIV(), PropertyType.String, 0, false);

			} else if (s.getTitel().contains("Import von CD")) {
				s.setBearbeitungsbeginn(pd.getImportDatum());
				s.setBearbeitungsstatusEnum(StepStatus.OPEN);
			}
		}
		session.flush();
		session.clear();
	}

	private void addNewPropertiesForExistingProcesses(Session session, int pId, ProductionData pd) throws HibernateException, SQLException,
			ConfigurationException {

		// Session session = Helper.getHibernateSession();
		// Session session = HibernateUtilOld.getSessionFactory().openSession();
		// Prozess holen
		Prozess p = null;
		Criteria crit = session.createCriteria(Prozess.class).add(Restrictions.eq("id", pId));
		if (crit.list().size() > 0) {
			p = (Prozess) crit.list().get(0);
		}
		if (p == null)
			return;

		/*******************************************
		 * properties and attributes for processes *
		 ******************************************/
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
		List<Prozesseigenschaft> eig = p.getEigenschaftenList();
		if (p.getProjekt().getTitel().equals("DigiWunschBuch")) {
			boolean sponsor = false;
			if (eig != null) {

				for (Prozesseigenschaft pe : eig) {
					if (pe.getTitel().contains("Besteller") && (pe.getWert() != null)) {
						sponsor = true;
					}
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
		List<Werkstueck> wl = p.getWerkstueckeList();
		boolean ppndigital = false;
		boolean ppnconflict = false;
		for (Werkstueck w : wl) {
			List<Werkstueckeigenschaft> wel = w.getEigenschaftenList();

			String ppn = pd.getWERKPPNDIGITAL();
			if (ppn.startsWith("ppn") || ppn.startsWith("PPN")) {
				ppn = ppn.substring(3);
			}
			for (Werkstueckeigenschaft we : wel) {
				if (we.getTitel().contains("PPN digital")) {
					if (we.getWert() == null) {
						ppndigital = true;
						we.setWert(pd.getWERKPPNDIGITAL());
					} else if (we.getWert().contains(ppn)) {
						ppndigital = true;
					} else if (!we.getWert().contains(ppn)) {
						ppnconflict = true;
					}
				}
			}
			if (!ppndigital && ppnconflict) {
				conflicts.add(new ImportConflicts(String.valueOf(w.getId()), "PPN digital f-Satz", "", pd.getWERKPPNDIGITAL()));
			}
		}

		if (!ppndigital) {
			Werkstueck newWerk = new Werkstueck();
			newWerk.setProzess(p);
			session.save(newWerk);
			// PPN digital f-Satz
			newWerk.setProzess(p);

			generateWerkProperty(session, newWerk, "PPN digital f-Satz", pd.getWERKPPNDIGITAL(), PropertyType.String, 0, false);
		}
		ppnconflict = false;
		boolean signatur = false;
		boolean ppnanalog = false;
		boolean sigconflict = false;
		Vorlage newVorlage = new Vorlage();
		newVorlage.setProzess(p);
		String ppn = pd.getWERKPPNANALOG();
		if (ppn != null) {
			if (ppn.startsWith("ppn") || ppn.startsWith("PPN")) {
				ppn = ppn.substring(3);
			}
			for (Vorlage v : p.getVorlagenList()) {
				for (Vorlageeigenschaft ve : v.getEigenschaftenList()) {
					if (pd.getWERKSIGNATUR() != null) {
						if (ve.getTitel().contains("Signatur")) {
							newVorlage = v;
							if (ve.getWert() == null) {
								signatur = true;
								ve.setWert(pd.getWERKSIGNATUR());
							} else if (ve.getWert().contains(pd.getWERKSIGNATUR())) {
								signatur = true;
							} else {
								sigconflict = true;
							}
						}
					}
					if (ve.getTitel().contains("PPN analog")) {
						newVorlage = v;
						if (ve.getWert() == null) {
							ppnanalog = true;
							ve.setWert(pd.getWERKPPNANALOG());
						} else if (ve.getWert().contains(ppn)) {
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

		List<Schritt> stepList = p.getSchritteList();

		for (Schritt s : stepList) {

			/********************************************
			 * step 'scannen' *
			 * TODO einzelne farbformate
			 *******************************************/
			if (s.getTitel().contains("scan") || s.getTitel().contains("Scan")) {
				boolean pages = false;
				boolean scangeraet = false;
				for (Schritteigenschaft se : s.getEigenschaftenList()) {
					if (se.getTitel().contains("Anzahl der Images") || se.getTitel().contains("Anzahl der Seiten")
							|| se.getTitel().contains("Seitenzahl")) {
						if (!se.getWert().equals(pd.getWERKSCANSEITEN())) {
							conflicts.add(new ImportConflicts(String.valueOf(s.getId()), "Seitenzahl", se.getWert(), String.valueOf(pd
									.getWERKSCANSEITEN())));
						}
						pages = true;
					} else if (se.getTitel().contains("Scangerät")) {
						if (se.getWert().equals(String.valueOf(pd.getSCANNERTYP()))) {
							scangeraet = true;
						} else {
							conflicts.add(new ImportConflicts(String.valueOf(s.getId()), "Scangerät", se.getWert(), pd.getSCANNERTYP()));
							scangeraet = true;
						}
					}
				}

				if (!pages) {
					// WERKSCANSEITEN
					generateStepProperty(session, s, "Seitenanzahl", String.valueOf(pd.getWERKSCANSEITEN()), PropertyType.Integer, 0, false);

				}
				if (!scangeraet) {
					// SCANNERTYP
					generateStepProperty(session, s, "Scangerät", pd.getSCANNERTYP(), PropertyType.String, 0, false);
				}
				// DRUCKQUALITAET
				generateStepProperty(session, s, "Druckqualität", String.valueOf(pd.getDRUCKQUALITAET()), PropertyType.Integer, 0, false);
			}
			/********************************************
			 * step 'Qualitätskontrolle' *
			 *******************************************/
			else if (s.getTitel().contains("Qualitätskontrolle")) {
				if (s.getBearbeitungsende() == null) {
					s.setBearbeitungsende(pd.getWERKQKONTROLLDATUM());

				}

				/********************************************
				 * step 'Imagenachbearbeitung' *
				 *******************************************/
			} else if (s.getTitel().contains("Imagenachbearbeitung")) {

				generateStepProperty(session, s, "BitonalImageNachbearbeitung", pd.getBITONALIMAGENACHBEARBEITUNG(), PropertyType.String, 0, false);

				// GRAUIMAGENACHBEARBEITUNG
				generateStepProperty(session, s, "GrauImageNachbearbeitung", pd.getGRAUIMAGENACHBEARBEITUNG(), PropertyType.String, 0, false);

				// FARBEIMAGENACHBEARBEITUNG
				generateStepProperty(session, s, "FarbeImageNachbearbeitung", pd.getFARBEIMAGENACHBEARBEITUNG(), PropertyType.String, 0, false);

				// FARBGRAUABB
				generateStepProperty(session, s, "FarbgrauABB", String.valueOf(pd.getFARBGRAUABB()), PropertyType.Integer, 0, false);

				// ImageNachbearbBitonalDatum
				generateStepProperty(session, s, "DatumBitonalImageNachbearbeitung", String.valueOf(pd.getImageNachbearbBitonalDatum()), PropertyType.Date,
						0, false);

				// ImageNachbearbBitonalPerson
//				generateStepProperty(session, s, "PersonBitonalImageCorrection", pd.getImageNachbearbBitonalPerson(), PropertyType.String, 0, false);

				// ImageNachbearbGrauDatum
				generateStepProperty(session, s, "DatumGrauImageNachbearbeitung", String.valueOf(pd.getImageNachbearbGrauDatum()), PropertyType.Date,
						0, false);

				// ImageNachbearbGrauPerson
//				generateStepProperty(session, s, "PersonGrayscaleImageCorrection", pd.getImageNachbearbGrauPerson(), PropertyType.String, 0, false);

				// ImageNachbearbFarbeDatum
				generateStepProperty(session, s, "DatumFarbeImageNachbearbeitung", String.valueOf(pd.getImageNachbearbFarbeDatum()), PropertyType.Date, 0,
						false);

				// ImageNachbearbFarbePerson
//				generateStepProperty(session, s, "PersonColorImageCorrection", pd.getImageNachbearbFarbePerson(), PropertyType.String, 0, false);

				/********************************************
				 * step 'Archiv' *
				 *******************************************/
			} else if (s.getTitel().contains("Archivierung")) {
				// List<IGoobiProperty> proplist = s.getProperties();
				generateStepProperty(session, s, "CD-Sicherung-BK", pd.getCDSICHERUNG(), PropertyType.String, 0, false);

				// MAARCHIV
				generateStepProperty(session, s, "MA-ArchivNr", pd.getMAARCHIV(), PropertyType.String, 0, false);

			}

		}

		// History Events

		for (Schritt s : stepList) {
			if (s.getTitel().equals("Bibliographische Aufnahme")) {
				p.getHistory().add(new HistoryEvent(pd.getDATUMAUFNAHMEWERK(), s.getReihenfolge(), s.getTitel(), HistoryEventType.stepDone, p));
			} else if (s.getTitel().equals("scannen")) {
				p.getHistory().add(new HistoryEvent(pd.getWERKSCANDATUM(), pd.getWERKSCANSEITEN(), null, HistoryEventType.imagesMasterDiff, p));
				p.getHistory().add(new HistoryEvent(pd.getWERKSCANDATUM(), s.getReihenfolge(), s.getTitel(), HistoryEventType.stepDone, p));
				p.getHistory().add(new HistoryEvent(pd.getDATUMAUFNAHMEWERK(), s.getReihenfolge(), s.getTitel(), HistoryEventType.stepOpen, p));
			} else if (s.getTitel().equals("Qualitaetskontrolle")) {
				p.getHistory().add(new HistoryEvent(pd.getWERKQKONTROLLDATUM(), pd.getWERKSCANSEITEN(), null, HistoryEventType.imagesWorkDiff, p));
				p.getHistory().add(new HistoryEvent(pd.getWERKQKONTROLLDATUM(), s.getReihenfolge(), s.getTitel(), HistoryEventType.stepDone, p));
				p.getHistory().add(new HistoryEvent(pd.getWERKSCANDATUM(), s.getReihenfolge(), s.getTitel(), HistoryEventType.stepOpen, p));
			} else if (s.getTitel().equals("Imagenachbearbeitung")) {
				p.getHistory().add(
						new HistoryEvent(pd.getImageNachbearbBitonalDatum(), s.getReihenfolge(), s.getTitel(), HistoryEventType.stepDone, p));
				p.getHistory().add(new HistoryEvent(pd.getWERKQKONTROLLDATUM(), s.getReihenfolge(), s.getTitel(), HistoryEventType.stepOpen, p));
				try {
					p.getHistory().add(
							new HistoryEvent(pd.getImageNachbearbBitonalDatum(), new Integer(pd.getBITONALIMAGENACHBEARBEITUNG()), null,
									HistoryEventType.bitonal, p));
					p.getHistory().add(
							new HistoryEvent(pd.getImageNachbearbBitonalDatum(), new Integer(pd.getGRAUIMAGENACHBEARBEITUNG()), null,
									HistoryEventType.grayScale, p));
					p.getHistory().add(
							new HistoryEvent(pd.getImageNachbearbBitonalDatum(), new Integer(pd.getFARBEIMAGENACHBEARBEITUNG()), null,
									HistoryEventType.color, p));

				} catch (NumberFormatException e) {

				} catch (NullPointerException e) {
				}
			}
		}

		session.flush();
		session.clear();
	}

	private Set<Schritt> getSteps(Prozess prozess) {
		Set<Schritt> stepList = new HashSet<Schritt>();
		try {
			Benutzergruppe adm = new BenutzergruppenDAO().get(6);
			Benutzergruppe importGoe = new BenutzergruppenDAO().get(15);
			Schritt biblio = new Schritt();
			biblio.setReihenfolge(0);
			biblio.setTitel("Bibliographische Aufnahme");
			biblio.setProzess(prozess);
			biblio.setBearbeitungsstatusEnum(StepStatus.DONE);
			biblio.getBenutzergruppen().add(adm);
			Schritt scanning = new Schritt();
			scanning.setReihenfolge(1);
			scanning.setTitel("scannen");
			scanning.setProzess(prozess);
			scanning.setBearbeitungsstatusEnum(StepStatus.OPEN);
			scanning.getBenutzergruppen().add(adm);
			Schritt qk = new Schritt();
			qk.setReihenfolge(2);
			qk.setTitel("Qualitaetskontrolle");
			qk.setProzess(prozess);
			qk.getBenutzergruppen().add(adm);
			Schritt image = new Schritt();
			image.setReihenfolge(3);
			image.setTitel("Imagenachbearbeitung");
			image.setProzess(prozess);
			image.getBenutzergruppen().add(adm);
			Schritt export = new Schritt();
			export.setReihenfolge(4);
			export.setTitel("Archivierung");
			export.setProzess(prozess);
			export.getBenutzergruppen().add(adm);
			Schritt cd = new Schritt();
			cd.setReihenfolge(5);
			cd.setTitel("Import von CD");
			cd.setProzess(prozess);
			cd.getBenutzergruppen().add(adm);

			Schritt importDms = new Schritt();
			importDms.setReihenfolge(6);
			importDms.setTitel("Import DMS");
			importDms.setProzess(prozess);
			importDms.getBenutzergruppen().add(importGoe);

			Schritt longtimearchive = new Schritt();
			longtimearchive.setReihenfolge(7);
			longtimearchive.setTitel("Langzeitarchivierung");
			longtimearchive.setProzess(prozess);
			longtimearchive.getBenutzergruppen().add(adm);
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

	private Projekt generateProject() {
		Projekt project = new Projekt();
		project.setTitel("Altdaten");
		project.setFileFormatDmsExport("Mets");
		project.setFileFormatInternal("Mets");
		return project;
	}

	private Projekt getProjekt(ProductionData pd) {
		for (Projekt p : projectList) {
			if (p.getTitel().equals(pd.getWERKPROJEKT())) {
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
			productionList = (ArrayList<ProductionData>) xstream.fromXML(new BufferedReader(new InputStreamReader(new FileInputStream(filename))));
		} catch (FileNotFoundException e) {
			logger.debug(e);
		}
		return productionList;
	}
}
