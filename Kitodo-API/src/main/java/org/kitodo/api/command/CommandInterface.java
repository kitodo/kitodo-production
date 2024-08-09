/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.api.command;

public interface CommandInterface {

    /**
     * Runs a given command.
     *
     * @param command
     *            The command as a String.
     * @return A commandResult, which contains id and result messages.
     */
    CommandResult runCommand(String command);
}
