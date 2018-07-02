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

import de.sub.goobi.helper.Helper;

import java.util.Arrays;
import java.util.Objects;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.DocketDAO;

@Named
public class DocketConverter implements Converter {
    private static final Logger logger = LogManager.getLogger(DocketConverter.class);

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (Objects.isNull(value) || value.isEmpty()) {
            return null;
        } else {
            try {
                return new DocketDAO().getById(Integer.valueOf(value));
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
        } else if (value instanceof Docket) {
            return String.valueOf(((Docket) value).getId().intValue());
        } else if (value instanceof String) {
            return (String) value;
        } else {
            throw new ConverterException(Helper.getTranslation("errorConvert",
                    Arrays.asList(value.getClass().getCanonicalName(), "Docket")));
        }
    }

}
