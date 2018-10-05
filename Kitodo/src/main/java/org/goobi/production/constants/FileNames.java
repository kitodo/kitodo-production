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

package org.goobi.production.constants;

/**
 * This class collects file names used throughout the code.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class FileNames {

    /**
     * Configuration file that lists the digital collections available for the
     * different projects.
     */
    public static final String DIGITAL_COLLECTIONS_FILE = "kitodo_digitalCollections.xml";

    /**
     * Configuration file that lists the available library catalogues along with
     * their respective DocType mappings.
     */
    public static final String OPAC_CONFIGURATION_FILE = "kitodo_opac.xml";

    /**
     * Configuration file for project configuration.
     */
    public static final String PROJECT_CONFIGURATION_FILE = "kitodo_projects.xml";

    /**
     * Configuration file for metadata display rules.
     */
    public static final String METADATA_DISPLAY_RULES_FILE = "kitodo_metadataDisplayRules.xml";

    /**
     * Configuration file for login blacklist.
     */
    public static final String LOGIN_BLACKLIST_FILE = "kitodo_loginBlacklist.txt";

    /**
     * Private constructor to hide the implicit public one.
     */
    private FileNames() {

    }

}
