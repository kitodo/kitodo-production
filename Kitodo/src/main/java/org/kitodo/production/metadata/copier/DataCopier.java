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

package org.kitodo.production.metadata.copier;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A data copier is a class that can be parametrised to copy data in processes
 * depending on rules.
 */
public class DataCopier {

    private static final Logger logger = LogManager.getLogger(DataCopier.class);

    private static final Pattern DATA_COPY_RULES_PARSE_PATTERN = Pattern.compile("[^\\s\"']+|\"[^\"]*\"");

    /**
     * Holds the rules this data copier can apply to a set of working data.
     */
    private final List<DataCopyrule> rules;

    /**
     * Creates a new DataCopier.
     *
     * @param program
     *            a semicolon-separated list of expressions defining rules to
     *            apply to the metadata
     * @throws ConfigurationException
     *             may be thrown if the program is syntactically wrong
     */
    public DataCopier(String program) throws ConfigurationException {
        List<List<String>> commands = parseDataCopyRules(program);
        rules = new ArrayList<>(commands.size());
        for (List<String> command : commands) {
            rules.add(new DataCopyrule(command));
        }
    }

    /**
     * Parses a string containing one or more data copy rules. Several rules are
     * separated by semicolon. This is especially done to handle white space
     * between quotes correctly.
     *
     * @param input
     *            string of copy data expressions
     * @return list of rules, where each rule is a list of tokens the rule
     *         consists of
     */
    private static List<List<String>> parseDataCopyRules(String input) {
        List<List<String>> output = new ArrayList<>();
        List<String> sequence = new ArrayList<>();
        Matcher matcher = DATA_COPY_RULES_PARSE_PATTERN.matcher(input);
        while (matcher.find()) {
            boolean endOfSequence = false;
            String token = matcher.group();
            if (token.endsWith(";")) {
                endOfSequence = true;
                token = token.substring(0, token.length() - 1);
            }
            if (!token.isEmpty()) {
                sequence.add(token);
            }
            System.out.println(token);
            if (endOfSequence) {
                output.add(sequence);
                sequence = new ArrayList<>();
            }
        }
        if (!sequence.isEmpty()) {
            output.add(sequence);
        }
        return output;
    }

    /**
     * Applies the rules defined by the “program” passed to the constructor onto
     * a given dataset.
     *
     * @param data
     *            a data object to work on
     */
    public void process(CopierData data) {
        for (DataCopyrule rule : rules) {
            try {
                rule.apply(data);
            } catch (RuntimeException notApplicable) {
                logger.info("Rule not applicable for \"{}\", skipped: {}", data.getProcess().getTitle(), rule);
            }
        }
    }

    /**
     * Returns a string that textually represents this data copier.
     *
     * @return a string representation of this data copier.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return rules.toString();
    }
}
