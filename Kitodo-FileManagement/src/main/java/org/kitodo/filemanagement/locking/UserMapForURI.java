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

package org.kitodo.filemanagement.locking;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;

/**
 * A HashMap that stores for each user, which URI has the read copy currently
 * issued for that user and how many locks the user has just opened on that URI.
 */
class UserMapForURI extends HashMap<String, Pair<URI, AtomicInteger>> {
    private static final long serialVersionUID = 1L;

}
