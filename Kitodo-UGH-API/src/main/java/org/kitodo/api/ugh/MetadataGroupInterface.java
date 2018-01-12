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

import java.util.Collection;
import java.util.List;

public interface MetadataGroupInterface {

    void addMetadata(MetadataInterface metadataInterface);

    void addPerson(PersonInterface personInterface);

    List<MetadataInterface> getMetadataByType(String type);

    public List<MetadataInterface> getMetadataList();

    public Iterable<PersonInterface> getPersonByType(String personType);

    public Collection<PersonInterface> getPersonList();

    public MetadataGroupTypeInterface getType();
}
