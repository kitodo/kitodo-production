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

package org.kitodo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyEncodingTest {

    private Properties messages;
    private static final Charset CORRECT_ENCODING = StandardCharsets.UTF_8;
    private static final Charset WRONG_ENCODING = StandardCharsets.ISO_8859_1;
    private static final String MESSAGES_DIR = "src/main/resources/messages/";
    private static final HashMap<String, String> GERMAN_TEST_MESSAGES = new HashMap<>();
    private static final HashMap<String, String> GERMAN_ERROR_MESSAGES = new HashMap<>();
    private static final HashMap<String, String> GERMAN_PASSWORD_MESSAGES = new HashMap<>();
    private static final HashMap<String, String> SPANISH_TEST_MESSAGES = new HashMap<>();
    private static final HashMap<String, String> SPANISH_ERROR_MESSAGES = new HashMap<>();
    private static final HashMap<String, String> SPANISH_PASSWORD_MESSAGES = new HashMap<>();
    private static final String GERMAN_MESSAGE_PROPERTIES = MESSAGES_DIR + "messages_de.properties";
    private static final String SPANISH_MESSAGE_PROPERTIES = MESSAGES_DIR + "messages_es.properties";
    private static final String GERMAN_ERROR_PROPERTIES = MESSAGES_DIR + "errors_de.properties";
    private static final String SPANISH_ERROR_PROPERTIES = MESSAGES_DIR + "errors_es.properties";
    private static final String GERMAN_PASSWORD_PROPERTIES = MESSAGES_DIR + "password_de.properties";
    private static final String SPANISH_PASSWORD_PROPERTIES = MESSAGES_DIR + "password_es.properties";

    @BeforeAll
    public static void prepare() {
        // prepare German test messages
        GERMAN_TEST_MESSAGES.put("viewPageInNewWindow", "Seite in neuem Browser-Fenster öffnen");
        GERMAN_TEST_MESSAGES.put("importConfig.searchFields.idPrefix", "ID-Präfix");
        GERMAN_TEST_MESSAGES.put("addMappingFile", "Abbildungsdatei hinzufügen");

        // prepare German error test messages
        GERMAN_ERROR_MESSAGES.put("errorAdding", "Fehler beim Hinzufügen von ''{0}''");
        GERMAN_ERROR_MESSAGES.put("batchPropertyEmpty", "Die Eigenschaft {0} im Vorgang {1} ist ein Pflichtfeld, enthält aber keinen Wert.");
        GERMAN_ERROR_MESSAGES.put("calendar.upload.isEmpty", "Es wurde keine Datei übertragen.");

        // prepare German password test messages
        GERMAN_PASSWORD_MESSAGES.put("HISTORY_VIOLATION", "Passwort entspricht einem von %1$s der vorherigen Passwörter.");
        GERMAN_PASSWORD_MESSAGES.put("ILLEGAL_USERNAME", "Passwort enthält die Benutzer-ID '%1$s'.");

        // prepare Spanish test messages
        SPANISH_TEST_MESSAGES.put("confirm", "Esta acción puede llevar algún tiempo. ¿Estás seguro de que deseas continuar?");
        SPANISH_TEST_MESSAGES.put("passwordChanged", "La contraseña se ha cambiado con éxito.");
        SPANISH_TEST_MESSAGES.put("projectNotAssignedToCurrentUser", "¡El proyecto \"{0}\" no está asignado al usuario actual!");

        // prepare Spanish error test messages
        SPANISH_ERROR_MESSAGES.put("errorAdding", "Error al añadir ''{0}''");
        SPANISH_ERROR_MESSAGES.put("projectTitleAlreadyInUse", "El título del proyecto ya está en uso.");
        SPANISH_ERROR_MESSAGES.put("workflowExceptionParallelGatewayNoTask", "¡Ninguna tarea sigue la ramificación paralela!");

        // prepare Spanish password test messages
        SPANISH_PASSWORD_MESSAGES.put("HISTORY_VIOLATION", "La contraseña corresponde a uno de los %1$s de las contraseñas anteriores.");
        SPANISH_PASSWORD_MESSAGES.put("ILLEGAL_WORD", "La contraseña contiene la palabra del diccionario '%1$s'.");
    }

    @Test
    public void checkCharacterEncodingOfGermanMessagesFile() throws Exception {
        // ensure messages are decoded as expected using UTF-8 encoding
        messages = readPropertiesWithEncoding(GERMAN_MESSAGE_PROPERTIES, CORRECT_ENCODING);
        for (Map.Entry<String, String> testMessage : GERMAN_TEST_MESSAGES.entrySet()) {
            assertEquals(testMessage.getValue(), messages.getProperty(testMessage.getKey()));
        }

        // ensure messages are _not_ decoded as expected using ISO_8859_1 encoding
        messages = readPropertiesWithEncoding(GERMAN_MESSAGE_PROPERTIES, WRONG_ENCODING);
        for (Map.Entry<String, String> testMessage : GERMAN_TEST_MESSAGES.entrySet()) {
            assertNotEquals(testMessage.getValue(), messages.getProperty(testMessage.getKey()));
        }
    }

    @Test
    public void checkCharacterEncodingsOfGermanErrorMessageFile() throws Exception {
        // ensure messages are decoded as expected using UTF-8 encoding
        messages = readPropertiesWithEncoding(GERMAN_ERROR_PROPERTIES, CORRECT_ENCODING);
        for (Map.Entry<String, String> testMessage : GERMAN_ERROR_MESSAGES.entrySet()) {
            assertEquals(testMessage.getValue(), messages.getProperty(testMessage.getKey()));
        }

        // ensure messages are _not_ decoded as expected using ISO_8859_1 encoding
        messages = readPropertiesWithEncoding(GERMAN_ERROR_PROPERTIES, WRONG_ENCODING);
        for (Map.Entry<String, String> testMessage : GERMAN_ERROR_MESSAGES.entrySet()) {
            assertNotEquals(testMessage.getValue(), messages.getProperty(testMessage.getKey()));
        }
    }

    @Test
    public void checkCharacterEncodingOfGermanPasswordMessageFile() throws Exception {
        // ensure messages are decoded as expected using UTF-8 encoding
        messages = readPropertiesWithEncoding(GERMAN_PASSWORD_PROPERTIES, CORRECT_ENCODING);
        for (Map.Entry<String, String> testMessage : GERMAN_PASSWORD_MESSAGES.entrySet()) {
            assertEquals(testMessage.getValue(), messages.getProperty(testMessage.getKey()));
        }

        // ensure messages are _not_ decoded as expected using ISO_8895_1 encoding
        messages = readPropertiesWithEncoding(GERMAN_PASSWORD_PROPERTIES, WRONG_ENCODING);
        for (Map.Entry<String, String> testMessage : GERMAN_PASSWORD_MESSAGES.entrySet()) {
            assertNotEquals(testMessage.getValue(), messages.getProperty(testMessage.getKey()));
        }
    }

    @Test
    public void checkCharacterEncodingsOfSpanishMessagesFile() throws Exception {
        // ensure messages are decoded as expected using UTF-8 encoding
        messages = readPropertiesWithEncoding(SPANISH_MESSAGE_PROPERTIES, CORRECT_ENCODING);
        for (Map.Entry<String, String> testMessage : SPANISH_TEST_MESSAGES.entrySet()) {
            assertEquals(testMessage.getValue(), messages.getProperty(testMessage.getKey()));
        }

        // ensure messages are _not_ decoded as expected using ISO_8859_1 encoding
        messages = readPropertiesWithEncoding(SPANISH_MESSAGE_PROPERTIES, WRONG_ENCODING);
        for (Map.Entry<String, String> testMessage : SPANISH_TEST_MESSAGES.entrySet()) {
            assertNotEquals(testMessage.getValue(), messages.getProperty(testMessage.getKey()));
        }
    }

    @Test
    public void checkCharacterEncodingsOfSpanishErrorMessagesFile() throws Exception {
        // ensure messages are decoded as expected using UTF-8 encoding
        messages = readPropertiesWithEncoding(SPANISH_ERROR_PROPERTIES, CORRECT_ENCODING);
        for (Map.Entry<String, String> testMessage : SPANISH_ERROR_MESSAGES.entrySet()) {
            assertEquals(testMessage.getValue(), messages.getProperty(testMessage.getKey()));
        }

        // ensure messages are _not_ decoded as expected using ISO_8859_1 encoding
        messages = readPropertiesWithEncoding(SPANISH_ERROR_PROPERTIES, WRONG_ENCODING);
        for (Map.Entry<String, String> testMessage : SPANISH_ERROR_MESSAGES.entrySet()) {
            assertNotEquals(testMessage.getValue(), messages.getProperty(testMessage.getKey()));
        }
    }

    @Test
    public void checkCharacterEncodingOfSpanishPasswordMessageFile() throws Exception {
        // ensure messages are decoded as expected using UTF-8 encoding
        messages = readPropertiesWithEncoding(SPANISH_PASSWORD_PROPERTIES, CORRECT_ENCODING);
        for (Map.Entry<String, String> testMessage : SPANISH_PASSWORD_MESSAGES.entrySet()) {
            assertEquals(testMessage.getValue(), messages.getProperty(testMessage.getKey()));
        }

        // ensure messages are _not_ decoded as expected using ISO_8859_1 encoding
        messages = readPropertiesWithEncoding(SPANISH_PASSWORD_PROPERTIES, WRONG_ENCODING);
        for (Map.Entry<String, String> testMessage : SPANISH_PASSWORD_MESSAGES.entrySet()) {
            assertNotEquals(testMessage.getValue(), messages.getProperty(testMessage.getKey()));
        }
    }

    private Properties readPropertiesWithEncoding(String pathString, Charset encoding)
            throws IOException {
        Path path = Paths.get(pathString);
        assertTrue(Files.exists(path));
        Properties properties = new Properties();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile()),
                encoding))) {
            properties.load(reader);
            return properties;
        }
    }
}
