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

package org.kitodo.data.database.persistence.apache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.beans.Ruleset;

//TODO: fix it!
public class MySQLUtils {

	public static final ResultSetHandler<List<StepObject>> resultSetToStepObjectListHandler = new ResultSetHandler<List<StepObject>>() {
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

	public static final ResultSetHandler<StepObject> resultSetToStepObjectHandler = new ResultSetHandler<StepObject>() {
		@Override
		public StepObject handle(ResultSet rs) throws SQLException {
			StepObject answer = null;

			if (rs.next()) {
				answer = parseStepObject(rs);
			}
			return answer;
		}
	};

	public static final ResultSetHandler<Ruleset> resultSetToRulesetHandler = new ResultSetHandler<Ruleset>() {
		@Override
		public Ruleset handle(ResultSet rs) throws SQLException {
			if (rs.next()) {
				int id = rs.getInt("id");
				String title = rs.getString("title");
				String file = rs.getString("file");
				boolean order = rs.getBoolean("orderMetadataByRuleset");
				Ruleset r = new Ruleset();
				r.setId(id);
				r.setTitle(title);
				r.setFile(file);
				r.setOrderMetadataByRuleset(order);
				return r;
			}
			return null;
		}
	};

	public static final ResultSetHandler<List<Property>> resultSetToProcessPropertyListHandler = new ResultSetHandler<List<Property>>() {
		@Override
		public List<Property> handle(ResultSet rs) throws SQLException {
			List<Property> answer = new ArrayList<Property>();
			while (rs.next()) {
				int id = rs.getInt("id");
				String title = rs.getString("title");
				String value = rs.getString("value");
				boolean isObligatorisch = rs.getBoolean("isObligatory");
				int dataType = rs.getInt("dataType");
				String choice = rs.getString("choice");
				Date creationDate = rs.getTimestamp("creationDate");
				int container = rs.getInt("container");
				Property prop = new Property(id, title, value, isObligatorisch, dataType, choice, creationDate, container);
				answer.add(prop);
			}
			return answer;
		}
	};

	public static final ResultSetHandler<List<Property>> resultSetToTemplatePropertyListHandler = new ResultSetHandler<List<Property>>() {
		@Override
		public List<Property> handle(ResultSet rs) throws SQLException {
			List<Property> answer = new ArrayList<Property>();
			while (rs.next()) {
				int id = rs.getInt("id");
				String title = rs.getString("title");
				String value = rs.getString("value");
				boolean isObligatory = rs.getBoolean("isObligatory");
				int dataType = rs.getInt("dataType");
				String choice = rs.getString("choice");
				Date creationDate = rs.getTimestamp("creationDate");
				int container = rs.getInt("container");
				Property prop = new Property(id, title, value, isObligatory, dataType, choice, creationDate, container);
				answer.add(prop);
			}
			return answer;
		}
	};

	public static final ResultSetHandler<List<Property>> resultSetToProductPropertyListHandler = new ResultSetHandler<List<Property>>() {
		@Override
		public List<Property> handle(ResultSet rs) throws SQLException {
			List<Property> answer = new ArrayList<Property>();
			while (rs.next()) {
				int id = rs.getInt("id");
				String title = rs.getString("title");
				String value = rs.getString("value");
				boolean isObligatory = rs.getBoolean("isObligatory");
				int dataType = rs.getInt("dataType");
				String choice = rs.getString("choice");
				Date creationDate = rs.getTimestamp("creationDate");
				int container = rs.getInt("container");
				Property prop = new Property(id, title, value, isObligatory, dataType, choice, creationDate, container);
				answer.add(prop);
			}
			return answer;
		}
	};

	public static final ResultSetHandler<ProcessObject> resultSetToProcessHandler = new ResultSetHandler<ProcessObject>() {
		@Override
		public ProcessObject handle(ResultSet rs) throws SQLException {
			if (rs.next()) {
				int processId = rs.getInt("id");
				String title = rs.getString("title");
				String outputName = rs.getString("outputName");
				boolean template = rs.getBoolean("template");
				boolean swappedOut = rs.getBoolean("swappedOut");
				boolean isChoiceListShown = rs.getBoolean("isChoiceListShown");
				String sortHelperStatus = rs.getString("sortHelperStatus");
				int sortHelperImages = rs.getInt("sortHelperImages");
				int sortHelperArticles = rs.getInt("sortHelperArticles");
				Date creationDate = rs.getTimestamp("creationDate");
				int projectId = rs.getInt("project_id");
				int rulesetId = rs.getInt("ruleset_id");
				int sortHelperDocstructs = rs.getInt("sortHelperDocstructs");
				int sortHelperMetadata = rs.getInt("sortHelperMetadata");
				String wikiField = rs.getString("wikiField");
				return new ProcessObject(processId, title, outputName, template, swappedOut, isChoiceListShown,
						sortHelperStatus, sortHelperImages, sortHelperArticles, creationDate, projectId,
						rulesetId, sortHelperDocstructs, sortHelperMetadata, wikiField);
			}
			return null;
		}
	};

	public static final ResultSetHandler<List<String>> resultSetToScriptsHandler = new ResultSetHandler<List<String>>() {
		@Override
		public List<String> handle(ResultSet rs) throws SQLException {
			List<String> answer = new ArrayList<String>();
			if (rs.next()) {
				if (rs.getString("typeAutomaticScriptPath") != null && rs.getString("typeAutomaticScriptPath").length() > 0) {
					answer.add(rs.getString("typeAutomaticScriptPath"));
				}
				if (rs.getString("typeAutomaticScriptPath2") != null && rs.getString("typeAutomaticScriptPath2").length() > 0) {
					answer.add(rs.getString("typeAutomaticScriptPath2"));
				}
				if (rs.getString("typeAutomaticScriptPath3") != null && rs.getString("typeAutomaticScriptPath3").length() > 0) {
					answer.add(rs.getString("typeAutomaticScriptPath3"));
				}
				if (rs.getString("typeAutomaticScriptPath4") != null && rs.getString("typeAutomaticScriptPath4").length() > 0) {
					answer.add(rs.getString("typeAutomaticScriptPath4"));
				}
				if (rs.getString("typeAutomaticScriptPath5") != null && rs.getString("typeAutomaticScriptPath5").length() > 0) {
					answer.add(rs.getString("typeAutomaticScriptPath5"));
				}
			}
			return answer;
		}
	};

	public static final ResultSetHandler<Map<String, String>> resultSetToScriptMapHandler = new ResultSetHandler<Map<String, String>>() {
		@Override
		public Map<String, String> handle(ResultSet rs) throws SQLException {
			Map<String, String> answer = new HashMap<String, String>();
			if (rs.next()) {
				if (rs.getString("typeAutomaticScriptPath") != null && rs.getString("typeAutomaticScriptPath").length() > 0) {
					String name = rs.getString("scriptName1");
					answer.put(name, rs.getString("typeAutomaticScriptPath"));
				}
				if (rs.getString("typeAutomaticScriptPath2") != null && rs.getString("typeAutomaticScriptPath2").length() > 0) {
					String name = rs.getString("scriptName2");
					answer.put(name, rs.getString("typeAutomaticScriptPath2"));
				}
				if (rs.getString("typeAutomaticScriptPath3") != null && rs.getString("typeAutomaticScriptPath3").length() > 0) {
					String name = rs.getString("scriptName3");
					answer.put(name, rs.getString("typeAutomaticScriptPath3"));
				}
				if (rs.getString("typeAutomaticScriptPath4") != null && rs.getString("typeAutomaticScriptPath4").length() > 0) {
					String name = rs.getString("scriptName4");
					answer.put(name, rs.getString("typeAutomaticScriptPath4"));
				}
				if (rs.getString("typeAutomaticScriptPath5") != null && rs.getString("typeAutomaticScriptPath5").length() > 0) {
					String name = rs.getString("scriptName5");
					answer.put(name, rs.getString("typeAutomaticScriptPath5"));
				}
			}
			return answer;
		}
	};

	private static StepObject parseStepObject(ResultSet rs) throws SQLException {
		StepObject so = null;

		if (rs != null) {
			int id = rs.getInt("id");
			String title = rs.getString("title");
			int ordering = rs.getInt("ordering");
			int processingStatus = rs.getInt("processingStatus");
			Date processingTime = rs.getTimestamp("processingTime");
			Date processingBegin = rs.getTimestamp("processingBegin");
			Date processingEnd = rs.getTimestamp("processingEnd");
			int processId = rs.getInt("process_id");
			int processingUser = rs.getInt("user_id");
			int editType = rs.getInt("editType");
			boolean typExport = rs.getBoolean("typeExportDMS");
			boolean typeAutomatic = rs.getBoolean("typeAutomatic");
			boolean readAccess = rs.getBoolean("typeImagesRead");
			boolean writeAccess = rs.getBoolean("typeImagesWrite");
			boolean metadataAccess = rs.getBoolean("typMetadata");
			boolean typeFinishImmediately = rs.getBoolean("typeAcceptClose");
			String stepPlugin = rs.getString("stepPlugin");
			String validationPlugin = rs.getString("validationPlugin");

			so = new StepObject(id, title, ordering, processingStatus, processingTime, processingBegin, processingEnd,
					processingUser, editType, typExport, typeAutomatic, processId, readAccess, writeAccess,
					metadataAccess, typeFinishImmediately, stepPlugin, validationPlugin);
		}

		return so;
	}

	public static final ResultSetHandler<ProjectObject> resultSetToProjectHandler = new ResultSetHandler<ProjectObject>() {
		@Override
		public ProjectObject handle(ResultSet rs) throws SQLException {
			if (rs.next()) {
				int projekteID = rs.getInt("id");
				String titel = rs.getString("title");
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

	public static final ResultSetHandler<List<ProjectFileGroup>> resultSetToProjectFilegroupListHandler = new ResultSetHandler<List<ProjectFileGroup>>() {
		@Override
		public List<ProjectFileGroup> handle(ResultSet rs) throws SQLException {
			List<ProjectFileGroup> answer = new ArrayList<ProjectFileGroup>();
			while (rs.next()) {
				int ProjectFileGroupID = rs.getInt("id");
				String name = rs.getString("name");
				String path = rs.getString("path");
				String mimeType = rs.getString("mimeType");
				String suffix = rs.getString("suffix");
				// int ProjekteID = rs.getInt("ProjekteID");
				String folder = rs.getString("folder");
				ProjectFileGroup pfg = new ProjectFileGroup();
				pfg.setId(ProjectFileGroupID);
				pfg.setName(name);
				pfg.setPath(path);
				pfg.setMimeType(mimeType);
				pfg.setSuffix(suffix);
				// ProjekteId?
				pfg.setFolder(folder);
				answer.add(pfg);
			}
			return answer;
		}
	};

	public static final ResultSetHandler<List<String>> resultSetToFilterListtHandler = new ResultSetHandler<List<String>>() {
		@Override
		public List<String> handle(ResultSet rs) throws SQLException {
			List<String> answer = new ArrayList<String>();
			while (rs.next()) {
				String filter = rs.getString("value");
				answer.add(filter);
			}
			return answer;
		}
	};

	public static final ResultSetHandler<List<Integer>> resultSetToIntegerListHandler = new ResultSetHandler<List<Integer>>() {
		@Override
		public List<Integer> handle(ResultSet rs) throws SQLException {
			List<Integer> answer = new ArrayList<Integer>();
			while (rs.next()) {
				answer.add(Integer.valueOf(rs.getInt(1)));
			}
			return answer;
		}
	};

	public static final ResultSetHandler<Integer> resultSetToIntegerHandler = new ResultSetHandler<Integer>() {
		@Override
		public Integer handle(ResultSet rs) throws SQLException {
			Integer answer = null;
			if (rs.next()) {
				answer = Integer.valueOf(rs.getInt(1));
			}
			return answer;
		}
	};
}
