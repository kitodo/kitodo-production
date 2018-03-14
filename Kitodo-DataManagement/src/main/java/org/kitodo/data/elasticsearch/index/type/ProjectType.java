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

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.ProjectFileGroup;

/**
 * Implementation of Project Type.
 */
public class ProjectType extends BaseType<Project> {

    @Override
    public HttpEntity createDocument(Project project) {
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

        JsonObject projectObject = Json.createObjectBuilder()
                .add("title", preventNull(project.getTitle()))
                .add("startDate", getFormattedDate(project.getStartDate()))
                .add("endDate", getFormattedDate(project.getEndDate()))
                .add("numberOfPages", project.getNumberOfPages())
                .add("numberOfVolumes", project.getNumberOfVolumes())
                .add("fileFormatDmsExport", project.getFileFormatDmsExport())
                .add("fileFormatInternal", project.getFileFormatInternal())
                .add("metsRightsOwner", project.getMetsRightsOwner())
                .add("active", project.isActive())
                .add("processes", addObjectRelation(project.getProcesses(), true))
                .add("users", addObjectRelation(project.getUsers(), true))
                .add("projectFileGroups", projectFileGroups.build())
                .build();

        return new NStringEntity(projectObject.toString(), ContentType.APPLICATION_JSON);
    }
}
