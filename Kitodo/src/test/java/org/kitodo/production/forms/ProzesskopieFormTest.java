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

package org.kitodo.production.forms;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Created for test switch statement.
 */
public class ProzesskopieFormTest {

    private static Map<String, Map<String, String>> testData;

    static {
        testData = new HashMap<>();

        testData.put("LuthSpoc", new HashMap<String, String>() {
            {
                put("Luther, Martin", "Spocžatki kscheszijanskeje wucžby aby D. M. Luthera mały Katechismuß");
            }
        });
        testData.put("BrunITr", new HashMap<String, String>() {
            {
                put("Bruno, Vincenzo", "I Tre Dialoghi Del Dottor Fisico Vincenzo Bruno Di Melfi");
            }
        });
        testData.put("PallAhi", new HashMap<String, String>() {
            {
                put("Palliser, Fanny", "A history of lace");
            }
        });
        testData.put("HaywDer", new HashMap<String, String>() {
            {
                put("Haywood, Eliza Fowler", "Der unsichtbare Kundschafter");
            }
        });
        testData.put("Schrdeund", new HashMap<String, String>() {
            {
                put("", "Schriften des unter dem Hohen Protektorate S. K. Hoheit des Prinzen Georg stehenden Sächsischen Fischereivereines");
            }
        });
        testData.put("DieacKid", new HashMap<String, String>() {
            {
                put("", "Die achtzig Kirchenlieder der Schulregulative");
            }
        });
        testData.put("Luth", new HashMap<String, String>() {
            {
                put("Luther, Martin", "");
            }
        });
        testData.put("", new HashMap<String, String>() {
            {
                put("", "");
            }
        });
    }

    @Test
    public void shouldCreateAtstsl() {
        for (String givenHash : testData.keySet()) {
            for (Map.Entry<String, String> entry : testData.get(givenHash).entrySet()) {
                String created = ProzesskopieForm.createAtstsl(entry.getValue(), entry.getKey());
                assertEquals("Created hash doesn't match the precomputed one!", givenHash, created);
            }
        }
    }
}
