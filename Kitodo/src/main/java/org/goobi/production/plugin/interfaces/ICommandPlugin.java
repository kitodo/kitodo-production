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

package org.goobi.production.plugin.interfaces;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.goobi.production.cli.CommandResponse;

public interface ICommandPlugin extends IPlugin {

    void setParameterMap(HashMap<String, String> parameterMap);

    CommandResponse validate();

    CommandResponse execute();

    CommandResponse help();

    boolean usesHttpSession();

    void setHttpResponse(HttpServletResponse resp);

    void setHttpRequest(HttpServletRequest resp);

}
