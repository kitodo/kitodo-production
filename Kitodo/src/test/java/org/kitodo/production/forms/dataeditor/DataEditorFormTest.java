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

package org.kitodo.production.forms.dataeditor;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DataEditorFormTest {

    /**
     * Test consecutive page selection in data editor form.
     */
    @Test
    public void shouldCheckIfConsecutivePagesAreSelected() {
        DataEditorForm dataEditorForm = new DataEditorForm();
        dataEditorForm.setSelectedMedia(prepareSelectedMedia(true, false));
        Assert.assertTrue("Should return true when all pages are selected",
                dataEditorForm.consecutivePagesSelected());
        dataEditorForm.setSelectedMedia(prepareSelectedMedia(false, false));
        Assert.assertFalse("Should return false when only second of three pages is not selected",
                dataEditorForm.consecutivePagesSelected());
        dataEditorForm.setSelectedMedia(prepareSelectedMedia(true, true));
        Assert.assertFalse("Should return false when pages are selected spanning multiple chapters",
                dataEditorForm.consecutivePagesSelected());
    }

    /**
     * Test media selection in data editor form.
     */
    @Test
    public void shouldCheckIfMediaIsSelected() {
        DataEditorForm dataEditorForm = new DataEditorForm();
        LogicalDivision chapter = new LogicalDivision();
        List<Pair<PhysicalDivision, LogicalDivision>> selectedMedia = new LinkedList<>();
        PhysicalDivision firstPage = createPhysicalDivision(1, chapter);
        PhysicalDivision secondPage = createPhysicalDivision(2, null);
        selectedMedia.add(new ImmutablePair<>(firstPage, chapter));
        selectedMedia.add(new ImmutablePair<>(firstPage, chapter));
        dataEditorForm.setSelectedMedia(selectedMedia);
        Assert.assertTrue("First page in chapter should be recognized as selected",
                dataEditorForm.isSelected(firstPage, chapter));
        Assert.assertFalse("Second page in chapter should be recognized as not selected",
                dataEditorForm.isSelected(secondPage, chapter));
    }

    private List<Pair<PhysicalDivision, LogicalDivision>> prepareSelectedMedia(boolean selectMiddlePage,
                                                                               boolean multipleChapters) {
        List<Pair<PhysicalDivision, LogicalDivision>> selectedMedia = new LinkedList<>();
        LogicalDivision firstChapter = new LogicalDivision();

        PhysicalDivision firstPage = createPhysicalDivision(1, firstChapter);
        selectedMedia.add(new ImmutablePair<>(firstPage, firstChapter));

        PhysicalDivision secondPage = createPhysicalDivision(2, firstChapter);
        if (selectMiddlePage) {
            selectedMedia.add(new ImmutablePair<>(secondPage, firstChapter));
        }

        LogicalDivision thirdPageDivision = firstChapter;
        if (multipleChapters) {
            thirdPageDivision = new LogicalDivision();
        }
        PhysicalDivision thirdPage = createPhysicalDivision(3, thirdPageDivision);
        selectedMedia.add(new ImmutablePair<>(thirdPage, thirdPageDivision));
        return selectedMedia;
    }

    private PhysicalDivision createPhysicalDivision(int order, LogicalDivision logicalDivision) {
        PhysicalDivision physicalDivision = new PhysicalDivision();
        physicalDivision.setOrder(order);
        View view = new View();
        view.setPhysicalDivision(physicalDivision);
        if (Objects.nonNull(logicalDivision)) {
            logicalDivision.getViews().add(view);
        }
        return physicalDivision;
    }

}
