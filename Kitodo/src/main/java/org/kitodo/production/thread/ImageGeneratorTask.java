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

package org.kitodo.production.thread;

import de.sub.goobi.helper.tasks.EmptyTask;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.helper.Helper;
import org.kitodo.tasks.ImageGenerator;
import org.kitodo.tasks.ImageGeneratorTaskVariant;

/**
 * Performs creating images in the task manager, when the operator clicks on it
 * manually.
 */
public class ImageGeneratorTask extends EmptyTask {
    private static final Logger logger = LogManager.getLogger(ImageGeneratorTask.class);

    /**
     * Image generator to be run by this thread.
     */
    private ImageGenerator imageGenerator;

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
    public ImageGeneratorTask(String processtitle, Folder sourceFolder, ImageGeneratorTaskVariant variant,
            List<Folder> outputs) {
        super(processtitle);
        this.imageGenerator = new ImageGenerator(processtitle, sourceFolder, variant, outputs);
    }

    /**
     * Creates a copy of this thread, to continue it after it was terminated.
     *
     * @param master
     *            stopped thread
     */
    public ImageGeneratorTask(ImageGeneratorTask master) {
        super(master);
        this.imageGenerator = master.imageGenerator;
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
            imageGenerator.setWorker(this);
            imageGenerator.run();
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            setException(e);
        }
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
