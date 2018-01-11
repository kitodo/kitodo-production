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

public interface MetadataType {

    Map<String, String> getAllLanguages();

    boolean getIsPerson();

    public String getLanguage(String language);

    public String getName();

    public String getNameByLanguage(String language);

    public String getNum();

    public void setAllLanguages(HashMap<String, String> labels);

    public void setIdentifier(boolean identifier);

    public void setIsPerson(boolean person);

    public void setName(String string);

    public void setNum(String quantityRestriction);
}
