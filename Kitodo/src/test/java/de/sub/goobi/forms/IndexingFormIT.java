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

package de.sub.goobi.forms;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;

public class IndexingFormIT {

    private IndexingForm indexingForm = new IndexingForm();

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNodeWithoutMapping();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
    }

    @Test
    public void shouldCreateMapping() throws Exception {
        Assert.assertFalse(indexingForm.indexExists());
        indexingForm.createMapping(false);
        Assert.assertTrue(indexingForm.indexExists());
    }
}
