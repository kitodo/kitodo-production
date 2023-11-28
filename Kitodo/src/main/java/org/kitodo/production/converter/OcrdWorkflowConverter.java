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

import java.util.Objects;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.production.services.ServiceManager;


@Named
public class OcrdWorkflowConverter extends BeanConverter implements Converter<Object> {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (StringUtils.isNotEmpty(value)) {
            return ServiceManager.getOcrdWorkflowService().getOcrdWorkflow(value);
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (Objects.nonNull(value) && value instanceof Pair<?,?>) {
            return (String) ((Pair<?,?>) value).getKey();
        }
        return null;
    }

}
