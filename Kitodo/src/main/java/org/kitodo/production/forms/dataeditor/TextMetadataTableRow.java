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

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;

public class TextMetadataTableRow extends SimpleMetadataTableRow {

    private String value = "";

    public TextMetadataTableRow(DataEditorForm dataEditor, FieldedMetadataTableRow container,
            SimpleMetadataViewInterface settings, MetadataEntry value) {
        super(dataEditor, container, settings);
        this.value = value.getValue();
    }

    @Override
    public String getInput() {
        switch (settings.getInputType()) {
            case DATE:
                return "calendar";
            case INTEGER:
                return "inputNumber";
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
        if (settings.isValid(value)) {
            throw new InvalidMetadataValueException(label, value);
        }
        MetadataEntry entry = new MetadataEntry();
        entry.setKey(settings.getId());
        entry.setDomain(DOMAIN_TO_MDSEC.get(settings.getDomain().orElse(Domain.DESCRIPTION)));
        entry.setValue(value);
        return Arrays.asList(entry);
    }

    @Override
    Pair<Method, Object> getStructureFieldValue() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        if (settings.getDomain().orElse(Domain.DESCRIPTION).equals(Domain.METS_DIV)) {
            if (settings.isValid(value)) {
                throw new InvalidMetadataValueException(label, value);
            }
            return Pair.of(super.getStructureFieldSetter(settings), value);
        } else {
            return null;
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
