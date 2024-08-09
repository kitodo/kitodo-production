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

var availableUserRoles = [];
var language = "de";

$(window).on("load", function () {

	$.ready.then(function () {
		if ($('#editForm\\:workflowTabView\\:btnReadXmlDiagram').length > 0) {
			$('#editForm\\:workflowTabView\\:btnReadXmlDiagram')[0].click();
		} else {
			$('#editForm\\:workflowTabView\\:js-create-diagram')[0].click();
		}
		language = $("#editForm\\:workflowTabView\\:editor_language").val();
		if(!language) {
			language = "de";
		}

		var userRoles = $("#editForm\\:workflowTabView\\:roleId_input").children();

		userRoles.each(function( index ) {
			var role = { name: $(this).text(), value: $(this).val() };
			availableUserRoles.push(role);
		});
	});
});

function getLocalizedStringForKey(key) {

	var availableLanguages = ["de", "en"];

	var localizedString;

	switch (availableLanguages.indexOf(language)) {
		default: {
				localizedString = de_DE[key];
			break;
		}
		case 1: {
			localizedString = en_EN[key];
			break;
		}
	}

	if(!localizedString) {
		localizedString = key;
	}

	return localizedString;
}
