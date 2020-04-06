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
import org.w3c.dom.NodeList;

class ModsResponseHandler extends XmlResponseHandler {

    private static final String MODS_NAMESPACE = "http://www.loc.gov/mods/v3";
    private static final String MODS_TAG = "mods";
    private static final String MODS_RECORD_ID_TAG = "recordIdentifier";
    private static final String MODS_RECORD_TITLE_TAG = "title";

    @Override
    String getRecordID(Element record) {
        Element recordIdentifier = getXmlElement(record, MODS_RECORD_ID_TAG);
        return recordIdentifier.getTextContent().trim();
    }

    @Override
    String getRecordTitle(Element record) {
        Element modsElement = getXmlElement(record, MODS_TAG);
        Element recordTitle = getXmlElement(modsElement, MODS_RECORD_TITLE_TAG);
        return recordTitle.getTextContent().trim();
    }

    private static Element getXmlElement(Element parentNode, String elementTag) {
        NodeList nodeList = parentNode.getElementsByTagNameNS(ModsResponseHandler.MODS_NAMESPACE, elementTag);
        return (Element) nodeList.item(0);
    }
}
