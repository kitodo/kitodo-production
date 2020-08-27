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
        IncludedStructuralElement parent = new IncludedStructuralElement();
        parent.setLabel("Parent");

        IncludedStructuralElement childOne = new IncludedStructuralElement();
        childOne.setLabel("Child 1");
        parent.getChildren().add(childOne);

        IncludedStructuralElement childOneOne = new IncludedStructuralElement();
        childOneOne.setLabel("Child 1.1");
        childOne.getChildren().add(childOneOne);

        IncludedStructuralElement childOneTwo = new IncludedStructuralElement();
        childOneTwo.setLabel("Child 1.2");
        childOne.getChildren().add(childOneTwo);

        IncludedStructuralElement childTwo = new IncludedStructuralElement();
        childTwo.setLabel("Child 2");
        parent.getChildren().add(childTwo);

        IncludedStructuralElement childTwoOne = new IncludedStructuralElement();
        childTwoOne.setLabel("Child 2.1");
        childTwo.getChildren().add(childTwoOne);

        IncludedStructuralElement childTwoTwo = new IncludedStructuralElement();
        childTwoTwo.setLabel("Child 2.2");
        childTwo.getChildren().add(childTwoTwo);

        List<String> allChildren = parent.getAllChildren().stream().map(IncludedStructuralElement::getLabel)
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
