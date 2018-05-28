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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
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

        HttpEntity document = docketType.createDocument(docket);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "default",
            actual.getString(DocketTypeField.TITLE.getName()));
        assertEquals("Key file doesn't match to given value!", "docket.xsl",
            actual.getString(DocketTypeField.FILE.getName()));
        assertTrue("Key file doesn't match to given value!", actual.getBoolean(DocketTypeField.ACTIVE.getName()));
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        DocketType docketType = new DocketType();
        Docket docket = prepareData().get(0);

        HttpEntity document = docketType.createDocument(docket);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 3, actual.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        DocketType docketType = new DocketType();

        List<Docket> dockets = prepareData();
        Map<Integer, HttpEntity> documents = docketType.createDocuments(dockets);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
