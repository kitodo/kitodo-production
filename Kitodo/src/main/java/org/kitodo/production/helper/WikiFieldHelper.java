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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WikiFieldHelper {

    private static final Logger logger = LogManager.getLogger(WikiFieldHelper.class);

    private static final String CORRECTION_FOR_TASK_DE = "Korrektur fuer Schritt";
    private static final String CORRECTION_FOR_TASK_EN = "Correction for step";

    /**
     * Private constructor to hide the implicit public one.
     */
    private WikiFieldHelper() {
    }

    /**
     * transform wiki field to Comment objects.
     *
     * @param process
     *            process as object.
     */
    public static Process transformWikiFieldToComment(Process process) throws DAOException, DAOException, ParseException {
        String wikiField = process.getWikiField();
        wikiField = wikiField.replaceAll("Ã¼", "ue");
        wikiField = wikiField.replaceAll("&uuml;", "ue");
        if (!wikiField.isEmpty()) {
            wikiField = wikiField.replace("</p>", "");
            String[] comments = wikiField.split("<p>");
            if (!comments[0].isEmpty()) {
                String oldComments = comments[0];
                oldComments = "<messages>" + oldComments + "</messages>";
                Document document = convertStringToDocument(oldComments);
                transformOldFormatWikifieldToComments(document, process);
            }
            List<String> list = new ArrayList<>(Arrays.asList(comments));
            list.remove(list.get(0));
            comments = list.toArray(new String[0]);
            transformNewFormatWikiFieldToComments(comments, process);
            Process processWithoutProperties = deleteProcessCorrectionProperties(process);
            processWithoutProperties.setWikiField("");
            ServiceManager.getProcessService().save(processWithoutProperties);
            return processWithoutProperties;
        }
        return process;
    }

    private static Property getCorrectionRequiredProperty(Process process, String message) {
        List<Property> properties = process.getProperties();
        for (Property property : properties) {
            String translation = Helper.getTranslation("correctionNecessary");
            String msg = getWikiFieldMessage(message);
            if (property.getTitle().equals(translation) && property.getValue().contains(msg)) {
                return property;
            }
        }
        return null;
    }

    private static Property getCorrectionPerformedProperty(Process process, String message, String language) {
        List<Property> properties = process.getProperties();
        for (Property property : properties) {
            Task task = getWikiFieldCorrectionTask(message, process, language);
            if (Objects.nonNull(task) && property.getTitle().equals(Helper.getTranslation("correctionPerformed"))
                    && property.getValue().endsWith(task.getTitle())) {
                return property;
            }
        }
        return null;
    }

    private static Task getCorrectionTask(Property property) throws DAOException {
        int correctionTaskId = Integer.parseInt(property.getValue()
                .substring(property.getValue().indexOf(" CorrectionTask: ") + 17, property.getValue().indexOf(')')));
        return ServiceManager.getTaskService().getById(correctionTaskId);

    }

    private static Task getCurrentTask(Property property) throws DAOException {
        int currentTaskId = Integer.parseInt(property.getValue().substring(
            property.getValue().indexOf("(CurrentTask: ") + 14, property.getValue().indexOf(" CorrectionTask: ")));
        return ServiceManager.getTaskService().getById(currentTaskId);
    }

    private static Date getCreationDate(Property property) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(property.getValue().substring(1, 20));
    }

    private static String getWikiFieldMessage(String message) {
        return message.substring(message.indexOf(':') + 1).trim();
    }

    private static User getWikiFieldAuthor(String message, String lang) {
        String[] parts = message.split(":");
        if (parts.length < 2) {
            return null;
        }
        String authorName = parts[0];
        if ("de".equals(lang)) {
            authorName = authorName.split(CORRECTION_FOR_TASK_DE)[0];
        } else if ("en".equals(lang)) {
            authorName = authorName.split(CORRECTION_FOR_TASK_EN)[0];
        }
        if (authorName.contains("Red K ")) {
            authorName = authorName.replace("Red K ", "");
        } else if (authorName.contains("Orange K ")) {
            authorName = authorName.replace("Orange K ", "");
        }
        return getUserByFullName(authorName);
    }

    private static Task getWikiFieldCorrectionTask(String message, Process process, String lang) {
        String[] parts = message.split(":");
        if (parts.length < 2) {
            return null;
        }
        String correctionTaskName = parts[0];
        if ("en".equals(lang)) {
            correctionTaskName = correctionTaskName.split(CORRECTION_FOR_TASK_EN)[1];
        } else if ("de".equals(lang)) {
            correctionTaskName = correctionTaskName.split(CORRECTION_FOR_TASK_DE)[1];
        }
        for (Task task : process.getTasks()) {
            if (task.getTitle().equals(correctionTaskName.trim())) {
                return task;
            }
        }
        return null;
    }

    private static void deleteProperty(Process process, Property property) throws DAOException, DAOException {
        property.getProcesses().clear();
        process.getProperties().remove(property);
        ServiceManager.getProcessService().save(process);
        ServiceManager.getPropertyService().removeFromDatabase(property);
    }

    /*
     * Example of wiki Field's new format: <p>Admin, test: bla bla <p>Orange K
     * Admin, test Korrektur f&uuml;r Schritt Scanning: bla bla <p>Red K Admin,
     * test Korrektur f&uuml;r Schritt Scanning: bla bla
     */
    private static void transformNewFormatWikiFieldToComments(String[] messages, Process process)
            throws DAOException, DAOException, ParseException {
        List<Comment> newComments = new ArrayList<>();
        for (String message : messages) {
            String lang = getMessageLanguage(message);
            Comment comment = new Comment();
            comment.setProcess(process);
            comment.setMessage(getWikiFieldMessage(message));
            comment.setAuthor(getWikiFieldAuthor(message, lang));
            if (message.contains("Red K")) {
                comment.setType(CommentType.ERROR);
                Property correctionRequiredProperty = getCorrectionRequiredProperty(process, message);
                if (Objects.nonNull(correctionRequiredProperty)) {
                    comment.setCreationDate(getCreationDate(correctionRequiredProperty));
                    comment.setCurrentTask(getCurrentTask(correctionRequiredProperty));
                    comment.setCorrectionTask(getCorrectionTask(correctionRequiredProperty));
                    deleteProperty(process, correctionRequiredProperty);
                }
            } else if (message.contains("Orange K")) {
                comment.setType(CommentType.ERROR);
                comment.setCorrected(Boolean.TRUE);
                Property correctionPerformed = getCorrectionPerformedProperty(process, message, lang);
                if (Objects.nonNull(correctionPerformed)) {
                    comment.setCreationDate(correctionPerformed.getCreationDate());
                    comment.setCorrectionDate(getCreationDate(correctionPerformed));
                    deleteProperty(process, correctionPerformed);
                }
                comment.setCurrentTask(ServiceManager.getProcessService().getCurrentTask(process));
                comment.setCorrectionTask(getWikiFieldCorrectionTask(message, process, lang));
            } else {
                comment.setType(CommentType.INFO);
            }
            newComments.add(comment);
        }
        saveComments(newComments);
    }

    private static String getMessageLanguage(String message) {
        if (message.contains(CORRECTION_FOR_TASK_EN)) {
            return "en";
        } else if (message.contains(CORRECTION_FOR_TASK_DE)) {
            return "de";
        }
        return "";
    }

    /*
        Example of wiki Field's very old format (#FF0000:Correction comment, #006600: is corrected, #0033CC: Info comment):
        <font color="#FF0000">Jun 16, 2016 1:12:58 PM: Korrektur fÃ¼r Schritt Scannen: bla bla bla.. (Admin, test)</font><br/>
        <font color="#006600">Jun 17, 2016 10:36:43 AM: bla bla (Admin, test)</font><br/>
        <font color="#0033CC">Jun 17, 2016 10:40:43 AM: bla bla (Admin, test)</font>

       Another existing format, with German-style formatted date:
       <font color="#FF0000">06.04.2017 09:38:58: bla bla (User, Example)</font><br/>
    */
    private static void transformOldFormatWikifieldToComments(Document document, Process process) {
        Element root = document.getDocumentElement();
        NodeList nodeList = root.getElementsByTagName("font");
        List<Comment> commentList = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            String color = element.getAttribute("color");
            if (Objects.equals(color, "#FF0000") || Objects.equals(color, "#0033CC")) {
                Comment comment = new Comment();
                comment.setProcess(process);
                String message = element.getTextContent();
                String lang = getMessageLanguage(message);
                comment.setCreationDate(getCreationDateOld(message));
                comment.setMessage(getOldComment(message, lang));
                String authorName = message.substring(message.lastIndexOf('(') + 1, message.lastIndexOf(')'));
                comment.setAuthor(getUserByFullName(authorName));
                if (Objects.equals(color, "#FF0000")) {
                    comment.setType(CommentType.ERROR);
                    comment.setCorrected(true);
                    comment.setCorrectionTask(getOldCorrectionTask(message, process, lang));
                } else if (Objects.equals(color, "#0033CC")) {
                    comment.setType(CommentType.INFO);
                }
                commentList.add(comment);
            }

        }
        if (!commentList.isEmpty()) {
            saveComments(commentList);
        }
    }

    private static void saveComments(List<Comment> commentList) {
        try {
            ServiceManager.getCommentService().saveList(commentList);
        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /*
     * fullName = surname, name
     */
    private static User getUserByFullName(String fullName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("surname", fullName.split(",")[0].trim());
        parameters.put("name", fullName.split(",")[1].trim());
        List<User> userList = ServiceManager.getUserService()
                .getByQuery("FROM User WHERE name = :name AND surname = :surname", parameters);
        if (userList.isEmpty()) {
            return null;
        }
        return userList.get(0);
    }

    private static Document convertStringToDocument(String xmlString) {
        Document document = null;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder
                    .parse(new InputSource(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            logger.error("{} Could not parse XML string '{}'!", e.getMessage(), xmlString, e);
        }
        return document;
    }

    private static Date getCreationDateOld(String message) {
        try {
            Pattern pattern = Pattern.compile("^\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2}(?=: )");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return new SimpleDateFormat("dd.MM.yyy hh:mm:ss").parse(matcher.group());
            }

            int index = message.contains("PM") ? message.indexOf("PM") : message.indexOf("AM");
            String date = message.substring(0, index + 2);
            DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm:ss a", Locale.ENGLISH);
            return dateFormat.parse(date);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private static String getOldComment(String message, String language) {
        String comment;
        if ("de".equals(language)) {
            comment = message.substring(message.indexOf(':', message.indexOf(CORRECTION_FOR_TASK_DE)) + 1,
                message.lastIndexOf('('));
        } else if ("en".equals(language)) {
            comment = message.substring(message.indexOf(':', message.indexOf(CORRECTION_FOR_TASK_EN)) + 1,
                message.lastIndexOf('('));
        } else {
            int index = message.contains("PM:") ? message.indexOf("PM:") : message.indexOf("AM:");
            comment = message.substring(index + 3, message.lastIndexOf('('));
        }
        return comment;
    }

    private static Task getOldCorrectionTask(String message, Process process, String language) {
        String correctionTaskName;
        if ("de".equals(language)) {
            int index = message.indexOf(CORRECTION_FOR_TASK_DE);
            correctionTaskName = message.substring(index + 23, message.indexOf(':', index));
        } else {
            int index = message.indexOf(CORRECTION_FOR_TASK_EN);
            correctionTaskName = message.substring(index + 20, message.indexOf(':', index));
        }
        for (Task task : process.getTasks()) {
            if (task.getTitle().equals(correctionTaskName.trim())) {
                return task;
            }
        }
        return null;
    }

    private static Process deleteProcessCorrectionProperties(Process process) throws DAOException, DAOException {
        List<Property> properties = new ArrayList<>(process.getProperties());

        for (Property property : properties) {
            String title = property.getTitle();
            if ("Korrektur notwendig".equals(title) || "Correction required".equals(title)
                    || "Korrektur durchgef\\u00FChrt".equals(title) || "Correction performed".equals(title)) {
                process.getProperties().remove(property);
                ServiceManager.getProcessService().save(process);
                ServiceManager.getPropertyService().removeFromDatabase(property);
                property.getProcesses().remove(process);
                ServiceManager.getPropertyService().removeFromDatabase(property);
                return ServiceManager.getProcessService().getById(process.getId());
            }
        }

        return process;
    }
}
