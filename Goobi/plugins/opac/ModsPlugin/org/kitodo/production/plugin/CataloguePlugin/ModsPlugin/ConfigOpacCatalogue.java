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

class ConfigOpacCatalogue {
    private String title = "";
    private String description = "";
    private String address = "";
    private String scheme = "";
    private int port = 80;
    private String charset = "iso-8859-1";
    private String path = "";

    ConfigOpacCatalogue(String title, String desciption, String address, String opacType, String scheme, String path, int port) {
        this.title = title;
        this.description = desciption;
        this.address = address;
        this.scheme = scheme;
        this.path = path;
        this.port = port;
    }

    String getTitle() {
        return this.title;
    }

    String getDescription() {
        return this.description;
    }

    String getAddress() {
        return this.address;
    }

    int getPort() {
        return this.port;
    }

    String getCharset() {
        return this.charset;
    }

    String getScheme() {
        return this.scheme;
    }

    String getPath() {
        return this.path;
    }
}
