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

package de.sub.goobi.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Authorization;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

public class AuthorizationConverter implements Converter {

    private ServiceManager serviceManager;
    private static final Logger logger = LogManager.getLogger(AuthorizationConverter.class);

    public AuthorizationConverter() {
        this.serviceManager = new ServiceManager();
    }

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) throws ConverterException {
        try {
            return serviceManager.getAuthorizationService().getById(Integer.parseInt(s));
        } catch (DAOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) throws ConverterException {
        return ((Authorization) o).getId().toString();
    }
}
