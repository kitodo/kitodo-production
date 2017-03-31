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

public class ShowStepCondition {

    private String name;
    private AccessCondition accessCondition = AccessCondition.READ;
    private boolean duplication = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public void setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
    }

    public boolean isDuplication() {
        return duplication;
    }

    public void setDuplication(boolean duplication) {
        this.duplication = duplication;
    }
}
