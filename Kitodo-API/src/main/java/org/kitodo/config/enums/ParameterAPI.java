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

package org.kitodo.config.enums;

public enum ParameterAPI implements ParameterInterface {

    DIR_MODULES("directory.modules"),

    /**
     * Absolute path to the directory that process directories will be created
     * in, terminated by a directory separator ("/"). The servlet container must
     * have write permission to that directory.
     */
    DIR_PROCESSES("directory.metadata"),

    /**
     * Absolute path to the directory that the configuration files are stored
     * in, terminated by a directory separator ("/").
     *
     * <p>
     * Note: Several, but not all configuration files are read from that
     * directory. You may want to decide to point this path to the directory
     * where the servlet container will extract the configuration files to (like
     * webapps/kitodo/WEB-INF/classes) in order to make sure they are found.
     */
    DIR_XML_CONFIG("directory.config");

    private String name;

    /**
     * Private constructor to hide the implicit public one.
     *
     * @param name
     *            of parameter
     */
    ParameterAPI(String name) {
        this.name = name;
    }

    @Override
    public java.lang.String toString() {
        return this.name;
    }
}
