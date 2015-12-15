package org.goobi.production.importer;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
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
import org.goobi.io.SafeFile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.goobi.production.plugin.interfaces.IGoobiHotfolder;

import de.sub.goobi.helper.Helper;

public class GoobiHotfolder implements IGoobiHotfolder {

	/** Logger for this class. */
	private static final Logger logger = Logger.getLogger(GoobiHotfolder.class);

	private String name;
	private SafeFile folder;
	private Integer template;
	private String updateStrategy;
	private String collection;

	public GoobiHotfolder(String name, SafeFile folder, Integer template, String updateStrategy, String collection) {
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

	@Override
	public List<java.io.File> getCurrentFiles() {
		return this.folder.getCurrentFiles();
	}

	/**
	 *
	 * @param name
	 * @return a list with all filenames containing the name in GoobiHotfolder
	 */

	@Override
	public List<String> getFilesByName(String name) {
		List<String> files = Arrays.asList(this.folder.list());
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

	@Override
	public List<String> getFileNamesByFilter(FilenameFilter filter) {
		return Arrays.asList(this.folder.list(filter));
	}

	/**
	 *
	 * @param filter
	 * @return a list with all file matching the filter
	 */

	@Override
	public List<File> getFilesByFilter(FilenameFilter filter) {
		return this.folder.getFilesByFilter(filter);
	}

	@Override
	public String getFolderAsString() {
		return this.folder.getAbsolutePath() + File.separator;
	}

	@Override
	public File getFolderAsFile() {
		return new File(this.folder.getPath());
	}

	@Override
	public URI getFolderAsUri() {
		return this.folder.toURI();
	}

	/**
	 * true if file is xml file and no anchor file
	 */

	public static final FilenameFilter filter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			if (!name.contains("anchor") && !name.endsWith("_") && name.endsWith(".xml")) {
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
			 XMLConfiguration config = new XMLConfiguration(new Helper().getGoobiConfigDirectory() + "goobi_hotfolder.xml");

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
				SafeFile folder = new SafeFile(config.getString("hotfolder(" + i + ")[@folder]"));
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
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param template
	 *            the template to set
	 */
	public void setTemplate(Integer template) {
		this.template = template;
	}

	/**
	 * @return the template
	 */
	public Integer getTemplate() {
		return this.template;
	}

	/**
	 * @param updateStrategy
	 *            the updateStrategy to set
	 */
	public void setUpdateStrategy(String updateStrategy) {
		this.updateStrategy = updateStrategy;
	}

	/**
	 * @return the updateStrategy
	 */
	public String getUpdateStrategy() {
		return this.updateStrategy;
	}

	/**
	 * @param collection
	 *            the collection to set
	 */
	public void setCollection(String collection) {
		this.collection = collection;
	}

	/**
	 * @return the collection
	 */
	public String getCollection() {
		return this.collection;
	}

	public SafeFile getLockFile() {
		return new SafeFile(this.folder, ".lock");

	}

	public boolean isLocked() {
		return getLockFile().exists();
	}

	public void lock() throws IOException {
		SafeFile f = getLockFile();
		if (!f.exists()) {
			f.createNewFile();
		}
	}

	public void unlock() throws IOException {
		SafeFile f = getLockFile();
		if (f.exists()) {
			f.forceDelete();
		}
	}
}
