package org.goobi.webservice.taglib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONValue;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigOpac;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.config.DigitalCollections;
import de.sub.goobi.helper.Helper;

public class WebServiceTaglib {

	/**
	 * The function getAdditionalFieldsToBeCompletedManually() returns a map
	 * that contains all fields which have to be provided manually on process
	 * creation for each process template.
	 * 
	 * @return A map in JSON format
	 */

	public static String getAdditionalFieldsToBeCompletedManually()
			throws Exception {
		Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();

		@SuppressWarnings("unchecked")
		List<Prozess> processes = (List<Prozess>) Helper.getHibernateSession()
				.createCriteria(Prozess.class).list();

		for (Prozess p : processes) {
			if (p.isIstTemplate()) {
				Map<String, Object> fields = new HashMap<String, Object>();
				ConfigProjects projectConfig = new ConfigProjects(
						p.getProjekt());
				Integer numFields = projectConfig.getParamList(
						"createNewProcess.itemlist.item").size();
				for (Integer field = 0; field < numFields; field++) {
					String fieldRef = "createNewProcess.itemlist.item(" + field
							+ ")";
					if (!projectConfig.getParamBoolean(fieldRef
							+ "[@ughbinding]")
							&& projectConfig.getParamString(
									fieldRef + "[@from]").equals("werk")) {

						String fieldName = projectConfig
								.getParamString(fieldRef);
						Map<String, Object> fieldConfig = new HashMap<String, Object>();
						Integer selectEntries = projectConfig.getParamList(
								fieldRef + ".select").size();
						if (selectEntries > 0) {
							Map<String, String> selectConfig = new HashMap<String, String>();
							for (Integer selectEntry = 0; selectEntry < selectEntries; selectEntry++) {
								String key = projectConfig
										.getParamString(fieldRef + ".select("
												+ selectEntry + ")");
								String value = projectConfig
										.getParamString(fieldRef + ".select("
												+ selectEntry + ")[@label]");
								selectConfig.put(key, value);
							}
							fieldConfig.put("select", selectConfig);
						}
						fieldConfig.put(
								"required",
								projectConfig.getParamBoolean(fieldRef
										+ "[@required]"));
						fields.put(fieldName, fieldConfig);
					}
				}
				result.put(p.getTitel(), fields);
			}
		}
		return JSONValue.toJSONString(result);
	}

	/**
	 * The function getCatalogues() returns a list that contains all catalogues
	 * configured to read in bibliographic data from
	 * 
	 * @return A list in JSON format
	 */
	public static String getCatalogues() throws Exception {
		List<String> catalogues = new ConfigOpac().getAllCatalogueTitles();
		return JSONValue.toJSONString(catalogues);
	}

	/**
	 * The function getCollections returns a map of all possible digital
	 * collections for all available templates
	 * 
	 * @return A map in JSON format
	 */
	public static String getCollections() throws Exception {
		HashMap<String, List<String>> result = new HashMap<String, List<String>>();

		@SuppressWarnings("unchecked")
		List<Prozess> processes = (List<Prozess>) Helper.getHibernateSession()
				.createCriteria(Prozess.class).list();
		for (Prozess process : processes) {
			if (process.isIstTemplate()) {
				result.put(process.getTitel(), DigitalCollections
						.possibleDigitalCollectionsForProcess(process));
			}
		}
		return JSONValue.toJSONString(result);
	}

	/**
	 * The function getProjects() returns a list of all projects configured.
	 * 
	 * @return A list in JSON format
	 */
	public static String getProjects() {
		Set<String> projects = new HashSet<String>();

		@SuppressWarnings("unchecked")
		List<Prozess> processes = (List<Prozess>) Helper.getHibernateSession()
				.createCriteria(Prozess.class).list();
		for (Prozess process : processes) {
			if (process.isIstTemplate()) {
				projects.add(process.getProjekt().getTitel());
			}
		}
		return JSONValue.toJSONString(new ArrayList<String>(projects));
	}

	/**
	 * The function getProjectsAndTemplates() returns a map with all projects
	 * and their associated templates.
	 * 
	 * @return a map in JSON format.
	 */
	public static String getProjectsAndTemplates() {
		Map<String, Set<String>> data = new HashMap<String, Set<String>>();

		@SuppressWarnings("unchecked")
		List<Prozess> processes = (List<Prozess>) Helper.getHibernateSession()
				.createCriteria(Prozess.class).list();
		for (Prozess process : processes) {
			if (process.isIstTemplate()) {
				String projectName = process.getProjekt().getTitel();
				Set<String> templateList = data.containsKey(projectName) ? data
						.get(projectName) : new HashSet<String>();
				templateList.add(process.getTitel());
				data.put(projectName, templateList);
			}
		}

		Map<String, List<String>> result = new HashMap<String, List<String>>();
		for (String projectName : data.keySet())
			result.put(projectName,
					new ArrayList<String>(data.get(projectName)));

		return JSONValue.toJSONString(result);
	}

	/**
	 * The function getSearchFields returns a map of all search fields available
	 * for any of the catalogs configured with their respective labels.
	 * 
	 * This is coded statically in /newpages/NewProcess/inc_process.jsp and so
	 * it is also hardcoded here by now.
	 * 
	 * @return A map in JSON format.
	 */
	public static String getSearchFields() throws Exception {
		Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();

		// Not yet configurable within Goobi.Production
		Map<String, String> staticSearchFields = new LinkedHashMap<String, String>();
		staticSearchFields.put("12", "PPN");
		staticSearchFields.put("8535", "Barcode");
		staticSearchFields.put("8200", "Barcode 8200");
		staticSearchFields.put("7", "ISBN");
		staticSearchFields.put("8", "ISSN");

		for (String catalogue : new ConfigOpac().getAllCatalogueTitles()) {
			result.put(catalogue, staticSearchFields);
		}
		return JSONValue.toJSONString(result);
	}

	/**
	 * The function getTemplates() returns a list of all available process
	 * templates.
	 * 
	 * @return a list in JSON format.
	 */
	public static String getTemplates() {
		ArrayList<String> result = new ArrayList<String>();

		@SuppressWarnings("unchecked")
		List<Prozess> processes = (List<Prozess>) Helper.getHibernateSession()
				.createCriteria(Prozess.class).list();
		for (Prozess process : processes) {
			if (process.isIstTemplate()) {
				result.add(process.getTitel());
			}
		}
		return JSONValue.toJSONString(result);
	}
}
