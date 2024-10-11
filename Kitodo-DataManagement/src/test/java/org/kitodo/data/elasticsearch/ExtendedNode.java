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

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.opensearch.common.settings.Settings;
import org.opensearch.node.InternalSettingsPreparer;
import org.opensearch.node.Node;
import org.opensearch.plugins.Plugin;

/**
 * Class extends standard Node class to give possibility for using own REST
 * client in in-memory ElasticSearch server.
 */
public class ExtendedNode extends Node {
    public ExtendedNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins,
                        Supplier<String> nodeNameSupplier) {
        super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, Collections.emptyMap(),
                        Paths.get("target"), nodeNameSupplier), classpathPlugins, false);
    }
}
