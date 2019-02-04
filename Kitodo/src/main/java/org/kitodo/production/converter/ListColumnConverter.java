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

package org.kitodo.production.converter;

import java.util.Arrays;
import java.util.Objects;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kitodo.data.database.beans.ListColumn;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named
public class ListColumnConverter implements Converter {
    private static final Logger logger = LogManager.getLogger(ListColumnConverter.class);

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
        if (Objects.isNull(value) || value.isEmpty()) {
            return null;
        } else {
            try {
                return ServiceManager.getListColumnService().getById(Integer.valueOf(value));
            } catch (DAOException e) {
                logger.error(e.getLocalizedMessage(), e);
                return "0";
            }
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
        if (Objects.isNull(value)) {
            return null;
        } else if (value instanceof ListColumn) {
            return String.valueOf(((ListColumn) value).getId().intValue());
        } else if (value instanceof String) {
            return (String) value;
        } else {
            throw new ConverterException(Helper.getTranslation("errorConvert",
                    Arrays.asList(value.getClass().getCanonicalName(), "ListColumn")));
        }
    }
}
