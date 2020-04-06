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

package org.kitodo.queryurlimport;

import org.w3c.dom.Element;

class MarcResponseHandler extends XmlResponseHandler {

    private static final String MARC_TITLE_XPATH
            = ".//*[local-name()='datafield'][@tag='245']/*[local-name()='subfield'][@code='a']/text()";
    private static final String MARC_ID_XPATH = ".//*[local-name()='controlfield'][@tag='001']/text()";

    @Override
    String getRecordTitle(Element record) {
        return getTextContent(record, MARC_TITLE_XPATH);
    }

    @Override
    String getRecordID(Element record) {
        return getTextContent(record, MARC_ID_XPATH);
    }
}
