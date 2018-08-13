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

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.kitodo.api.filemanagement.FileManagementInterface;
import org.kitodo.api.imagemanagement.ImageFileFormat;
import org.kitodo.api.imagemanagement.ImageManagementInterface;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.serviceloader.KitodoServiceLoader;

/**
 * An encapsulation to access the generator properties of the folder.
 */
public class FolderGenerator {
    /**
     * Generator method that changes the DPI of an image.
     */
    private static final String CHANGE_DPI = "changeDpi";

    /**
     * Generator method that creates a derivative for an image.
     */
    private static final String CREATE_DERIVATIVE = "createDerivative";

    /**
     * Generator method that scales an image.
     */
    private static final String GET_SCALED_WEB_IMAGE = "getScaledWebImage";

    /**
     * Generator method that changes the size (in pixel) of the image.
     */
    private static final String GET_SIZED_WEB_IMAGE = "getSizedWebImage";

    /**
     * Multiplier used to display percentage.
     */
    private static final double PERCENT = 100.0d;

    /**
     * Image resoulution in DPI.
     */
    private int dpi = 300;

    /**
     * Scale factor.
     */
    private double factor = 1.00d;

    /**
     * {@code Folder.this}.
     */
    private final Folder folder;

    /**
     * Image width in pixels.
     */
    private int width = 150;

    /**
     * Creates a new generator for this folder.
     *
     * @param folder
     *            {@code Folder.this}
     */
    public FolderGenerator(Folder folder) {
        this.folder = folder;
    }

    /**
     * Generate a different image from an image.
     *
     * @param source
     *            image data source to read
     * @param canonical
     *            canonincal part of the image file name
     * @param fileFormat
     *            output file format to be used when generating derivatives,
     *            else may be empty
     * @param formatName
     *            name of output format to be used in other cases, may be empty
     *            when derivatives is generated
     * @param vars
     * @throws IOException
     *             if I/O fails
     */
    public void generate(URI source, String canonical, String extensionWithoutDot, Optional<ImageFileFormat> fileFormat,
            Optional<String> formatName, Map<String, String> vars) throws IOException {
        KitodoServiceLoader<ImageManagementInterface> imageManagementInterface = new KitodoServiceLoader<>(
                ImageManagementInterface.class);
        KitodoServiceLoader<FileManagementInterface> fileManagementInterface = new KitodoServiceLoader<>(
                FileManagementInterface.class);
        URI destination = folder.getURI(vars, canonical, extensionWithoutDot);

        switch (this.getMethod()) {
            case CHANGE_DPI:
                try (OutputStream outputStream = fileManagementInterface.loadModule().write(destination)) {
                    ImageIO.write(
                        (RenderedImage) imageManagementInterface.loadModule().changeDpi(source, folder.getDpi().get()),
                        formatName.get(), outputStream);
                }
                break;
            case CREATE_DERIVATIVE:
                imageManagementInterface.loadModule().createDerivative(source, folder.getDerivative().get(),
                    destination, fileFormat.get());
                break;
            case GET_SCALED_WEB_IMAGE:
                try (OutputStream outputStream = fileManagementInterface.loadModule().write(destination)) {
                    ImageIO.write((RenderedImage) imageManagementInterface.loadModule().getScaledWebImage(source,
                        folder.getImageScale().get()), formatName.get(), outputStream);
                }
                break;
            case GET_SIZED_WEB_IMAGE:
                try (OutputStream outputStream = fileManagementInterface.loadModule().write(destination)) {
                    ImageIO.write((RenderedImage) imageManagementInterface.loadModule().getSizedWebImage(source,
                        folder.getImageSize().get()), formatName.get(), outputStream);
                }
                break;
            default:
                throw new IllegalStateException("Illegal String value to switch: " + this.getMethod());
        }
    }

    /**
     * Returns the image resolution in DPI.
     *
     * @return the DPI
     */
    public int getDpi() {
        return dpi;
    }

    /**
     * Returns the scale factor to set.
     *
     * @return the scale factor
     */
    public double getFactor() {
        return factor * PERCENT;
    }

    /**
     * Returns the selected generator method.
     *
     * @return the generator methodd
     */
    public String getMethod() {
        if (folder.getDerivative().isPresent()) {
            return CREATE_DERIVATIVE;
        } else if (folder.getDpi().isPresent()) {
            return CHANGE_DPI;
        } else if (folder.getImageScale().isPresent()) {
            return GET_SCALED_WEB_IMAGE;
        } else if (folder.getImageSize().isPresent()) {
            return GET_SIZED_WEB_IMAGE;
        } else {
            return "";
        }
    }

    /**
     * Sets the image width in pixels.
     *
     * @return the image width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the image resolution in DPI.
     *
     * @param dpi
     *            resolution to set
     */
    public void setDpi(int dpi) {
        this.dpi = dpi;
        setMethod(getMethod());
    }

    /**
     * Sets the scale factor.
     *
     * @param factor
     *            scale factor to set
     */
    public void setFactor(double factor) {
        this.factor = factor / PERCENT;
        setMethod(getMethod());
    }

    /**
     * Sets the generator method.
     *
     * @param method
     *            method to set
     */
    public void setMethod(String method) {
        folder.setDerivative(method.equals(CREATE_DERIVATIVE) ? factor : null);
        folder.setDpi(method.equals(CHANGE_DPI) ? dpi : null);
        folder.setImageScale(method.equals(GET_SCALED_WEB_IMAGE) ? factor : null);
        folder.setImageSize(method.equals(GET_SIZED_WEB_IMAGE) ? width : null);
    }

    /**
     * Sets the image width in pixels.
     *
     * @param width
     *            image width
     */
    public void setWidth(int width) {
        this.width = width;
        setMethod(getMethod());
    }
}
