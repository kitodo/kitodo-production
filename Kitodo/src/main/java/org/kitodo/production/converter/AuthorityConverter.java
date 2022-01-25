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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Named;

import org.kitodo.data.database.beans.Authority;
import org.kitodo.production.services.ServiceManager;

@Named
public class AuthorityConverter extends BeanConverter implements Converter<Authority> {

    @Override
    public Authority getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        return (Authority) getAsObject(ServiceManager.getAuthorityService(), value);
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Authority value) {
        return getAsString(value, "authority");
    }
}
