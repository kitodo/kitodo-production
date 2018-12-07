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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.elasticsearch.index.type.enums.ClientTypeField;

import static org.junit.Assert.assertEquals;

public class ClientTypeTest {

    private static List<Client> prepareData() {
        List<Client> clients = new ArrayList<>();

        Client firstClient = new Client();
        firstClient.setId(1);
        firstClient.setName("First client");

        Client secondClient = new Client();
        secondClient.setId(2);
        secondClient.setName("New client");

        clients.add(firstClient);
        clients.add(secondClient);

        return clients;
    }

    @Test
    public void shouldCreateFirstDocument() throws Exception {
        ClientType clientType = new ClientType();

        Client client = prepareData().get(0);
        HttpEntity document = clientType.createDocument(client);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key name doesn't match to given value!", "First client",
            ClientTypeField.NAME.getStringValue(actual));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        ClientType clientType = new ClientType();

        Client client = prepareData().get(1);
        HttpEntity document = clientType.createDocument(client);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key name doesn't match to given value!", "New client",
            ClientTypeField.NAME.getStringValue(actual));
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        ClientType clientType = new ClientType();

        Client client = prepareData().get(0);
        HttpEntity document = clientType.createDocument(client);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 1, actual.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        ClientType clientType = new ClientType();

        List<Client> clients = prepareData();
        Map<Integer, HttpEntity> documents = clientType.createDocuments(clients);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
