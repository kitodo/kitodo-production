/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General private License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.lugh;

import org.junit.Test;

public class NoDataExceptionTest {

    @Test(expected = NoDataException.class)
    public void testNoDataExceptionCanBeThrown() throws NoDataException {
        throw new NoDataException();
    }

}
