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

package org.kitodo.production.forms.createprocess;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataformat.Division;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.exceptions.NoSuchMetadataFieldException;

abstract class ProcessSimpleMetadata extends ProcessDetail implements Serializable {

    static final List<Class<? extends Division<?>>> PARENT_CLASSES = Arrays.asList(LogicalDivision.class,
        MediaUnit.class);

    /**
     * Container to store the ruleset settings.
     */
    protected SimpleMetadataViewInterface settings;

    /**
     * Constructor, must be called from the subclass.
     *
     * @param settings
     *            the ruleset settings for this field.
     */
    protected ProcessSimpleMetadata(ProcessFieldedMetadata container, SimpleMetadataViewInterface settings,
            String label) {
        super(container, label);
        this.settings = settings;
    }

    /**
     * Returns an independently mutable copy of this.
     *
     * @return an independently mutable copy
     */
    abstract ProcessSimpleMetadata getClone();

    protected BiConsumer<Division<?>, String> getStructureFieldSetters(MetadataViewInterface field)
            throws NoSuchMetadataFieldException {
        String key = field.getId();

        switch (key.toUpperCase()) {
            case "LABEL":
                return Division::setLabel;
            case "ORDERLABEL":
                return Division::setOrderlabel;
            case "CONTENTIDS":
                return (division, value) -> division.getContentIds().add(URI.create(value));
            default:
                throw new NoSuchMetadataFieldException(key, field.getLabel());
        }
    }

    /**
     * Returns if the field may be edited. Some fields may be disallowed to be
     * edit from the rule set.
     *
     * @return whether the field is editable
     */
    public boolean isEditable() {
        return Objects.isNull(settings) || settings.isEditable();
    }

    @Override
    public boolean isUndefined() {
        return Objects.isNull(settings) || settings.isUndefined();
    }

    public boolean isRequired() {
        return Objects.nonNull(settings) && settings.getMinOccurs() > 0;
    }
}
