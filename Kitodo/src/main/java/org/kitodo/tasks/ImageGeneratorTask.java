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

package org.kitodo.tasks;

import static org.kitodo.tasks.ImageGeneratorStep.DETERMINE_WHICH_IMAGES_NEED_TO_BE_GENERATED;
import static org.kitodo.tasks.ImageGeneratorStep.GENERATE_IMAGES;
import static org.kitodo.tasks.ImageGeneratorStep.LIST_SOURCE_FOLDER;
import static org.kitodo.tasks.ImageGeneratorTaskVariant.ALL_IMAGES;

import de.sub.goobi.helper.tasks.EmptyTask;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.helper.Helper;

public class ImageGeneratorTask extends EmptyTask {
    private static final Logger logger = LogManager.getLogger(ImageGeneratorTask.class);

    /**
     * Folder with source images.
     */
    Folder sourceFolder;

    /**
     * List of possible source images.
     */
    List<Pair<String, URI>> sources;

    /**
     * Current step of the generation process.
     */
    ImageGeneratorStep state;

    /**
     * List of elements to be generated.
     */
    List<Pair<Pair<String, URI>, List<Folder>>> toBeGenerated;

    /**
     * Output folders.
     */
    List<Folder> contentFolders;

    /**
     * Current position in list.
     */
    int position;

    /**
     * Variant of image generation, see there.
     */
    ImageGeneratorTaskVariant variant;

    /**
     * Variables to be replaced in the path.
     */
    Map<String, String> vars;

    /**
     * Creates a new process title.
     *
     * @param processtitle
     *            process title to generate
     * @param sourceFolder
     *            image source folder
     * @param variant
     *            variant of image generation
     * @param contentFolders
     *            output folders to generate to
     */
    public ImageGeneratorTask(String processtitle, Folder sourceFolder, ImageGeneratorTaskVariant variant,
            List<Folder> contentFolders) {
        super(processtitle);
        this.sourceFolder = sourceFolder;
        this.variant = variant;
        this.contentFolders = contentFolders;
        this.state = LIST_SOURCE_FOLDER;
        this.sources = Collections.emptyList();
        this.toBeGenerated = new LinkedList<>();
        this.vars = new HashMap<>();
        vars.put("processtitle", processtitle);
    }

    /**
     * Creates a copy of this thread, to continue it after it was terminated.
     *
     * @param master
     *            stopped thread
     */
    public ImageGeneratorTask(ImageGeneratorTask master) {
        super(master);
        this.sourceFolder = master.sourceFolder;
        this.variant = master.variant;
        this.contentFolders = master.contentFolders;
        this.state = master.state;
        this.position = master.position;
        this.sources = master.sources;
        this.toBeGenerated = master.toBeGenerated;
        this.vars = master.vars;
    }

    /**
     * Returns the display name of the task to show to the user.
     *
     * @see de.sub.goobi.helper.tasks.INameableTask#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return Helper.getTranslation("ImageGeneratorTask");
    }

    /**
     * If the task is started, it will execute this run() method which will
     * start the export on the ExportDms. This task instance is passed in
     * addition so that the ExportDms can update the task’s state.
     *
     * @see de.sub.goobi.helper.tasks.EmptyTask#run()
     */
    @Override
    public void run() {
        try {
            do {
                state.accept(this);
                position++;
                setProgress();
                if (isInterrupted()) {
                    return;
                }
            } while (!(state.equals(GENERATE_IMAGES) && position == toBeGenerated.size()));
            logger.info("Completed");
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            setException(e);
        }
    }

    private void setProgress() {
        int before = (state.equals(GENERATE_IMAGES) ? variant.equals(ALL_IMAGES) ? 1 : sources.size() : 0)
                + (variant.equals(ALL_IMAGES) && state.equals(DETERMINE_WHICH_IMAGES_NEED_TO_BE_GENERATED) ? 0
                        : position);
        int all = sources.size() + (variant.equals(ALL_IMAGES) ? 1 : toBeGenerated.size()) + 1;
        super.setProgress(100d * (before + 1) / all);
    }

    /**
     * Calls the clone constructor to create a not yet executed instance of this
     * thread object. This is necessary for threads that have terminated in
     * order to render possible to restart them.
     *
     * @return a not-yet-executed replacement of this thread
     * @see de.sub.goobi.helper.tasks.EmptyTask#replace()
     */
    @Override
    public ImageGeneratorTask replace() {
        return new ImageGeneratorTask(this);
    }
}
