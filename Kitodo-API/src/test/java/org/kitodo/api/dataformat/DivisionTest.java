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

package org.kitodo.api.dataformat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class DivisionTest {

    /**
     * Tests the method {@code Division.getAllChildren()}.
     */
    @Test
    public void getAllChildrenTest() {
        LogicalDivision parent = new LogicalDivision();
        parent.setLabel("Parent");

        LogicalDivision childOne = new LogicalDivision();
        childOne.setLabel("Child 1");
        parent.getChildren().add(childOne);

        LogicalDivision childOneOne = new LogicalDivision();
        childOneOne.setLabel("Child 1.1");
        childOne.getChildren().add(childOneOne);

        LogicalDivision childOneTwo = new LogicalDivision();
        childOneTwo.setLabel("Child 1.2");
        childOne.getChildren().add(childOneTwo);

        LogicalDivision childTwo = new LogicalDivision();
        childTwo.setLabel("Child 2");
        parent.getChildren().add(childTwo);

        LogicalDivision childTwoOne = new LogicalDivision();
        childTwoOne.setLabel("Child 2.1");
        childTwo.getChildren().add(childTwoOne);

        LogicalDivision childTwoTwo = new LogicalDivision();
        childTwoTwo.setLabel("Child 2.2");
        childTwo.getChildren().add(childTwoTwo);

        List<String> allChildren = parent.getAllChildren().stream().map(LogicalDivision::getLabel)
                .collect(Collectors.toList());

        assertFalse(allChildren.contains("Parent"));
        assertTrue(allChildren.contains("Child 1"));
        assertTrue(allChildren.contains("Child 1.1"));
        assertTrue(allChildren.contains("Child 1.2"));
        assertTrue(allChildren.contains("Child 2"));
        assertTrue(allChildren.contains("Child 2.1"));
        assertTrue(allChildren.contains("Child 2.1"));
    }
}
