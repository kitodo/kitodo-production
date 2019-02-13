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

package org.kitodo.production.helper.metadata;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataTypeHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyReferenceHelper;

/**
 * This is part of {@link MetadataHelper}.
 */
class ReferencesSortHelper implements Comparator<LegacyReferenceHelper> {

    private LegacyPrefsHelper prefs;

    ReferencesSortHelper(LegacyPrefsHelper prefs) {
        this.prefs = prefs;
    }

    @Override
    public int compare(LegacyReferenceHelper firstObject, LegacyReferenceHelper secondObject) {
        Integer firstPage = 0;
        Integer secondPage = 0;
        final LegacyMetadataTypeHelper mdt = prefs.getMetadataTypeByName("physPageNumber");
        List<? extends LegacyMetadataHelper> listMetadata = firstObject.getTarget().getAllMetadataByType(mdt);
        if (Objects.nonNull(listMetadata) && !listMetadata.isEmpty()) {
            final LegacyMetadataHelper page = listMetadata.get(0);
            firstPage = Integer.parseInt(page.getValue());
        }
        listMetadata = secondObject.getTarget().getAllMetadataByType(mdt);
        if (Objects.nonNull(listMetadata) && !listMetadata.isEmpty()) {
            final LegacyMetadataHelper page = listMetadata.get(0);
            secondPage = Integer.parseInt(page.getValue());
        }
        return firstPage.compareTo(secondPage);
    }
}
