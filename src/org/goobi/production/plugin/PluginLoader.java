/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.production.plugin;

import java.io.File;
import java.util.Collection;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IPlugin;

import de.sub.goobi.config.ConfigMain;



public class PluginLoader {

	private PluginType type;
	private String id;
	
	public PluginLoader(PluginType t, String id) {
		this.type = t;
		this.id = id;
	}
	
	public IPlugin getPlugin() {
		PluginManager pm = PluginManagerFactory.createPluginManager();
		pm.addPluginsFrom(new File(ConfigMain.getParameter("pluginFolder")).toURI());
		PluginManagerUtil pmu = new PluginManagerUtil(pm);
		Collection<IPlugin> plugins = pmu.getPlugins(IPlugin.class);
		for (IPlugin p : plugins) {
			if (p.getType().equals(type) && p.getId().equals(id)) {
				return p;
			}	
		}
		return null;
	}
	
}
