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

package org.kitodo.exceptions;

import org.kitodo.production.helper.Helper;

/**
 * This exception is thrown when the user tries to delete an ImportConfiguration that is currently in use.
 */
public class ImportConfigurationInUseException extends RuntimeException {

    private static final String CONFIG_IN_USE = "importConfig.assignedAsDefaultConfig";

    /**
     * Constructor with message.
     *
     * @param configurationTitle title of the ImportConfiguration
     * @param projectTitle title of the Project to which the ImportConfiguration is assigned as default configuration
     */
    public ImportConfigurationInUseException(String configurationTitle, String projectTitle) {
        super(Helper.getTranslation(CONFIG_IN_USE, configurationTitle, projectTitle));
    }

}
