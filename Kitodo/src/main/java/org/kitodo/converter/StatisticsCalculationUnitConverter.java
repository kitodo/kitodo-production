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

import org.goobi.production.flow.statistics.enums.CalculationUnit;

/**
 * StatisticCalculationUnitConverter for statistics CalculationUnits as
 * select-items in jsf-guis.
 *
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 */
@Named
public class StatisticsCalculationUnitConverter implements Converter {

    /**
     * Convert String to CalculationUnit.
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
        if (value == null) {
            return CalculationUnit.volumes;
        } else {
            return CalculationUnit.getById(value);
        }
    }

    /**
     * Convert ResultOutput to String.
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
        if (value == null || !(value instanceof CalculationUnit)) {
            return CalculationUnit.volumes.getId();
        } else {
            return ((CalculationUnit) value).getId();
        }
    }

}
