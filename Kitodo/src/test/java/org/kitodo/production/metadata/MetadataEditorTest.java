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
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.mets.LinkedMetsResource;

public class MetadataEditorTest {

    @Test
    public void testDetermineLogicalDivisionPathToChildRecursive() throws Exception {
        LogicalDivision logicalDivision = new LogicalDivision();
        logicalDivision.setType("newspaperYear");

        LogicalDivision monthLogicalDivision = new LogicalDivision();
        monthLogicalDivision.setType("newspaperMonth");

        LogicalDivision wrongDayLogicalDivision = new LogicalDivision();
        wrongDayLogicalDivision.setType("newspaperDay");
        wrongDayLogicalDivision.setLabel("wrong");
        LinkedMetsResource wrongLink = new LinkedMetsResource();
        wrongLink.setUri(URI.create("database://?process.id=13"));
        wrongDayLogicalDivision.setLink(wrongLink);
        monthLogicalDivision.getChildren().add(wrongDayLogicalDivision);

        LogicalDivision correctDayLogicalDivision = new LogicalDivision();
        correctDayLogicalDivision.setType("newspaperDay");
        correctDayLogicalDivision.setLabel("correct");
        LinkedMetsResource correctLink = new LinkedMetsResource();
        correctLink.setUri(URI.create("database://?process.id=42"));
        correctDayLogicalDivision.setLink(correctLink);
        monthLogicalDivision.getChildren().add(correctDayLogicalDivision);

        logicalDivision.getChildren().add(monthLogicalDivision);
        int number = 42;

        Method determineLogicalDivisionPathToChild = MetadataEditor.class.getDeclaredMethod(
            "determineLogicalDivisionPathToChild", LogicalDivision.class, int.class);
        determineLogicalDivisionPathToChild.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<LogicalDivision> result = (List<LogicalDivision>) determineLogicalDivisionPathToChild
                .invoke(null, logicalDivision, number);

        Assert.assertEquals(new LinkedList<>(Arrays.asList(logicalDivision, monthLogicalDivision,
            correctDayLogicalDivision)), result);
    }
}
