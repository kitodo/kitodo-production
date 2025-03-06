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

package org.kitodo.production.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.services.ServiceManager;

public class WikiFieldHelperIT {

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldTransformWikiFieldToComment() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        process.setWikiField("<font color=\"#FF0000\">Oct 10, 2016 8:42:55 AM: Korrektur fÃ¼r Schritt Scans kopieren: Bitte Korrekturen "
                + "vornehmen. (Kowalski, Jan)</font><p>Admin, test: test1</p>");
        process = WikiFieldHelper.transformWikiFieldToComment(process);
        List<Comment> comments =  ServiceManager.getCommentService().getAllCommentsByProcess(process);

        int found = comments.size();
        assertEquals(2, found, "Not all comments in wiki field are converted");

        assertEquals("", process.getWikiField(), "Wiki field is not correctly converted");

        DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm:ss a", Locale.ENGLISH);
        assertEquals("Oct 10, 2016 8:42:55 AM", dateFormat.format(comments.get(0).getCreationDate()));
    }

    @Test
    public void shouldTransformWikiFieldToCommentWithGermanDateTime() throws Exception {
        Process process = ServiceManager.getProcessService().getById(2);
        process.setWikiField(
            "<font color=\"#FF0000\">10.10.2016 08:42:55: Korrektur fÃ¼r Schritt Scans kopieren: Bitte Korrekturen "
                    + "vornehmen. (Kowalski, Jan)</font><p>Admin, test: test1</p>");
        process = WikiFieldHelper.transformWikiFieldToComment(process);
        List<Comment> comments = ServiceManager.getCommentService().getAllCommentsByProcess(process);

        int found = comments.size();
        assertEquals(2, found, "Not all comments in wiki field are converted");

        assertEquals("", process.getWikiField(), "Wiki field is not correctly converted");

        DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm:ss a", Locale.ENGLISH);
        assertEquals("Oct 10, 2016 8:42:55 AM", dateFormat.format(comments.get(0).getCreationDate()));
    }

}
