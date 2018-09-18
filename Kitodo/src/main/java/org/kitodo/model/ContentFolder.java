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

package org.kitodo.model;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Transient;

import org.apache.myfaces.util.FilenameUtils;
import org.kitodo.api.filemanagement.FileManagementInterface;
import org.kitodo.config.Config;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.helper.LexicographicalOrder;
import org.kitodo.serviceloader.KitodoServiceLoader;

/**
 * Stores configuration settings regarding a type of sub-folder in the process
 * directories of processes in a project.
 *
 * <p>
 * Typically, a folder has a corresponding sub-directory in the process
 * directory of each process and a {@code <fileGrp>} structure in the produced
 * METS file. The assumption is, that each folder contains the same number of
 * files with the same names, except for the file extension, which can vary.
 * This structure is used to represent different versions of the same object (a
 * small low quality JPEG thumbnail, a high quality JPEG, an OCR-processed PDF,
 * etc.) Each version is represented by one {@code Folder} object.
 *
 * <p>
 * The sub-directory can be located by appending the value of {@link #path} to
 * the path to the process directory. The {@code <fileGrp>} structure has the
 * {@code USE} attribute set to the value of {@link #fileGroup}. It contains
 * links to the files contained in the directory. The links are formed by
 * concatenating the {@link #urlStructure} with the simple name of the file.
 *
 * <p>
 * However, a {@code Folder} can also only exist on the drive without being
 * exported to METS. Or, it can exist only virtually without correspondence on a
 * drive, just to produce the METS {@code <fileGrp>} structure.
 */
public class ContentFolder {
    private Folder folderBean;

    public ContentFolder(Folder bean) {
        folderBean = bean;
    }

    private static final BinaryOperator<URI> LATEST_URI = (previous, latest) -> latest;
    private static final Supplier<TreeMap<String, URI>> MAP_FACTORY = () -> new TreeMap<>(new LexicographicalOrder());

    private static String replaceInString(String string, Map<String, String> replacements) {
        for (Entry<String, String> replacement : replacements.entrySet()) {
            string = string.replace('(' + replacement.getKey() + ')', replacement.getValue());
        }
        return string;
    }

    /**
     * Returns the path to the folder.
     *
     * @return the path
     */
    public String getRelativePath(Map<String, String> vars) {
        int lastDelimiter = folderBean.getPath().lastIndexOf(File.separatorChar);
        return replaceInString(folderBean.getPath().substring(lastDelimiter + 1).indexOf('*') > -1
                ? lastDelimiter >= 0 ? folderBean.getPath().substring(0, lastDelimiter) : ""
                : folderBean.getPath(),
            vars);
    }

    /**
     * Returns the URI to a file in this folder.
     *
     * @param vars
     *            a map of the variable assignments of the variables that can
     *            occur in the {@link #path}
     * @param canonical
     *            the canonical part of the file name. The canonical part is the
     *            part that is different from one file to another in the folder,
     *            but equal between two files representing the same content in
     *            two different folders. A typical canonical part could be
     *            “00000001”.
     * @param extensionWithoutDot
     *            filename extension without dot, to be read from the
     *            configuration. The extension can be retrieved from the
     *            configuration based on the MIME type, but reading the
     *            configuration is part of the core module, so it cannot be done
     *            here and must be returned here from the caller. Typically,
     *            this function has to be called like this: <code>folder<!--
     *            -->.getURI(vars, canonical, FileFormatsConfig<!--
     *            -->.getFileFormat(folder.getMimeType()).get()<!--
     *            -->.getExtension(false))</code>
     * @return composed URI
     */
    @Transient
    public URI getURI(Map<String, String> vars, String canonical, String extensionWithoutDot) {
        int lastSeparator = folderBean.getPath().lastIndexOf(File.separatorChar);
        String lastSegment = folderBean.getPath().substring(lastSeparator + 1);
        String projectId = folderBean.getProject().getId().toString();
        if (lastSegment.indexOf('*') == -1) {
            String localName = canonical + '.' + extensionWithoutDot;
            return Paths.get(Config.getKitodoDataDirectory(), projectId, replaceInString(folderBean.getPath(), vars),
                localName).toUri();
        } else {
            String realPath = folderBean.getPath().substring(0, lastSeparator);
            String localName = lastSegment.replaceFirst("\\*", canonical).replaceFirst("\\*$", extensionWithoutDot);
            if (realPath.isEmpty()) {
                return Paths.get(Config.getKitodoDataDirectory(), projectId, localName).toUri();
            } else {
                return Paths.get(Config.getKitodoDataDirectory(), projectId, replaceInString(realPath, vars), localName)
                        .toUri();
            }
        }
    }

    /**
     * Returns the URI to a file in this folder if that file does exist.
     *
     * @param vars
     *            a map of the variable assignments of the variables that can
     *            occur in the {@link #path}
     * @param canonical
     *            the canonical part of the file name. The canonical part is the
     *            part that is different from one file to another in the folder,
     *            but equal between two files representing the same content in
     *            two different folders. A typical canonical part could be
     *            “00000001”.
     * @param extensionWithoutDot
     *            filename extension without dot, to be read from the
     *            configuration. The extension can be retrieved from the
     *            configuration based on the MIME type, but reading the
     *            configuration is part of the core module, so it cannot be done
     *            here and must be returned here from the caller. Typically,
     *            this function has to be called like this: <code>folder<!--
     *            -->.getURIIfExists(vars, canonical, FileFormatsConfig<!--
     *            -->.getFileFormat(folder.getMimeType()).get()<!--
     *            -->.getExtension(false))</code>
     * @return URI, or empty
     */
    @Transient
    public Optional<URI> getURIIfExists(Map<String, String> vars, String canonical, String extensionWithoutDot) {
        URI uri = getURI(vars, canonical, extensionWithoutDot);
        return new File(uri.getPath()).isFile() ? Optional.of(uri) : Optional.empty();
    }

    /**
     * Returns a map of canonical file name parts to URIs with all files
     * actually contained in this folder. The canonical part is the part that is
     * different from one file to another in the folder, but equal between two
     * files representing the same content in two different folders. A typical
     * canonical part could be “00000001”.
     *
     * @param extensionWithoutDot
     *            filename extension without dot, to be read from the
     *            configuration. The extension can be retrieved from the
     *            configuration based on the mimeType, but reading the
     *            configuration is part of the core module, so it cannot be done
     *            here and must be returned here from the caller.
     * @return map of canonical file name parts to URIs
     */
    public Map<String, URI> listContents(Map<String, String> vars, String extensionWithoutDot) {
        int lastSeparator = folderBean.getPath().lastIndexOf(File.separatorChar);
        String lastSegment = folderBean.getPath().substring(lastSeparator + 1);
        String projectId = folderBean.getProject().getId().toString();
        URI root;
        String pattern;
        int firstStar = lastSegment.indexOf('*');
        if (firstStar == -1) {
            root = Paths.get(Config.getKitodoDataDirectory(), projectId, replaceInString(folderBean.getPath(), vars))
                    .toUri();
            pattern = "(.*)\\." + Pattern.quote(extensionWithoutDot);
        } else {
            String realPath = replaceInString(folderBean.getPath().substring(0, lastSeparator), vars);
            root = (realPath.isEmpty() ? Paths.get(Config.getKitodoDataDirectory(), projectId)
                    : Paths.get(Config.getKitodoDataDirectory(), projectId, realPath)).toUri();
            StringBuilder patternBuilder = new StringBuilder();
            if (firstStar > 0) {
                patternBuilder.append(Pattern.quote(lastSegment.substring(0, firstStar)));
            }
            patternBuilder.append("(.*)");
            if (firstStar < lastSegment.length() - 1) {
                patternBuilder.append(
                    Pattern.quote(lastSegment.substring(firstStar + 1).replaceFirst("\\*$", extensionWithoutDot)));
            }
            pattern = patternBuilder.toString();
        }
        KitodoServiceLoader<FileManagementInterface> fileManagementInterface = new KitodoServiceLoader<>(
                FileManagementInterface.class);
        final Pattern compiledPattern = Pattern.compile(pattern);
        FilenameFilter filter = (dir, name) -> compiledPattern.matcher(name).matches();
        Stream<URI> relativeURIs = fileManagementInterface.loadModule().getSubUris(filter, root).parallelStream();
        Stream<URI> absoluteURIs = relativeURIs
                .map(λ -> new File(FilenameUtils.concat(Config.getKitodoDataDirectory(), λ.getPath())).toURI());
        return absoluteURIs.collect(Collectors.toMap(new Function<URI, String>() {
            @Override
            public String apply(URI uri) {
                Matcher matcher = compiledPattern.matcher(FilenameUtils.getName(uri.getPath()));
                matcher.matches();
                return matcher.group(1);
            }
        }, Function.identity(), LATEST_URI, MAP_FACTORY));
    }
}
