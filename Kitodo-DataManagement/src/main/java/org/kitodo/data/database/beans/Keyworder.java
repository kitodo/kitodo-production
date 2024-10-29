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

package org.kitodo.data.database.beans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.KitodoConfig;
import org.kitodo.data.database.enums.TaskStatus;

/**
 * Prepares the search keywords for a process or task.
 */
class Keyworder {
    private static final Logger logger = LogManager.getLogger(Keyworder.class);

    private static final Pattern TITLE_GROUPS_PATTERN = Pattern.compile("[\\p{IsLetter}\\p{Digit}]+");

    private static final String PSEUDOWORD_TASK_AUTOMATIC = "automatic";
    private static final String PSEUDOWORD_TASK_DONE = "closed";
    private static final String PSEUDOWORD_TASK_DONE_PROCESSING_USER = "closeduser";

    private static final String ANY_METADATA_MARKER = "mdWrap";
    private static final Map<String, String> DOMAIN_MAPPING = new HashMap<>();
    static {
        DOMAIN_MAPPING.put("dmdSec", "description");
        DOMAIN_MAPPING.put("sourceMD", "source");
        DOMAIN_MAPPING.put("techMD", "technical");
    }
    private static final char DOMAIN_SEPARATOR = 'j';
    private static final char VALUE_SEPARATOR = 'q';
    private static final Pattern METADATA_PATTERN = Pattern.compile("name=\"([^\"]+)\">([^<]*)<", Pattern.DOTALL);
    private static final Pattern METADATA_SECTIONS_PATTERN = Pattern.compile(
        "<mets:(dmdSec|sourceMD|techMD).*?o>(.*?)</kitodo:k", Pattern.DOTALL);
    private static final Pattern RULESET_KEY_PATTERN = Pattern.compile("key id=\"([^\"]+)\">(.*?)</key>",
        Pattern.DOTALL);
    private static final Pattern RULESET_LABEL_PATTERN = Pattern.compile("<label[^>]*>([^<]+)", Pattern.DOTALL);

    private static final Map<String, Map<String, Collection<String>>> rulesetCache = new HashMap<>();

    private Set<String> titleKeywords;
    private Set<String> projectKeywords;
    private Set<String> batchKeywords;
    private Set<String> taskKeywords;
    private Set<String> taskPseudoKeywords;
    private Set<String> metadataKeywords;
    private Set<String> metadataPseudoKeywords;
    private String processId;
    private Set<String> commentKeywords;

    public Keyworder(Process process) {
        this.titleKeywords = initTitleKeywords(process.getTitle());
        this.projectKeywords = initProjectKeywords(process.getProject().getTitle());
        this.batchKeywords = initBatchKeywords(process.getBatches());
        var taskKeywords = initTaskKeywords(process.getTasks());
        this.taskKeywords = taskKeywords.getLeft();
        this.taskPseudoKeywords = taskKeywords.getRight();
        var metadataKeywords = initMetadataKeywords(process);
        this.metadataKeywords = metadataKeywords.getLeft();
        this.metadataPseudoKeywords = metadataKeywords.getRight();
        this.processId = process.getId().toString();
        this.commentKeywords = initCommentKeywords(process.getComments());

        if (logger.isTraceEnabled()) {
            logTrace("process \"" + process.getTitle() + "\" [" + processId + ']');
        }
    }

    public Keyworder(Task task) {
        this.titleKeywords = initTitleKeywords(task.getProcess().getTitle());
        this.projectKeywords = initProjectKeywords(task.getProcess().getProject().getTitle());
        this.batchKeywords = initBatchKeywords(task.getProcess().getBatches());
        var taskKeywords = initTaskKeywords(Collections.singleton(task));
        this.taskKeywords = taskKeywords.getLeft();
        this.taskPseudoKeywords = taskKeywords.getRight();
        var metadataKeywords = initMetadataKeywords(task.getProcess());
        this.metadataKeywords = metadataKeywords.getLeft();
        this.metadataPseudoKeywords = metadataKeywords.getRight();
        this.processId = task.getProcess().getId().toString();
        List<Comment> commentsOfTask = task.getProcess().getComments().stream().filter(comment -> Objects.equals(comment
                .getCurrentTask(), task) || Objects.equals(comment.getCorrectionTask(), task)).collect(Collectors
                        .toList());
        this.commentKeywords = initCommentKeywords(commentsOfTask);

        if (logger.isTraceEnabled()) {
            logTrace("task #" + task.getOrdering() + " \"" + task.getTitle() + "\" [" + task.getId() + "] of process \""
                    + task.getProcess().getTitle() + "\" [" + processId + "]");
        }
    }

    /**
     * Creates search terms for process titles. These are created in groups
     * aligned to the left so that the beginning of the title is found.
     * 
     * @param processTitle
     *            the title of the process
     * @return keywords
     */
    private static Set<String> initTitleKeywords(String processTitle) {
        Set<String> tokens = new HashSet<>();
        Matcher matcher = TITLE_GROUPS_PATTERN.matcher(processTitle);
        while (matcher.find()) {
            String normalized = normalize(matcher.group());
            int length = normalized.length();
            for (int end = 1; end <= length; end++) {
                tokens.add(normalized.substring(0, end));
            }
        }
        return tokens;
    }

    /**
     * Makes the keywords for searching by project title. These are just single
     * words.
     * 
     * @param projectTitle
     *            the title of the project
     * @return keywords
     */
    private static final Set<String> initProjectKeywords(String projectTitle) {
        Set<String> tokens = new HashSet<>();
        for (String term : splitValues(projectTitle)) {
            tokens.add(normalize(term));
        }
        return tokens;
    }

    /**
     * Generates the search terms by batch. Note that batch title can be
     * optional. Batch ID is not indexed because ID search is done via the
     * database.
     * 
     * @param batches
     *            batches containing the process
     * @return batch search terms
     */
    private static final Set<String> initBatchKeywords(Collection<Batch> batches) {
        if (batches.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> tokens = new HashSet<>();
        for (Batch batch : batches) {
            String optionalTitle = batch.getTitle();
            if (StringUtils.isNotBlank(optionalTitle)) {
                tokens.addAll(initTitleKeywords(optionalTitle));
            }
        }
        return tokens;
    }

    /**
     * Generates all search terms by task and pseudo search terms.
     * 
     * @param tasks
     *            tasks for the words to be generated
     * @return search terms and pseudo search terms
     */
    private static final Pair<Set<String>, Set<String>> initTaskKeywords(Collection<Task> tasks) {
        Set<String> taskKeywords = new HashSet<>();
        Set<String> taskPseudoKeywords = new HashSet<>();
        for (Task task : tasks) {
            for (String token : splitValues(task.getTitle())) {
                String term = normalize(token);
                taskKeywords.add(term);
                if (task.isTypeAutomatic()) {
                    taskKeywords.add(PSEUDOWORD_TASK_AUTOMATIC + VALUE_SEPARATOR + token);
                }
                TaskStatus taskStatus = task.getProcessingStatus();
                if (Objects.isNull(taskStatus)) {
                    continue;
                }
                if (Objects.equals(taskStatus, TaskStatus.DONE)) {
                    taskPseudoKeywords.add(PSEUDOWORD_TASK_DONE + VALUE_SEPARATOR + token);
                    User closedUser = task.getProcessingUser();
                    if (Objects.isNull(closedUser)) {
                        continue;
                    }
                    if (StringUtils.isNotBlank(closedUser.getName())) {
                        taskPseudoKeywords.add(PSEUDOWORD_TASK_DONE_PROCESSING_USER + VALUE_SEPARATOR + normalize(
                            closedUser.getName()));
                    }
                    if (StringUtils.isNotBlank(closedUser.getSurname())) {
                        taskPseudoKeywords.add(PSEUDOWORD_TASK_DONE_PROCESSING_USER + VALUE_SEPARATOR + normalize(
                            closedUser.getSurname()));
                    }
                } else {
                    taskPseudoKeywords.add(taskStatus.toString().toLowerCase() + VALUE_SEPARATOR + token);
                }
            }
        }
        return Pair.of(taskKeywords, taskPseudoKeywords);
    }

    /**
     * Generates all metadata keywords and pseudowords for metadata of a METS
     * file KITODO-metadata.
     * 
     * @param process
     *            process of the METS file
     * @return metadata keywords, and metadata pseudo keywords
     */
    private static final Pair<Set<String>, Set<String>> initMetadataKeywords(Process process) {
        try {
            String processId = Integer.toString(process.getId());
            File metaFilePath = Paths.get(KitodoConfig.getKitodoDataDirectory(), processId, "meta.xml").toFile();
            logger.debug("Reading {} ...", metaFilePath);
            String metaXml = FileUtils.readFileToString(metaFilePath, StandardCharsets.UTF_8);
            if (!metaXml.contains(ANY_METADATA_MARKER)) {
                return Pair.of(Collections.emptySet(), Collections.emptySet());
            }
            Set<String> metadataKeywords = new HashSet<>();
            Set<String> metadataPseudoKeywords = new HashSet<>();
            Map<String, Collection<String>> rulesetLabelMap = getRulesetLabelMap(process.getRuleset().getFile());
            Matcher metadataSectionsMatcher = METADATA_SECTIONS_PATTERN.matcher(metaXml);
            while (metadataSectionsMatcher.find()) {
                String domain = DOMAIN_MAPPING.get(metadataSectionsMatcher.group(1));
                Matcher keyMatcher = METADATA_PATTERN.matcher(metadataSectionsMatcher.group(2));
                while (keyMatcher.find()) {
                    String key = normalize(keyMatcher.group(1));
                    String valueString = keyMatcher.group(2);
                    for (String singleValue : splitValues(valueString)) {
                        String value = normalize(singleValue);
                        metadataKeywords.add(value);
                        metadataPseudoKeywords.add(domain + VALUE_SEPARATOR + value);
                        metadataPseudoKeywords.add(key + VALUE_SEPARATOR + value);
                        metadataPseudoKeywords.add(key + DOMAIN_SEPARATOR + domain + VALUE_SEPARATOR + value);
                        for (String label : rulesetLabelMap.getOrDefault(key, Collections.emptyList())) {
                            metadataPseudoKeywords.add(label + VALUE_SEPARATOR + value);
                            metadataPseudoKeywords.add(label + DOMAIN_SEPARATOR + domain + VALUE_SEPARATOR + value);
                        }
                    }
                }
            }
            return Pair.of(metadataKeywords, metadataPseudoKeywords);
        } catch (IOException | RuntimeException e) {
            logger.catching(e instanceof FileNotFoundException ? Level.INFO : Level.WARN, e);
            return Pair.of(Collections.emptySet(), Collections.emptySet());
        }
    }

    /**
     * Returns a map for ruleset key translations. A cache is used, but if there
     * is nothing in cache, the ruleset is parsed and the map is created. Since
     * Kitodo-DataEditor is not available here, we have to do this directly, and
     * it also increases performance massively.
     * 
     * @param file
     *            indicates a ruleset
     * @return a map
     */
    private static Map<String, Collection<String>> getRulesetLabelMap(String file) {
        Map<String, Collection<String>> rulesetLabelMap = rulesetCache.get(file);
        if (Objects.nonNull(rulesetLabelMap)) {
            return rulesetLabelMap;
        }
        try {
            File rulesetFile = Paths.get(KitodoConfig.getParameter("directory.rulesets"), file).toFile();
            logger.debug("Reading {} ...", rulesetFile);
            String ruleset = FileUtils.readFileToString(rulesetFile, StandardCharsets.UTF_8);
            rulesetLabelMap = new HashMap<>();
            Matcher keysMatcher = RULESET_KEY_PATTERN.matcher(ruleset);
            Set<String> labels = new HashSet<>();
            while (keysMatcher.find()) {
                String key = normalize(keysMatcher.group(1));
                Matcher labelMatcher = RULESET_LABEL_PATTERN.matcher(keysMatcher.group(2));
                while (labelMatcher.find()) {
                    labels.add(normalize(labelMatcher.group(1)));
                }
                rulesetLabelMap.put(key, labels);
            }
            rulesetCache.put(file, rulesetLabelMap);
            return rulesetLabelMap;
        } catch (IOException | RuntimeException e) {
            logger.catching(Level.WARN, e);
            return Collections.emptyMap();
        }
    }

    /**
     * Creates the keywords for searching in correction messages. This is a
     * double-truncated search, i.e. any substring.
     * 
     * @param comments
     *            the comments of a process
     * @return keywords
     */
    private static final Set<String> initCommentKeywords(List<Comment> comments) {
        Set<String> tokens = new HashSet<>();
        for (Comment comment : comments) {
            String message = comment.getMessage();
            if (StringUtils.isNotBlank(message)) {
                for (String splitWord : splitValues(message)) {
                    String word = normalize(splitWord);
                    for (int i = 0; i < word.length(); i++) {
                        for (int j = i + 1; j <= word.length(); j++) {
                            tokens.add(word.substring(i, j));
                        }
                    }
                }
            }
        }
        return tokens;
    }

    /**
     * Converts the string to lowercase and removes special characters.
     * 
     * @param string
     *            string to clean
     * @return clean string in lowercase
     */
    private static String normalize(String string) {
        return string.toLowerCase().replaceAll("[\0-/:-`{-¿]", "");
    }

    /**
     * Splits the values ​​of a string at special characters. Groups of letters
     * and numbers written together are not split.
     * 
     * @param value
     *            string to split
     * @return groups
     */
    private static List<String> splitValues(String value) {
        String initializedValue = value != null ? value : "";
        return Arrays.asList(initializedValue.split("[ ,\\-._]+"));
    }

    /**
     * Returns the search keywords for the free search. These are search
     * keywords for title terms, the title of the project, names of assigned
     * batches, task names, and metadata.
     * 
     * @return search keywords for the free search
     */
    public String getSearch() {
        Set<String> freeKeywords = new HashSet<>();
        freeKeywords.addAll(titleKeywords);
        freeKeywords.addAll(projectKeywords);
        freeKeywords.addAll(batchKeywords);
        freeKeywords.addAll(taskKeywords);
        freeKeywords.addAll(metadataKeywords);
        freeKeywords.add(processId);
        freeKeywords.addAll(commentKeywords);
        return String.join(" ", freeKeywords);
    }

    /**
     * Returns the search keywords for the title search. The title is sequenced
     * in a meaningful way to achieve meaningful hits even with the substring
     * search.
     * 
     * <p>
     * A process with title "PineSeve_313539383" would be searchable as: p, pi,
     * pin, pine, pines, pinese, pinesev, pineseve, 3, 31, 313, 3135, 31353,
     * 313539, 3135393, 31353938, 313539383.
     * 
     * @return search keywords for the title
     */
    public String getSearchTitle() {
        return String.join(" ", titleKeywords);
    }

    /**
     * Returns the search keywords for the project name search. These are the
     * words from the project name in normalized form.
     * 
     * @return search keywords for the project
     */
    public String getSearchProject() {
        return String.join(" ", projectKeywords);
    }

    /**
     * Returns the search keywords for searching for operations assigned to a
     * batch. The same splitting criteria apply as for the title.
     * 
     * @return search keywords for batches
     */
    public String getSearchBatch() {
        return String.join(" ", batchKeywords);
    }

    /**
     * Returns search keywords for finding tasks. This uses pseudowords to
     * particularly powerfully search the various task states.
     * 
     * <p>
     * Given an automated task called "OCR" and the task is running, it
     * generates the tokens: {@code ocr}, {@code automaticqocr},
     * {@code inworkqocr}.
     * 
     * <p>
     * If a task "Quality Assurance" is finished and it was processed by John
     * Doe, the token results: {@code quality}, {@code assurance},
     * {@code closedqquality}, {@code closedqassurance},
     * {@code closeduserqjohn}, {@code closeduserqdoe}.
     * 
     * @return search keywords for tasks
     */
    public String getSearchTask() {
        Set<String> allTaskKeywords = new HashSet<>();
        allTaskKeywords.addAll(taskKeywords);
        allTaskKeywords.addAll(taskPseudoKeywords);
        return String.join(" ", allTaskKeywords);
    }

    /**
     * Returns search keywords for searching the metadata. These are both the
     * bare terms and pseudowords to particularly powerfully search the various
     * metadata and domains.
     * 
     * <p>
     * Suppose there is a metadata in a {@code dmdSec} with the key
     * "TitleDocMain" and a value containing the string "Berlin,
     * Charlottenburg". In the ruleset, "TitleDocMain" is translated as "Maint
     * title" and "Hauptsachtitel". This produces the following pseudo search
     * terms in addition to the search term "berlin":
     * <ul>
     * <li>{@code descriptionqberlin} - for search "workpiece:berlin"</li>
     * <li>{@code titledocmainqberlin} - for search "TitleDocMain:berlin"</li>
     * <li>{@code mainttitleqberlin} - for search "Maint title:berlin"</li>
     * <li>{@code hauptsachtitelqberlin} - for search
     * "Hauptsachtitel:berlin"</li>
     * <li>{@code titledocmainqberlin} - for search
     * "workpiece:TitleDocMain:berlin"</li>
     * <li>{@code mainttitleqberlin} - for search "workpiece:Maint
     * title:berlin"</li>
     * <li>{@code hauptsachtitelqberlin} - for search
     * "workpiece:Hauptsachtitel:berlin"</li>
     * <li>and, formed according to the same scheme, {@code charlottenburg} and
     * its psodowords.
     * </ul>
     * This means that if a user searches for "Maint title:Berlin,
     * Charlottenburg", the index has to search for:
     * {@code mainttitleqberlin mainttitleqcharlottenburg}.
     * 
     * @return search keywords for searching the metadata
     */
    public String getSearchMetadata() {
        Set<String> allMetadataKeywords = new HashSet<>();
        allMetadataKeywords.addAll(metadataKeywords);
        allMetadataKeywords.addAll(metadataPseudoKeywords);
        return String.join(" ", allMetadataKeywords);
    }

    private void logTrace(String about) {
        logger.trace("Search keywords for {}:/- title words: {}/- project words: {}/- batch words: {}/"
                + "- task words: {}/- task pseudo-words: {}/- metadata words: {}/- metadata pseudo-words: {}/"
                + "- process id word: {}/- comment words: {}".replace("/", System.lineSeparator()), about,
                String.join(", ", titleKeywords), String.join(", ", projectKeywords), String.join(", ", batchKeywords),
                String.join(", ", taskKeywords), String.join(", ", taskPseudoKeywords),
                String.join(", ", metadataKeywords), String.join(", ", metadataPseudoKeywords),
                processId, String.join(", ", commentKeywords));
    }
}
