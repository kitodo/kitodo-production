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

import static org.kitodo.tasks.ImageGeneratorTaskVariant.ALL_IMAGES;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.kitodo.api.filemanagement.FileManagementInterface;
import org.kitodo.api.imagemanagement.ImageManagementInterface;
import org.kitodo.config.xml.fileformats.FileFormat;
import org.kitodo.config.xml.fileformats.FileFormatsConfig;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.helper.Helper;
import org.kitodo.serviceloader.KitodoServiceLoader;

/**
 * Enumerates the steps to go to generate images.
 */
public enum ImageGeneratorStep implements Consumer<ImageGenerator> {
    /**
     * First step, get the list of images in the folder of source images.
     */
    LIST_SOURCE_FOLDER {
        @Override
        public void accept(ImageGenerator generatorTask) {
            try {
                generatorTask.setWorkDetail(Helper.getTranslation("listSourceFolder"));
                generatorTask.sources = generatorTask.sourceFolder
                        .listContents(generatorTask.vars,
                            FileFormatsConfig.getFileFormat(generatorTask.sourceFolder.getMimeType()).get()
                                    .getExtension(false))
                        .entrySet().stream().map(λ -> Pair.of(λ.getKey(), λ.getValue())).collect(Collectors.toList());
                generatorTask.state = DETERMINE_WHICH_IMAGES_NEED_TO_BE_GENERATED;
                generatorTask.position = -1;
            } catch (IOException | JAXBException e) {
                throw new UndeclaredThrowableException(e);
            }
        }
    },

    /**
     * Second step, (if demanded) check if the image exists in the destination
     * folder, and optionally validate the image file content for not being
     * corrupted.
     */
    DETERMINE_WHICH_IMAGES_NEED_TO_BE_GENERATED {
        @Override
        public void accept(ImageGenerator generatorTask) {
            Pair<String, URI> source = generatorTask.sources.get(generatorTask.position);
            if (!generatorTask.variant.equals(ALL_IMAGES)) {
                generatorTask.setWorkDetail(
                    Helper.getTranslation("determineWhichImagesNeedToBeGenerated", Arrays.asList(source.getKey())));
            }

            /*
             * The generation variant MISSING_OR_DAMAGED_IMAGES uses the image
             * validation module to check whether image files are damaged. For
             * reasons unknown, the ModuleLoader does not work when invoked from
             * a parallelStream(). That's why we use a classic loop here. This
             * could be parallelized after the underlying problem has been
             * resolved.
             */
            List<Folder> generations = new ArrayList<Folder>(generatorTask.outputs.size());
            Predicate<? super Folder> requiresGeneration = generatorTask.variant.getFilter(generatorTask.vars,
                source.getKey());
            for (Folder folder : generatorTask.outputs) {
                if (requiresGeneration.test(folder)) {
                    generations.add(folder);
                }
            }

            if (!generations.isEmpty()) {
                generatorTask.toBeGenerated.add(Pair.of(source, generations));
            }

            if (generatorTask.position == generatorTask.sources.size() - 1) {
                generatorTask.state = GENERATE_IMAGES;
                generatorTask.position = -1;
            }
        }
    },

    /**
     * Third step, generate whatever needs to be generated.
     */
    GENERATE_IMAGES {
        @Override
        public void accept(ImageGenerator generatorTask) {
            try {
                Pair<Pair<String, URI>, List<Folder>> generation = generatorTask.toBeGenerated
                        .get(generatorTask.position);
                generatorTask.setWorkDetail(
                    Helper.getTranslation("generateImages", Arrays.asList(generation.getKey().getKey())));
                LogManager.getLogger(ImageGeneratorStep.class).info("Generating ".concat(generation.toString()));

                /*
                 * The image generation uses the image management module to
                 * generate the images. For reasons unknown, the ModuleLoader
                 * does not work when invoked from a parallelStream(). That's
                 * why we use a classic loop here. This could be parallelized
                 * after the underlying problem has been resolved.
                 */
                for (Folder folder : generation.getRight()) {
                    FileFormat fileFormat = FileFormatsConfig.getFileFormat(folder.getMimeType()).get();
                    Pair<String, URI> dataSource = generation.getLeft();

                    KitodoServiceLoader<ImageManagementInterface> imageManagementServiceLoader = new KitodoServiceLoader<>(
                            ImageManagementInterface.class);
                    KitodoServiceLoader<FileManagementInterface> fileManagementServiceLoader = new KitodoServiceLoader<>(
                            FileManagementInterface.class);
                    URI destination = folder.getURI(generatorTask.vars, dataSource.getKey(),
                        fileFormat.getExtension(false));

                    if (folder.getDerivative().isPresent()) {
                        imageManagementServiceLoader.loadModule().createDerivative(dataSource.getValue(),
                            folder.getDerivative().get(), destination, fileFormat.getImageFileFormat().get());
                    } else if (folder.getDpi().isPresent()) {
                        try (OutputStream outputStream = fileManagementServiceLoader.loadModule().write(destination)) {
                            ImageIO.write(
                                (RenderedImage) imageManagementServiceLoader.loadModule()
                                        .changeDpi(dataSource.getValue(), folder.getDpi().get()),
                                fileFormat.getFormatName().get(), outputStream);
                        }
                    } else if (folder.getImageScale().isPresent()) {
                        try (OutputStream outputStream = fileManagementServiceLoader.loadModule().write(destination)) {
                            ImageIO.write(
                                (RenderedImage) imageManagementServiceLoader.loadModule()
                                        .getScaledWebImage(dataSource.getValue(), folder.getImageScale().get()),
                                fileFormat.getFormatName().get(), outputStream);
                        }
                    } else if (folder.getImageSize().isPresent()) {
                        try (OutputStream outputStream = fileManagementServiceLoader.loadModule().write(destination)) {
                            ImageIO.write(
                                (RenderedImage) imageManagementServiceLoader.loadModule()
                                        .getSizedWebImage(dataSource.getValue(), folder.getImageSize().get()),
                                fileFormat.getFormatName().get(), outputStream);
                        }
                    }

                }

                if (generatorTask.position == generatorTask.toBeGenerated.size() - 1) {
                    generatorTask.setProgress(100);
                    return;
                }
            } catch (IOException | JAXBException e) {
                throw new UndeclaredThrowableException(e);
            }
        }
    };
}
