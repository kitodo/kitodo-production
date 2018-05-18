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
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.plugin.interfaces.IGoobiHotfolder;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class GoobiHotfolder implements IGoobiHotfolder {

    /** Logger for this class. */
    private static final Logger logger = LogManager.getLogger(GoobiHotfolder.class);

    private String name;
    private URI folder;
    private Integer template;
    private String updateStrategy;
    private String collection;
    private static final String HOTFOLDER = "hotfolder";
    private final ServiceManager serviceManager = new ServiceManager();
    public final FileService fileService = serviceManager.getFileService();

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
    public GoobiHotfolder(String name, URI folder, Integer template, String updateStrategy, String collection) {
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
    public List<URI> getCurrentFiles() {
        return fileService.getSubUris(this.folder);
    }

    /**
     * Get files by name.
     *
     * @param name
     *            String
     * @return a list with all filenames containing the name in GoobiHotfolder
     */

    @Override
    public List<URI> getFilesByName(String name) {
        List<URI> files = fileService.getSubUris(this.folder);
        List<URI> answer = new ArrayList<>();
        for (URI file : files) {
            if (file.toString().contains(name) && !file.toString().contains("anchor")) {
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
     * @return a list with all file names matching the filter
     */
    @Override
    public List<URI> getFileNamesByFilter(FilenameFilter filter) {
        return fileService.getSubUris(filter, this.folder);
    }

    /**
     * Get files by filter.
     *
     * @param filter
     *            FilenameFilter object
     * @return a list with all file matching the filter
     */
    @Override
    public List<URI> getFilesByFilter(FilenameFilter filter) {
        return fileService.getSubUris(filter, this.folder);
    }

    @Override
    public String getFolderAsString() {
        return this.folder.getPath() + File.separator;
    }

    @Override
    public File getFolderAsFile() {
        return new File(this.folder.getPath());
    }

    @Override
    public URI getFolderAsUri() {
        return this.folder;
    }

    /**
     * true if file is xml file and no anchor file.
     */
    public static final FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return !name.contains("anchor") && !name.endsWith("_") && name.endsWith(".xml");
        }
    };

    /**
     * Get instances.
     *
     * @return list of GoobiHotfolder objects
     */
    public static List<GoobiHotfolder> getInstances() {
        logger.trace("config 1");
        List<GoobiHotfolder> answer = new ArrayList<>();
        logger.trace("config 2");

        try {
            XMLConfiguration config = new XMLConfiguration(
                    ConfigCore.getKitodoConfigDirectory() + "kitodo_hotfolder.xml");

            logger.trace("config 3");

            config.setListDelimiter('&');

            logger.trace("config 4");
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            logger.trace("config 5");

            int count = config.getMaxIndex(HOTFOLDER);
            logger.trace("config 6");

            for (int i = 0; i <= count; i++) {
                logger.trace("config 7");
                String name = config.getString(HOTFOLDER + "(" + i + ")[@name]");
                logger.trace("config 8");
                URI folder = URI.create(config.getString(HOTFOLDER + "(" + i + ")[@folder]"));
                logger.trace("config 9");
                Integer template = config.getInt(HOTFOLDER + "(" + i + ")[@template]");
                logger.trace("config 10");
                String updateStrategy = config.getString(HOTFOLDER + "(" + i + ")[@updateStrategy]");
                logger.trace("config 11");
                String collection = config.getString(HOTFOLDER + "(" + i + ")[@collection]");
                logger.trace("config 12");
                if (name == null || name.equals("")) {
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

        } catch (ConfigurationException | RuntimeException e) {
            logger.trace("config 19 {}", e.getMessage());
            return new ArrayList<>();
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

    public URI getLockFile() throws IOException {
        return fileService.createResource(this.folder, ".lock");

    }

    public boolean isLocked() throws IOException {
        return fileService.fileExist(getLockFile());
    }

    /**
     * Lock.
     */
    public void lock() throws IOException {
        URI f = getLockFile();
        if (!fileService.fileExist(f)) {
            fileService.createResource(f.toString());
        }
    }

    /**
     * Unlock.
     */
    public void unlock() throws IOException {
        URI f = getLockFile();
        if (fileService.fileExist(f)) {
            fileService.delete(f);
        }
    }
}
