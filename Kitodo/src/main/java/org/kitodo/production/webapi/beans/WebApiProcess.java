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

package org.kitodo.production.webapi.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WebApiProcess {

    private String identifier;

    private String title;

    public WebApiProcess() {
    }

    public WebApiProcess(String identifier, String title) {
        this.identifier = identifier;
        this.title = title;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTitle() {
        return title;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
