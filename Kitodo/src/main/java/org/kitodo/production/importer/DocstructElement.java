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

package org.kitodo.production.importer;

public class DocstructElement {
    private String docStruct;
    private int order;

    public DocstructElement(String docStruct, int order) {
        this.docStruct = docStruct;
        this.order = order;
    }

    public String getDocStruct() {
        return docStruct;
    }

    public void setDocStruct(String docStruct) {
        this.docStruct = docStruct;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
