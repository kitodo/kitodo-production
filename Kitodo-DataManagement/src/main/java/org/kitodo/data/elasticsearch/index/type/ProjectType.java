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

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.ProjectFileGroup;

/**
 * Implementation of Project Type.
 */
public class ProjectType extends BaseType<Project> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(Project project) {

        JSONObject projectObject = new JSONObject();
        projectObject.put("title", project.getTitle());
        String startDate = project.getStartDate() != null ? formatDate(project.getStartDate()) : null;
        projectObject.put("startDate", startDate);
        String endDate = project.getEndDate() != null ? formatDate(project.getEndDate()) : null;
        projectObject.put("endDate", endDate);
        projectObject.put("numberOfPages", project.getNumberOfPages());
        projectObject.put("numberOfVolumes", project.getNumberOfVolumes());
        projectObject.put("fileFormatDmsExport", project.getFileFormatDmsExport());
        projectObject.put("fileFormatInternal", project.getFileFormatInternal());
        projectObject.put("archived", project.getProjectIsArchived());
        projectObject.put("processes", addObjectRelation(project.getProcesses(), true));
        projectObject.put("users", addObjectRelation(project.getUsers(), true));

        JSONArray projectFileGroups = new JSONArray();
        List<ProjectFileGroup> projectProjectFileGroups = project.getProjectFileGroups();
        for (ProjectFileGroup projectFileGroup : projectProjectFileGroups) {
            JSONObject projectFileGroupObject = new JSONObject();
            projectFileGroupObject.put("name", projectFileGroup.getName());
            projectFileGroupObject.put("path", projectFileGroup.getPath());
            projectFileGroupObject.put("mimeType", projectFileGroup.getMimeType());
            projectFileGroupObject.put("suffix", projectFileGroup.getSuffix());
            projectFileGroupObject.put("folder", projectFileGroup.getFolder());
            projectFileGroups.add(projectFileGroupObject);
        }
        projectObject.put("projectFileGroups", projectFileGroups);

        return new NStringEntity(projectObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
