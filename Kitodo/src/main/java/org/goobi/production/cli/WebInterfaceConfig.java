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

package org.goobi.production.cli;

import de.sub.goobi.config.ConfigCore;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

public class WebInterfaceConfig {

    /**
     * Private constructor to hide the implicit public one.
     */
    private WebInterfaceConfig() {

    }

    /**
     * Get credentials.
     *
     * @param requestIp
     *            String
     * @param requestPassword
     *            String
     * @return list of Strings
     */
    public static List<String> getCredentials(String requestIp, String requestPassword) {
        ArrayList<String> allowed = new ArrayList<>();
        try {
            XMLConfiguration config = new XMLConfiguration(ConfigCore.getKitodoConfigDirectory() + "kitodo_webapi.xml");
            config.setListDelimiter('&');
            config.setReloadingStrategy(new FileChangedReloadingStrategy());

            int count = config.getMaxIndex("credentials");
            for (int i = 0; i <= count; i++) {
                String ip = config.getString("credentials(" + i + ")[@ip]");
                String password = config.getString("credentials(" + i + ")[@password]");
                if (requestIp.startsWith(ip) && requestPassword.equals(password)) {
                    int countCommands = config.getMaxIndex("credentials(" + i + ").command");

                    for (int j = 0; j <= countCommands; j++) {
                        allowed.add(config.getString("credentials(" + i + ").command(" + j + ")[@name]"));
                    }
                }
            }
        } catch (ConfigurationException | RuntimeException e) {
            allowed = new ArrayList<>();
        }
        return allowed;

    }
}
