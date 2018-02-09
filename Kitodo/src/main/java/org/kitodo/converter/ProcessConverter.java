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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

public class ProcessConverter implements Converter {
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(ProcessConverter.class);

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
        if (value == null) {
            return null;
        } else {
            try {
                return serviceManager.getProcessService().getById(Integer.valueOf(value));
            } catch (NumberFormatException | DAOException e) {
                logger.error(e);
                return "0";
            }
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
        if (value == null) {
            return null;
        } else if (value instanceof Process) {
            return String.valueOf(((Process) value).getId().intValue());
        } else if (value instanceof String) {
            return (String) value;
        } else {
            throw new ConverterException("Falscher Typ: " + value.getClass() + " muss 'Prozess' sein!");
        }
    }

}
