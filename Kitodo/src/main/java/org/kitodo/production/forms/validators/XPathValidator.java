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
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.production.helper.XMLUtils;

@FacesValidator("XPathValidator")
public class XPathValidator implements Validator<String> {
    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, String xpathString)
            throws ValidatorException {
        if (StringUtils.isNotBlank(xpathString)) {
            try {
                XMLUtils.validateXPathSyntax(xpathString);
            } catch (XPathExpressionException e) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "XPath syntax error in provided String '"
                                + xpathString + "': " + e.getMessage(), null));
            }
        }
    }
}
