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

package org.goobi.production.enums;

import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;

public enum PluginType {
    IMPORT(1, "import", IImportPlugin.class),
    CATALOGUE(5, "opac", null);

    private int id;
    private String name;
    private Class<IPlugin> interfaz;

    @SuppressWarnings("unchecked")
    PluginType(int id, String name, Class<? extends IPlugin> inInterfaz) {
        this.id = id;
        this.name = name;
        this.interfaz = (Class<IPlugin>) inInterfaz;
    }

    public int getId() {
        return this.id;
    }

    /**
     * @deprecated Using this function is discouraged. Use
     *             {@link org.goobi.production.plugin.UnspecificPlugin#typeOf(Class)}
     *             instead.
     */
    @Deprecated
    public Class<IPlugin> getInterfaz() {
        return this.interfaz;
    }

    public String getName() {
        return this.name;
    }

}
