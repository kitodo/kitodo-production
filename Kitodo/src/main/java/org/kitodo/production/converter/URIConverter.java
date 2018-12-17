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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Named("URIConverter")
public class URIConverter implements Converter {
    private static final Logger logger = LogManager.getLogger(URIConverter.class);

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (Objects.isNull(value) || value.isEmpty()) {
            return null;
        } else {
            try {
                return new URI(value);
            } catch (URISyntaxException e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object object) {
        if (Objects.nonNull(object)) {
            return String.valueOf(((URI) object).getPath());
        } else {
            return null;
        }
    }
}
