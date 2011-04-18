package org.goobi.production.Import;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;
import org.goobi.production.plugin.interfaces.IGoobiHotfolder;

public class GoobiHotfolder implements IGoobiHotfolder {

	/** Logger for this class. */
	private static final Logger logger = Logger.getLogger(GoobiHotfolder.class);

	private String name;
	private File folder;
	private Integer template;
	private String updateStrategy;
	private String collection;

	// public GoobiHotfolder(File folder) {
	// this.folder = folder;
	// }

	public GoobiHotfolder(String name, File folder, Integer template, String updateStrategy, String collection) {
		this.setName(name);
		this.folder = folder;
		this.setTemplate(template);
		this.setUpdateStrategy(updateStrategy);
		this.setCollection(collection);
	}

	/**
	 * 
	 * @return a list with all xml files in GoobiHotfolder
	 */

	public List<File> getCurrentFiles() {
		List<File> files = new ArrayList<File>();
		File[] data = folder.listFiles();
		if (data != null) {
			files = Arrays.asList(data);
		}
		return files;
	}

	/**
	 * 
	 * @param name
	 * @return a list with all filenames containing the name in GoobiHotfolder
	 */

	public List<String> getFilesByName(String name) {
		List<String> files = Arrays.asList(folder.list());
		List<String> answer = new ArrayList<String>();
		for (String file : files) {
			if (file.contains(name) && !file.contains("anchor")) {
				answer.add(file);
			}
		}
		return answer;
	}

	/**
	 * 
	 * @param filter
	 * @return a list with all filenames matching the filter
	 */

	public List<String> getFileNamesByFilter(FilenameFilter filter) {
		return Arrays.asList(folder.list(filter));
	}

	/**
	 * 
	 * @param filter
	 * @return a list with all file matching the filter
	 */

	public List<File> getFilesByFilter(FilenameFilter filter) {
		return Arrays.asList(folder.listFiles(filter));
	}

	public String getFolderAsString() {
		return folder.getAbsolutePath() + File.separator;
	}

	public File getFolderAsFile() {
		return folder;
	}

	public URI getFolderAsUri() {
		return folder.toURI();
	}

	/**
	 * true if file is xml file and no anchor file
	 */

	public static FilenameFilter Filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			if (!name.contains("anchor") && name.endsWith(".xml")) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static List<GoobiHotfolder> getInstances() {
		logger.trace("config 1");
		List<GoobiHotfolder> answer = new ArrayList<GoobiHotfolder>();
		logger.trace("config 2");

		try {
			XMLConfiguration config = new XMLConfiguration("config_hotfolder.xml");

			logger.trace("config 3");

			config.setListDelimiter('&');

			logger.trace("config 4");
			config.setReloadingStrategy(new FileChangedReloadingStrategy());
			logger.trace("config 5");

			int count = config.getMaxIndex("hotfolder");
			logger.trace("config 6");

			for (int i = 0; i <= count; i++) {

				logger.trace("config 7");
				String name = config.getString("hotfolder(" + i + ")[@name]");
				logger.trace("config 8");
				File folder = new File(config.getString("hotfolder(" + i + ")[@folder]"));
				logger.trace("config 9");
				Integer template = config.getInt("hotfolder(" + i + ")[@template]");
				logger.trace("config 10");

				String updateStrategy = config.getString("hotfolder(" + i + ")[@updateStrategy]");
				logger.trace("config 11");
				String collection = config.getString("hotfolder(" + i + ")[@collection]");
				logger.trace("config 12");
				if (name == null || name.equals("") || template == null) {
					logger.trace("config 13");
					break;
				}
				logger.trace("config 14");
				if (updateStrategy == null || updateStrategy.equals("")) {
					logger.trace("config 15");
					updateStrategy = "ignore";
				}
				if (collection.equals("")) {
					logger.trace("config 16");
					collection = null;
				}
				logger.trace("config 17");
				answer.add(new GoobiHotfolder(name, folder, template, updateStrategy, collection));
			}
			logger.trace("config 18");

		} catch (Exception e) {
			logger.trace("config 19" + e.getMessage());
			return new ArrayList<GoobiHotfolder>();
		}
		logger.trace("config 20");
		return answer;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(Integer template) {
		this.template = template;
	}

	/**
	 * @return the template
	 */
	public Integer getTemplate() {
		return template;
	}

	/**
	 * @param updateStrategy the updateStrategy to set
	 */
	public void setUpdateStrategy(String updateStrategy) {
		this.updateStrategy = updateStrategy;
	}

	/**
	 * @return the updateStrategy
	 */
	public String getUpdateStrategy() {
		return updateStrategy;
	}

	/**
	 * @param collection the collection to set
	 */
	public void setCollection(String collection) {
		this.collection = collection;
	}

	/**
	 * @return the collection
	 */
	public String getCollection() {
		return collection;
	}
}
