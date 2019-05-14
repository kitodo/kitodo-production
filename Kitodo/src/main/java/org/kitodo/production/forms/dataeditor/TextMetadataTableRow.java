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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.production.helper.Helper;

public class TextMetadataTableRow extends SimpleMetadataTableRow implements Serializable {

    private String value;

    TextMetadataTableRow(MetadataPanel panel, FieldedMetadataTableRow container,
                         SimpleMetadataViewInterface settings, MetadataEntry value) {
        super(panel, container, settings);
        if (Objects.nonNull(value)) {
            this.value = value.getValue();
        }
    }

    @Override
    public String getInput() {
        switch (settings.getInputType()) {
            case DATE:
                return "calendar";
            case INTEGER:
                return "spinner";
            case MULTI_LINE_TEXT:
                return "inputTextarea";
            case ONE_LINE_TEXT:
                return "inputText";
            default:
                return "";
        }
    }

    @Override
    Collection<Metadata> getMetadata() throws InvalidMetadataValueException {
        if (!settings.isValid(value)) {
            throw new InvalidMetadataValueException(label, value);
        }
        MetadataEntry entry = new MetadataEntry();
        entry.setKey(settings.getId());
        entry.setDomain(DOMAIN_TO_MDSEC.get(settings.getDomain().orElse(Domain.DESCRIPTION)));
        entry.setValue(value);
        return Collections.singletonList(entry);
    }

    @Override
    Pair<Method, Object> getStructureFieldValue() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        if (settings.getDomain().orElse(Domain.DESCRIPTION).equals(Domain.METS_DIV)) {
            if (!settings.isValid(value)) {
                throw new InvalidMetadataValueException(label, value);
            }
            return Pair.of(super.getStructureFieldSetter(settings), value);
        } else {
            return null;
        }
    }

    /**
     * Return value.
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value.
     *
     * @param value
     *          value
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void validatorQuery(FacesContext context, UIComponent component, Object value) {
        if (!settings.isValid(Objects.toString(value))) {
            String message = Helper.getTranslation("dataEditor.invalidMetadataValue",
                Arrays.asList(settings.getLabel(), Objects.toString(value)));
            FacesMessage facesMessage = new FacesMessage(message, message);
            facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(facesMessage);
        }
    }
}
