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
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataformat.Division;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;

/**
 * A row on the metadata panel that contains an on/off switch.
 */
public class ProcessBooleanMetadata extends ProcessSimpleMetadata implements Serializable {

    /**
     * Whether the switch is on or off.
     */
    private boolean active;

    /**
     * Creates a new metadata panel row with an on/off switch.
     *
     * @param settings
     *            configuration settings from the rule set
     * @param data
     *            data to display
     */
    ProcessBooleanMetadata(ProcessFieldedMetadata container, SimpleMetadataViewInterface settings, MetadataEntry data) {
        super(container, settings, settings.getLabel());
        this.active = Objects.nonNull(data) || settings.getBooleanDefaultValue();
    }

    private ProcessBooleanMetadata(ProcessBooleanMetadata template) {
        super(template.container, template.settings, template.label);
        this.active = template.active;
    }

    @Override
    ProcessBooleanMetadata getClone() {
        return new ProcessBooleanMetadata(this);
    }

    @Override
    public String getMetadataID() {
        return settings.getId();
    }

    @Override
    public String getInput() {
        return "toggleSwitch";
    }

    @Override
    public Collection<Metadata> getMetadata() throws InvalidMetadataValueException {
        if (!isValid()) {
            throw new InvalidMetadataValueException(label, settings.convertBoolean(active).orElse(""));
        }
        Optional<String> value = settings.convertBoolean(active);
        if (value.isPresent()) {
            MetadataEntry entry = new MetadataEntry();
            entry.setKey(settings.getId());
            entry.setDomain(DOMAIN_TO_MDSEC.get(settings.getDomain().orElse(Domain.DESCRIPTION)));
            entry.setValue(value.get());
            return Collections.singletonList(entry);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    Pair<BiConsumer<Division<?>, String>, String> getStructureFieldValue()
            throws InvalidMetadataValueException, NoSuchMetadataFieldException {

        if (settings.getDomain().orElse(Domain.DESCRIPTION).equals(Domain.METS_DIV)) {
            if (!isValid()) {
                throw new InvalidMetadataValueException(label, settings.convertBoolean(active).orElse(""));
            }
            return Pair.of(super.getStructureFieldSetters(settings), settings.convertBoolean(active).orElse(null));
        } else {
            return null;
        }
    }

    /**
     * Returns whether the switch is on.
     *
     * @return whether the switch is on
     */
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isValid() {
        Optional<String> value = settings.convertBoolean(active);
        return !value.isPresent() || settings.isValid(value.get(), container.getListForLeadingMetadataFields());
    }

    /**
     * Setter if the user changes the switch.
     *
     * @param active
     *            whether the switch is on
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
