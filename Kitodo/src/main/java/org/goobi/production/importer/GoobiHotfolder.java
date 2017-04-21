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

import de.sub.goobi.config.ConfigCore;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;
import org.goobi.io.SafeFile;
import org.goobi.production.plugin.interfaces.IGoobiHotfolder;

public class GoobiHotfolder implements IGoobiHotfolder {

    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(GoobiHotfolder.class);

    private String name;
    private SafeFile folder;
    private Integer template;
    private String updateStrategy;
    private String collection;

    /**
     * Constructor.
     *
     * @param name
     *            String
     * @param folder
     *            SafeFile
     * @param template
     *            Integer
     * @param updateStrategy
     *            String
     * @param collection
     *            String
     */
    public GoobiHotfolder(String name, SafeFile folder, Integer template, String updateStrategy, String collection) {
        this.setName(name);
        this.folder = folder;
        this.setTemplate(template);
        this.setUpdateStrategy(updateStrategy);
        this.setCollection(collection);
    }

    /**
     * Get current files.
     *
     * @return a list with all xml files in GoobiHotfolder
     */
    @Override
    public List<java.io.File> getCurrentFiles() {
        return this.folder.getCurrentFiles();
    }

    /**
     * Get files by name.
     *
     * @param name
     *            String
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
     * Get file names by filter.
     *
     * @param filter
     *            FilenameFilter object
     * @return a list with all filenames matching the filter
     */
    @Override
    public List<String> getFileNamesByFilter(FilenameFilter filter) {
        return Arrays.asList(this.folder.list(filter));
    }

    /**
     * Get files by filter.
     *
     * @param filter
     *            FilenameFilter object
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
     * true if file is xml file and no anchor file.
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

    /**
     * Get instances.
     *
     * @return list of GoobiHotfolder objects
     */
    public static List<GoobiHotfolder> getInstances() {
        logger.trace("config 1");
        List<GoobiHotfolder> answer = new ArrayList<GoobiHotfolder>();
        logger.trace("config 2");

        try {
            XMLConfiguration config = new XMLConfiguration(
                    ConfigCore.getKitodoConfigDirectory() + "kitodo_hotfolder.xml");

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
            if (logger.isTraceEnabled()) {
                logger.trace("config 19" + e.getMessage());
            }
            return new ArrayList<GoobiHotfolder>();
        }
        logger.trace("config 20");
        return answer;
    }

    /**
     * Set name.
     *
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set template.
     *
     * @param template
     *            the template to set
     */
    public void setTemplate(Integer template) {
        this.template = template;
    }

    /**
     * Get template.
     *
     * @return the template
     */
    public Integer getTemplate() {
        return this.template;
    }

    /**
     * Set update strategy.
     *
     * @param updateStrategy
     *            the updateStrategy to set
     */
    public void setUpdateStrategy(String updateStrategy) {
        this.updateStrategy = updateStrategy;
    }

    /**
     * Get update strategy.
     *
     * @return the updateStrategy
     */
    public String getUpdateStrategy() {
        return this.updateStrategy;
    }

    /**
     * Set collection.
     *
     * @param collection
     *            the collection to set
     */
    public void setCollection(String collection) {
        this.collection = collection;
    }

    /**
     * Get collection.
     *
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

    /**
     * Lock.
     */
    public void lock() throws IOException {
        SafeFile f = getLockFile();
        if (!f.exists()) {
            f.createNewFile();
        }
    }

    /**
     * Unlock.
     */
    public void unlock() throws IOException {
        SafeFile f = getLockFile();
        if (f.exists()) {
            f.forceDelete();
        }
    }
}
