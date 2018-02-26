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
import javax.inject.Named;

import org.goobi.production.flow.statistics.enums.ResultOutput;

/**
 * StatisticOutputConverter for statistics ResultOutput as select-items in
 * jsf-guis.
 *
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 */
@Named
public class StatisticsResultOutputConverter implements Converter {

    /**
     * Convert String to ResultOutput.
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null) {
            return ResultOutput.table;
        } else {
            return ResultOutput.getById(value);
        }
    }

    /**
     * Convert ResultOutput to String.
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null || !(value instanceof ResultOutput)) {
            return ResultOutput.table.getId();
        } else {
            return ((ResultOutput) value).getId();
        }
    }

}
