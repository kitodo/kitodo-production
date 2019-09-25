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
import java.lang.reflect.Method;

import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.exceptions.NoSuchMetadataFieldException;

abstract class SimpleAdditionalDetailsTableRow extends AdditionalDetailsTableRow implements Serializable {

    /**
     * Container to store the ruleset settings.
     */
    protected SimpleMetadataViewInterface settings;

    /**
     * Constructor, must be called from the subclass.
     *
     * @param tab
     *            the metadata panel this row is in
     * @param container
     *            the parental metadata group
     * @param settings
     *            the ruleset settings for this field.
     */
    protected SimpleAdditionalDetailsTableRow(AdditionalDetailsTab tab, FieldedAdditionalDetailsTableRow container,
                                              SimpleMetadataViewInterface settings) {
        super(tab, container, settings.getLabel());
        this.settings = settings;
    }

    protected Method getStructureFieldSetter(MetadataViewInterface field) throws NoSuchMetadataFieldException {
        String key = field.getId();
        for (Method method : IncludedStructuralElement.class.getDeclaredMethods()) {
            if (method.getName().startsWith("set") && method.getParameterTypes().length == 1
                    && method.getName().substring(3).equalsIgnoreCase(key)) {
                return method;
            }
        }
        throw new NoSuchMetadataFieldException(key, field.getLabel());
    }

    /**
     * Returns if the field may be edited. Some fileds may be disallowed to be
     * edit from the rule set.
     *
     * @return whether the field is editable
     */
    public boolean isEditable() {
        return settings.isEditable();
    }

    @Override
    public boolean isUndefined() {
        return settings.isUndefined();
    }

    public boolean isRequired() {
        return settings.getMinOccurs() > 0;
    }

}
