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

package org.kitodo.production.metadata;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.mets.LinkedMetsResource;

public class MetadataEditorTest {

    @Test
    public void testDetermineIncludedStructuralElementPathToChildRecursive() throws Exception {
        IncludedStructuralElement includedStructuralElement = new IncludedStructuralElement();
        includedStructuralElement.setType("newspaperYear");

        IncludedStructuralElement monthIncludedStructuralElement = new IncludedStructuralElement();
        monthIncludedStructuralElement.setType("newspaperMonth");

        IncludedStructuralElement wrongDayIncludedStructuralElement = new IncludedStructuralElement();
        wrongDayIncludedStructuralElement.setType("newspaperDay");
        wrongDayIncludedStructuralElement.setLabel("wrong");
        LinkedMetsResource wrongLink = new LinkedMetsResource();
        wrongLink.setUri(URI.create("database://?process.id=13"));
        wrongDayIncludedStructuralElement.setLink(wrongLink);
        monthIncludedStructuralElement.getChildren().add(wrongDayIncludedStructuralElement);

        IncludedStructuralElement correctDayIncludedStructuralElement = new IncludedStructuralElement();
        correctDayIncludedStructuralElement.setType("newspaperDay");
        correctDayIncludedStructuralElement.setLabel("correct");
        LinkedMetsResource correctLink = new LinkedMetsResource();
        correctLink.setUri(URI.create("database://?process.id=42"));
        correctDayIncludedStructuralElement.setLink(correctLink);
        monthIncludedStructuralElement.getChildren().add(correctDayIncludedStructuralElement);

        includedStructuralElement.getChildren().add(monthIncludedStructuralElement);
        int number = 42;

        Method determineIncludedStructuralElementPathToChild = MetadataEditor.class.getDeclaredMethod(
            "determineIncludedStructuralElementPathToChild", IncludedStructuralElement.class, int.class);
        determineIncludedStructuralElementPathToChild.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<IncludedStructuralElement> result = (List<IncludedStructuralElement>) determineIncludedStructuralElementPathToChild
                .invoke(null, includedStructuralElement, number);

        Assert.assertEquals(new LinkedList<>(Arrays.asList(includedStructuralElement, monthIncludedStructuralElement,
            correctDayIncludedStructuralElement)), result);
    }
}
