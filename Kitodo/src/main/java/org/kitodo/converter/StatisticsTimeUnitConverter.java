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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;

import org.goobi.production.flow.statistics.enums.TimeUnit;

/**
 * TimeUnitConverter for statistics TimeUnits as select-items in jsf-guis.
 *
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 */
@Named
public class StatisticsTimeUnitConverter implements Converter {

    /**
     * Convert String to TimeUnit.
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
        if (value == null) {
            return TimeUnit.days;
        } else {
            return TimeUnit.getById(value);
        }
    }

    /**
     * Convert TimeUnit to String.
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
        if (value == null || !(value instanceof TimeUnit)) {
            return TimeUnit.days.getId();
        } else {
            return ((TimeUnit) value).getId();
        }
    }

}
