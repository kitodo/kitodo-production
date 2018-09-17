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

package org.kitodo.converter;

import java.util.Arrays;
import java.util.Objects;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.BatchDAO;
import org.kitodo.helper.Helper;

@Named
public class BatchConverter implements Converter {
    private static final Logger logger = LogManager.getLogger(BatchConverter.class);

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (Objects.isNull(value) || value.isEmpty()) {
            return null;
        } else {
            try {
                return new BatchDAO().getById(Integer.valueOf(value));
            } catch (DAOException | NumberFormatException e) {
                logger.error(e.getMessage(), e);
                return "0";
            }
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (Objects.isNull(value)) {
            return null;
        } else if (value instanceof Batch) {
            return String.valueOf(((Batch) value).getId().intValue());
        } else if (value instanceof String) {
            return (String) value;
        } else {
            throw new ConverterException(Helper.getTranslation("errorConvert",
                    Arrays.asList(value.getClass().getCanonicalName(), "Batch")));
        }
    }

}
