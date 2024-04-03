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

package org.kitodo.production.services.image;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.xml.fileformats.FileFormat;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.production.enums.GenerationMode;
import org.kitodo.production.enums.ImageGeneratorStep;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.thread.TaskImageGeneratorThread;
import org.kitodo.production.thread.TaskScriptThread;

/**
 * A program that generates images using the image management interface. This
 * program is run by the {@link TaskImageGeneratorThread} when the user manually
 * initiates the creation of the images. If the images are generated when the
 * task is completed, this is done by the {@link TaskScriptThread}.
 */
public class ImageGenerator implements Runnable {
    private static final Logger logger = LogManager.getLogger(ImageGenerator.class);
    private final FileService fileService = ServiceManager.getFileService();
    private final ImageService imageService = ServiceManager.getImageService();

    /**
     * Output folders.
     */
    private final Collection<Subfolder> outputs;

    /**
     * Current position in list.
     */
    private int position;

    /**
     * Folder with source images.
     */
    private final Subfolder sourceFolder;

    /**
     * List of possible source images.
     */
    private List<Pair<String, URI>> sources;

    /**
     * Current step of the generation process.
     */
    private ImageGeneratorStep state;

    /**
     * Task in the TaskManager that runs this ImageGenerator.
     */
    private EmptyTask supervisor;

    /**
     * List of elements to be generated.
     */
    private final List<ContentToBeGenerated> contentToBeGenerated;

    /**
     * Variant of image generation, see there.
     */
    private final GenerationMode mode;

    /**
     * Creates a new image generator.
     *
     * @param sourceFolder
     *            image source folder
     * @param mode
     *            whether all, or only a few images are to be generated
     * @param outputs
     *            output folders to generate to
     */
    public ImageGenerator(Subfolder sourceFolder, GenerationMode mode, Collection<Subfolder> outputs) {
        this.sourceFolder = sourceFolder;
        this.mode = mode;
        this.outputs = outputs;
        this.state = ImageGeneratorStep.LIST_SOURCE_FOLDER;
        this.sources = Collections.emptyList();
        this.contentToBeGenerated = new LinkedList<>();
    }

    /**
     * Cleanup target folders if <i>all</i> output is generated.
     */
    public void removeGeneratedContent() {
        for (Subfolder subfolder : outputs) {
            for (URI uri : subfolder.listContents(true).values()) {
                try {
                    ServiceManager.getFileService().delete(uri);
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }

    /**
     * Appends the element to the list of elements to be generated.
     *
     * @param canonical
     *            the canonical part of the file name
     * @param sourceURI
     *            the source URI of the content to be generated
     * @param subfoldersWhoseContentsAreToBeGenerated
     *            subfolders whose contents are to be generated
     */
    public void addToContentToBeGenerated(String canonical, URI sourceURI,
            List<Subfolder> subfoldersWhoseContentsAreToBeGenerated) {

        contentToBeGenerated
                .add(new ContentToBeGenerated(canonical, sourceURI, subfoldersWhoseContentsAreToBeGenerated));
    }

    /**
     * Generates a set of derivatives.
     *
     * @param instruction
     *            Instruction, which pictures are to be generated. Left: image
     *            source, right: destination folder. The image source consists
     *            of the canonical part of the file name and the resolved file
     *            name for the source image. The canonical part of the file name
     *            is needed to calculate the corresponding file name in the
     *            destination folder. The type of derivative to be generated is
     *            defined in the properties of the destination folder.
     */
    public void createDerivatives(ContentToBeGenerated instruction) {
        try {
            for (Subfolder destinationFolder : instruction.getSubfoldersWhoseContentsAreToBeGenerated()) {
                generateDerivative(instruction.getSourceURI(), destinationFolder, instruction.getCanonical());
            }
        } catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Generates a derived image and saves it with the on-board tools of Java.
     * The image is created by the image management interface. Which method of
     * the interface is called and its parameters are determined in the
     * configuration of the folder. The same is true for the file type under
     * which Java stores the image.
     *
     * @param sourceImage
     *            reference to the image that serves as a template for the
     *            reproduction process
     * @param imageProperties
     *            folder settings define what an image is created
     * @param fileFormat
     *            the file format specifies how the image should be saved
     * @param destinationImage
     *            specifies the location where the image should be written
     * @throws IOException
     *             if an underlying disk operation fails
     */
    private void createImageWithImageIO(URI sourceImage, Folder imageProperties, FileFormat fileFormat,
            URI destinationImage) throws IOException {

        try (OutputStream outputStream = fileService.write(destinationImage)) {
            Image image = retrieveJavaImage(sourceImage, imageProperties);
            Optional<String> optionalFormatName = fileFormat.getFormatName();
            if (optionalFormatName.isPresent()) {
                ImageIO.write((RenderedImage) image, optionalFormatName.get(), outputStream);
            }
        }
    }

    /**
     * Determines the folders in which a derivative must be created. Because the
     * ModuleLoader does not work when invoked from a parallelStream(), we use a
     * classic loop here.
     *
     * @param canonical
     *            canonical part of the file name, to determine the file names
     *            in the destination folder
     * @return the images to be generated
     */
    public List<Subfolder> determineFoldersThatNeedDerivatives(String canonical) {
        List<Subfolder> foldersThatNeedDerivatives = new ArrayList<>(outputs.size());
        Predicate<? super Subfolder> requiresGeneration = mode.getFilter(canonical);
        for (Subfolder folder : outputs) {
            if (requiresGeneration.test(folder)) {
                foldersThatNeedDerivatives.add(folder);
            }
        }
        return foldersThatNeedDerivatives;
    }

    /**
     * Gets the file list from the content folder, converts it into the required
     * form, and stores it in the sources field.
     */
    public void determineSources() {
        Map<String, URI> contents = sourceFolder.listContents();
        Stream<Entry<String, URI>> contentsStream = contents.entrySet().stream();
        Stream<Pair<String, URI>> sourcesStream = contentsStream.map(lambda -> Pair.of(lambda.getKey(), lambda.getValue()));
        this.sources = sourcesStream.collect(Collectors.toList());
    }

    /**
     * Generates the derivative depending on the declared generator function.
     *
     * @param sourceImage
     *            source file
     * @param destinationImage
     *            path to the target file to be generated
     * @param canonical
     *            the canonical part of the file name
     * @throws IOException
     *             if filesystem I/O fails
     */
    private void generateDerivative(URI sourceImage, Subfolder destinationImage, String canonical)
            throws IOException {

        Folder imageProperties = destinationImage.getFolder();
        boolean isChangingDpi = imageProperties.getDpi().isPresent();
        boolean isGettingSizedWebImage = imageProperties.getImageSize().isPresent();

        Optional<Double> optionalDerivative = imageProperties.getDerivative();
        if (optionalDerivative.isPresent() && destinationImage.getFileFormat().getImageFileFormat().isPresent()) {
            imageService.createDerivative(sourceImage, optionalDerivative.get(), destinationImage.getUri(canonical),
                destinationImage.getFileFormat().getImageFileFormat().orElseThrow(IllegalStateException::new));
        } else if (isChangingDpi || isGettingSizedWebImage) {
            createImageWithImageIO(sourceImage, imageProperties, destinationImage.getFileFormat(),
                destinationImage.getUri(canonical));
        }
    }

    /**
     * Returns from contentToBeGenerated the item specified by position.
     *
     * @return the item specified by position
     */
    public ContentToBeGenerated getFromContentToBeGeneratedByPosition() {
        return contentToBeGenerated.get(position);
    }

    /**
     * Returns the current position in the list.
     *
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Returns the list of source images.
     *
     * @return the list of source images
     */
    public List<Pair<String, URI>> getSources() {
        return sources;
    }

    /**
     * Returns the list of image generation task descriptions.
     *
     * @return the list of image generation task descriptions
     */
    public List<ContentToBeGenerated> getContentToBeGenerated() {
        return contentToBeGenerated;
    }

    /**
     * Returns the enum constant inicating the variant of the image generator
     * task.
     *
     * @return the variant of the image generator task
     */
    public GenerationMode getMode() {
        return mode;
    }

    /**
     * If there is a supervisor, lets him take an action. Otherwise nothing
     * happens.
     *
     * @param action
     *            what the supervisor should do
     */
    public void letTheSupervisorDo(Consumer<EmptyTask> action) {
        if (Objects.nonNull(supervisor)) {
            action.accept(supervisor);
        }
    }

    /**
     * Invokes one of the three methods of the image management interface that
     * return a Java image. Which method is called and its parameters are
     * determined in the configuration of the folder.
     *
     * @param sourceImage
     *            address of the source image from which the derivative is to be
     *            calculated.
     * @param imageProperties
     *            configuration for the target image
     * @return an image in memory
     * @throws IOException
     *             if an underlying disk operation fails
     */
    private Image retrieveJavaImage(URI sourceImage, Folder imageProperties) throws IOException {
        Optional<Integer> optionalDpi = imageProperties.getDpi();
        Optional<Integer> optionalImageSize = imageProperties.getImageSize();
        if (optionalDpi.isPresent()) {
            return imageService.changeDpi(sourceImage, optionalDpi.get());
        } else if (optionalImageSize.isPresent()) {
            return imageService.getSizedWebImage(sourceImage, optionalImageSize.get());
        }
        throw new IllegalArgumentException(imageProperties + " does not give any method to create a java image");
    }

    /**
     * If the task is started, it will execute this run() method which will
     * start the export on the ExportDms. This task instance is passed in
     * addition so that the ExportDms can update the task’s state.
     *
     * @see org.kitodo.production.helper.tasks.EmptyTask#run()
     */
    @Override
    public void run() {
        do {
            state.accept(this);
            if (state.equals(ImageGeneratorStep.DETERMINE_WHICH_IMAGES_NEED_TO_BE_GENERATED) && position == -1
                    && sources.isEmpty()) {
                if (Objects.nonNull(supervisor)) {
                    supervisor.setProgress(100);
                    supervisor.setWorkDetail(Helper.getTranslation("noImagesToGenerate"));
                }
                return;
            }
            position++;
            setProgress();
            if (Objects.nonNull(supervisor) && supervisor.isInterrupted()) {
                return;
            }
        } while (!(state.equals(ImageGeneratorStep.GENERATE_IMAGES)
                && getPosition() == getContentToBeGenerated().size()));
        logger.info("Completed");
    }

    /**
     * Sets the current position in the list.
     *
     * @param position
     *            position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Calculates and reports the progress of the task.
     */
    private void setProgress() {
        if (Objects.nonNull(supervisor)) {
            int checked = state.equals(ImageGeneratorStep.GENERATE_IMAGES)
                    ? getMode().equals(GenerationMode.ALL) ? 1 : sources.size()
                    : 0;
            int generated = getMode().equals(GenerationMode.ALL)
                    && state.equals(ImageGeneratorStep.DETERMINE_WHICH_IMAGES_NEED_TO_BE_GENERATED) ? 0 : getPosition();
            int total = sources.size() + (getMode().equals(GenerationMode.ALL) ? 1 : getContentToBeGenerated().size())
                    + 1;
            supervisor.setProgress(100d * (1 + checked + generated) / total);
        }
    }

    /**
     * Sets the current processing state.
     *
     * @param state
     *            state to set
     */
    public void setState(ImageGeneratorStep state) {
        this.state = state;
    }

    /**
     * Set a supervisor for this activity. If a supervisor is set, the progress
     * is reported back to him, and he responds to his interrupt requests.
     *
     * @param supervisor
     *            supervisor task to set
     */
    public void setSupervisor(EmptyTask supervisor) {
        this.supervisor = supervisor;
    }
}
