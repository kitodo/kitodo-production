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

import de.sub.goobi.helper.Helper;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;
import org.goobi.production.plugin.interfaces.IPlugin;

public class ConfigPlugins {

	private static final Logger logger = Logger.getLogger(ConfigPlugins.class);

	/**
	 * @param inPlugin add description
	 * @return add description
	 */
	public static XMLConfiguration getPluginConfig(IPlugin inPlugin) {
		String file = "plugin_" + inPlugin.getClass().getSimpleName() + ".xml";
		XMLConfiguration config;
		try {
			config = new XMLConfiguration(new Helper().getGoobiConfigDirectory() + file);
		} catch (ConfigurationException e) {
			logger.error(e);
			config = new XMLConfiguration();
		}
		config.setListDelimiter('&');
		config.setReloadingStrategy(new FileChangedReloadingStrategy());
		return config;
	}
}
