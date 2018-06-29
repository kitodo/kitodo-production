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

package org.kitodo.longtimepreservationvalidationmodule;

import edu.harvard.hul.ois.jhove.App;
import edu.harvard.hul.ois.jhove.JhoveBase;
import edu.harvard.hul.ois.jhove.Module;

import java.net.URI;

import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.longtimepreservation.FileType;
import org.kitodo.api.validation.longtimepreservation.LongTimePreservationValidationInterface;
import org.kitodo.config.Config;

/**
 * An LongTimePreservationValidationInterface implementation using Jhove.
 */
public class LongTimePreservationValidationModule implements LongTimePreservationValidationInterface {
    /**
     * Path to the {@code jhove.conf} file
     *
     * <p>
     * Example:
     * LongTimePreservationValidationModule.jhoveConf=/var/lib/tomcat8/webapps/kitodo-production/WEB-INF/classes/jhove.conf
     */
    private static final String PARAMETER_JHOVE_CONF = "LongTimePreservationValidationModule.jhoveConf";

    /**
     * Returns the matching module name for the given file type.
     *
     * @param fileType
     *            file type that a module name shall be returned for
     * @return the matching module name
     */
    private String determineModuleName(FileType fileType) {
        switch (fileType) {
            case GIF:
                return "GIF-hul";
            case JPEG:
                return "JPEG-hul";
            case JPEG_2000:
                return "JPEG2000-hul";
            case PDF:
                return "PDF-hul";
            case PNG:
                return "PNG-gdm";
            case TIFF:
                return "TIFF-hul";
            default:
                throw new IllegalStateException("Complete switch");
        }
    }

    /**
     * {@inheritDoc}<!-- . -->
     *
     * @param fileUri
     *            file URI to validate
     * @param fileType
     *            file type to validate
     */
    @Override
    public ValidationResult validate(URI fileUri, FileType fileType) {
        KitodoOutputHandler result = new KitodoOutputHandler();
        try {
            JhoveBase jhoveBase = new JhoveBase();
            jhoveBase.init(Config.getParameter(PARAMETER_JHOVE_CONF, "src/main/resources/jhove.conf"),
                JhoveBase.getSaxClassFromProperties());
            App app = App.newAppWithName("Jhove");
            Module module = jhoveBase.getModule(determineModuleName(fileType));
            String[] dirFileOrUri = new String[] {fileUri.getPath() };
            jhoveBase.dispatch(app, module, /* OutputHandler aboutHandler */ null, result,
                /* String outputFile */ null, dirFileOrUri);
        } catch (Exception e) {
            result.treatException(e);
        }
        return result.toValidationResult();
    }

}
