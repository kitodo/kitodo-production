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
import org.junit.Before;
import org.junit.Test;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartialView;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.mockito.Mockito;
import org.omnifaces.util.Ajax;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MediaPartialPanelTest {

    MediaPartialsPanel mediaPartialsPanel;

    @Before
    public void init() {
        // mock frontend update calls
        Mockito.mockStatic(Ajax.class);
    }

    @Before
    public void initTest() {
        DataEditorForm dataEditorForm = mock(DataEditorForm.class);
        mediaPartialsPanel = spy(new MediaPartialsPanel(dataEditorForm));
    }

    @Test
    public void testGenerateExtentAndSortMediaPartials() {
        List<LogicalDivision> logicalDivisions = new ArrayList<>();
        logicalDivisions.add(getLogicalDivisionWithMediaPartial("Lorem ipsum", "00:00:45"));
        logicalDivisions.add(getLogicalDivisionWithMediaPartial("Lorem ipsum", "00:00:00"));
        logicalDivisions.add(getLogicalDivisionWithMediaPartial("Lorem ipsum", "00:00:55"));
        logicalDivisions.add(getLogicalDivisionWithMediaPartial("Lorem ipsum", "00:00:35"));

        // one minute media duration
        MediaPartialsPanel.generateExtentAndSortMediaPartials(logicalDivisions, Long.valueOf(60000L));

        Assert.assertEquals("00:00:00", ((MediaPartialView) logicalDivisions.get(0).getViews().get(0)).getBegin());
        Assert.assertEquals("00:00:35", ((MediaPartialView) logicalDivisions.get(0).getViews().get(0)).getExtent());
        Assert.assertEquals("00:00:35", ((MediaPartialView) logicalDivisions.get(1).getViews().get(0)).getBegin());
        Assert.assertEquals("00:00:10", ((MediaPartialView) logicalDivisions.get(1).getViews().get(0)).getExtent());
        Assert.assertEquals("00:00:45", ((MediaPartialView) logicalDivisions.get(2).getViews().get(0)).getBegin());
        Assert.assertEquals("00:00:10", ((MediaPartialView) logicalDivisions.get(2).getViews().get(0)).getExtent());
        Assert.assertEquals("00:00:55", ((MediaPartialView) logicalDivisions.get(3).getViews().get(0)).getBegin());
        Assert.assertEquals("00:00:05", ((MediaPartialView) logicalDivisions.get(3).getViews().get(0)).getExtent());
    }

    private static LogicalDivision getLogicalDivisionWithMediaPartial(String label, String begin) {
        LogicalDivision logicalDivision = new LogicalDivision();
        logicalDivision.setLabel(label);
        PhysicalDivision physicalDivision = new PhysicalDivision();
        MediaPartialView mediaPartialView = new MediaPartialView(begin);
        physicalDivision.setMediaPartialView(mediaPartialView);
        logicalDivision.getViews().add(mediaPartialView);
        return logicalDivision;
    }

    @Test
    public void testMediaDurationValidation() {
        assertEquals("mediaPartialFormMediaDurationEmpty", mediaPartialsPanel.validateMediaDuration());
        when(mediaPartialsPanel.getMediaDuration()).thenReturn("123456");
        assertEquals("mediaPartialFormMediaDurationWrongTimeFormat", mediaPartialsPanel.validateMediaDuration());
        when(mediaPartialsPanel.getMediaDuration()).thenReturn("00:01:00");
        Assert.assertNull(mediaPartialsPanel.validateMediaDuration());
    }

    @Test
    public void testConverting() {
        assertEquals(Long.valueOf(3661000L), mediaPartialsPanel.convertFormattedTimeToMilliseconds("01:01:01"));
        assertEquals("01:01:01", mediaPartialsPanel.convertMillisecondsToFormattedTime(3661000L));
    }
}
