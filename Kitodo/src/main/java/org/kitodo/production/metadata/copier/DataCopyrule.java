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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.kitodo.exceptions.MetadataException;

/**
 * The abstract class DataCopyrule defines method signatures to implement a rule
 * which may later be used to modify metadata depending on various conditions,
 * and provides a factory method to create the matching metadata copy rule
 * implementation from a given command string.
 */
public abstract class DataCopyrule {

    /**
     * The final Map AVAILABLE_RULES maps the operators of the available
     * metadata copyrules to their respective classes. If more metadata
     * copyrules are to be added to this implementation, they will have to be
     * listed named here.
     */
    private static final Map<String, Class<? extends DataCopyrule>> AVAILABLE_RULES = new HashMap<>(4);

    static {
        // FIXME: here is possible deadlock!
        AVAILABLE_RULES.put(ComposeFormattedRule.OPERATOR, ComposeFormattedRule.class);
        AVAILABLE_RULES.put(CopyIfMetadataIsAbsentRule.OPERATOR, CopyIfMetadataIsAbsentRule.class);
        AVAILABLE_RULES.put(OverwriteOrCreateRule.OPERATOR, OverwriteOrCreateRule.class);
    }

    /**
     * Factory method to create a class implementing the metadata copy rule
     * referenced by a given command string.
     *
     * @param arguments
     *            A list of strings consisting of subject (aka. patiens),
     *            operator (aka. agens) and (optional) objects (depending on
     *            what objects the operator requires).
     * @return a class implementing the metadata copy rule referenced
     * @throws ConfigurationException
     *             if the operator cannot be resolved or the number of arguments
     *             doesn’t match
     */
    public static DataCopyrule createFor(List<String> arguments) throws ConfigurationException {
        String operator;
        try {
            operator = arguments.get(1);
        } catch (IndexOutOfBoundsException e) {
            throw new ConfigurationException(
                    "Missing operator (second argument) in line: " + String.join(" ", arguments));
        }
        Class<? extends DataCopyrule> ruleClass = AVAILABLE_RULES.get(operator);
        if (ruleClass == null) {
            throw new ConfigurationException("Unknown operator: " + operator);
        }
        DataCopyrule ruleImplementation;
        try {
            ruleImplementation = ruleClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MetadataException(e.getMessage(), e);
        }
        ruleImplementation.setSubject(arguments.get(0));
        if (ruleImplementation.getMaxObjects() > 0) {
            List<String> objects = arguments.subList(2, arguments.size());
            if (objects.size() < ruleImplementation.getMinObjects()) {
                throw new ConfigurationException("Too few arguments in line: " + String.join(" ", arguments));
            }
            if (objects.size() > ruleImplementation.getMaxObjects()) {
                throw new ConfigurationException("Too many arguments in line: " + String.join(" ", arguments));
            }
            ruleImplementation.setObjects(objects);
        }
        return ruleImplementation;
    }

    /**
     * When called, the rule must be applied to the given fileformat.
     *
     * @param data
     *            data to apply yourself on
     */
    protected abstract void apply(CopierData data);

    /**
     * The function getMinObject must return the maximal number of objects
     * required by the rule to work as expected. If it returns 0, the
     * setObjects() method will not be called.
     *
     * @return the maximal number of objects required by the rule
     */
    protected abstract int getMaxObjects();

    /**
     * The function getMinObject must return the minimal number of objects
     * required by the rule to work as expected.
     *
     * @return the minimal number of objects required by the rule
     */
    protected abstract int getMinObjects();

    /**
     * The method is called to pass the rule its objects. The list
     * passed is reliable to the restrictions defined by getMinObjects() and
     * getMaxObjects().
     *
     * @param objects
     *            a list of objects to be used by the rule
     * @throws ConfigurationException
     *             may be thrown if one of the objects cannot be processed
     */
    protected abstract void setObjects(List<String> objects) throws ConfigurationException;

    /**
     * The method is called to pass the rule its subject.
     *
     * @param subject
     *            a subject to be used by the rule
     * @throws ConfigurationException
     *             may be thrown if the subject cannot be processed
     */
    protected abstract void setSubject(String subject) throws ConfigurationException;
}
