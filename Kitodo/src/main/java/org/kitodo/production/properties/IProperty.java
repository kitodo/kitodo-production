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

package org.kitodo.production.properties;

import java.util.List;

public interface IProperty {

    String getName();

    void setName(String name);

    int getContainer();

    void setContainer(int container);

    String getValidation();

    void setValidation(String validation);

    Type getType();

    void setType(Type type);

    String getValue();

    void setValue(String value);

    List<String> getProjects();

    void setProjects(List<String> projects);

    boolean isValid();
}
