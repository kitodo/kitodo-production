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

package org.kitodo.production.plugin.opac.pica;

class Catalogue {

    private String cbs = "";

    private final String dataBase;
    private final String serverAddress;
    private final int port;

    Catalogue(ConfigOpacCatalogue coc) {
        this.serverAddress = coc.getAddress();
        this.port = coc.getPort();
        this.dataBase = coc.getDatabase();
        this.cbs = coc.getCbs();
    }

    String getDataBase() {
        return dataBase;
    }

    int getPort() {
        return port;
    }

    String getServerAddress() {
        return serverAddress;
    }

    String getCbs() {
        return cbs;
    }
}
