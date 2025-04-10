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

package org.kitodo.longtermpreservationvalidation.jhove;

import edu.harvard.hul.ois.jhove.App;
import edu.harvard.hul.ois.jhove.JhoveBase;
import edu.harvard.hul.ois.jhove.JhoveException;
import edu.harvard.hul.ois.jhove.Module;
import edu.harvard.hul.ois.jhove.RepInfo;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.validation.longtermpreservation.FileType;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionInterface;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationError;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResult;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResultState;
import org.kitodo.longtermpreservationvalidation.conditions.LtpValidationConditionEvaluator;

/**
 * A programmatically initializable {@code JHoveBase} class.
 */
public class KitodoJhoveBase {

    private static final Logger logger = LogManager.getLogger(KitodoJhoveBase.class);

    /**
     * Returns the matching module name for the given file type.
     */
    private static final Map<FileType, String> MODULE_NAMES_BY_FILE_TYPE = Map.ofEntries(
        Map.entry(FileType.GIF, "GIF-hul"),
        Map.entry(FileType.JPEG, "JPEG-hul"),
        Map.entry(FileType.JPEG_2000, "JPEG2000-hul"),
        Map.entry(FileType.PDF, "PDF-hul"),
        Map.entry(FileType.PNG, "PNG-gdm"),
        Map.entry(FileType.TIFF, "TIFF-hul")
    );

    /**
     * Modules to initialize.
     */
    private static final List<String> JHOVE_MODULE_CLASSES = Arrays.asList(
        "edu.harvard.hul.ois.jhove.module.GifModule",
        "edu.harvard.hul.ois.jhove.module.Jpeg2000Module", 
        "edu.harvard.hul.ois.jhove.module.JpegModule",
        "edu.harvard.hul.ois.jhove.module.PdfModule", 
        "com.mcgath.jhove.module.PngModule",
        "edu.harvard.hul.ois.jhove.module.TiffModule"
    );

    private static final App app = App.newAppWithName("JHOVE");

    private static JhoveBase base = null;

    private static final JhoveBase initBase() {
        logger.debug("initialize jhove base class and load jhove modules");
        try {
			JhoveBase base = new JhoveBase();
            base.setEncoding("utf-8");
            for(String moduleClass : JHOVE_MODULE_CLASSES) {
                try {
                    Class<?> cl = Class.forName(moduleClass);
                    Module module = (Module) cl.getDeclaredConstructor().newInstance();
                    module.setDefaultParams(Collections.emptyList());
                    base.getModuleMap().put(module.getName().toLowerCase(), module);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
			return base;
		} catch (JhoveException e) {
			throw new IllegalStateException("Couldn't initialise JHOVE base", e);
		}
    }

    private static JhoveBase getJhoveBase() {
        if (Objects.isNull(base)) {
            base = initBase();
        }
        return base;
    }


    /**
     * Return Kitodo file types that are supported to be validated by Jhove.
     * @return
     */
    public static Set<FileType> getSupportedFileTypes() {
        return MODULE_NAMES_BY_FILE_TYPE.keySet();
    }

    public static List<String> getListOfProperties(FileType fileType) {
        if (fileType.equals(FileType.TIFF)) {
            return Arrays.asList(new String[] {
                "valid", 
                "wellFormed", 
                "imageWidth", 
                "imageLength"
            });
        }
        return Collections.emptyList();
    }

    /**
     * Validates a file.
     *
     * @param filepath
     *            filepath of file that is validated
     * @param moduleName
     *            name of validation module
     * @param handler
     *            handler to write the output to
     * @throws Exception
     *             if something goes wrong
     */
    public static LtpValidationResult validate(String filepath, FileType fileType, List<? extends LtpValidationConditionInterface> conditions) {
        if (!getSupportedFileTypes().contains(fileType)) {
            return new LtpValidationResult(
                LtpValidationResultState.ERROR, 
                Collections.singletonList(LtpValidationError.FILE_TYPE_NOT_SUPPORTED)
            );
        }

        Module module = getJhoveBase().getModule(MODULE_NAMES_BY_FILE_TYPE.get(fileType));
        RepInfo info = new RepInfo(filepath);
        File file = new File(filepath);

        if (!file.exists()) {
            return new LtpValidationResult(
                LtpValidationResultState.ERROR, 
                Collections.singletonList(LtpValidationError.FILE_NOT_FOUND)
            );
        }

        if (!file.canRead()) {
            return new LtpValidationResult(
                LtpValidationResultState.ERROR, 
                Collections.singletonList(LtpValidationError.IO_ERROR)
            );
        }

        try {
            base.processFile(app, module, false, file, info);
        } catch (Exception e) {
            logger.error("exception while validating with jhove", e);
            return new LtpValidationResult(
                LtpValidationResultState.ERROR, 
                Collections.singletonList(LtpValidationError.UNKNOWN_ERROR),
                Collections.emptyList(),
                Collections.singletonList("Jhove Exception: " + e.getMessage())
            );
        }

        Map<String, String> properties = KitodoJhoveRepInfoParser.repInfoToPropertyMap(info);
        List<LtpValidationConditionResult> conditionResults = LtpValidationConditionEvaluator.evaluateValidationConditions(conditions, properties);

        return new LtpValidationResult(
            LtpValidationConditionEvaluator.summarizeValidationState(conditions, conditionResults), 
            Collections.emptyList(), 
            conditionResults, 
            KitodoJhoveRepInfoParser.repInfoMessagesToStringList(info)
        );
    }
}
