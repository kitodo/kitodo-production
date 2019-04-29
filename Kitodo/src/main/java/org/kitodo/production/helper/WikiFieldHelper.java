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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;

public class WikiFieldHelper {

    private static final Logger logger = LogManager.getLogger(WikiFieldHelper.class);

    private static final String CORRECTION_FOR_TASK_EN = "Correction for step";

    /**
     * transform wiki field to Comment objects.
     *
     * @param process
     *          process as object.
     */
    public static void transformWikiFieldToComment(Process process) {
        String[] messages = getWikiField(process);
        if (messages.length > 0) {
            for (String message : messages) {
                Comment comment = new Comment();
                comment.setProcess(process);
                comment.setMessage(getWikiFieldMessage(message));
                comment.setAuthor(getWikiFieldAuthor(message));
                if (message.contains("Red K")) {
                    comment.setType(CommentType.ERROR);
                    Property correctionRequiredProperty = getCorrectionRequiredProperty(process, message);
                    if (Objects.nonNull(correctionRequiredProperty)) {
                        try {
                            comment.setCreationDate(getCreationDate(correctionRequiredProperty));
                            comment.setCurrentTask(getCurrentTask(correctionRequiredProperty));
                            comment.setCorrectionTask(getCorrectionTask(correctionRequiredProperty));
                            deleteProperty(process, correctionRequiredProperty);
                        } catch (DAOException | DataException | ParseException e) {
                            Helper.setErrorMessage("PropertyError", logger, e);
                        }
                    }
                } else if (message.contains("Orange K")) {
                    comment.setType(CommentType.ERROR);
                    comment.setCorrected(Boolean.TRUE);
                    Property correctionPerformed = getCorrectionPerformedProperty(process, message);
                    if (Objects.nonNull(correctionPerformed)) {
                        comment.setCreationDate(correctionPerformed.getCreationDate());
                        try {
                            comment.setCorrectionDate(getCreationDate(correctionPerformed));
                            deleteProperty(process, correctionPerformed);
                        } catch (ParseException | DAOException | DataException e) {
                            Helper.setErrorMessage("Deleting error", logger, e);
                        }
                    }
                    comment.setCurrentTask(ServiceManager.getProcessService().getCurrentTask(process));
                    comment.setCorrectionTask(getWikiFieldCorrectionTask(message, process));
                } else {
                    comment.setType(CommentType.INFO);
                }
                try {
                    ServiceManager.getCommentService().saveToDatabase(comment);
                    process.setWikiField("");
                    ServiceManager.getProcessService().save(process);
                } catch (DAOException | DataException e) {
                    Helper.setErrorMessage("Saving error", logger, e);
                }
            }
        }
    }

    private static Property getCorrectionRequiredProperty(Process process, String message) {
        List<Property> properties = process.getProperties();
        for (Property property : properties) {
            String translation = Helper.getTranslation("correctionNecessary");
            String msg = getWikiFieldMessage(message);
            if (property.getTitle().equals(translation)
                    && property.getValue().contains(msg)) {
                return property;
            }
        }
        return null;
    }

    private static Property getCorrectionPerformedProperty(Process process, String message) {
        List<Property> properties = process.getProperties();
        for (Property property : properties) {
            if (property.getTitle().equals(Helper.getTranslation("correctionPerformed"))
                    && property.getValue().endsWith(getWikiFieldCorrectionTask(message, process).getTitle())) {
                return property;
            }
        }
        return null;
    }

    private static Task getCorrectionTask(Property property) throws DAOException {
        int correctionTaskId = Integer.parseInt(property.getValue().substring(property.getValue().indexOf(" CorrectionTask: ") + 17,
                property.getValue().indexOf(")")));
        return ServiceManager.getTaskService().getById(correctionTaskId);

    }

    private static Task getCurrentTask(Property property) throws DAOException {
        int currentTaskId = Integer.parseInt(property.getValue().substring(property.getValue().indexOf("(CurrentTask: ") + 14,
                property.getValue().indexOf(" CorrectionTask: ")));
        return ServiceManager.getTaskService().getById(currentTaskId);
    }

    private static Date getCreationDate(Property property) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(property.getValue().substring(1, 20));
    }

    private static String getWikiFieldMessage(String message) {
        return message.substring(message.indexOf(':') + 1).trim();
    }

    private static User getWikiFieldAuthor(String message) {
        String[] parts = message.split(":");
        if (parts.length < 2) {
            return null;
        }
        String authorName = parts[0];
        if (authorName.contains(CORRECTION_FOR_TASK_EN)) {
            authorName = authorName.split(CORRECTION_FOR_TASK_EN)[0];
        } else if (authorName.contains(CORRECTION_FOR_TASK_DE)) {
            authorName = authorName.split(CORRECTION_FOR_TASK_DE)[0];
        }
        if (authorName.contains("Red K ")) {
            authorName = authorName.replace("Red K ", "");
        } else if (authorName.contains("Orange K ")) {
            authorName = authorName.replace("Orange K ", "");
        }
        String surname = authorName.split(",")[0].trim();
        String name = authorName.split(",")[1].trim();
        List<User> userList = ServiceManager.getUserService().getByQuery("FROM User WHERE name ='" + name
                + "' AND surname = '" + surname + "'");
        if (userList.isEmpty()) {
            return null;
        }
        return userList.get(0);
    }

    private static Task getWikiFieldCorrectionTask(String message, Process process) {
        String[] parts = message.split(":");
        if (parts.length < 2) {
            return null;
        }
        String correctionTaskName = parts[0];
        if (correctionTaskName.contains(CORRECTION_FOR_TASK_EN)) {
            correctionTaskName = correctionTaskName.split(CORRECTION_FOR_TASK_EN)[1];
        } else if (correctionTaskName.contains(CORRECTION_FOR_TASK_DE)) {
            correctionTaskName = correctionTaskName.split(CORRECTION_FOR_TASK_DE)[1];
        }
        for (Task task : process.getTasks()) {
            if (task.getTitle().equals(correctionTaskName.trim())) {
                return task;
            }
        }
        return null;
    }

    private static String[] getWikiField(Process process) {
        String wiki = process.getWikiField();
        if (!wiki.isEmpty()) {
            wiki = wiki.replace("</p>", "");
            String[] comments = wiki.split("<p>");
            if (comments[0].isEmpty()) {
                List<String> list = new ArrayList<>(Arrays.asList(comments));
                list.remove(list.get(0));
                comments = list.toArray(new String[0]);
            }
            return comments;
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    private static void deleteProperty(Process process, Property property) throws DAOException, DataException {
        property.getProcesses().clear();
        process.getProperties().remove(property);
        ServiceManager.getProcessService().save(process);
        ServiceManager.getPropertyService().removeFromDatabase(property);
    }
}
