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

package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

class Catalogue {

	private String uncf = "";

	private final String database;
	private final String address;
	private final int port;

	Catalogue(ConfigOpacCatalogue coc) {
		this.address = coc.getAddress();
		this.port = coc.getPort();
		this.database = coc.getDatabase();
		this.uncf = coc.getUncf();
	}

	String getDatabase() {
		return database;
	}

	int getPort() {
		return port;
	}

	String getAddress() {
		return address;
	}

	String getUncf() {
		return uncf;
	}
}
