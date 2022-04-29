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

import org.junit.Assert;
import org.junit.Test;

public class GalleryPanelTest {

    @Test
    public void preventNPEOnGetMediaViewMimeType() {
        DataEditorForm dummyDataEditorForm = new DataEditorForm();
        GalleryPanel galleryPanel = new GalleryPanel(dummyDataEditorForm);
        Assert.assertNotNull(galleryPanel.getMediaViewMimeType());
    }

    @Test
    public void preventNPEOnGetPreviewMimeType() {
        DataEditorForm dummyDataEditorForm = new DataEditorForm();
        GalleryPanel galleryPanel = new GalleryPanel(dummyDataEditorForm);
        Assert.assertNotNull(galleryPanel.getPreviewMimeType());
    }
}
