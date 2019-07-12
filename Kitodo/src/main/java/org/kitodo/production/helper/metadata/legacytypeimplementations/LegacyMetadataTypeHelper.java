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

package org.kitodo.production.helper.metadata.legacytypeimplementations;

import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;

/**
 * Connects a legacy metadata type to a key view. This is a soldering class to
 * keep legacy code operational which is about to be removed. Do not use this
 * class.
 */
public class LegacyMetadataTypeHelper {

    /**
     * A representative for a special legacy metadata type to read and write
     * the METS ORDER attribute.
     */
    @Deprecated
    public static final LegacyMetadataTypeHelper SPECIAL_TYPE_ORDER = new LegacyMetadataTypeHelper() {

        @Override
        @Deprecated
        public String getName() {
            return "physPageNumber";
        }
    };

    /**
     * A representative for a special legacy metadata type to read and write
     * the METS ORDERLABEL attribute.
     */
    @Deprecated
    public static final LegacyMetadataTypeHelper SPECIAL_TYPE_ORDERLABEL = new LegacyMetadataTypeHelper() {

        @Override
        @Deprecated
        public String getName() {
            return "logicalPageNumber";
        }
    };

    /**
     * The key view accessed via this soldering class.
     */
    private MetadataViewInterface keyView;

    private LegacyMetadataTypeHelper() {
        this.keyView = null;
    }

    @Deprecated
    public LegacyMetadataTypeHelper(MetadataViewInterface keyView) {
        this.keyView = keyView;
    }

    @Deprecated
    public String getName() {
        return keyView.getId();
    }
}
