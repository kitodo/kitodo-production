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

package org.kitodo.production.metadata.elements.renderable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.helper.metadata.LegacyMetadataHelper;
import org.kitodo.helper.metadata.LegacyMetadataTypeHelper;

/**
 * Backing bean for a (multi-line) text input element to edit metadata
 * renderable by JSF.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class LineEdit extends RenderableMetadata
        implements RenderableGroupableMetadata, SingleValueRenderableMetadata {
    /**
     * Line separator used in web front-end I/O.
     */
    private static final String HTML_TEXTAREA_LINE_SEPARATOR = "\r\n";

    /**
     * Line separator used in filesystem I/O.
     */
    private static final String METADATA_LINE_SEPARATOR = "\n";

    /**
     * Holds the content lines of the edit box.
     */
    private List<String> value;

    /**
     * Constructor. Creates a RenderableLineEdit.
     *
     * @param metadataType
     *            metadata type editable by this drop-down list
     * @param binding
     *            a metadata group whose corresponding metadata element shall be
     *            updated if the setter method is called
     * @param container
     *            metadata group this drop-down list is showing in
     */
    public LineEdit(LegacyMetadataTypeHelper metadataType, MetadataGroupInterface binding,
                    RenderableMetadataGroup container) {

        super(metadataType, binding, container);
        if (binding != null) {
            for (LegacyMetadataHelper data : binding.getMetadataByType(metadataType.getName())) {
                addContent(data);
            }
        }
    }

    /**
     * Adds the data passed from the metadata element as content to the input.
     * If there is data already (shouldn’t be, but however) it is appended for
     * not being lost.
     *
     * @param data
     *            data to add
     */
    @Override
    public void addContent(LegacyMetadataHelper data) {
        if (value == null) {
            value = new ArrayList<>(Arrays.asList(data.getValue().split(METADATA_LINE_SEPARATOR)));
        } else {
            value.addAll(Arrays.asList(data.getValue().split(METADATA_LINE_SEPARATOR)));
        }
    }

    /**
     * Returns the edit field value.
     *
     * @see org.kitodo.production.metadata.elements.renderable.SingleValueRenderableMetadata#getValue()
     */
    @Override
    public String getValue() {
        if (value != null) {
            return StringUtils.join(value, HTML_TEXTAREA_LINE_SEPARATOR);
        } else {
            return "";
        }
    }

    /**
     * Saves the value entered by the user.
     *
     * @see org.kitodo.production.metadata.elements.renderable.SingleValueRenderableMetadata#setValue(java.lang.String)
     */
    @Override
    public void setValue(String value) {
        this.value = Arrays.asList(value.split(HTML_TEXTAREA_LINE_SEPARATOR));
        updateBinding();
    }

    /**
     * Returns the value of this edit component as metadata element.
     *
     * @return a list with one metadata element with the value of this component
     * @see org.kitodo.production.metadata.elements.renderable.RenderableGroupableMetadata#toMetadata()
     */
    @Override
    public List<LegacyMetadataHelper> toMetadata() {
        List<LegacyMetadataHelper> result = new ArrayList<>(1);
        result.add(getMetadata(StringUtils.join(value, METADATA_LINE_SEPARATOR)));
        return result;
    }
}
