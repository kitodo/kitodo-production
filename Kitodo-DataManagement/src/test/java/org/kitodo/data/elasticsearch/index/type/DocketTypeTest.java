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

package org.kitodo.data.elasticsearch.index.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.elasticsearch.index.type.enums.DocketTypeField;

/**
 * Test class for DocketType.
 */
public class DocketTypeTest {

    private static List<Docket> prepareData() {

        List<Docket> dockets = new ArrayList<>();

        Docket firstDocket = new Docket();
        firstDocket.setId(1);
        firstDocket.setTitle("default");
        firstDocket.setFile("docket.xsl");
        dockets.add(firstDocket);

        Docket secondDocket = new Docket();
        secondDocket.setId(2);
        secondDocket.setTitle("custom");
        secondDocket.setFile("docket_custom.xsl");
        dockets.add(secondDocket);

        return dockets;
    }

    @Test
    public void shouldCreateDocument() throws Exception {
        DocketType docketType = new DocketType();
        Docket docket = prepareData().get(0);

        Map<String, Object> actual = docketType.createDocument(docket);

        assertEquals("Key title doesn't match to given value!", "default",
            DocketTypeField.TITLE.getStringValue(actual));
        assertEquals("Key file doesn't match to given value!", "docket.xsl",
            DocketTypeField.FILE.getStringValue(actual));
        assertTrue("Key file doesn't match to given value!", DocketTypeField.ACTIVE.getBooleanValue(actual));
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        DocketType docketType = new DocketType();
        Docket docket = prepareData().get(0);

        Map<String, Object> actual = docketType.createDocument(docket);

        assertEquals("Amount of keys is incorrect!", 5, actual.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        DocketType docketType = new DocketType();

        List<Docket> dockets = prepareData();
        Map<Integer, Map<String, Object>> documents = docketType.createDocuments(dockets);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
