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
    private static final Logger logger = LogManager.getLogger(CustomListColumnInitializer.class);
    private static String[] processProperties;

    /**
     * Load process properties from Kitodo configuration file and save them as custom list columns to
     * database during application startup.
     *
     * @param context ServletContext
     */
    public void init(@Observes @Initialized(ApplicationScoped.class) ServletContext context) {
        try {
            processProperties = Arrays.stream(ConfigCore.getParameter(ParameterCore.PROCESS_PROPERTIES).split(","))
                    .filter(name -> !name.trim().isEmpty())
                    .map(name -> PROCESS_PREFIX + name.trim())
                    .toArray(String[]::new);

            ServiceManager.getListColumnService().removeProcessCustomListColumns(Arrays.asList(processProperties));

            List<String> availableColumnNames = ServiceManager.getListColumnService().getAllCustomListColumns().stream()
                    .map(ListColumn::getTitle)
                    .collect(Collectors.toList());
            logger.info("Loading process property names from configuration file...");
            for (String ppn : processProperties) {
                if (!ppn.trim().isEmpty() && !availableColumnNames.contains(ppn)) {
                    ListColumn processPropertyColumn = new ListColumn(ppn, true);
                    ServiceManager.getListColumnService().saveToDatabase(processPropertyColumn);
                    logger.info("Process property '" + ppn + "' successfully saved to database as custom list column!");
                } else {
                    logger.info("Process property '" + ppn + "' already saved to database as custom list column!");
                }
            }
        } catch (DAOException e) {
            logger.error("Unable to save process property names from Kitodo configuration file as custom list columns to database!");
        } catch (NoSuchElementException e) {
            logger.error("Configuration key '"
                    + ParameterCore.PROCESS_PROPERTIES.toString()
                    + "' not found in configuration => unable to process properties as custom columns!");
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
}
