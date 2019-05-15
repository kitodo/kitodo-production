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

package org.kitodo.production.forms.dataeditor;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;

/**
 * Provides functions that are common to all input elements.
 */
abstract class SimpleMetadataTableRow extends MetadataTableRow implements Serializable {

    /**
     * Container to store the ruleset settings.
     */
    protected SimpleMetadataViewInterface settings;

    /**
     * Constructor, must be called from the subclass.
     *
     * @param panel
     *            the meta-data panel this row is in
     * @param container
     *            the parental meta-data group
     * @param settings
     *            the ruleset settings for this field.
     */
    protected SimpleMetadataTableRow(MetadataPanel panel, FieldedMetadataTableRow container,
            SimpleMetadataViewInterface settings) {
        super(panel, container, settings.getLabel());
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

    /**
     * Returns if the value of the field validates. If not, the field cannot be
     * saved.
     */
    public abstract void validatorQuery(FacesContext context, UIComponent component, Object value);
}
