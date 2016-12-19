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

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.persistence.apache.StepObject;

public interface IValidatorPlugin extends IPlugin {

	public void initialize(Prozess inProcess);

	public boolean validate();

	public Schritt getStep();

	public void setStep(Schritt step);

	public StepObject getStepObject();

	public void setStepObject(StepObject so);

}
