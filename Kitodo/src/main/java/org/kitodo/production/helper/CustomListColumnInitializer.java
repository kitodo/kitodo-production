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

package org.kitodo.production.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.servlet.ServletContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.ListColumn;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

@ApplicationScoped
@SuppressWarnings("unused")
public class CustomListColumnInitializer {

    private static final String PROCESS_PREFIX = "process.";
    private static final String TASK_PREFIX = "task.";
    private static final Logger logger = LogManager.getLogger(CustomListColumnInitializer.class);
    private String[] processProperties;
    private String[] taskProcessProperties;

    /**
     * Load custom columns from Kitodo configuration file and save them as custom list columns to
     * database during application startup.
     *
     * @param context ServletContext
     */
    public void init(@Observes @Initialized(ApplicationScoped.class) ServletContext context) {
        processProperties = loadCustomColumnsFromConfigurationFile(ParameterCore.PROCESS_PROPERTIES, PROCESS_PREFIX);
        taskProcessProperties = loadCustomColumnsFromConfigurationFile(ParameterCore.TASK_CUSTOM_COLUMNS, TASK_PREFIX);
        try {
            ArrayList<String> customColumnList = new ArrayList<>();
            customColumnList.addAll(Arrays.asList(processProperties));
            customColumnList.addAll(Arrays.asList(taskProcessProperties));
            ServiceManager.getListColumnService().removeCustomListColumns(customColumnList);

            List<String> availableColumnNames = ServiceManager.getListColumnService().getAllCustomListColumns().stream()
                    .map(ListColumn::getTitle)
                    .collect(Collectors.toList());
            logger.info("Loading custom column names from configuration file...");
            for (String ccn : customColumnList) {
                if (!ccn.trim().isEmpty()) {
                    if (availableColumnNames.contains(ccn)) {
                        logger.info("Custom column '" + ccn + "' already saved to database as custom list column!");
                    } else {
                        ListColumn customColumn = new ListColumn(ccn, true);
                        ServiceManager.getListColumnService().saveToDatabase(customColumn);
                        logger.info("Custom column '" + ccn + "' successfully saved to database as custom list column!");
                    }
                }
            }
        } catch (DAOException e) {
            logger.error("Unable to update custom list columns in database!");

        }
    }

    /**
     * Get names of process properties configured in kitodo_config.properties as custom columns for the process list
     *
     * @return array of process propterties
     */
    public String[] getProcessProperties() {
        return processProperties;
    }

    /**
     * Get names of custom task columns configured in kitodo_config.properties as custom columns for the task list
     *
     * @return array of custom task columns
     */
    public String[] getTaskProcessProperties() {
        return taskProcessProperties;
    }

    private String[] loadCustomColumnsFromConfigurationFile(ParameterCore configurationKey, String prefix) {
        String[] customColumns;
        try {
            customColumns = Arrays.stream(ConfigCore.getParameter(configurationKey).split(","))
                    .filter(name -> !name.trim().isEmpty())
                    .map(name -> prefix + name.trim())
                    .toArray(String[]::new);
        } catch (NoSuchElementException e) {
            customColumns = new String[0];
            logger.info("Configuration key '"
                    + ParameterCore.PROCESS_PROPERTIES.toString()
                    + "' not found in configuration => unable to load process list custom columns!");
        }
        return customColumns;
    }
}
