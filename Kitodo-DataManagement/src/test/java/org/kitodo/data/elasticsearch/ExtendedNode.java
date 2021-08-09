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

package org.kitodo.data.elasticsearch;

import java.util.Collection;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;

/**
 * Class extends standard Node class to give possibility for using own REST
 * client in in-memory ElasticSearch server.
 */
public class ExtendedNode extends Node {
    public ExtendedNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
        // TODO: parameters "properties", "configPath" and "defaultNodeName" should probably not be "null"!
        super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null, null, null),
                classpathPlugins, false);
    }
}
