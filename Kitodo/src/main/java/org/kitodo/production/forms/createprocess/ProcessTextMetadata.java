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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;

public class ProcessTextMetadata extends ProcessSimpleMetadata implements Serializable {
    private static final Logger logger = LogManager.getLogger(ProcessTextMetadata.class);

    private String value;
    private Date date;

    ProcessTextMetadata(SimpleMetadataViewInterface settings, MetadataEntry value) {
        super(settings);
        if (Objects.nonNull(value)) {
            this.value = value.getValue();
        }
    }

    @Override
    public String getMetadataID() {
        return settings.getId();
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
    public Collection<Metadata> getMetadata() throws InvalidMetadataValueException {
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

    @Override
    public boolean isValid() {
        if (Objects.isNull(value) || value.isEmpty()) {
            return false;
        }
        return settings.isValid(value);
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
        if (Objects.nonNull(date)) {
            this.value = new SimpleDateFormat("yyyy-mm-dd").format(date);
        }
    }

}
