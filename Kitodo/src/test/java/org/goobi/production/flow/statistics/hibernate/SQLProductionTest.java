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

package org.goobi.production.flow.statistics.hibernate;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.junit.BeforeClass;
import org.junit.Test;

public class SQLProductionTest {

    static SQLProduction sqlProd;

    /**
     * Performs computationally expensive setup shared several tests. This
     * compromises the independence of the tests, bit is a necessary
     * optimization here.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @BeforeClass
    public static void setUp() {
        ArrayList<Integer> testList = new ArrayList<>();
        testList.add(15);
        testList.add(45);
        testList.add(25);
        sqlProd = new SQLProduction(new Date(64871232), new Date(66871232), TimeUnit.days, testList);
    }

    @Test
    public void testGetSQL() {
        String testStr = "SELECT count(table_1.singleProcess) AS volumes, sum(table_1.pages) AS pages, "
                + "table_1.intervall FROM (SELECT process.id AS singleProcess, process.sortHelperImages AS pages, ";
        assertTrue(sqlProd.getSQL().contains(testStr));

    }

    @Test
    public void testGetSQLInteger() {
        String testStr = "SELECT count(table_1.singleProcess) AS volumes, sum(table_1.pages) AS pages, "
                + "table_1.intervall FROM (SELECT process.id AS singleProcess, process.sortHelperImages AS pages, ";
        assertTrue(sqlProd.getSQL(4).contains(testStr));
    }

}
