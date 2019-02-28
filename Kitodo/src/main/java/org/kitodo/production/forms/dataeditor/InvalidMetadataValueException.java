package org.kitodo.production.forms.dataeditor;

import java.util.Arrays;

import javax.management.InvalidAttributeValueException;

import org.kitodo.production.helper.Helper;

public class InvalidMetadataValueException extends InvalidAttributeValueException {

    private String key;
    private String value;

    public InvalidMetadataValueException(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * When the error occurs on a child meta-data element of a parent, while
     * bubbling up, the exception can be added the name of the parent, so that
     * the user knows what input is wrong.
     *
     * @param parentLabel
     *            name of the parent which is wrong
     */
    public void addParent(String parentLabel) {
        key = parentLabel + " » " + key;
    }

    @Override
    public String getLocalizedMessage() {
        return Helper.getTranslation("dataEditor.invalidMetadataValue", Arrays.asList(key, value));
    }

    @Override
    public String getMessage() {
        return "Cannot store \"" + key + "\": The value is invalid. Value: " + value;
    }
}
