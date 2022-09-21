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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.InputType;
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
        metadataTreeNode.setData(new ProcessTextMetadata(null, getSettingsObject(metadataId), metadataEntry));
        return metadataTreeNode;
    }

    /**
     * Get the simple metadata view mocked with the metadata id.
     *
     * @param id
     *         the metadata id
     * @return the simple metadata view
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static SimpleMetadataViewInterface getSettingsObject(String id) {
        return new SimpleMetadataViewInterface() {
            public Optional<Domain> getDomain() {
                throw new UnsupportedOperationException("Not implemented");
            }

            public String getId() {
                return id;
            }

            public String getLabel() {
                return "";
            }

            public int getMaxOccurs() {
                throw new UnsupportedOperationException("Not implemented");
            }

            public int getMinOccurs() {
                throw new UnsupportedOperationException("Not implemented");
            }

            public boolean isUndefined() {
                throw new UnsupportedOperationException("Not implemented");
            }

            public Collection<String> getDefaultItems() {
                throw new UnsupportedOperationException("Not implemented");
            }

            public InputType getInputType() {
                return InputType.ONE_LINE_TEXT;
            }

            public int getMinDigits() {
                throw new UnsupportedOperationException("Not implemented");
            }

            public Map<String, String> getSelectItems(List<Map<MetadataEntry, Boolean>> metadata) {
                throw new UnsupportedOperationException("Not implemented");
            }

            public boolean isEditable() {
                throw new UnsupportedOperationException("Not implemented");
            }

            public boolean isValid(String value, List<Map<MetadataEntry, Boolean>> metadata) {
                throw new UnsupportedOperationException("Not implemented");
            }
        };
    }

}
