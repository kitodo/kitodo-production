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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Workpiece;

/**
 * Test class for WorkpieceType.
 */
public class WorkpieceTypeTest {

    private static List<Workpiece> prepareData() {

        List<Workpiece> workpieces = new ArrayList<>();
        List<Property> properties = new ArrayList<>();

        Process firstProcess = new Process();
        firstProcess.setId(1);

        Process secondProcess = new Process();
        secondProcess.setId(2);

        Property firstProperty = new Property();
        firstProperty.setId(1);
        properties.add(firstProperty);

        Property secondProperty = new Property();
        secondProperty.setId(2);
        properties.add(secondProperty);

        Workpiece firstWorkpiece = new Workpiece();
        firstWorkpiece.setId(1);
        firstWorkpiece.setProcess(firstProcess);
        firstWorkpiece.setProperties(properties);
        workpieces.add(firstWorkpiece);

        Workpiece secondWorkpiece = new Workpiece();
        secondWorkpiece.setId(2);
        secondWorkpiece.setProcess(secondProcess);
        workpieces.add(secondWorkpiece);

        return workpieces;
    }

    @Test
    public void shouldCreateDocument() throws Exception {
        WorkpieceType workpieceType = new WorkpieceType();
        JSONParser parser = new JSONParser();

        Workpiece workpiece = prepareData().get(0);
        HttpEntity document = workpieceType.createDocument(workpiece);
        JSONObject actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        JSONObject expected = (JSONObject) parser.parse("{\"process\":1,\"properties\":[{\"id\":1},{\"id\":2}]}");
        assertEquals("Workpiece value for process key doesn't match to given plain text!", expected, actual);

        workpiece = prepareData().get(1);
        document = workpieceType.createDocument(workpiece);
        actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        expected = (JSONObject) parser.parse("{\"process\":2,\"properties\":[]}");
        assertEquals("Workpiece value for process key doesn't match to given plain text!", expected, actual);
    }

    @Test
    public void shouldCreateDocuments() throws Exception {
        WorkpieceType workpieceType = new WorkpieceType();

        List<Workpiece> workpieces = prepareData();
        HashMap<Integer, HttpEntity> documents = workpieceType.createDocuments(workpieces);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
