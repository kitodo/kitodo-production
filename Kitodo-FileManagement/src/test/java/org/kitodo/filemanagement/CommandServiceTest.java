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

package org.kitodo.filemanagement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class CommandServiceTest {

    @Test
    public void runCommandReturnsNullOnNullParameter() {
        CommandService commandService = new CommandService();
        assertDoesNotThrow(() -> {
            assertNull(commandService.runCommand(null));
        });
    }

    @Test
    public void runCommandThrowsIOExceptionIfExecutedCommandFailsWithIOException() {
        CommandService commandService = new CommandService();
        Exception exception = assertThrows(IOException.class, () -> {
            commandService.runCommand("fooBar");
        });
        String expectedExceptionMessage = "IOException: Cannot run program \"fooBar\"";
        String actualExceptionMessage = exception.getMessage();
        assertTrue(actualExceptionMessage.contains(expectedExceptionMessage));
    }
}
