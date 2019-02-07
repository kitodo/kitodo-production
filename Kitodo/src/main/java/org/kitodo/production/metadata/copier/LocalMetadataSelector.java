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

package org.kitodo.production.metadata.copier;

import org.apache.commons.configuration.ConfigurationException;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;

public class LocalMetadataSelector extends MetadataSelector {

    public LocalMetadataSelector(String path) throws ConfigurationException {
        throw new UnsupportedOperationException("Dead code pending removal");
    }

    @Override
    protected void createIfPathExistsOnly(CopierData data, LegacyDocStructHelperInterface logicalNode, String value) {
        throw new UnsupportedOperationException("Dead code pending removal");
    }

    @Override
    protected void createOrOverwrite(CopierData data, LegacyDocStructHelperInterface logicalNode, String value) {
        throw new UnsupportedOperationException("Dead code pending removal");
    }

    @Override
    protected Iterable<MetadataSelector> findAll(LegacyDocStructHelperInterface node) {
        throw new UnsupportedOperationException("Dead code pending removal");
    }

    @Override
    protected String findIn(LegacyDocStructHelperInterface node) {
        throw new UnsupportedOperationException("Dead code pending removal");
    }
}
