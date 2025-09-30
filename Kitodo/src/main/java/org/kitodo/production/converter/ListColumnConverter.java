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

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.inject.Named;

import org.kitodo.production.services.ServiceManager;

@Named
public class ListColumnConverter extends BeanConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return getAsObject(ServiceManager.getListColumnService(), value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return getAsString(value, "listColumn");
    }
}
