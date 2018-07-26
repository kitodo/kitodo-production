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

import java.util.List;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;

/**
 * Implementation of Project Type.
 */
public class ProjectType extends BaseType<Project> {

    @Override
    JsonObject getJsonObject(Project project) {

        JsonArrayBuilder folders = Json.createArrayBuilder();
        List<Folder> projectFolders = project.getFolders();
        for (Folder folder : projectFolders) {
            JsonObject folderObject = Json.createObjectBuilder()
                    .add(ProjectTypeField.FOLDER_FILE_GROUP.getKey(), preventNull(folder.getFileGroup()))
                    .add(ProjectTypeField.FOLDER_URL_STRUCTURE.getKey(), preventNull(folder.getUrlStructure()))
                    .add(ProjectTypeField.FOLDER_MIME_TYPE.getKey(), preventNull(folder.getMimeType()))
                    .add(ProjectTypeField.FOLDER_PATH.getKey(), preventNull(folder.getPath())).build();
            folders.add(folderObject);
        }

        Integer clientId = Objects.nonNull(project.getClient()) ? project.getClient().getId() : 0;
        String clientName = Objects.nonNull(project.getClient()) ? project.getClient().getName() : "";

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(ProjectTypeField.TITLE.getKey(), preventNull(project.getTitle()));
        jsonObjectBuilder.add(ProjectTypeField.START_DATE.getKey(), getFormattedDate(project.getStartDate()));
        jsonObjectBuilder.add(ProjectTypeField.END_DATE.getKey(), getFormattedDate(project.getEndDate()));
        jsonObjectBuilder.add(ProjectTypeField.NUMBER_OF_PAGES.getKey(), project.getNumberOfPages());
        jsonObjectBuilder.add(ProjectTypeField.NUMBER_OF_VOLUMES.getKey(), project.getNumberOfVolumes());
        jsonObjectBuilder.add(ProjectTypeField.FILE_FORMAT_DMS_EXPORT.getKey(), project.getFileFormatDmsExport());
        jsonObjectBuilder.add(ProjectTypeField.FILE_FORMAT_INTERNAL.getKey(), project.getFileFormatInternal());
        jsonObjectBuilder.add(ProjectTypeField.METS_RIGTS_OWNER.getKey(), project.getMetsRightsOwner());
        jsonObjectBuilder.add(ProjectTypeField.ACTIVE.getKey(), project.isActive());
        jsonObjectBuilder.add(ProjectTypeField.PROCESSES.getKey(), addObjectRelation(project.getProcesses(), true));
        jsonObjectBuilder.add(ProjectTypeField.TEMPLATES.getKey(), addObjectRelation(project.getTemplates(), true));
        jsonObjectBuilder.add(ProjectTypeField.USERS.getKey(), addObjectRelation(project.getUsers(), true));
        jsonObjectBuilder.add(ProjectTypeField.CLIENT_ID.getKey(), clientId);
        jsonObjectBuilder.add(ProjectTypeField.CLIENT_NAME.getKey(), clientName);
        jsonObjectBuilder.add(ProjectTypeField.FOLDER.getKey(), folders.build());
        return jsonObjectBuilder.build();
    }
}
