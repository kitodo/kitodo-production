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

package de.sub.goobi.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.constants.FileNames;

public class ConfigProjects {
    private XMLConfiguration config;
    private String projektTitel;
    private static final Logger logger = LogManager.getLogger(ConfigProjects.class);

    public ConfigProjects(String projectTitle) throws IOException {
        this(projectTitle, ConfigCore.getKitodoConfigDirectory() + FileNames.PROJECT_CONFIGURATION_FILE);
    }

    /**
     * Constructor.
     *
     * @param projectTitle
     *            project title
     * @param configPath
     *            config path
     */
    public ConfigProjects(String projectTitle, String configPath) throws IOException {
        if (!(new File(configPath)).exists()) {
            throw new IOException("File not found: " + configPath);
        }
        try {
            this.config = new XMLConfiguration(configPath);
        } catch (ConfigurationException e) {
            logger.error(e.getMessage(), e);
            this.config = new XMLConfiguration();
        }
        this.config.setListDelimiter('&');
        this.config.setReloadingStrategy(new FileChangedReloadingStrategy());

        int countProjects = this.config.getMaxIndex("project");
        for (int i = 0; i <= countProjects; i++) {
            String title = this.config.getString("project(" + i + ")[@name]");
            if (title.equals(projectTitle)) {
                this.projektTitel = "project(" + i + ").";
                break;
            }
        }

        try {
            this.config.getBoolean(this.projektTitel + "createNewProcess.opac[@use]");
        } catch (NoSuchElementException e) {
            this.projektTitel = "project(0).";
        }

    }

    /**
     * Ermitteln eines bestimmten Parameters der Konfiguration als String.
     *
     * @return Parameter als String
     */
    public String getParamString(String inParameter) {
        try {
            this.config.setListDelimiter('&');
            String rueckgabe = this.config.getString(this.projektTitel + inParameter);
            return cleanXmlFormattedString(rueckgabe);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Ermitteln eines bestimmten Parameters der Konfiguration mit Angabe eines
     * Default-Wertes.
     *
     * @return Parameter als String
     */
    public String getParamString(String inParameter, String inDefaultIfNull) {
        try {
            this.config.setListDelimiter('&');
            String myParam = this.projektTitel + inParameter;
            String rueckgabe = this.config.getString(myParam, inDefaultIfNull);
            return cleanXmlFormattedString(rueckgabe);
        } catch (RuntimeException e) {
            return inDefaultIfNull;
        }
    }

    private String cleanXmlFormattedString(String inString) {
        if (inString != null) {
            inString = inString.replaceAll("\t", " ");
            inString = inString.replaceAll("\n", " ");
            while (inString.contains("  ")) {
                inString = inString.replaceAll("  ", " ");
            }
        }
        return inString;
    }

    /**
     * Ermitteln eines boolean-Parameters der Konfiguration.
     *
     * @return Parameter als String
     */
    public boolean getParamBoolean(String inParameter) {
        try {
            return this.config.getBoolean(this.projektTitel + inParameter);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Ermitteln eines long-Parameters der Konfiguration.
     *
     * @return Parameter als Long
     */
    public long getParamLong(String inParameter) {
        try {
            return this.config.getLong(this.projektTitel + inParameter);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Ermitteln einer Liste von Parametern der Konfiguration.
     *
     * @return Parameter als List
     */
    @SuppressWarnings("unchecked")
    public List<String> getParamList(String inParameter) {
        try {
            return this.config.getList(this.projektTitel + inParameter);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

}
