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

import org.goobi.production.plugin.interfaces.ICommandPlugin;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;

public enum PluginType {
    // TODO: Use upper case for constants; use „CATALOGUE“ instead of „Opac“
    Import(1, "import", IImportPlugin.class),
    Step(2, "step", IStepPlugin.class),
    Validation(3, "validation", IValidatorPlugin.class),
    Command(4, "command", ICommandPlugin.class),
    Opac(5, "opac", null);

    private int id;
    private String name;
    private Class<IPlugin> interfaz;

    @SuppressWarnings("unchecked")
    PluginType(int id, String name, Class<? extends IPlugin> inInterfaz) {
        this.id = id;
        this.name = name;
        this.interfaz = (Class<IPlugin>) inInterfaz;
    }

    /**
     * Get type from value.
     *
     * @param pluginType
     *            String
     * @return PluginType object
     */
    public static PluginType getTypeFromValue(String pluginType) {
        if (pluginType != null) {
            for (PluginType type : PluginType.values()) {
                if (type.getName().equals(pluginType)) {
                    return type;
                }
            }
        }
        return null;
    }

    /**
     * Get types from id.
     *
     * @param pluginType
     *            int
     * @return PluginType object
     */
    public static PluginType getTypesFromId(int pluginType) {
        for (PluginType type : PluginType.values()) {
            if (type.getId() == pluginType) {
                return type;
            }
        }
        return null;
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
