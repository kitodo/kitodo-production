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

package org.kitodo.production.metadata.display;

import org.kitodo.production.metadata.display.enums.BindState;

public class Modes {

    private static BindState bindState = BindState.CREATE;

    /**
     * Add a private constructor to hide the implicit public one.
     */
    private Modes() {

    }

    public static BindState getBindState() {
        return bindState;
    }

    public static void setBindState(BindState inBindState) {
        bindState = inBindState;
    }

}
