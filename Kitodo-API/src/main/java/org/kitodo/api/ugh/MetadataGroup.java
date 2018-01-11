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

public interface MetadataGroup {

    void addMetadata(Metadata metadata);

    void addPerson(Person person);

    List<Metadata> getMetadataByType(String type);

    public List<Metadata> getMetadataList();

    public Iterable<Person> getPersonByType(String personType);

    public Collection<Person> getPersonList();

    public MetadataGroupType getType();
}
