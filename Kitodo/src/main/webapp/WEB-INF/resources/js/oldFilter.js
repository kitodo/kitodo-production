/*
 *
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

function setFilter(filterString) {
    document.getElementById('filterfield').value = filterString;
    applyFilter(filterString);
}

function applyFilter(filterString) {
    var invisibleFilter = document.getElementById('processform:auflistung:ajaxcolumn:filter');
    invisibleFilter.value = filterString;
    invisibleFilter.dispatchEvent(new Event('keyup'));
}

window.onload = function () {
    document.getElementById('filterfield').addEventListener('change', function () {
        applyFilter(this.value);
    });
    document.getElementById('select').addEventListener('change', function () {
        setFilter(this.value);
        applyFilter(this.value);
    });
}
