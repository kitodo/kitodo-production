/**
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

function copyToClipboard(inputFieldName, successMessage, errorMessage) {
    let inputField = document.getElementById(inputFieldName);
    try {
        // Temporarily activate deactivated inputFields to enable selecting
        if (inputField.getAttribute('disabled') && inputField.getAttribute('disabled').localeCompare('disabled') === 0) {
            inputField.removeAttribute('disabled');
            inputField.select();
            inputField.setAttribute('disabled', 'disabled');
        } else {
            inputField.select();
        }

        document.execCommand('copy');

        $(inputField).removeClass('ui-state-focus');

        PF('notifications').renderMessage({
            "summary": successMessage,
            "detail": inputField.value,
            "severity": "info"
        });
    } catch (e) {
        PF('notifications').renderMessage({
            "summary": errorMessage,
            "detail": inputField.value,
            "severity": "error"
        });
    }
}

