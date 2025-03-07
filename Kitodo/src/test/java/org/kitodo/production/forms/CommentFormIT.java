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

package org.kitodo.production.forms;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.kitodo.test.utils.ProcessTestUtils;

public class CommentFormIT {

    private int testProcessId = 0;

    @BeforeAll
    public static void setUpClass() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
        User userTwo = ServiceManager.getUserService().getById(2);
        SecurityTestUtils.addUserDataToSecurityContext(userTwo, 1);
    }

    @AfterEach
    public void removeTestProcess() throws DAOException {
        ProcessTestUtils.removeTestProcess(testProcessId);
        testProcessId = 0;
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldAddComment() throws DAOException, DataException, IOException {
        CommentForm commentForm = new CommentForm();
        Process testProcess = addTestProcess("Test process for adding comment");
        commentForm.setProcessById(testProcess.getId());
        commentForm.setCorrectionComment(false);
        commentForm.setCommentMessage("This is a comment");
        long numberOfCommentsBeforeAddingComment = ServiceManager.getCommentService().count();
        commentForm.addComment();
        long numberOfCommentsAfterAddingComment = ServiceManager.getCommentService().count();
        Assertions.assertEquals(numberOfCommentsAfterAddingComment, numberOfCommentsBeforeAddingComment + 1);
    }

    @Test
    public void shouldRemoveComment() throws DAOException, DataException, IOException {
        CommentForm commentForm = new CommentForm();
        Process testProcess = addTestProcess("Test process for removing comment");
        commentForm.setProcessById(testProcess.getId());
        Comment testComment = addTestComment(testProcess);
        long numberOfCommentsBeforeRemovingComment = ServiceManager.getCommentService().count();
        commentForm.removeComment(testComment);
        long numberOfCommentsAfterRemovingComment = ServiceManager.getCommentService().count();
        Assertions.assertEquals(numberOfCommentsAfterRemovingComment, numberOfCommentsBeforeRemovingComment - 1);
    }

    @Test
    public void shouldGetPreviousStepsForProblemReporting() throws DAOException, DataException, IOException {
        CommentForm commentForm = new CommentForm();
        Process testProcess = ServiceManager.getProcessService().getById(1);
        commentForm.setProcessById(testProcess.getId());
        List<Task> tasks = commentForm.getPreviousStepsForProblemReporting();
        long numberOfClosedTasks = testProcess.getTasks().stream()
                .filter(t -> TaskStatus.DONE.equals(t.getProcessingStatus())).count();
        Assertions.assertEquals(numberOfClosedTasks, tasks.size(), "Number of potential correction tasks is wrong");
        WorkflowControllerService workflowControllerService = new WorkflowControllerService();
        workflowControllerService.setTasksStatusUp(testProcess);
        commentForm.setProcessById(testProcess.getId());
        tasks = commentForm.getPreviousStepsForProblemReporting();
        Assertions.assertEquals(numberOfClosedTasks + 1, tasks.size(), "List or potential correction tasks for error" +
                " reporting should contain at least one more task than before after setting up the process status");
    }

    private Process addTestProcess(String processTitle) throws DAOException, DataException, IOException {
        Process testProcess = ProcessTestUtils.addProcess(processTitle);
        testProcessId = testProcess.getId();
        ProcessTestUtils.copyTestMetadataFile(testProcessId, "testmeta.xml");
        return testProcess;
    }

    private Comment addTestComment(Process process) throws DAOException, DataException {
        Comment comment = new Comment();
        comment.setMessage("This is a comment");
        comment.setProcess(process);
        ServiceManager.getCommentService().save(comment);
        ServiceManager.getProcessService().save(process);
        return comment;
    }

}
