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

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.tasks.EmptyTask;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.xml.fileformats.FileFormat;
import org.kitodo.config.xml.fileformats.FileFormatsConfig;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.exceptions.UnknownCaseException;

public class ImageGeneratorTask extends EmptyTask {
    private static final Logger logger = LogManager.getLogger(ImageGeneratorTask.class);

    /**
     * Folder with source images.
     */
    private Folder sourceFolder;

    /**
     * List of possible source images.
     */
    private List<Pair<String, URI>> sources;

    /**
     * Current step of the generation process.
     */
    private ImageGeneratorStep state;

    /**
     * List of elements to be generated.
     */
    private List<Pair<Pair<String, URI>, List<Folder>>> toBeGenerated;

    /**
     * Output folders.
     */
    private List<Folder> outputs;

    /**
     * Current position in list.
     */
    private int position;

    /**
     * Variant of image generation, see there.
     */
    private ImageGeneratorTaskVariant variant;

    /**
     * Variables to be replaced in the path.
     */
    private Map<String, String> vars;

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
     * Creates a copy of this thread, to continue it after it was terminated.
     *
     * @param master
     *            stopped thread
     */
    public ImageGeneratorTask(ImageGeneratorTask master) {
        super(master);
        this.sourceFolder = master.sourceFolder;
        this.variant = master.variant;
        this.outputs = master.outputs;
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
                switch (state) {
                    case LIST_SOURCE_FOLDER:
                        super.setWorkDetail(Helper.getTranslation("listSourceFolder"));
                        sources = sourceFolder
                                .listContents(vars,
                                    FileFormatsConfig.getFileFormat(sourceFolder.getMimeType()).get()
                                            .getExtension(false))
                                .entrySet().stream().map(λ -> Pair.of(λ.getKey(), λ.getValue()))
                                .collect(Collectors.toList());
                        state = DETERMINE_WHICH_IMAGES_NEED_TO_BE_GENERATED;
                        position = -1;
                        break;

                    case DETERMINE_WHICH_IMAGES_NEED_TO_BE_GENERATED:
                        Pair<String, URI> source = sources.get(position);
                        if (!variant.equals(ALL_IMAGES)) {
                            super.setWorkDetail(Helper.getTranslation("determineWhichImagesNeedToBeGenerated",
                                Arrays.asList(source.getKey())));
                        }
                        List<Folder> generations = outputs.parallelStream()
                                .filter(variant.getFilter(vars, source.getKey())).collect(Collectors.toList());
                        if (!generations.isEmpty()) {
                            toBeGenerated.add(Pair.of(source, generations));
                        }

                        if (position == sources.size() - 1) {
                            state = GENERATE_IMAGES;
                            position = -1;
                        }
                        break;

                    case GENERATE_IMAGES:
                        Pair<Pair<String, URI>, List<Folder>> generation = toBeGenerated.get(position);
                        super.setWorkDetail(
                            Helper.getTranslation("generateImages", Arrays.asList(generation.getKey().getKey())));
                        for (Folder folder : generation.getRight()) {
                            FileFormat fileFormat = FileFormatsConfig.getFileFormat(folder.getMimeType()).get();
                            Pair<String, URI> dataSource = generation.getLeft();
                            folder.getGenerator().generate(dataSource.getValue(), dataSource.getKey(),
                                fileFormat.getExtension(false), fileFormat.getImageFileFormat(),
                                fileFormat.getFormatName(), vars);
                        }

                        if (position == toBeGenerated.size() - 1) {
                            super.setProgress(100);
                            return;
                        }
                        break;

                    default:
                        throw new UnknownCaseException(ImageGeneratorStep.class, state);
                }
                position++;
                setProgress();
                if (isInterrupted()) {
                    return;
                }
            } while (!(state.equals(GENERATE_IMAGES) && position == toBeGenerated.size()));
        } catch (RuntimeException | IOException | JAXBException e) {
            logger.error(e.getMessage(), e);
            setException(e);
        }
    }

    private void setProgress() {
        int numerator = (state.equals(GENERATE_IMAGES) ? variant.equals(ALL_IMAGES) ? 1 : sources.size() : 0)
                + (variant.equals(ALL_IMAGES) && state.equals(DETERMINE_WHICH_IMAGES_NEED_TO_BE_GENERATED) ? 0
                        : position)
                + 1;
        int denominator = sources.size() + (variant.equals(ALL_IMAGES) ? 1 : toBeGenerated.size()) + 1;
        super.setProgress(100d * numerator / denominator);
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
