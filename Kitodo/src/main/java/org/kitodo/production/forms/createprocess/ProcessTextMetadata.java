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
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.InputType;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataformat.Division;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;

public class ProcessTextMetadata extends ProcessSimpleMetadata implements Serializable {
    private static final Logger logger = LogManager.getLogger(ProcessTextMetadata.class);

    private String value;

    ProcessTextMetadata(ProcessFieldedMetadata container, SimpleMetadataViewInterface settings, MetadataEntry value) {
        super(container, settings, Objects.isNull(settings) ? value.getKey() : settings.getLabel());
        this.value = Objects.isNull(value) ? settings.getDefaultValue() : value.getValue();
    }

    ProcessTextMetadata(ProcessTextMetadata template) {
        super(template.container, template.settings, template.label);
        this.value = template.value;
    }

    @Override
    ProcessTextMetadata getClone() {
        return new ProcessTextMetadata(this);
    }

    @Override
    public String getMetadataID() {
        return settings.getId();
    }

    @Override
    public String getInput() {
        InputType inputType = Objects.isNull(settings) ? InputType.ONE_LINE_TEXT : settings.getInputType();
        switch (inputType) {
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
    public Collection<Metadata> getMetadata() {
        value = value.trim();
        if (value.isEmpty()) {
            return Collections.emptyList();
        }
        /* if (!settings.isValid(value)) {
            throw new InvalidMetadataValueException(label, value);
        }*/
        MetadataEntry entry = new MetadataEntry();
        entry.setKey(settings.getId());
        entry.setDomain(DOMAIN_TO_MDSEC.get(settings.getDomain().orElse(Domain.DESCRIPTION)));
        entry.setValue(value);
        return Collections.singletonList(entry);
    }

    @Override
    Pair<BiConsumer<Division<?>, String>, String> getStructureFieldValue()
            throws InvalidMetadataValueException, NoSuchMetadataFieldException {

        if (settings.getDomain().orElse(Domain.DESCRIPTION).equals(Domain.METS_DIV)) {
            if (!settings.isValid(value, container.getListForLeadingMetadataFields())) {
                throw new InvalidMetadataValueException(label, value);
            }
            return Pair.of(super.getStructureFieldSetters(settings), value);
        } else {
            return null;
        }
    }

    @Override
    public boolean isValid() {
        if (Objects.isNull(value) || value.isEmpty()) {
            return false;
        }
        return settings.isValid(value, container.getListForLeadingMetadataFields());
    }

    /**
     * Returns the contents of the text input field of this process metadata.
     *
     * @return the contents of the input field
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the contents of the text input field of this process metadata.
     *
     * @param value
     *            value to be set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
