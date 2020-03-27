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

class PicaResponseHandler extends XmlResponseHandler {

    private static final String PICA_TITLE_XPATH
            = ".//*[local-name()='datafield'][@tag='021A']/*[local-name()='subfield'][@code='a']/text()";
    private static final String PICA_ID_XPATH
            = ".//*[local-name()='datafield'][@tag='003@']/*[local-name()='subfield'][@code='0']/text()";

    @Override
    String getRecordTitle(Element record) {
        return getTextContent(record, PICA_TITLE_XPATH);
    }

    @Override
    String getRecordID(Element record) {
        return getTextContent(record, PICA_ID_XPATH);
    }
}
