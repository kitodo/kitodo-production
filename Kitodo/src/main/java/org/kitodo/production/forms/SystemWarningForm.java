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

package org.kitodo.production.forms;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.kitodo.production.channel.SystemMessageChannel;

@Named("SystemWarningForm")
@SessionScoped
public class SystemWarningForm implements Serializable {

    @Inject
    private SystemMessageChannel systemMessageChannel;

    /**
     * Set warning String.
     *
     * @param warningText warning String
     */
    public void setWarning(String warningText) {
        this.systemMessageChannel.setMessage(warningText);
    }

    /**
     * Get warning String.
     *
     * @return warning String
     */
    public String getWarning() {
        return "";
    }
}
