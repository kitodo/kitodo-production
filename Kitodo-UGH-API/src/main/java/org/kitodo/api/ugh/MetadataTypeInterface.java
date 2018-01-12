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

import java.util.HashMap;
import java.util.Map;

public interface MetadataTypeInterface {

    Map<String, String> getAllLanguages();

    boolean getIsPerson();

    public String getLanguage(String language);

    public String getName();

    public String getNameByLanguage(String language);

    public String getNum();

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean setAllLanguages(HashMap<String, String> labels);

    public void setIdentifier(boolean identifier);

    public void setIsPerson(boolean person);

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean setName(String string);

    /**
     * @param num
     *            one of "1m", "1o", "+", or "*"
     * @return {@code false}, if the string argument is not one of these four
     *         string; true otherwise. The return value is never used.
     */
    boolean setNum(String quantityRestriction);
}
