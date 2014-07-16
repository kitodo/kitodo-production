package de.sub.goobi.persistence.apache;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import de.sub.goobi.beans.ProjectFileGroup;
import de.sub.goobi.beans.Regelsatz;

public class MySQLUtils {

	public static ResultSetHandler<List<StepObject>> resultSetToStepObjectListHandler = new ResultSetHandler<List<StepObject>>() {
		@Override
		public List<StepObject> handle(ResultSet rs) throws SQLException {
			List<StepObject> answer = new ArrayList<StepObject>();

			while (rs.next()) {
				StepObject o = parseStepObject(rs);
				if (o != null) {
					answer.add(o);
				}
			}
			return answer;
		}
	};

	public static ResultSetHandler<StepObject> resultSetToStepObjectHandler = new ResultSetHandler<StepObject>() {
		@Override
		public StepObject handle(ResultSet rs) throws SQLException {
			StepObject answer = null;

			if (rs.next()) {
				answer = parseStepObject(rs);
			}
			return answer;
		}
	};

	public static ResultSetHandler<Regelsatz> resultSetToRulesetHandler = new ResultSetHandler<Regelsatz>() {
		@Override
		public Regelsatz handle(ResultSet rs) throws SQLException {
			if (rs.next()) {
				int id = rs.getInt("MetadatenKonfigurationID");
				String title = rs.getString("Titel");
				String file = rs.getString("Datei");
				boolean order = rs.getBoolean("orderMetadataByRuleset");
				Regelsatz r = new Regelsatz();
				r.setId(id);
				r.setTitel(title);
				r.setDatei(file);
				r.setOrderMetadataByRuleset(order);
				return r;
			}
			return null;
		}
	};

	public static ResultSetHandler<List<Property>> resultSetToProcessPropertyListHandler = new ResultSetHandler<List<Property>>() {
		@Override
		public List<Property> handle(ResultSet rs) throws SQLException {
			List<Property> answer = new ArrayList<Property>();
			while (rs.next()) {
				int id = rs.getInt("prozesseeigenschaftenID");
				String title = rs.getString("Titel");
				String value = rs.getString("Wert");
				boolean isObligatorisch = rs.getBoolean("IstObligatorisch");
				int datentypenID = rs.getInt("DatentypenID");
				String auswahl = rs.getString("Auswahl");
				Date creationDate = rs.getTimestamp("creationDate");
				int container = rs.getInt("container");
				Property prop = new Property(id, title, value, isObligatorisch, datentypenID, auswahl, creationDate, container);
				answer.add(prop);
			}
			return answer;
		}
	};

	public static ResultSetHandler<List<Property>> resultSetToTemplatePropertyListHandler = new ResultSetHandler<List<Property>>() {
		@Override
		public List<Property> handle(ResultSet rs) throws SQLException {
			List<Property> answer = new ArrayList<Property>();
			while (rs.next()) {
				int id = rs.getInt("vorlageneigenschaftenID");
				String title = rs.getString("Titel");
				String value = rs.getString("Wert");
				boolean isObligatorisch = rs.getBoolean("IstObligatorisch");
				int datentypenID = rs.getInt("DatentypenID");
				String auswahl = rs.getString("Auswahl");
				Date creationDate = rs.getTimestamp("creationDate");
				int container = rs.getInt("container");
				Property prop = new Property(id, title, value, isObligatorisch, datentypenID, auswahl, creationDate, container);
				answer.add(prop);
			}
			return answer;
		}
	};

	public static ResultSetHandler<List<Property>> resultSetToProductPropertyListHandler = new ResultSetHandler<List<Property>>() {
		@Override
		public List<Property> handle(ResultSet rs) throws SQLException {
			List<Property> answer = new ArrayList<Property>();
			while (rs.next()) {
				int id = rs.getInt("werkstueckeeigenschaftenID");
				String title = rs.getString("Titel");
				String value = rs.getString("Wert");
				boolean isObligatorisch = rs.getBoolean("IstObligatorisch");
				int datentypenID = rs.getInt("DatentypenID");
				String auswahl = rs.getString("Auswahl");
				Date creationDate = rs.getTimestamp("creationDate");
				int container = rs.getInt("container");
				Property prop = new Property(id, title, value, isObligatorisch, datentypenID, auswahl, creationDate, container);
				answer.add(prop);
			}
			return answer;
		}
	};

	public static ResultSetHandler<ProcessObject> resultSetToProcessHandler = new ResultSetHandler<ProcessObject>() {
		@Override
		public ProcessObject handle(ResultSet rs) throws SQLException {
			if (rs.next()) {
				int processId = rs.getInt("ProzesseID");
				String title = rs.getString("Titel");
				String ausgabename = rs.getString("ausgabename");
				boolean isTemplate = rs.getBoolean("IstTemplate");
				boolean swappedOut = rs.getBoolean("swappedOut");
				boolean inAuswahllisteAnzeigen = rs.getBoolean("inAuswahllisteAnzeigen");
				String sortHelperStatus = rs.getString("sortHelperStatus");
				int sortHelperImages = rs.getInt("sortHelperImages");
				int sortHelperArticles = rs.getInt("sortHelperArticles");
				Date erstellungsdatum = rs.getTimestamp("erstellungsdatum");
				int projekteID = rs.getInt("ProjekteID");
				int metadatenKonfigurationID = rs.getInt("MetadatenKonfigurationID");
				int sortHelperDocstructs = rs.getInt("sortHelperDocstructs");
				int sortHelperMetadata = rs.getInt("sortHelperMetadata");
				String wikifield = rs.getString("wikifield");
				return new ProcessObject(processId, title, ausgabename, isTemplate, swappedOut, inAuswahllisteAnzeigen,
						sortHelperStatus, sortHelperImages, sortHelperArticles, erstellungsdatum, projekteID,
						metadatenKonfigurationID, sortHelperDocstructs, sortHelperMetadata, wikifield);
			}
			return null;
		}
	};

	public static ResultSetHandler<List<String>> resultSetToScriptsHandler = new ResultSetHandler<List<String>>() {
		@Override
		public List<String> handle(ResultSet rs) throws SQLException {
			List<String> answer = new ArrayList<String>();
			if (rs.next()) {
				if (rs.getString("typAutomatischScriptpfad") != null && rs.getString("typAutomatischScriptpfad").length() > 0) {
					answer.add(rs.getString("typAutomatischScriptpfad"));
				}
				if (rs.getString("typAutomatischScriptpfad2") != null && rs.getString("typAutomatischScriptpfad2").length() > 0) {
					answer.add(rs.getString("typAutomatischScriptpfad2"));
				}
				if (rs.getString("typAutomatischScriptpfad3") != null && rs.getString("typAutomatischScriptpfad3").length() > 0) {
					answer.add(rs.getString("typAutomatischScriptpfad3"));
				}
				if (rs.getString("typAutomatischScriptpfad4") != null && rs.getString("typAutomatischScriptpfad4").length() > 0) {
					answer.add(rs.getString("typAutomatischScriptpfad4"));
				}
				if (rs.getString("typAutomatischScriptpfad5") != null && rs.getString("typAutomatischScriptpfad5").length() > 0) {
					answer.add(rs.getString("typAutomatischScriptpfad5"));
				}
			}
			return answer;
		}
	};

	public static ResultSetHandler<Map<String, String>> resultSetToScriptMapHandler = new ResultSetHandler<Map<String, String>>() {
		@Override
		public Map<String, String> handle(ResultSet rs) throws SQLException {
			Map<String, String> answer = new HashMap<String, String>();
			if (rs.next()) {
				if (rs.getString("typAutomatischScriptpfad") != null && rs.getString("typAutomatischScriptpfad").length() > 0) {
					String name = rs.getString("scriptName1");
					answer.put(name, rs.getString("typAutomatischScriptpfad"));
				}
				if (rs.getString("typAutomatischScriptpfad2") != null && rs.getString("typAutomatischScriptpfad2").length() > 0) {
					String name = rs.getString("scriptName2");
					answer.put(name, rs.getString("typAutomatischScriptpfad2"));
				}
				if (rs.getString("typAutomatischScriptpfad3") != null && rs.getString("typAutomatischScriptpfad3").length() > 0) {
					String name = rs.getString("scriptName3");
					answer.put(name, rs.getString("typAutomatischScriptpfad3"));
				}
				if (rs.getString("typAutomatischScriptpfad4") != null && rs.getString("typAutomatischScriptpfad4").length() > 0) {
					String name = rs.getString("scriptName4");
					answer.put(name, rs.getString("typAutomatischScriptpfad4"));
				}
				if (rs.getString("typAutomatischScriptpfad5") != null && rs.getString("typAutomatischScriptpfad5").length() > 0) {
					String name = rs.getString("scriptName5");
					answer.put(name, rs.getString("typAutomatischScriptpfad5"));
				}
			}
			return answer;
		}
	};

	private static StepObject parseStepObject(ResultSet rs) throws SQLException {
		StepObject so = null;

		if (rs != null) {
			int id = rs.getInt("SchritteID");
			String title = rs.getString("Titel");
			int reihenfolge = rs.getInt("Reihenfolge");
			int bearbeitungsstatus = rs.getInt("bearbeitungsstatus");

			Date bearbeitungszeitpunkt = rs.getTimestamp("bearbeitungszeitpunkt");
			Date bearbeitungsbeginn = rs.getTimestamp("bearbeitungsbeginn");
			Date bearbeitungsende = rs.getTimestamp("bearbeitungsende");
			int processId = rs.getInt("ProzesseID");
			int bearbeitungsbenutzer = rs.getInt("BearbeitungsBenutzerID");
			int editType = rs.getInt("edittype");
			boolean typExport = rs.getBoolean("typExportDMS");
			boolean typAutomatisch = rs.getBoolean("typAutomatisch");
			boolean readAccess = rs.getBoolean("typImagesLesen");
			boolean writeAccess = rs.getBoolean("typImagesSchreiben");
			boolean metadataAccess = rs.getBoolean("typMetadaten");
			boolean typeFinishImmediately = rs.getBoolean("typBeimAnnehmenAbschliessen");
			String stepPlugin = rs.getString("stepPlugin");
			String validationPlugin = rs.getString("validationPlugin");
			
			so = new StepObject(id, title, reihenfolge, bearbeitungsstatus, bearbeitungszeitpunkt, bearbeitungsbeginn, bearbeitungsende,
					bearbeitungsbenutzer, editType, typExport, typAutomatisch, processId, readAccess, writeAccess, metadataAccess,
					typeFinishImmediately, stepPlugin, validationPlugin);
		}

		return so;
	}

	public static ResultSetHandler<ProjectObject> resultSetToProjectHandler = new ResultSetHandler<ProjectObject>() {
		@Override
		public ProjectObject handle(ResultSet rs) throws SQLException {
			if (rs.next()) {
				int projekteID = rs.getInt("ProjekteID");
				String titel = rs.getString("Titel");
				boolean useDmsImport = rs.getBoolean("useDmsImport");
				int dmsImportTimeOut = rs.getInt("dmsImportTimeOut");
				String dmsImportRootPath = rs.getString("dmsImportRootPath");
				String dmsImportImagesPath = rs.getString("dmsImportImagesPath");
				String dmsImportSuccessPath = rs.getString("dmsImportSuccessPath");
				String dmsImportErrorPath = rs.getString("dmsImportErrorPath");
				boolean dmsImportCreateProcessFolder = rs.getBoolean("dmsImportCreateProcessFolder");
				String fileFormatInternal = rs.getString("fileFormatInternal");
				String fileFormatDmsExport = rs.getString("fileFormatDmsExport");
				String metsRightsOwner = rs.getString("metsRightsOwner");
				String metsRightsOwnerLogo = rs.getString("metsRightsOwnerLogo");
				String metsRightsOwnerSite = rs.getString("metsRightsOwnerSite");
				String metsDigiprovReference = rs.getString("metsDigiprovReference");
				String metsDigiprovPresentation = rs.getString("metsDigiprovPresentation");
				String metsPointerPath = rs.getString("metsPointerPath");
				String metsPointerPathAnchor = rs.getString("metsPointerPathAnchor");
				String metsDigiprovReferenceAnchor = rs.getString("metsDigiprovReferenceAnchor");
				String metsDigiprovPresentationAnchor = rs.getString("metsDigiprovPresentationAnchor");
				String metsPurl = rs.getString("metsPurl");
				String metsContentIDs = rs.getString("metsContentIDs");
				String metsRightsOwnerMail = rs.getString("metsRightsOwnerMail");
				Date startDate = rs.getTimestamp("startDate");
				Date endDate = rs.getTimestamp("endDate");
				int numberOfPages = rs.getInt("numberOfPages");
				int numberOfVolumes = rs.getInt("numberOfVolumes");
				boolean projectIsArchived = rs.getBoolean("projectIsArchived");

				ProjectObject po = new ProjectObject(projekteID, titel, useDmsImport, dmsImportTimeOut, dmsImportRootPath, dmsImportImagesPath,
						dmsImportSuccessPath, dmsImportErrorPath, dmsImportCreateProcessFolder, fileFormatInternal, fileFormatDmsExport,
						metsRightsOwner, metsRightsOwnerLogo, metsRightsOwnerSite, metsDigiprovReference, metsDigiprovPresentation, metsPointerPath,
						metsPointerPathAnchor, metsDigiprovReferenceAnchor, metsDigiprovPresentationAnchor, metsPurl, metsContentIDs,
						metsRightsOwnerMail, startDate, endDate, numberOfPages, numberOfVolumes, projectIsArchived);
				return po;
			}
			return null;
		}
	};

	public static ResultSetHandler<List<ProjectFileGroup>> resultSetToProjectFilegroupListHandler = new ResultSetHandler<List<ProjectFileGroup>>() {
		@Override
		public List<ProjectFileGroup> handle(ResultSet rs) throws SQLException {
			List<ProjectFileGroup> answer = new ArrayList<ProjectFileGroup>();
			while (rs.next()) {
				int ProjectFileGroupID = rs.getInt("ProjectFileGroupID");
				String name = rs.getString("name");
				String path = rs.getString("path");
				String mimetype = rs.getString("mimetype");
				String suffix = rs.getString("suffix");
				// int ProjekteID = rs.getInt("ProjekteID");
				String folder = rs.getString("folder");
				ProjectFileGroup pfg = new ProjectFileGroup();
				pfg.setId(ProjectFileGroupID);
				pfg.setName(name);
				pfg.setPath(path);
				pfg.setMimetype(mimetype);
				pfg.setSuffix(suffix);
				// ProjekteId?
				pfg.setFolder(folder);
				answer.add(pfg);
			}
			return answer;
		}
	};

	public static ResultSetHandler<List<String>> resultSetToFilterListtHandler = new ResultSetHandler<List<String>>() {
		@Override
		public List<String> handle(ResultSet rs) throws SQLException {
			List<String> answer = new ArrayList<String>();
			while (rs.next()) {
				String filter = rs.getString("Wert");
				answer.add(filter);
			}
			return answer;
		}
	};

	public static ResultSetHandler<List<Integer>> resultSetToIntegerListHandler = new ResultSetHandler<List<Integer>>() {
		@Override
		public List<Integer> handle(ResultSet rs) throws SQLException {
			List<Integer> answer = new ArrayList<Integer>();
			while (rs.next()) {
				answer.add(new Integer(rs.getInt(1)));
			}
			return answer;
		}
	};

	public static ResultSetHandler<Integer> resultSetToIntegerHandler = new ResultSetHandler<Integer>() {
		@Override
		public Integer handle(ResultSet rs) throws SQLException {
			Integer answer = null;
			if (rs.next()) {
				answer = new Integer(rs.getInt(1));
			}
			return answer;
		}
	};
}
