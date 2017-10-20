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

import java.net.URI;
import java.net.URISyntaxException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@FacesConverter("URIConverter")
public class URIConverter implements Converter {
    public static final String CONVERTER_ID = "URIConverter";
    private static final Logger logger = LogManager.getLogger(URIConverter.class);

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
        if (value == null || value.length() == 0) {
            return null;
        } else {
            try {
                return new URI(value);
            } catch (URISyntaxException e) {
                logger.error(e);
                return null;
            }
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
        if (object != null) {
            return String.valueOf(((URI) object).getPath());
        } else {
            return null;
        }
    }
}
