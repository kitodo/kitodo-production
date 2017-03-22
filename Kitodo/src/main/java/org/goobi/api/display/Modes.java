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

package org.goobi.api.display;

import org.goobi.api.display.enums.BindState;

public class Modes {

    public static BindState myBindState = BindState.create;

    public static BindState getBindState() {
        return myBindState;
    }

    public static void setBindState(BindState inBindState) {
        myBindState = inBindState;
    }

}
