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
import org.kitodo.production.thread.ImageGeneratorTask;
import org.kitodo.production.thread.TaskScriptThread;

/**
 * A program that generates images using the image management interface. This
 * program is run by the {@link ImageGeneratorTask} when the user manually
 * initiates the creation of the images. If the images are generated when the
 * task is completed, this is done by the {@link TaskScriptThread}.
 */
public class ImageGenerator implements Runnable {
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
    List<Folder> outputs;

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

    EmptyTask worker;

    /**
     * Creates a new process title.
     *
     * @param processtitle
     *            process title to generate
     * @param sourceFolder
     *            image source folder
     * @param variant
     *            variant of image generation
     * @param outputs
     *            output folders to generate to
     */
    public ImageGenerator(String processtitle, Folder sourceFolder, ImageGeneratorTaskVariant variant,
            List<Folder> outputs) {
        this.sourceFolder = sourceFolder;
        this.variant = variant;
        this.outputs = outputs;
        this.state = LIST_SOURCE_FOLDER;
        this.sources = Collections.emptyList();
        this.toBeGenerated = new LinkedList<>();
        this.vars = new HashMap<>();
        vars.put("processtitle", processtitle);
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
        do {
            state.accept(this);
            position++;
            setProgress();
            if (worker.isInterrupted()) {
                return;
            }
        } while (!(state.equals(GENERATE_IMAGES) && position == toBeGenerated.size()));
        logger.info("Completed");
    }

    private void setProgress() {
        int before = (state.equals(GENERATE_IMAGES) ? variant.equals(ALL_IMAGES) ? 1 : sources.size() : 0)
                + (variant.equals(ALL_IMAGES) && state.equals(DETERMINE_WHICH_IMAGES_NEED_TO_BE_GENERATED) ? 0
                        : position);
        int all = sources.size() + (variant.equals(ALL_IMAGES) ? 1 : toBeGenerated.size()) + 1;
        if (worker != null) {
            worker.setProgress(100d * (before + 1) / all);
        }
    }

    public void setWorker(EmptyTask worker) {
        this.worker = worker;
    }

    public void setWorkDetail(String translation) {
        if (worker != null) {
            worker.setWorkDetail(translation);
        }
    }

    public void setProgress(int i) {
        if (worker != null) {
            worker.setProgress(i);
        }
    }
}
