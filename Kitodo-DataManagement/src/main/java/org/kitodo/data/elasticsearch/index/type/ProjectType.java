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

package org.kitodo.data.elasticsearch.index.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;

/**
 * Implementation of Project Type.
 */
public class ProjectType extends BaseType<Project> {

    @Override
    Map<String, Object> getJsonObject(Project project) {
        List<Map<String, Object>> folders = new ArrayList<>();
        List<Folder> projectFolders = project.getFolders();
        for (Folder folder : projectFolders) {
            Map<String, Object> folderObject = new HashMap<>();
            folderObject.put(ProjectTypeField.FOLDER_FILE_GROUP.getKey(), preventNull(folder.getFileGroup()));
            folderObject.put(ProjectTypeField.FOLDER_URL_STRUCTURE.getKey(), preventNull(folder.getUrlStructure()));
            folderObject.put(ProjectTypeField.FOLDER_MIME_TYPE.getKey(), preventNull(folder.getMimeType()));
            folderObject.put(ProjectTypeField.FOLDER_PATH.getKey(), preventNull(folder.getPath()));
            folders.add(folderObject);
        }

        int clientId = Objects.nonNull(project.getClient()) ? project.getClient().getId() : 0;
        String clientName = Objects.nonNull(project.getClient()) ? project.getClient().getName() : "";

        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(ProjectTypeField.TITLE.getKey(), preventNull(project.getTitle()));
        jsonObject.put(ProjectTypeField.START_DATE.getKey(), getFormattedDate(project.getStartDate()));
        jsonObject.put(ProjectTypeField.END_DATE.getKey(), getFormattedDate(project.getEndDate()));
        jsonObject.put(ProjectTypeField.NUMBER_OF_PAGES.getKey(), project.getNumberOfPages());
        jsonObject.put(ProjectTypeField.NUMBER_OF_VOLUMES.getKey(), project.getNumberOfVolumes());
        jsonObject.put(ProjectTypeField.FILE_FORMAT_DMS_EXPORT.getKey(), project.getFileFormatDmsExport());
        jsonObject.put(ProjectTypeField.FILE_FORMAT_INTERNAL.getKey(), project.getFileFormatInternal());
        jsonObject.put(ProjectTypeField.METS_RIGTS_OWNER.getKey(), project.getMetsRightsOwner());
        jsonObject.put(ProjectTypeField.ACTIVE.getKey(), project.isActive());
        jsonObject.put(ProjectTypeField.PROCESSES.getKey(), addObjectRelation(project.getProcesses(), true));
        jsonObject.put(ProjectTypeField.TEMPLATES.getKey(), addObjectRelation(project.getTemplates(), true));
        jsonObject.put(ProjectTypeField.USERS.getKey(), addObjectRelation(project.getUsers(), true));
        jsonObject.put(ProjectTypeField.CLIENT_ID.getKey(), clientId);
        jsonObject.put(ProjectTypeField.CLIENT_NAME.getKey(), clientName);
        jsonObject.put(ProjectTypeField.FOLDER.getKey(), folders);
        return jsonObject;
    }
}
