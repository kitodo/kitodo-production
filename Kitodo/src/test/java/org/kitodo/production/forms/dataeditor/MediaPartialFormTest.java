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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartial;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.production.helper.metadata.MediaPartialHelper;
import org.kitodo.production.metadata.MetadataEditor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;

public class MediaPartialFormTest {

    public static final String EXISTING_MEDIA_PARTIAL_BEGIN = "00:00:30.000";
    DataEditorForm dataEditorForm;
    MediaPartialsPanel mediaPartialsPanel;
    MediaPartialForm mediaPartialForm;
    LogicalDivision logicalDivision;
    PhysicalDivision physicalDivision;
    PhysicalDivision physicalStructure;


    /**
     * Initialize test class.
     */
    @BeforeAll
    public static void initTestClass() {
        // mock frontend update calls
        Mockito.mockStatic(Ajax.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class);
        MockedStatic<PrimeFaces> primefacesSingleton = Mockito.mockStatic(PrimeFaces.class);
        primefacesSingleton.when(PrimeFaces::current).thenReturn(primeFaces);
    }

    /**
     * Initialize test function.
     */
    @BeforeEach
    public void initTest() {
        dataEditorForm = mock(DataEditorForm.class);
        Workpiece workpiece = mock(Workpiece.class);
        physicalStructure = new PhysicalDivision();
        when(workpiece.getPhysicalStructure()).thenReturn(physicalStructure);
        when(dataEditorForm.getWorkpiece()).thenReturn(workpiece);

        GalleryPanel galleryPanel = mock(GalleryPanel.class);
        mediaPartialsPanel = spy(new MediaPartialsPanel(dataEditorForm));
        when(dataEditorForm.getGalleryPanel()).thenReturn(galleryPanel);
        when(galleryPanel.getMediaPartialsPanel()).thenReturn(mediaPartialsPanel);

        View view = mock(View.class);
        PhysicalDivision childPhysicalDivision = spy(PhysicalDivision.class);
        childPhysicalDivision.setMediaPartial(new MediaPartial(EXISTING_MEDIA_PARTIAL_BEGIN));
        when(view.getPhysicalDivision()).thenReturn(childPhysicalDivision);

        LogicalDivision childLogicalDivision = spy(LogicalDivision.class);
        childLogicalDivision.getViews().add(view);
        logicalDivision = new LogicalDivision();
        logicalDivision.getChildren().add(childLogicalDivision);

        physicalDivision = new PhysicalDivision();

        mediaPartialForm = spy(new MediaPartialForm(dataEditorForm));
    }

    /**
     * Test save function.
     */
    @Test
    public void testSave() {
        assertEquals(1, logicalDivision.getChildren().size());

        when(mediaPartialForm.getMediaSelection()).thenReturn(new ImmutablePair<>(physicalDivision, logicalDivision));

        // overwrite to test save function
        LinkedList<PhysicalDivision> ancestorsOfPhysicalDivision = new LinkedList<>();
        ancestorsOfPhysicalDivision.add(new PhysicalDivision());
        try (MockedStatic<MetadataEditor> metadataEditorMockedStatic = Mockito.mockStatic(MetadataEditor.class)) {
            metadataEditorMockedStatic.when(() -> MetadataEditor.getAncestorsOfPhysicalDivision(physicalDivision, physicalStructure))
                        .thenReturn(ancestorsOfPhysicalDivision);
        }

        // add media partial

        when(mediaPartialsPanel.getMediaDuration()).thenReturn("00:01:00.000");
        when(mediaPartialForm.getTitle()).thenReturn("Lorem");
        when(mediaPartialForm.getBegin()).thenReturn("00:00:45.000");

        mediaPartialForm.save();

        assertEquals(2, logicalDivision.getChildren().size());
        assertEquals("Lorem", logicalDivision.getChildren().get(1).getLabel());
        LogicalDivision mediaPartialLogicalDivision = logicalDivision.getChildren().get(1);
        MediaPartial mediaPartial = mediaPartialLogicalDivision.getViews().get(0).getPhysicalDivision()
                .getMediaPartial();
        assertEquals("00:00:45.000", mediaPartial.getBegin());
        assertEquals("00:00:15.000", mediaPartial.getExtent());

        // edit media partial
        when(mediaPartialForm.getTitle()).thenReturn("Lorem ipsum");
        when(mediaPartialForm.getBegin()).thenReturn("00:00:10.000");
        mediaPartialForm.setMediaPartialDivision(
                new AbstractMap.SimpleEntry<>(mediaPartialLogicalDivision, mediaPartial));

        mediaPartialForm.save();

        assertEquals(2, logicalDivision.getChildren().size());
        // 'media partial' is now designated as the first child in the sorting order.
        assertEquals("Lorem ipsum", logicalDivision.getChildren().get(0).getLabel());
        mediaPartialLogicalDivision = logicalDivision.getChildren().get(0);
        mediaPartial = mediaPartialLogicalDivision.getViews().get(0).getPhysicalDivision()
                .getMediaPartial();
        assertEquals("00:00:10.000", mediaPartial.getBegin());
        assertEquals("00:00:20.000",
                mediaPartial.getExtent()); // changed calculation of duration to the begin of next media partial
    }

    /**
     * Test valid function.
     */
    @Test
    public void testValidation() throws NoSuchFieldException, IllegalAccessException {
        assertFalse(mediaPartialForm.valid());
        assertEquals("mediaPartialFormNoMedium", getValidationError(mediaPartialForm));

        when(mediaPartialForm.getMediaSelection()).thenReturn(new ImmutablePair<>(physicalDivision, logicalDivision));
        assertFalse(mediaPartialForm.valid());
        assertEquals("mediaPartialFormMediaDurationEmpty", getValidationError(mediaPartialForm));

        when(mediaPartialsPanel.getMediaDuration()).thenReturn("12345");
        assertFalse(mediaPartialForm.valid());
        assertEquals("mediaPartialFormMediaDurationWrongTimeFormat", getValidationError(mediaPartialForm));

        when(mediaPartialsPanel.getMediaDuration()).thenReturn("00:01:00.000");
        assertFalse(mediaPartialForm.valid());
        assertEquals("mediaPartialFormStartEmpty", getValidationError(mediaPartialForm));

        when(mediaPartialForm.getBegin()).thenReturn("12345");
        assertFalse(mediaPartialForm.valid());
        assertEquals("mediaPartialFormStartWrongTimeFormat", getValidationError(mediaPartialForm));

        when(mediaPartialForm.getBegin()).thenReturn("00:02:00.000");
        assertFalse(mediaPartialForm.valid());
        assertEquals("mediaPartialFormStartLessThanMediaDuration", getValidationError(mediaPartialForm));

        when(mediaPartialForm.getBegin()).thenReturn(EXISTING_MEDIA_PARTIAL_BEGIN);
        assertFalse(mediaPartialForm.valid());
        assertEquals("mediaPartialFormStartExists", getValidationError(mediaPartialForm));

        when(mediaPartialForm.getBegin()).thenReturn("00:00:45.000");
        assertTrue(mediaPartialForm.valid());
    }

    /**
     * Test time to formatted time conversion.
     */
    @Test
    public void testTimeToFormattedTimeConversion() {
        assertEquals("00:00:01.000", MediaPartialHelper.convertTimeToFormattedTime("1"));
        assertEquals("00:01:01.000", MediaPartialHelper.convertTimeToFormattedTime("1:1"));
        assertEquals("01:01:01.000", MediaPartialHelper.convertTimeToFormattedTime("1:1:1"));
        assertEquals("00:00:01.100", MediaPartialHelper.convertTimeToFormattedTime("1.1"));
        assertEquals("00:01:01.110", MediaPartialHelper.convertTimeToFormattedTime("1:1.11"));
        assertEquals("01:01:01.111", MediaPartialHelper.convertTimeToFormattedTime("1:1:1.111"));
    }

    private static String getValidationError(MediaPartialForm mediaPartialForm)
            throws NoSuchFieldException, IllegalAccessException {
        Field validationErrorField = mediaPartialForm.getClass().getDeclaredField("validationError");
        validationErrorField.setAccessible(true);
        return (String) validationErrorField.get(mediaPartialForm);
    }
}
