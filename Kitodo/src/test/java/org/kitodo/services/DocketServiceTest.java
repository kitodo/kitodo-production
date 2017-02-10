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

package org.kitodo.services;

import java.util.List;

import org.junit.Test;

import org.kitodo.data.database.beans.Docket;

import static org.junit.Assert.*;

/**
 * Tests for DocketService class.
 */
public class DocketServiceTest {

    @Test
    public void shouldFindDocket() throws Exception {
        DocketService docketService = new DocketService();

        Docket docket = docketService.find(1);
        boolean condition = docket.getName().equals("default") && docket.getFile().equals("docket.xsl");
        assertTrue("Docket was not found in database!", condition);
    }

    @Test
    public void shouldFindAllDockets() throws Exception {
        DocketService docketService = new DocketService();

        List<Docket> dockets = docketService.findAll();
        assertEquals("Not all dockets were found in database!", 2, dockets.size());
    }
}
