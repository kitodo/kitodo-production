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

import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.ProjectFileGroup;

/**
 * Implementation of Project Type.
 */
public class ProjectType extends BaseType<Project> {

    @Override
    JsonObject getJsonObject(Project project) {
        JsonArrayBuilder projectFileGroups = Json.createArrayBuilder();
        List<ProjectFileGroup> projectProjectFileGroups = project.getProjectFileGroups();
        for (ProjectFileGroup projectFileGroup : projectProjectFileGroups) {
            JsonObject projectFileGroupObject = Json.createObjectBuilder()
                    .add("name", preventNull(projectFileGroup.getName()))
                    .add("path", preventNull(projectFileGroup.getPath()))
                    .add("mimeType", preventNull(projectFileGroup.getMimeType()))
                    .add("suffix", preventNull(projectFileGroup.getSuffix()))
                    .add("folder", preventNull(projectFileGroup.getFolder()))
                    .build();
            projectFileGroups.add(projectFileGroupObject);
        }

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("title", preventNull(project.getTitle()));
        jsonObjectBuilder.add("startDate", getFormattedDate(project.getStartDate()));
        jsonObjectBuilder.add("endDate", getFormattedDate(project.getEndDate()));
        jsonObjectBuilder.add("numberOfPages", project.getNumberOfPages());
        jsonObjectBuilder.add("numberOfVolumes", project.getNumberOfVolumes());
        jsonObjectBuilder.add("fileFormatDmsExport", project.getFileFormatDmsExport());
        jsonObjectBuilder.add("fileFormatInternal", project.getFileFormatInternal());
        jsonObjectBuilder.add("metsRightsOwner", project.getMetsRightsOwner());
        jsonObjectBuilder.add("active", project.isActive());
        jsonObjectBuilder.add("processes", addObjectRelation(project.getProcesses(), true));
        jsonObjectBuilder.add("users", addObjectRelation(project.getUsers(), true));
        if (Objects.nonNull(project.getClient())) {
            jsonObjectBuilder.add("client.id", project.getClient().getId());
            jsonObjectBuilder.add("client.clientName", project.getClient().getName());
        } else {
            jsonObjectBuilder.add("client.id", 0);
            jsonObjectBuilder.add("client.clientName", "");
        }
        jsonObjectBuilder.add("projectFileGroups", projectFileGroups.build());
        return jsonObjectBuilder.build();
    }
}
