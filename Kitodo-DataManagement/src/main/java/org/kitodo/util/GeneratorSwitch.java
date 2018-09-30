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

package org.kitodo.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;

/**
 * An encapsulation to access the content creation properties of a task from
 * JSF.
 *
 * <p>
 * The content creation properties of a task are stored internally differently
 * than they are displayed in the user interface. The user interface displays a
 * switch for each folder, for which a generator function has been configured.
 * Internally, however, the configuration of the activated generator functions
 * is stored in a list of the task in which all folders are contained, in which
 * the generator function is activated, that is, the switch is selected.
 * Conversely, the task knows nothing about which non-selected folders with
 * configured generator function there.
 */
public class GeneratorSwitch {
    /**
     * Folder represented by this generator switch.
     */
    private Folder folder;

    /**
     * Modifyable list containing enabled generators. This list is member of the
     * {@link Task} and saves the generator state when the task is saved.
     */
    private List<Folder> contentFolders;

    /**
     * Returns a list of generator switches for all folders whose contents can
     * be generated.
     *
     * @param projects
     *            stream of projects this task is used in
     * @param contentFolders
     *            modifiable list of folders whose contents are to be generated
     * @return list of GeneratorSwitch objects or empty list
     */
    public static List<GeneratorSwitch> getGeneratorSwitches(Stream<Project> projects, List<Folder> contentFolders) {

        //TODO: find more meaningful name
        // Ignore all projects that do not have a source folder configured.
        Stream<Project> projectsWithSourceFolder = projects.filter(lambda -> Objects.nonNull(lambda.getGeneratorSource()));

        Stream<Folder> allowedFolders = dropOwnSourceFolders(projectsWithSourceFolder);

        // Remove all folders to generate which do not have anything to generate
        // configured.
        Stream<Folder> generatableFolders = allowedFolders.filter(lambda -> lambda.getDerivative().isPresent()
                || lambda.getDpi().isPresent() || lambda.getImageScale().isPresent() || lambda.getImageSize().isPresent());

        // For all remaining folders, create an encapsulation to access the
        // generator properties of the folder.
        Stream<GeneratorSwitch> taskGenerators = generatableFolders.map(lambda -> new GeneratorSwitch(lambda, contentFolders));

        return taskGenerators.collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Drop all folders to generate if they are their own source folder.
     *
     * @param projects
     *            projects whose folders allowed to be generated are to be
     *            determined
     * @return a stream of folders that are allowed to be generated
     */
    private static Stream<Folder> dropOwnSourceFolders(Stream<Project> projects) {
        Stream<Pair<Folder, Folder>> foldersWithSources = projects
                .flatMap(lambda -> lambda.getFolders().stream().map(mi -> Pair.of(mi, lambda.getGeneratorSource())));
        return foldersWithSources.filter(lambda -> !lambda.getLeft().equals(lambda.getRight())).map(lambda -> lambda.getLeft());
    }

    /**
     * Creates a new generator for this task.
     *
     * @param folder
     *            folder represented by this toggle switch
     * @param contentFolders
     *            modifiable list of enabled toggle switches
     */
    public GeneratorSwitch(Folder folder, List<Folder> contentFolders) {
        this.folder = folder;
        this.contentFolders = contentFolders;
    }

    /**
     * Returns a label for the folder.
     *
     * @return a label for the folder
     */
    public String getLabel() {
        return folder.toString();
    }

    /**
     * Returns the toggle switch value by looking into the list of folders to
     * generate.
     *
     * @return the value for the toggle switch
     */
    public boolean isValue() {
        return contentFolders.contains(folder);
    }

    /**
     * Sets the boolean value by updating the list of folders to generate.
     *
     * @param value
     *            value to set
     */
    public void setValue(boolean value) {
        if (!value) {
            contentFolders.remove(folder);
        } else if (!contentFolders.contains(folder)) {
            contentFolders.add(folder);
        }
    }
}
