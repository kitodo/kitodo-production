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

package org.kitodo.api.ugh;

public interface VirtualFileGroup {

    public void setFileSuffix(String suffix);

    public void setMimetype(String mimeType);

    public void setName(String name);

    public void setOrdinary(boolean ordinary);

    public void setPathToFiles(String replace);
}
