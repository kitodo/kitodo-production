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

public interface DocStructTypeInterface {

    List<String> getAllAllowedDocStructTypes(); /*
                                                 * note: list<String>! not
                                                 * list<DocStructType>
                                                 */

    List<MetadataTypeInterface> getAllMetadataTypes(); // iterable would be sufficient

    String getAnchorClass();

    String getName(); /* internal name, ID */

    String getNameByLanguage(String language);

    String getNumberOfMetadataType(MetadataTypeInterface metadataTypeInterface);

}
