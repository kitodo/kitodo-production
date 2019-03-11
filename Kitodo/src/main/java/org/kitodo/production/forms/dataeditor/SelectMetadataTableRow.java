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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.production.helper.Helper;

/**
 * The meta-data input is some kind of select input.
 */
public class SelectMetadataTableRow extends SimpleMetadataTableRow implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Converts the select items map to the select items type required by JSF to
     * display the select lists.
     *
     * @param selectItems
     *            items as map key to label
     * @return list of select item objects
     */
    private static final List<SelectItem> toItems(Map<String, String> selectItems) {
        ArrayList<SelectItem> result = new ArrayList<>(selectItems.entrySet().size());
        for (Entry<String, String> entry : selectItems.entrySet()) {
            result.add(new SelectItem(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    /**
     * Elements the user can choose from.
     */
    private List<SelectItem> items;

    /**
     * Elements selected by the user.
     */
    private List<String> selectedItems = new ArrayList<>();

    SelectMetadataTableRow(MetadataPanel panel, FieldedMetadataTableRow container, SimpleMetadataViewInterface settings,
            Collection<MetadataEntry> selected) {
        super(panel, container, settings);
        this.items = toItems(settings.getSelectItems());
        for (MetadataEntry entry : selected) {
            selectedItems.add(entry.getValue());
        }
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
    Collection<Metadata> getMetadata() throws InvalidMetadataValueException {
        int items = selectedItems.size();
        Collection<Metadata> result = new HashSet<>((int) Math.ceil(items / .75));
        String key = settings.getId();
        MdSec domain = DOMAIN_TO_MDSEC.get(settings.getDomain().orElse(Domain.DESCRIPTION));
        for (String selectedItem : selectedItems) {
            if (!settings.isValid(selectedItem)) {
                throw new InvalidMetadataValueException(label, selectedItem);
            }
            MetadataEntry entry = new MetadataEntry();
            entry.setKey(key);
            entry.setDomain(domain);
            entry.setValue(selectedItem);
            result.add(entry);
        }
        return result;
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
    Pair<Method, Object> getStructureFieldValue() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        if (settings.getDomain().orElse(Domain.DESCRIPTION).equals(Domain.METS_DIV)) {
            String value = String.join(" ", selectedItems);
            if (!settings.isValid(value)) {
                throw new InvalidMetadataValueException(label, value);
            }
            return Pair.of(super.getStructureFieldSetter(settings), value);
        } else {
            return null;
        }
    }

    @Override
    public void validatorQuery(FacesContext context, UIComponent component, Object value) {
        for (String selectedItem : selectedItems) {
            if (!settings.isValid(selectedItem)) {
                String message = Helper.getTranslation("dataEditor.invalidMetadataValue",
                    Arrays.asList(settings.getLabel(), selectedItem));
                FacesMessage facesMessage = new FacesMessage(message, message);
                facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(facesMessage);
            }
        }
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
        this.selectedItems = selectedItem.isEmpty() ? Collections.emptyList() : Arrays.asList(selectedItem);
    }
}
