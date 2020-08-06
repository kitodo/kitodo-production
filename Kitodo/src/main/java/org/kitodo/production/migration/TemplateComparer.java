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

package org.kitodo.production.migration;

import java.util.Objects;

import org.kitodo.data.database.beans.Template;

public class TemplateComparer {

    public boolean isEqual(Template firstTemplate, Template secondTemplate) {
        if (Objects.isNull(firstTemplate)) {
            return Objects.isNull(secondTemplate);
        }
        if (Objects.isNull(secondTemplate)) {
            return false;
        }
        if (Objects.isNull(firstTemplate.getWorkflow()) ? Objects.nonNull(secondTemplate.getWorkflow())
                : Objects.isNull(secondTemplate.getWorkflow())
                        || !firstTemplate.getWorkflow().getId().equals(secondTemplate.getWorkflow().getId())) {
            return false;
        }
        if (Objects.isNull(firstTemplate.getRuleset()) ? Objects.nonNull(secondTemplate.getRuleset())
                : Objects.isNull(secondTemplate.getRuleset())
                        || !firstTemplate.getRuleset().getId().equals(secondTemplate.getRuleset().getId())) {
            return false;
        }
        if (Objects.isNull(firstTemplate.getDocket()) ? Objects.nonNull(secondTemplate.getDocket())
                : Objects.isNull(secondTemplate.getDocket())
                        || !firstTemplate.getDocket().getId().equals(secondTemplate.getDocket().getId())) {
            return false;
        }
        return true;
    }
}
