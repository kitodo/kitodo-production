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

package org.kitodo.data.index.elasticsearch.type;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.beans.User;

/**
 * Implementation of Project Type.
 */
public class ProjectType extends BaseType<Project> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(Project project) {

        LinkedHashMap<String, String> orderedProjectMap = new LinkedHashMap<>();
        orderedProjectMap.put("name", project.getTitle());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = project.getStartDate() != null ? dateFormat.format(project.getStartDate()) : null;
        orderedProjectMap.put("startDate", startDate);
        String endDate = project.getEndDate() != null ? dateFormat.format(project.getEndDate()) : null;
        orderedProjectMap.put("endDate", endDate);
        String numberOfPages = project.getNumberOfPages() != null ? project.getNumberOfPages().toString() : "null";
        orderedProjectMap.put("numberOfPages", numberOfPages);
        String numberOfVolumes = project.getNumberOfVolumes() != null ? project.getNumberOfVolumes().toString()
                : "null";
        orderedProjectMap.put("numberOfVolumes", numberOfVolumes);
        String archived = project.getProjectIsArchived() != null ? project.getProjectIsArchived().toString() : "null";
        orderedProjectMap.put("archived", archived);

        JSONObject projectObject = new JSONObject(orderedProjectMap);

        JSONArray processes = new JSONArray();
        List<Process> projectProcesses = project.getProcesses();
        for (Process process : projectProcesses) {
            JSONObject processObject = new JSONObject();
            processObject.put("id", process.getId().toString());
            processes.add(processObject);
        }
        projectObject.put("processes", processes);

        JSONArray users = new JSONArray();
        List<User> projectUsers = project.getUsers();
        for (User user : projectUsers) {
            JSONObject userObject = new JSONObject();
            userObject.put("id", user.getId().toString());
            users.add(userObject);
        }
        projectObject.put("users", users);

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
