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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

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

/**
 * A row on the meta-data panel that contains an on/off switch.
 */
public class BooleanMetadataTableRow extends SimpleMetadataTableRow {
    /**
     * Whether the switch is on or off.
     */
    private boolean on;

    /**
     * An additional description text to be printed with the switch.
     */
    private final String optionLabel;

    /**
     * Creates a new meta-data panel row with an on/off switch.
     * 
     * @param dataEditor
     *            meta-data editor instance
     * @param container
     *            containing meta-data group
     * @param settings
     *            configuration settings from the rule set
     * @param data
     *            data to display
     */
    BooleanMetadataTableRow(DataEditorForm dataEditor, FieldedMetadataTableRow container,
            SimpleMetadataViewInterface settings, MetadataEntry data) {
        super(dataEditor, container, settings);
        this.on = Objects.nonNull(data);

        Iterator<Entry<String, String>> selectItems = settings.getSelectItems().entrySet().iterator();
        this.optionLabel = selectItems.hasNext() ? selectItems.next().getValue() : "";
    }

    @Override
    public String getInput() {
        return "toggleSwitch";
    }

    @Override
    public Collection<Metadata> getMetadata() throws InvalidMetadataValueException {
        if (!isValid()) {
            throw new InvalidMetadataValueException(label, settings.convertBoolean(on).orElse(""));
        }
        Optional<String> value = settings.convertBoolean(on);
        if (value.isPresent()) {
            MetadataEntry entry = new MetadataEntry();
            entry.setKey(settings.getId());
            entry.setDomain(DOMAIN_TO_MDSEC.get(settings.getDomain().orElse(Domain.DESCRIPTION)));
            entry.setValue(value.get());
            return Arrays.asList(entry);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns an additional text that may be shown next to the switch.
     *
     * @return some text
     */
    public String getOptionLabel() {
        return optionLabel;
    }

    @Override
    Pair<Method, Object> getStructureFieldValue() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        if (settings.getDomain().orElse(Domain.DESCRIPTION).equals(Domain.METS_DIV)) {
            if (!isValid()) {
                throw new InvalidMetadataValueException(label, settings.convertBoolean(on).orElse(""));
            }
            return Pair.of(super.getStructureFieldSetter(settings), settings.convertBoolean(on).orElse(null));
        } else {
            return null;
        }
    }

    /**
     * Returns whether the switch is on.
     *
     * @return whether the switch is on
     */
    public boolean isOn() {
        return on;
    }

    private boolean isValid() {
        Optional<String> value = settings.convertBoolean(on);
        return !value.isPresent() || settings.isValid(value.get());
    }

    @Override
    public void validatorQuery(FacesContext context, UIComponent component, Object value) {
        if ((value instanceof Boolean) && (Boolean) value) {
            String stringValue = settings.convertBoolean((Boolean) value).orElseThrow(IllegalStateException::new);
            if (!settings.isValid(stringValue)) {
                String message = Helper.getTranslation("dataEditor.invalidMetadataValue",
                    Arrays.asList(settings.getLabel(), stringValue));
                FacesMessage facesMessage = new FacesMessage(message, message);
                facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(facesMessage);
            }
        }
    }

    /**
     * Setter if the user changes the switch.
     *
     * @param on
     *            whether the switch is on
     */
    public void setOn(boolean on) {
        this.on = on;
    }
}
