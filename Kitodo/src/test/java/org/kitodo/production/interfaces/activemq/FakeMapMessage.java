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

package org.kitodo.production.interfaces.activemq;

import java.util.*;

import org.mockito.invocation.*;
import org.mockito.stubbing.*;

public class FakeMapMessage implements Answer<Object> {

    private static final HashMap<String, Object> data = new HashMap<>();

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        Object[] parameters = invocation.getArguments();
        switch (invocation.getMethod().getName()) {
            case "getInt":
            case "getString":
            case "getObject":
                return data.get(parameters[0]);

            case "setInt":
            case "setString":
            case "setObject":
                data.put((String) parameters[0], parameters[1]);
                return null;

            case "itemExists":
                return data.containsKey(parameters[0]);

            default:
                throw new UnsupportedOperationException();
        }
    }

}
