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

import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.production.services.ServiceManager;

@Named
public class LdapServerConverter extends BeanConverter implements Converter<LdapServer> {

    @Override
    public LdapServer getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        return (LdapServer) getAsObject(ServiceManager.getLdapServerService(), value);
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, LdapServer value) {
        return getAsString(value, "ldapServer");
    }
}
