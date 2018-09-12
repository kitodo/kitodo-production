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

package org.kitodo.metadata.elements.renderable;

import java.util.List;

import org.kitodo.api.ugh.MetadataInterface;

/**
 * A RenderableGroupableMetadatum is a metadata which can—but doesn’t have to
 * be—a member of a RenderableMetadataGroup. A RenderableGroupableMetadata can
 * be a RenderablePersonMetadataGroup—which is a special case of a
 * RenderableMetadataGroup—but must not be a RenderableMetadataGroup.
 * <p/>
 * Java interfaces are always public and this interface holds the public methods
 * that are accessed by JSF during rendering. Other methods with a more
 * restricted visibility cannot be defined here. They will be defined in the
 * abstract class {@link RenderableGroupableMetadata}.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
interface RenderableGroupableMetadata {

    /**
     * Shall add the data passed from the metadata element as content to the
     * element.
     * 
     * @param data
     *            data to add
     */
    void addContent(MetadataInterface data);

    /**
     * Shall return the label for the metadatum in the language previously set.
     * 
     * @return the label for the metadatum
     */
    String getLabel();

    /**
     * Shall return true if the element is contained in a group and is the first
     * element in its members list, false otherwise.
     * 
     * @return if the element is the first in its list
     */
    boolean isFirst();

    /**
     * Shall return whether the user shall be depredated the permission to edit
     * the value(s) on the screen.
     * 
     * @return whether the component shall be read-only
     */
    boolean isReadonly();

    /**
     * Shall return the metadata elements contained in this display element
     * backing bean.
     * 
     * @return the metadata elements contained in this bean
     */
    List<? extends MetadataInterface> toMetadata();
}
