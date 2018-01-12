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

import java.util.List;
import java.util.Map;

public interface MetadataGroupTypeInterface {

    void addMetadataType(MetadataTypeInterface metadataTypeInterface);

    Map<String, String> getAllLanguages();

    String getLanguage(String language);

    List<MetadataTypeInterface> getMetadataTypeList();

    String getName();

    void setAllLanguages(Map<String, String> allLanguages);

    void setName(String name);

    /**
     * @param num
     *            one of "1m", "1o", "+", or "*"
     * @return {@code false}, if the string argument is not one of these four
     *         string; true otherwise. The return value is never used.
     */
    boolean setNum(String num);
}
