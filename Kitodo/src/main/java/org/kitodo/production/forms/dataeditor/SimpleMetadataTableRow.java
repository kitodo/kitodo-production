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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import javax.faces.application.FacesMessage;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataformat.Structure;
import org.kitodo.production.helper.Helper;

/**
 * Provides functions that are common to all input elements.
 */
abstract class SimpleMetadataTableRow extends MetadataTableRow {
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
    protected SimpleMetadataTableRow(DataEditorForm dataEditor, FieldedMetadataTableRow container,
            SimpleMetadataViewInterface settings) {
        super(dataEditor, container, settings.getLabel());
        this.settings = settings;
    }

    protected Method getStructureFieldSetter(MetadataViewInterface field) throws NoSuchMetadataFieldException {
        String key = field.getId();
        for (Method method : Structure.class.getDeclaredMethods()) {
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
     *
     * @return if the value validates
     */
    public Validator getValidator() {
        return (context, component, value) -> {
            if (!settings.isValid(Objects.toString(value))) {
                String message = Helper.getTranslation("dataEditor.invalidMetadataValue",
                    Arrays.asList(settings.getLabel(), Objects.toString(value)));
                FacesMessage facesMessage = new FacesMessage(message, message);
                facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(facesMessage);
            }
        };
    }
}
