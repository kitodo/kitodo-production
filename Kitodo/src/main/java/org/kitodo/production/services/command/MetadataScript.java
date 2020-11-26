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

package org.kitodo.production.services.command;

import java.util.Objects;

public class MetadataScript {

    private String goal;
    private String root;
    private String value;

    /**
     * Creates a MetadataScript with given command.
     * @param command the given command.
     */
    public MetadataScript(String command) {
        if (command.contains("=")) {
            String[] commandParts = command.split("=");
            goal = commandParts[0];
            String rootOrValue = commandParts[1];
            if (rootOrValue.startsWith("@")) {
                root = rootOrValue;
            } else {
                value = rootOrValue;
            }
        }
        else {
            goal = command;
        }
    }

    /**
     * Get goal.
     * @return goal.
     */
    public String getGoal() {
        return goal;
    }

    /**
     * Get root.
     * @return root.
     */
    public String getRoot() {
        return root;
    }

    /**
     * Get value.
     * @return value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value.
     * @param value the value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get name of root metadata.
     * @return the name of the root metadata.
     */
    public String getRootName() {
        return Objects.isNull(root) ? null : root.substring(1);
    }
}
