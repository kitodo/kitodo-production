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

package org.kitodo.production.services.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.production.services.ServiceManager;

public class CommentServiceIT {

    private static final CommentService commentService = ServiceManager.getCommentService();
    private static final ProcessService processService = ServiceManager.getProcessService();

    /**
     * Prepare tests by inserting dummy processes into database.
     * 
     * @throws Exception
     *             when saving of dummy or test processes fails.
     */
    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
    }

    /**
     * Clean up database after tests.
     * 
     * @throws Exception
     *             when cleaning up database fails.
     */
    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    /**
     * Tests wether comment is correctly saved and removed from database.
     * 
     * @throws Exception
     *             when saving or deleting comment fails.
     */
    @Test
    public void shouldSaveAndRemoveInfoComment() throws Exception {
        Process process = processService.getById(1);
        Comment comment = new Comment();

        comment.setProcess(process);
        comment.setMessage("TEST_MESSAGE");
        comment.setAuthor(ServiceManager.getUserService().getById(1));
        comment.setCreationDate(new Date());
        comment.setType(CommentType.INFO);
        commentService.save(comment);
        Comment newComment = commentService.getAll().getFirst();
        assertEquals("TEST_MESSAGE", newComment.getMessage(), "Comment was not found in database!");

        commentService.removeComment(newComment);
        List<Comment> comments = commentService.getAll();
        assertEquals(0, comments.size(), "Comments were found in database!");
    }
}
