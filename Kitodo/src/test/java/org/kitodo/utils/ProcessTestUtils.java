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

package org.kitodo.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.production.forms.createprocess.ProcessTextMetadata;
import org.primefaces.model.DefaultTreeNode;

public class ProcessTestUtils {

    /**
     * Get a tree node with ProcessTextMetadata item.
     *
     * @param metadataId
     *         The metadata id
     * @param metadataKey
     *         The metadata key
     * @param metadataValue
     *         The metadata value
     * @return the tree node
     */
    public static DefaultTreeNode getTreeNode(String metadataId, String metadataKey, String metadataValue) {
        DefaultTreeNode metadataTreeNode = new DefaultTreeNode();
        MetadataEntry metadataEntry = new MetadataEntry();
        metadataEntry.setKey(metadataKey);
        metadataEntry.setValue(metadataValue);
        metadataTreeNode.setData(new ProcessTextMetadata(null, getSimpleMetadataView(metadataId), metadataEntry));
        return metadataTreeNode;
    }

    private static SimpleMetadataViewInterface getSimpleMetadataView(String metadataId) {
        SimpleMetadataViewInterface simpleMetadataView = mock(SimpleMetadataViewInterface.class);
        when(simpleMetadataView.getId()).thenReturn(metadataId);
        return simpleMetadataView;
    }

}
