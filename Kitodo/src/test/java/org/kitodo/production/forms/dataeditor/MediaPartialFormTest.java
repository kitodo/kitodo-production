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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartialView;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;

public class MediaPartialFormTest {

    public static final String EXISTING_MEDIA_PARTIAL_BEGIN = "00:00:30";
    DataEditorForm dataEditorForm;
    MediaPartialsPanel mediaPartialsPanel;
    MediaPartialForm mediaPartialForm;
    LogicalDivision logicalDivision;
    PhysicalDivision physicalDivision;

    /**
     * Initialize test class.
     */
    @BeforeClass
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
    @Before
    public void initTest() {
        dataEditorForm = mock(DataEditorForm.class);
        Workpiece workpiece = mock(Workpiece.class);
        when(workpiece.getPhysicalStructure()).thenReturn(new PhysicalDivision());
        when(dataEditorForm.getWorkpiece()).thenReturn(workpiece);

        GalleryPanel galleryPanel = mock(GalleryPanel.class);
        mediaPartialsPanel = spy(new MediaPartialsPanel(dataEditorForm));
        when(dataEditorForm.getGalleryPanel()).thenReturn(galleryPanel);
        when(galleryPanel.getMediaPartialsPanel()).thenReturn(mediaPartialsPanel);

        View view = mock(View.class);
        PhysicalDivision childPhysicalDivision = spy(PhysicalDivision.class);
        childPhysicalDivision.setMediaPartialView(new MediaPartialView(EXISTING_MEDIA_PARTIAL_BEGIN));
        when(view.getPhysicalDivision()).thenReturn(childPhysicalDivision);

        LogicalDivision childLogicalDivision = spy(LogicalDivision.class);
        childLogicalDivision.getViews().add(view);
        logicalDivision = new LogicalDivision();
        logicalDivision.getChildren().add(childLogicalDivision);

        physicalDivision = new PhysicalDivision();

        mediaPartialForm = spy(new MediaPartialForm(dataEditorForm));

        // overwrites to handle save function
        LinkedList<PhysicalDivision> ancestorsOfPhysicalDivision = new LinkedList<>();
        ancestorsOfPhysicalDivision.add(new PhysicalDivision());
        doReturn(ancestorsOfPhysicalDivision).when(mediaPartialForm).getAncestorsOfPhysicalDivision();
    }

    /**
     * Test save function.
     */
    @Test
    public void testSave() {
        assertEquals(1, logicalDivision.getChildren().size());

        // add media partial
        when(mediaPartialForm.getMediaSelection()).thenReturn(new ImmutablePair<>(physicalDivision, logicalDivision));
        when(mediaPartialsPanel.getMediaDuration()).thenReturn("00:01:00");
        when(mediaPartialForm.getTitle()).thenReturn("Lorem");
        when(mediaPartialForm.getBegin()).thenReturn("00:00:45");

        mediaPartialForm.save();

        assertEquals(2, logicalDivision.getChildren().size());
        assertEquals("Lorem", logicalDivision.getChildren().get(1).getLabel());
        LogicalDivision mediaPartialLogicalDivision = logicalDivision.getChildren().get(1);
        MediaPartialView mediaPartialView = (MediaPartialView) mediaPartialLogicalDivision.getViews().get(0);
        assertEquals("00:00:45", mediaPartialView.getBegin());
        assertEquals("00:00:15", mediaPartialView.getExtent());

        // edit media partial
        when(mediaPartialForm.getTitle()).thenReturn("Lorem ipsum");
        when(mediaPartialForm.getBegin()).thenReturn("00:00:10");
        mediaPartialForm.setMediaPartialDivision(
                new AbstractMap.SimpleEntry<>(mediaPartialLogicalDivision, mediaPartialView));

        mediaPartialForm.save();

        assertEquals(2, logicalDivision.getChildren().size());
        // 'media partial' is now designated as the first child in the sorting order.
        assertEquals("Lorem ipsum", logicalDivision.getChildren().get(0).getLabel());
        mediaPartialLogicalDivision = logicalDivision.getChildren().get(0);
        mediaPartialView = (MediaPartialView) mediaPartialLogicalDivision.getViews().get(0);
        assertEquals("00:00:10", mediaPartialView.getBegin());
        assertEquals("00:00:20",
                mediaPartialView.getExtent()); // changed calculation of duration to the begin of next media partial
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

        when(mediaPartialsPanel.getMediaDuration()).thenReturn("00:01:00");
        assertFalse(mediaPartialForm.valid());
        assertEquals("mediaPartialFormStartEmpty", getValidationError(mediaPartialForm));

        when(mediaPartialForm.getBegin()).thenReturn("12345");
        assertFalse(mediaPartialForm.valid());
        assertEquals("mediaPartialFormStartWrongTimeFormat", getValidationError(mediaPartialForm));

        when(mediaPartialForm.getBegin()).thenReturn("00:02:00");
        assertFalse(mediaPartialForm.valid());
        assertEquals("mediaPartialFormStartLessThanMediaDuration", getValidationError(mediaPartialForm));

        when(mediaPartialForm.getBegin()).thenReturn(EXISTING_MEDIA_PARTIAL_BEGIN);
        assertFalse(mediaPartialForm.valid());
        assertEquals("mediaPartialFormStartExists", getValidationError(mediaPartialForm));

        when(mediaPartialForm.getBegin()).thenReturn("00:00:45");
        assertTrue(mediaPartialForm.valid());
    }

    private static String getValidationError(MediaPartialForm mediaPartialForm)
            throws NoSuchFieldException, IllegalAccessException {
        Field validationErrorField = mediaPartialForm.getClass().getDeclaredField("validationError");
        validationErrorField.setAccessible(true);
        return (String) validationErrorField.get(mediaPartialForm);
    }
}
