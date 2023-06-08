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

package org.kitodo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.SystemUtils;

public class ExecutionPermission {

    public static void setExecutePermission(File file) throws IOException {
        if (SystemUtils.IS_OS_UNIX) {
            Set<PosixFilePermission> permissions = setNoExecutePermission();

            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);

            Files.setPosixFilePermissions(file.toPath(), permissions);
        }
    }

    public static void setNoExecutePermission(File file) throws IOException {
        if (SystemUtils.IS_OS_UNIX) {
            Files.setPosixFilePermissions(file.toPath(), setNoExecutePermission());
        }
    }

    private static Set<PosixFilePermission> setNoExecutePermission() {
        Set<PosixFilePermission> permissions = new HashSet<>();

        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);

        permissions.add(PosixFilePermission.OTHERS_READ);
        permissions.add(PosixFilePermission.OTHERS_WRITE);

        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.GROUP_WRITE);

        return permissions;
    }
}
