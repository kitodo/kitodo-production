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

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;

public class WikiFieldHelperTest {

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldTransformWikiFieldToComment() throws DAOException, DataException {
        Process process = ServiceManager.getProcessService().getById(1);
        process.setWikiField("<font color=\"#FF0000\">Oct 10, 2016 8:42:55 AM: Korrektur fÃ¼r Schritt Scans kopieren: Bitte Korrekturen "
                + "vornehmen. (Kowalski, Jan)</font><p>Admin, test: test1</p>");
        process = WikiFieldHelper.transformWikiFieldToComment(process);
        int found = ServiceManager.getCommentService().getAllCommentsByProcess(process).size();
        assertEquals("Not all comments in wiki field are converted", 2, found);
        assertEquals("Wiki field is not correctly converted", "", process.getWikiField());
    }
}
