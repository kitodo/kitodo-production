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

package org.kitodo.identifiergenerator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for PersistentIdentifier.
 */
public class DNBURNIdentifierGeneratorTest {

    @Test
    public void shouldGenerateUnifiedResourceName() throws Exception {
        DNBURNIdentifierGenerator dnburnIdentifierGenerator = new DNBURNIdentifierGenerator();
        String urn = dnburnIdentifierGenerator.generateUnifiedResourceName("bsz", "14", "db", "378704842");
        assertEquals("Incorrect check digit in URN ", "urn:nbn:de:bsz:14-db-id3787048428", urn);
    }
}
