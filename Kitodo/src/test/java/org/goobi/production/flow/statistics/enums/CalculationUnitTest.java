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

package org.goobi.production.flow.statistics.enums;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CalculationUnitTest {

    @Test
    public final void testGetId() {
        assertEquals("1", CalculationUnit.volumes.getId());
        assertEquals("2", CalculationUnit.pages.getId());
        assertEquals("3", CalculationUnit.volumesAndPages.getId());

    }

    // @Test
    // public final void testGetTitle() {
    // assertEquals("Volumes",CalculationUnit.volumes.getTitle());
    // assertEquals("Pages",CalculationUnit.pages.getTitle());
    // assertEquals("Volumes &
    // pages",CalculationUnit.volumesAndPages.getTitle());
    // }

    @Test
    public final void testGetById() {
        assertEquals(CalculationUnit.volumes, CalculationUnit.getById("1"));
        assertEquals(CalculationUnit.pages, CalculationUnit.getById("2"));
        assertEquals(CalculationUnit.volumesAndPages, CalculationUnit.getById("3"));

    }

}
