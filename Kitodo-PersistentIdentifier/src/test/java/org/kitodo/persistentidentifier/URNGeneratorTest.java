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

package org.kitodo.persistentidentifier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for PersistentIdentifier.
 */
public class URNGeneratorTest {

    @Test
    public void shouldGenerateUnifiedResourceName() {
        URNGenerator urnPersistentIdentifier = new URNGenerator();

        String urn = urnPersistentIdentifier.generate("bsz", "14", "db", "id378704842");
        assertEquals("Incorrect check digit in URN ", "urn:nbn:de:bsz:14-db-id3787048428", urn);

        urn = urnPersistentIdentifier.generate("bsz", "14", "db", "id470035625");
        assertEquals("Incorrect check digit in URN ", "urn:nbn:de:bsz:14-db-id4700356257", urn);

        urn = urnPersistentIdentifier.generate("boc", "12", "db", "478704844");
        assertEquals("Incorrect check digit in URN ", "urn:nbn:de:boc:12-db-4787048440", urn);

        urn = urnPersistentIdentifier.generate("bsz", "14", "qucosa", "22579");
        assertEquals("Incorrect check digit in URN ", "urn:nbn:de:bsz:14-qucosa-225799", urn);

        urn = urnPersistentIdentifier.generate("bsz", "d120", "qucosa", "22440");
        assertEquals("Incorrect check digit in URN ", "urn:nbn:de:bsz:d120-qucosa-224404", urn);

        urn = urnPersistentIdentifier.generate("bsz", "ch1", "qucosa", "22583");
        assertEquals("Incorrect check digit in URN ", "urn:nbn:de:bsz:ch1-qucosa-225834", urn);

    }
}
