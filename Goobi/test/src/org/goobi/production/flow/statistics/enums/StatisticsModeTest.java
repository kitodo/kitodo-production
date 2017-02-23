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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StatisticsModeTest {

    @Test
    public final void testGetIsSimple() {
        assertTrue(StatisticsMode.SIMPLE_RUNTIME_STEPS.getIsSimple());
        assertFalse(StatisticsMode.PRODUCTION.getIsSimple());
    }

//  @Test
//  public final void testGetTitle() {
//      assertEquals("Duration of the steps", StatisticsMode.SIMPLE_RUNTIME_STEPS.getTitle());
//      assertEquals("Production throughput", StatisticsMode.THROUGHPUT.getTitle());
//      assertEquals("Error tracking", StatisticsMode.CORRECTIONS.getTitle());
//      assertEquals("Storage calculator", StatisticsMode.STORAGE.getTitle());
//      assertEquals("Production statistics", StatisticsMode.PRODUCTION.getTitle());
//      assertEquals("Project association", StatisticsMode.PROJECTS.getTitle());
//  }

    @Test
    public final void testGetStatisticalQuestion() {
        assertNotNull(StatisticsMode.STORAGE.getStatisticalQuestion());
    }

    @Test
    public final void testGetByClassName() {
        assertNotNull(StatisticsMode.PROJECTS.getStatisticalQuestion().getClass().getName());

    }

}
