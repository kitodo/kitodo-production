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

package org.kitodo.services;

import org.kitodo.data.database.beans.ProcessProperty;

public class ProcessPropertyService {

    public String getNormalizedTitle(ProcessProperty processProperty) {
        return processProperty.getTitle().replace(" ", "_").trim();
    }

    public String getNormalizedValue(ProcessProperty processProperty) {
        return processProperty.getValue().replace(" ", "_").trim();
    }
}
