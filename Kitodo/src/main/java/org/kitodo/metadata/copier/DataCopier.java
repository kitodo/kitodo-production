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

package org.kitodo.metadata.copier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A data copier is a class that can be parametrised to copy data in goobi
 * processes depending on rules.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class DataCopier {

    private static final Logger logger = LogManager.getLogger(DataCopier.class);

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
        List<String> commands = Arrays.asList(program.split(";"));
        rules = new ArrayList<>(commands.size());
        for (String command : commands) {
            rules.add(DataCopyrule.createFor(command));
        }
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
                logger.info("Rule not applicable for \"{}\", skipped: {}", data.getProcessTitle(), rule);
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
