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

package org.goobi.production.plugin.interfaces;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;

public interface IValidatorPlugin extends IPlugin {

    void initialize(Process inProcess);

    boolean validate();

    Task getStep();

    void setStep(Task step);
}
