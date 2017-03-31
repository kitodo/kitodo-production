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

package org.kitodo.production.plugin.interfaces;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.persistence.apache.StepObject;

public interface IValidatorPlugin extends IPlugin {

    public void initialize(Process inProcess);

    public boolean validate();

    public Task getStep();

    public void setStep(Task step);

    public StepObject getStepObject();

    public void setStepObject(StepObject so);

}
