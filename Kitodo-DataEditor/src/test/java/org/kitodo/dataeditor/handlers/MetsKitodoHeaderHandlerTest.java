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

package org.kitodo.dataeditor.handlers;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.MetsType;

public class MetsKitodoHeaderHandlerTest {

    @Test
    public void missingMetsHeaderShouldNotCreteNullPointerException() {
        String noteMessage = "test note";
        Mets mets = new Mets();

        Mets actual = MetsKitodoHeaderHandler.addNoteToMetsHeader(noteMessage, mets);
        Assert.assertEquals("Mets object differ.", mets, actual);
    }

    @Test
    public void missingMetsHeaderAgentSectionDidNotChangeAnything() {
        String noteMessage = "test note";
        Mets mets = new Mets();

        Mets actual = MetsKitodoHeaderHandler.addNoteToMetsHeader(noteMessage, mets);
        Assert.assertEquals("Mets object differ.", mets, actual);
    }

    @Test
    public void addNoteToExistingAgentSection() {
        String noteMessage = "test note";
        List<String> expectedNotes = new ArrayList<>();
        expectedNotes.add(noteMessage);

        MetsType.MetsHdr.Agent agent = new MetsType.MetsHdr.Agent();
        agent.setName("Kitodo");
        MetsType.MetsHdr metsHeader = new MetsType.MetsHdr();
        metsHeader.getAgent().add(0, agent);
        Mets mets = new Mets();
        mets.setMetsHdr(metsHeader);
        Mets actual = MetsKitodoHeaderHandler.addNoteToMetsHeader(noteMessage, mets);

        List<String> resultNotes = actual.getMetsHdr().getAgent().get(0).getNote();
        Assert.assertEquals("MetsHeader notes differ.", expectedNotes, resultNotes);
    }
}
