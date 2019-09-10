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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;


/**
 * A row on the metadata panel that contains an on/off switch.
 */
public class BooleanMetadataTableRow extends SimpleMetadataTableRow implements Serializable {

    /**
     * Whether the switch is on or off.
     */
    private boolean active;

    /**
     * An additional description text to be printed with the switch.
     */
    private final String optionLabel;

    /**
     * Creates a new metadata panel row with an on/off switch.
     *
     * @param tab
     *            MetadataPanel instance
     * @param container
     *            containing metadata group
     * @param settings
     *            configuration settings from the rule set
     * @param data
     *            data to display
     */
    BooleanMetadataTableRow(AdditionalDetailsTab tab, FieldedAdditionalDetailsTableRow container,
            SimpleMetadataViewInterface settings, MetadataEntry data) {

        super(tab, container, settings);
        this.active = Objects.nonNull(data);
        Iterator<Entry<String, String>> selectItems = settings.getSelectItems().entrySet().iterator();
        this.optionLabel = selectItems.hasNext() ? selectItems.next().getValue() : "";
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
                throw new InvalidMetadataValueException(label, settings.convertBoolean(active).orElse(""));
            }
            return Pair.of(super.getStructureFieldSetter(settings), settings.convertBoolean(active).orElse(null));
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

    private boolean isValid() {
        Optional<String> value = settings.convertBoolean(active);
        return !value.isPresent() || settings.isValid(value.get());
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
