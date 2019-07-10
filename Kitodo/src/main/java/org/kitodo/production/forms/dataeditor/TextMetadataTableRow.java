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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.production.helper.Helper;

public class TextMetadataTableRow extends SimpleMetadataTableRow implements Serializable {
    private static final Logger logger = LogManager.getLogger(TextMetadataTableRow.class);

    private String value;
    private Date date;

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
     * Returns the contents of the text input field of this meta-data table row.
     *
     * @return the contents of the input field
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the contents of the text input field of this meta-data table row.
     *
     * @param value
     *            value to be set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get date.
     *
     * @return value of date
     */
    public Date getDate() {
        if (Objects.isNull(date) && Objects.nonNull(getValue())) {
            try {
                date = new SimpleDateFormat("yyyy-mm-dd").parse(getValue());
            } catch (ParseException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        return date;
    }

    /**
     * Set date.
     *
     * @param date as java.util.Date
     */
    public void setDate(Date date) {
        this.date = date;
        this.value = new SimpleDateFormat("yyyy-mm-dd").format(date);
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
