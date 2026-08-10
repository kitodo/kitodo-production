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

package org.kitodo.production.security;

import java.io.IOException;
import java.util.Objects;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.myfaces.renderkit.html.HtmlFormRenderer;
import org.springframework.security.web.csrf.CsrfToken;

/**
 * Modify how HTML forms are rendered and add nonce tokens for CSRF protection.
 * 
 * <p>This class needs to be registered in the "faces-config.xml" as custom renderer for form elements.
 */
public class CsrfFormRenderer extends HtmlFormRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        HttpServletRequest request =
                (HttpServletRequest) context.getExternalContext().getRequest();

        CsrfToken token =
                (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (Objects.nonNull(token) && Objects.nonNull(token.getToken()) && !token.getToken().isBlank()) {
            // add hidden input field containing CSRF nonce token
            context.getResponseWriter().startElement("input", null);
            context.getResponseWriter().writeAttribute("type", "hidden", null);
            context.getResponseWriter().writeAttribute("name", token.getParameterName(), null);
            context.getResponseWriter().writeAttribute("value", token.getToken(), null);
            context.getResponseWriter().endElement("input");
            context.getResponseWriter().write("\n");
        }

        // continue rendering of form element using Apache MyFaces
        super.encodeEnd(context, component);
    }
}
