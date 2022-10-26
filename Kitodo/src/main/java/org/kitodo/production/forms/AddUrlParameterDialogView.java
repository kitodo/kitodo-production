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


package org.kitodo.production.forms;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.kitodo.data.database.beans.UrlParameter;

@Named
@RequestScoped
public class AddUrlParameterDialogView {

    private UrlParameter urlParameter;

    /**
     * Default constructor.
     */
    public AddUrlParameterDialogView() {
        init();
    }

    /**
     * Get urlParameter.
     *
     * @return value of urlParameter
     */
    public UrlParameter getUrlParameter() {
        return urlParameter;
    }

    /**
     * Set urlParameter.
     *
     * @param urlParameter as org.kitodo.data.database.beans.UrlParameter
     */
    public void setUrlParameter(UrlParameter urlParameter) {
        this.urlParameter = urlParameter;
    }

    /**
     * Initializes url parameter.
     */
    public void init() {
        urlParameter = new UrlParameter();
    }

}
