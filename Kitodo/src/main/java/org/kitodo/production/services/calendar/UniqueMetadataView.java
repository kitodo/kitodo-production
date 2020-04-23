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

package org.kitodo.production.services.calendar;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;

public class UniqueMetadataView implements Predicate<MetadataViewInterface> {

    Set<String> idsAlreadyOccurred = ConcurrentHashMap.newKeySet();
    
    @Override
    public boolean test(MetadataViewInterface metadataView) {
        return idsAlreadyOccurred.add(metadataView.getId());
    }
}
