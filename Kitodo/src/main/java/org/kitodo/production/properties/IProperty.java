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

import java.util.Date;
import java.util.List;

public interface IProperty {

    public abstract String getName();

    public abstract void setName(String name);

    public abstract int getContainer();

    public abstract void setContainer(int container);

    public abstract String getValidation();

    public abstract void setValidation(String validation);

    public abstract Type getType();

    public abstract void setType(Type type);

    public abstract String getValue();

    public abstract void setValue(String value);

    public abstract List<String> getPossibleValues();

    public abstract void setPossibleValues(List<String> possibleValues);

    public abstract List<String> getProjects();

    public abstract void setProjects(List<String> projects);

    public abstract List<ShowStepCondition> getShowStepConditions();

    public abstract void setShowStepConditions(List<ShowStepCondition> showStepConditions);

    public abstract AccessCondition getShowProcessGroupAccessCondition();

    public abstract void setShowProcessGroupAccessCondition(AccessCondition showProcessGroupAccessCondition);

    public abstract boolean isValid();

    public void setDateValue(Date inDate);

    public Date getDateValue();

    public abstract IProperty getClone(int containerNumber);

    public abstract void transfer();

}
