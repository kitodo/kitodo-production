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

$(function() {
    $('#editForm').on('input', ':input', function() {
        if (!$(this).parent().parent().hasClass('locked-button')) {
            setConfirmUnload(true);
        }
    });
});

function setConfirmUnload(on) {
    window.onbeforeunload = (on) ? function() { return true; } : undefined;
}
