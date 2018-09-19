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

package org.kitodo.forms;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Project;

public class GeneratorSwitchTest {
    @Test
    public void getGeneratorSwitchesTest() {
        Project projectWithoutSourceFolder = new Project();
        Folder folderInProjectWithoutSourceFolder = new Folder();
        folderInProjectWithoutSourceFolder.setPath("folderInProjectWithoutSourceFolder");
        folderInProjectWithoutSourceFolder.setImageScale(1.0);
        projectWithoutSourceFolder.getFolders().add(folderInProjectWithoutSourceFolder);
        Collection<Project> projects = new ArrayList<>();
        projects.add(projectWithoutSourceFolder);

        Project project = new Project();
        List<Folder> projectFolders = project.getFolders();
        Folder sourceFolder = new Folder();
        sourceFolder.setPath("sourceFolder");
        sourceFolder.setImageScale(1.0);
        projectFolders.add(sourceFolder);
        project.setGeneratorSource(sourceFolder);
        Folder folderWhichHasNothingToBeGenerated = new Folder();
        folderWhichHasNothingToBeGenerated.setPath("folderWhichHasNothingToBeGenerated");
        projectFolders.add(folderWhichHasNothingToBeGenerated);
        Folder folderToBeGenerated = new Folder();
        folderToBeGenerated.setPath("folderToBeGenerated");
        folderToBeGenerated.setImageScale(1.0);
        projectFolders.add(folderToBeGenerated);
        projects.add(project);

        List<Folder> contentFolders = new ArrayList<>();

        List<GeneratorSwitch> testOutcome = GeneratorSwitch.getGeneratorSwitches(projects.stream(), contentFolders);
        List<String> switchesGenerated = testOutcome.parallelStream().map(λ -> λ.getLabel())
                .collect(Collectors.toList());

        assertThat(switchesGenerated, not(contains("folderInProjectWithoutSourceFolder")));
        assertThat(switchesGenerated, not(contains("sourceFolder")));
        assertThat(switchesGenerated, not(contains("folderWhichHasNothingToBeGenerated")));
        assertThat(switchesGenerated, contains("folderToBeGenerated"));
    }

    @Test
    public void getLabelTest() {
        Folder folderToBeGenerated = new Folder();
        folderToBeGenerated.setPath("folderToBeGenerated");
        List<Folder> contentFolders = new ArrayList<>();

        GeneratorSwitch generatorSwitch = new GeneratorSwitch(folderToBeGenerated, contentFolders);

        assertThat(generatorSwitch.getLabel(), is(equalTo(("folderToBeGenerated"))));
    }

    @Test
    public void isValueTest() {
        Folder folder = new Folder();
        List<Folder> contentFolders = new ArrayList<>();
        GeneratorSwitch generatorSwitch = new GeneratorSwitch(folder, contentFolders);
        assertThat(generatorSwitch.isValue(), is(equalTo((false))));
        contentFolders.add(folder);
        assertThat(generatorSwitch.isValue(), is(equalTo((true))));
        contentFolders.remove(folder);
        assertThat(generatorSwitch.isValue(), is(equalTo((false))));
    }

    @Test
    public void setValueTest() {
        Folder folder = new Folder();
        List<Folder> contentFolders = new ArrayList<>();
        GeneratorSwitch generatorSwitch = new GeneratorSwitch(folder, contentFolders);
        assertThat(contentFolders, not(contains(folder)));
        generatorSwitch.setValue(true);
        assertThat(contentFolders, contains(folder));
        generatorSwitch.setValue(false);
        assertThat(contentFolders, not(contains(folder)));
    }
}
