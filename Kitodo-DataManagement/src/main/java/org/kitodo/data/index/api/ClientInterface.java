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

package org.kitodo.data.index.api;

import java.io.IOException;

/**
 * Interface for different types of clients (actually only elasticsearch is
 * used).
 */
public interface ClientInterface {

    void initiateClient(String host, Integer port, String protocol);

    String getServerInformation() throws IOException;

    void closeClient() throws IOException;
}
