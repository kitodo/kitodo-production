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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartial;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;
import org.kitodo.production.helper.metadata.MediaPartialHelper;

public class MediaPartialPanelTest {

    MediaPartialsPanel mediaPartialsPanel;

    /**
     * Initialize test function.
     */
    @BeforeEach
    public void initTest() {
        DataEditorForm dataEditorForm = mock(DataEditorForm.class);
        mediaPartialsPanel = spy(new MediaPartialsPanel(dataEditorForm));
    }

    /**
     * Test generation of extent and sorting of media partials.
     */
    @Test
    public void testGenerateExtentAndSortMediaPartials() {
        List<LogicalDivision> logicalDivisions = new ArrayList<>();
        logicalDivisions.add(getLogicalDivisionWithMediaPartial("00:00:45.001"));
        logicalDivisions.add(getLogicalDivisionWithMediaPartial("00:00:00.002"));
        logicalDivisions.add(getLogicalDivisionWithMediaPartial("00:00:55.894"));
        logicalDivisions.add(getLogicalDivisionWithMediaPartial("00:00:35.123"));

        // one minute media duration
        MediaPartialHelper.calculateExtentAndSortMediaPartials(logicalDivisions, 60000L);

        assertEquals("00:00:00.002", getMediaPartialOfLogicalDivision(logicalDivisions, 0).getBegin());
        assertEquals("00:00:35.121", getMediaPartialOfLogicalDivision(logicalDivisions, 0).getExtent());
        assertEquals("00:00:35.123", getMediaPartialOfLogicalDivision(logicalDivisions, 1).getBegin());
        assertEquals("00:00:09.878", getMediaPartialOfLogicalDivision(logicalDivisions, 1).getExtent());
        assertEquals("00:00:45.001", getMediaPartialOfLogicalDivision(logicalDivisions, 2).getBegin());
        assertEquals("00:00:10.893", getMediaPartialOfLogicalDivision(logicalDivisions, 2).getExtent());
        assertEquals("00:00:55.894", getMediaPartialOfLogicalDivision(logicalDivisions, 3).getBegin());
        assertEquals("00:00:04.106", getMediaPartialOfLogicalDivision(logicalDivisions, 3).getExtent());
    }

    private static MediaPartial getMediaPartialOfLogicalDivision(List<LogicalDivision> logicalDivisions, int index) {
        return logicalDivisions.get(index).getViews().getFirst().getPhysicalDivision().getMediaPartial();
    }

    /**
     * Test media duration validation.
     */
    @Test
    public void testMediaDurationValidation() {
        assertEquals("mediaPartialFormMediaDurationEmpty", mediaPartialsPanel.validateMediaDuration());
        when(mediaPartialsPanel.getMediaDuration()).thenReturn("123456");
        assertEquals("mediaPartialFormMediaDurationWrongTimeFormat", mediaPartialsPanel.validateMediaDuration());
        when(mediaPartialsPanel.getMediaDuration()).thenReturn("00:01:00.000");
        assertNull(mediaPartialsPanel.validateMediaDuration());
    }

    /**
     * Test converting of formatted time and milliseconds.
     */
    @Test
    public void testConverting() {
        assertEquals(Long.valueOf(3661012L), MediaPartialHelper.convertFormattedTimeToMilliseconds("01:01:01.012"));
        assertEquals("01:01:01.120", MediaPartialHelper.convertMillisecondsToFormattedTime(3661120L));
    }

    private static LogicalDivision getLogicalDivisionWithMediaPartial(String begin) {
        LogicalDivision logicalDivision = new LogicalDivision();
        logicalDivision.setLabel("Lorem ipsum");
        PhysicalDivision physicalDivision = new PhysicalDivision();
        MediaPartial mediaPartial = new MediaPartial(begin);
        physicalDivision.setMediaPartial(mediaPartial);
        logicalDivision.getViews().add(View.of(physicalDivision));
        return logicalDivision;
    }
}
