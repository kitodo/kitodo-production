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

package org.kitodo.config.enums;

import java.io.File;
import java.io.FileNotFoundException;

import org.kitodo.config.KitodoConfig;

/**
 * This class collects config files and their names used throughout the code.
 * Additionally it extends some of java.io.File methods to avoid double call.
 */
public enum KitodoConfigFile {

    /**
     * Configuration file that lists the available library catalogs along with
     * their respective DocType mappings.
     */
    OPAC_CONFIGURATION("kitodo_opac.xml"),

    /**
     * Configuration file for project configuration.
     */
    PROJECT_CONFIGURATION("kitodo_projects.xml"),

    /**
     * Configuration file for login blacklist.
     */
    LOGIN_BLACKLIST("kitodo_loginBlacklist.txt");

    private String name;
    private File file;

    /**
     * Private constructor for KitodoConfigFile enum.
     * 
     * @param name
     *            of the configuration file
     */
    KitodoConfigFile(String name) {
        this.name = name;
        this.file = new File(KitodoConfig.getKitodoConfigDirectory() + this.name);
    }

    /**
     * Get name of file.
     *
     * @return value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Get file as File object.
     *
     * @return value of file
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Get absolute path for config file.
     * 
     * @return absolute path for config file
     */
    public String getAbsolutePath() {
        return this.file.getAbsolutePath();
    }

    /**
     * Get by configuration file name.
     *
     * @param name
     *            of configuration file
     * @return File
     */
    public static KitodoConfigFile getByName(String name) throws FileNotFoundException {
        for (KitodoConfigFile file : KitodoConfigFile.values()) {
            if (file.getName().equals(name)) {
                return file;
            }
        }
        throw new FileNotFoundException("Configuration file '" + name + "' doesn't exists!");
    }

    /**
     * Check if configuration file exists.
     * 
     * @return true if exists, false otherwise
     */
    public boolean exists() {
        return this.file.exists();
    }

    @Override
    public String toString() {
        return this.name;
    }
}
