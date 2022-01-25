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

import org.kitodo.data.database.beans.Workflow;
import org.kitodo.production.services.ServiceManager;

@Named
public class WorkflowConverter extends BeanConverter implements Converter<Workflow> {

    @Override
    public Workflow getAsObject(FacesContext context, UIComponent component, String value) {
        return (Workflow) getAsObject(ServiceManager.getWorkflowService(), value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Workflow value) {
        return getAsString(value, "workflow");
    }

}
