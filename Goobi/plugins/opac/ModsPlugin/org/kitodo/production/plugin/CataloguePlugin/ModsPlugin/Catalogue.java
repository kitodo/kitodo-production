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

package org.kitodo.production.plugin.CataloguePlugin.ModsPlugin;

class Catalogue {

    private final String serverAddress;
    private final int port;
    private final String scheme;
    private final String path;

    Catalogue(ConfigOpacCatalogue coc) {
        this.serverAddress = coc.getAddress();
        this.port = coc.getPort();
        this.scheme = coc.getScheme();
        this.path = coc.getPath();
    }

    int getPort() {
        return port;
    }

    String getServerAddress() {
        return serverAddress;
    }

    public String getScheme() {
        return scheme;
    }

    public String getPath() {
        return path;
    }
}
