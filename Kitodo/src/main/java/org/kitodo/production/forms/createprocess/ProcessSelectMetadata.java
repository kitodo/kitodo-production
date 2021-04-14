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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import javax.faces.model.SelectItem;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataformat.Division;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;

public class ProcessSelectMetadata extends ProcessSimpleMetadata implements Serializable {

    /**
     * Converts the select items map to the select items type required by JSF to
     * display the select lists.
     *
     * @param selectItems
     *            items as map key to label
     * @return list of select item objects
     */
    private static List<SelectItem> toItems(Map<String, String> selectItems) {
        ArrayList<SelectItem> items = new ArrayList<>(selectItems.entrySet().size());
        for (Entry<String, String> entry : selectItems.entrySet()) {
            items.add(new SelectItem(entry.getKey(), entry.getValue()));
        }
        return items;
    }

    /**
     * Elements the user can choose from.
     */
    private final List<SelectItem> items;

    /**
     * Elements selected by the user.
     */
    private List<String> selectedItems = new ArrayList<>();

    ProcessSelectMetadata(ProcessFieldedMetadata container, SimpleMetadataViewInterface settings,
            Collection<MetadataEntry> selected) {
        super(container, settings, settings.getLabel());
        List<Map<MetadataEntry, Boolean>> leadingMetadataFields = container.getListForLeadingMetadataFields();
        this.items = toItems(settings.getSelectItems(leadingMetadataFields));
        container.markLeadingMetadataFields(leadingMetadataFields);
        if (selected.isEmpty()) {
            selectedItems.addAll(settings.getDefaultItems());
        } else {
            for (MetadataEntry entry : selected) {
                selectedItems.add(entry.getValue());
            }
        }
    }

    private ProcessSelectMetadata(ProcessSelectMetadata template) {
        super(template.container, template.settings, template.label);
        this.items = template.items;
        this.selectedItems = new ArrayList<>(template.selectedItems);
    }

    @Override
    ProcessSelectMetadata getClone() {
        return new ProcessSelectMetadata(this);
    }

    /**
     * Whether the list is multi-select or not can be told from this.
     */
    @Override
    public String getInput() {
        switch (settings.getInputType()) {
            case MULTIPLE_SELECTION:
                return "manyMenu";
            case MULTI_LINE_SINGLE_SELECTION:
                return "oneRadio";
            case ONE_LINE_SINGLE_SELECTION:
                return "oneMenu";
            default:
                return "";
        }
    }

    /**
     * Returns the select item objects required to display the list.
     *
     * @return the select item objects
     */
    public List<SelectItem> getItems() {
        return items;
    }

    @Override
    public Collection<Metadata> getMetadata() throws InvalidMetadataValueException {
        int items = selectedItems.size();
        Collection<Metadata> metadata = new HashSet<>((int) Math.ceil(items / .75));
        String key = settings.getId();
        MdSec domain = DOMAIN_TO_MDSEC.get(settings.getDomain().orElse(Domain.DESCRIPTION));
        for (String selectedItem : selectedItems) {
            if (!settings.isValid(selectedItem, container.getListForLeadingMetadataFields())) {
                throw new InvalidMetadataValueException(label, selectedItem);
            }
            MetadataEntry entry = new MetadataEntry();
            entry.setKey(key);
            entry.setDomain(domain);
            entry.setValue(selectedItem);
            metadata.add(entry);
        }
        return metadata;
    }

    /**
     * Returns the keys which io selected in single-select scenario.
     *
     * @return identifier of selected element
     */
    public String getSelectedItem() {
        return selectedItems.isEmpty() ? "" : selectedItems.get(0);
    }

    /**
     * Returns the list of keys which are selected in multi-select scenario.
     *
     * @return identifiers of selected elements
     */
    public List<String> getSelectedItems() {
        return selectedItems;
    }

    @Override
    Pair<BiConsumer<Division<?>, String>, String> getStructureFieldValue()
            throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        if (settings.getDomain().orElse(Domain.DESCRIPTION).equals(Domain.METS_DIV)) {
            String value = String.join(" ", selectedItems);
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
        for (String selectedItem : selectedItems) {
            if (!settings.isValid(selectedItem, container.getListForLeadingMetadataFields())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Setter to set the selected elements in multi-select scenario.
     *
     * @param selectedItems
     *            selected items to set
     */
    public void setSelectedItems(List<String> selectedItems) {
        this.selectedItems = selectedItems;
    }

    /**
     * Setter to set the one selected element in single-select scenario.
     *
     * @param selectedItem
     *            selected item to set
     */
    public void setSelectedItem(String selectedItem) {
        this.selectedItems = selectedItem.isEmpty() ? Collections.emptyList() : Collections.singletonList(selectedItem);
    }

    @Override
    public String getMetadataID() {
        return settings.getId();
    }
}
