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

package org.kitodo.production.services.ocr;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;

public class OcrdWorkflowService {

    private static final Logger logger = LogManager.getLogger(OcrdWorkflowService.class);
    private static volatile OcrdWorkflowService instance = null;

    /**
     * Return singleton variable of type OcrdWorkflowService.
     *
     * @return unique instance of OcrdWorkflowService
     */
    public static OcrdWorkflowService getInstance() {
        OcrdWorkflowService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (OcrdWorkflowService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new OcrdWorkflowService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Get list of OCR-D workflows from directory.
     * <p>
     * The files are relative paths to the OCR-D workflow directory.
     * </p>
     *
     * @return list of OCR-D workflows
     */
    public List<Pair<?, ?>> getOcrdWorkflows() {
        String ocrdWorkflowsDirectoryConfig = ConfigCore.getParameterOrDefaultValue(ParameterCore.OCRD_WORKFLOWS_DIR);
        if (StringUtils.isNotEmpty(ocrdWorkflowsDirectoryConfig)) {
            Path ocrdWorkflowsDirectory = Path.of(ocrdWorkflowsDirectoryConfig);
            if (Files.isDirectory(ocrdWorkflowsDirectory)) {
                try (Stream<Path> ocrProfilePaths = Files.walk(ocrdWorkflowsDirectory, FileVisitOption.FOLLOW_LINKS)) {
                    return ocrProfilePaths.filter(Files::isRegularFile).map(Path::toAbsolutePath).sorted()
                            .map(path -> getImmutablePairFromPath(path, ocrdWorkflowsDirectory))
                            .collect(Collectors.toList());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return new ArrayList<>();
    }

    private static Pair<?, ?> getImmutablePairFromPath(Path filePath, Path ocrdWorkflowsDirectory) {
        String path = filePath.toString();
        path = path.replace(ocrdWorkflowsDirectory.toString(), StringUtils.EMPTY);
        path = StringUtils.removeStart(path, "/");
        return ImmutablePair.of(path, path);
    }

    /**
     * Get an OCR-D workflow by identifier.
     *
     * @param ocrdWorkflowId The OCR-D workflow identifier
     * @return The OCR-D workflow
     */
    public Pair<?, ?> getOcrdWorkflow(String ocrdWorkflowId) {
        if (StringUtils.isNotEmpty(ocrdWorkflowId)) {
            return getOcrdWorkflows().stream().filter(pair -> pair.getKey().equals(ocrdWorkflowId)).findFirst()
                    .orElse(null);
        }
        return null;
    }

}
