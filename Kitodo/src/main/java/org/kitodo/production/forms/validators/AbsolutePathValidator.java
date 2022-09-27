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

package org.kitodo.production.forms.validators;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;

@FacesValidator("AbsolutePathValidator")
public class AbsolutePathValidator implements Validator<String> {

    private static final String SAVE = "editForm:save";

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, String pathString)
            throws ValidatorException {
        // only validate when saving
        if (!facesContext.getExternalContext().getRequestParameterMap().containsKey(SAVE)) {
            return;
        }
        if (StringUtils.isNotBlank(pathString) && !pathString.startsWith("/")) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Non empty URL path must be absolute, e.g. start with a '/'!", null));
        }
    }
}
