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
import org.junit.Assert;
import org.junit.Test;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartialView;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MediaPartialFormTest {

    @Test
    public void testValidation() throws NoSuchFieldException, IllegalAccessException {
        DataEditorForm dataEditorForm = mock(DataEditorForm.class);
        GalleryPanel galleryPanel = mock(GalleryPanel.class);
        MediaPartialsPanel mediaPartialsPanel = spy(new MediaPartialsPanel(dataEditorForm));
        when(dataEditorForm.getGalleryPanel()).thenReturn(galleryPanel);
        when(galleryPanel.getMediaPartialsPanel()).thenReturn(mediaPartialsPanel);

        MediaPartialForm mediaPartialForm = spy(new MediaPartialForm(dataEditorForm));

        Assert.assertFalse(mediaPartialForm.valid());
        Assert.assertEquals("mediaPartialFormNoMedium", getValidationError(mediaPartialForm));

        View view = mock(View.class);
        PhysicalDivision physicalDivision = spy(PhysicalDivision.class);
        String existingMediaPartialBegin = "00:00:30";
        physicalDivision.setMediaPartialView(new MediaPartialView(existingMediaPartialBegin));
        when(view.getPhysicalDivision()).thenReturn(physicalDivision);

        LogicalDivision childLogicalDivision = spy(LogicalDivision.class);
        childLogicalDivision.getViews().add(view);
        LogicalDivision logicalDivision = new LogicalDivision();
        logicalDivision.getChildren().add(childLogicalDivision);

        when(mediaPartialForm.getMediaSelection()).thenReturn(new ImmutablePair<>(null, logicalDivision));
        Assert.assertFalse(mediaPartialForm.valid());
        Assert.assertEquals("mediaPartialFormMediaDurationEmpty", getValidationError(mediaPartialForm));

        when(mediaPartialsPanel.getMediaDuration()).thenReturn("12345");
        Assert.assertFalse(mediaPartialForm.valid());
        Assert.assertEquals("mediaPartialFormMediaDurationWrongTimeFormat", getValidationError(mediaPartialForm));

        when(mediaPartialsPanel.getMediaDuration()).thenReturn("00:01:00");
        Assert.assertFalse(mediaPartialForm.valid());
        Assert.assertEquals("mediaPartialFormStartEmpty", getValidationError(mediaPartialForm));

        when(mediaPartialForm.getBegin()).thenReturn("12345");
        Assert.assertFalse(mediaPartialForm.valid());
        Assert.assertEquals("mediaPartialFormStartWrongTimeFormat", getValidationError(mediaPartialForm));

        when(mediaPartialForm.getBegin()).thenReturn("00:02:00");
        Assert.assertFalse(mediaPartialForm.valid());
        Assert.assertEquals("mediaPartialFormStartLessThanMediaDuration", getValidationError(mediaPartialForm));

        when(mediaPartialForm.getBegin()).thenReturn(existingMediaPartialBegin);
        Assert.assertFalse(mediaPartialForm.valid());
        Assert.assertEquals("mediaPartialFormStartExists", getValidationError(mediaPartialForm));

        when(mediaPartialForm.getBegin()).thenReturn("00:00:45");
        Assert.assertTrue(mediaPartialForm.valid());
    }

    private static String getValidationError(MediaPartialForm mediaPartialForm)
            throws NoSuchFieldException, IllegalAccessException {
        Field validationErrorField = mediaPartialForm.getClass().getDeclaredField("validationError");
        validationErrorField.setAccessible(true);
        String validationError = (String) validationErrorField.get(mediaPartialForm);
        return validationError;
    }
}
