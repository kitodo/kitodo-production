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

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.kitodo.data.database.beans.UrlParameter;

@Named
@ViewScoped
public class UpdateUrlParameterDialogView implements Serializable {

    private UrlParameter urlParameter;

    private int urlParameterIndex;

    public UpdateUrlParameterDialogView() {

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
        urlParameterIndex = urlParameter.getImportConfiguration().getUrlParameters().indexOf(urlParameter);
    }

    /**
     * Get urlParameterIndex.
     *
     * @return value of urlParameterIndex
     */
    public int getUrlParameterIndex() {
        return urlParameterIndex;
    }

    /**
     * Set urlParameterIndex.
     *
     * @param urlParameterIndex as int
     */
    public void setUrlParameterIndex(int urlParameterIndex) {
        this.urlParameterIndex = urlParameterIndex;
    }
}
