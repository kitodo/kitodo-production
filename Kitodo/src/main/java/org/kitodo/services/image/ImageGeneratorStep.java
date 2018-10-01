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

package org.kitodo.services.image;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.kitodo.helper.Helper;
import org.kitodo.model.UseFolder;

/**
 * Enumerates the steps to go to generate images.
 */
public enum ImageGeneratorStep implements Consumer<ImageGenerator> {
    /**
     * First step, get the list of images in the folder of source images.
     */
    LIST_SOURCE_FOLDER {
        @Override
        public void accept(ImageGenerator imageGenerator) {
            imageGenerator.letTheSupervisor(λ -> λ.setWorkDetail(Helper.getTranslation("listSourceFolder")));
            imageGenerator.determineSources();
            imageGenerator.setState(DETERMINE_WHICH_IMAGES_NEED_TO_BE_GENERATED);
            imageGenerator.setPosition(-1);

        }
    },

    /**
     * Second step, (if demanded) check if the image exists in the destination
     * folder, and optionally validate the image file content for not being
     * corrupted.
     */
    DETERMINE_WHICH_IMAGES_NEED_TO_BE_GENERATED {
        @Override
        public void accept(ImageGenerator imageGenerator) {
            Pair<String, URI> source = imageGenerator.getSources().get(imageGenerator.getPosition());
            if (!imageGenerator.getVariant().equals(GenerationMode.ALL)) {
                imageGenerator.letTheSupervisor(λ -> λ.setWorkDetail(
                    Helper.getTranslation("determineWhichImagesNeedToBeGenerated", Arrays.asList(source.getKey()))));
            }

            List<UseFolder> generations = imageGenerator.determineFoldersThatNeedDerivatives(source.getKey());
            if (!generations.isEmpty()) {
                imageGenerator.addToToBeGenerated(Pair.of(source, generations));
            }
            if (imageGenerator.getPosition() == imageGenerator.getSources().size() - 1) {
                imageGenerator.setState(GENERATE_IMAGES);
                imageGenerator.setPosition(-1);
            }
        }
    },

    /**
     * Third step, generate whatever needs to be generated.
     */
    GENERATE_IMAGES {
        @Override
        public void accept(ImageGenerator imageGenerator) {
            Pair<Pair<String, URI>, List<UseFolder>> instructuon = imageGenerator.getFromToBeGeneratedByPosition();
            imageGenerator.letTheSupervisor(λ -> λ.setWorkDetail(
                Helper.getTranslation("generateImages", Arrays.asList(instructuon.getKey().getKey()))));
            LogManager.getLogger(ImageGeneratorStep.class).info("Generating ".concat(instructuon.toString()));
            imageGenerator.createDerivatives(instructuon);
            if (imageGenerator.getPosition() == imageGenerator.getToBeGenerated().size() - 1) {
                imageGenerator.letTheSupervisor(λ -> λ.setProgress(100));
                return;
            }
        }
    };
}
