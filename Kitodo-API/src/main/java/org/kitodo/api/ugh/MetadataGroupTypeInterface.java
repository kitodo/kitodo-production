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

    void setNum(String num);
}
