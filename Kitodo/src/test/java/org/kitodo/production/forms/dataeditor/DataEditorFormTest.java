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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;

public class DataEditorFormTest {

    private final static String TITLE_METADATA = "Test-Titel";
    private final static String TITLE_METADATA_KEY = "maintitle";

    /**
     * Test consecutive page selection in data editor form.
     */
    @Test
    public void shouldCheckIfConsecutivePagesAreSelected() {
        DataEditorForm dataEditorForm = new DataEditorForm();
        dataEditorForm.setSelectedMedia(prepareSelectedMedia(true, false));
        assertTrue(dataEditorForm.consecutivePagesSelected(),
                "Should return true when all pages are selected");
        dataEditorForm.setSelectedMedia(prepareSelectedMedia(false, false));
        assertFalse(dataEditorForm.consecutivePagesSelected(),
                "Should return false when only second of three pages is not selected");
        dataEditorForm.setSelectedMedia(prepareSelectedMedia(true, true));
        assertFalse(dataEditorForm.consecutivePagesSelected(),
                "Should return false when pages are selected spanning multiple chapters");
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
        assertTrue(dataEditorForm.isSelected(firstPage, chapter),
                "First page in chapter should be recognized as selected");
        assertFalse(dataEditorForm.isSelected(secondPage, chapter),
                "Second page in chapter should be recognized as not selected");
    }

    /**
     * Test retrieving structure element title.
     */
    @Test
    public void shouldGetStructuralElementTitle() {
        // define which metadata keys to use as title
        Collection<String> metadataKeys = Arrays.asList(TITLE_METADATA_KEY);
        // prepare test structure
        LogicalDivision structure = new LogicalDivision();
        MetadataEntry titleMetadata = new MetadataEntry();
        titleMetadata.setKey(TITLE_METADATA_KEY);
        titleMetadata.setValue(TITLE_METADATA);
        structure.getMetadata().add(titleMetadata);
        assertEquals(TITLE_METADATA, DataEditorForm.getStructureElementTitle(structure, metadataKeys),
                "Wrong title metadata value retrieved from structure element");
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
