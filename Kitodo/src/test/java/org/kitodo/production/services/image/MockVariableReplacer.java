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

package org.kitodo.production.services.image;

import org.kitodo.production.helper.VariableReplacer;

/**
 * A replacement for the variable replacer, which simply replaces the process
 * title.
 *
 * <p>
 * The variable replacer fetches the process data directory from the process
 * service. If the latter does not find a process base URI in the process object
 * in the JUnit integration test, it would try to determine it via the file
 * service and then would want to store it in the process object in the
 * database. This cannot work in the JUnit integration test and also should not
 * happen because the objects that the test runs on are defined in the test
 * method and do not exist in the database. Therefore, this mocked variable
 * replacer is stored in the use folder by the test method in the JUnit
 * integration test. To make this possible, the variable replacer must be stored
 * in a class field and no local variable can be used.
 */
public class MockVariableReplacer extends VariableReplacer {

    private final String processtitle;

    MockVariableReplacer(String processtitle) {
        super(null, null, null);
        this.processtitle = processtitle;
    }

    @Override
    public String replace(String inString) {
        return inString.replace("(processtitle)", processtitle);
    }
}
