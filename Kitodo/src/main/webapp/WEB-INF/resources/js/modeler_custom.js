/**
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
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
		language = document.body.getAttribute('lang') || 'de';
		if (['de', 'en', 'es'].indexOf(language) === -1) {
			language = 'de';
		}

		var userRoles = $("#editForm\\:workflowTabView\\:roleId_input").children();

		userRoles.each(function( index ) {
			var role = { name: $(this).text(), value: $(this).val() };
			availableUserRoles.push(role);
		});
	});
});

function getLocalizedStringForKey(key) {

	var availableLanguages = ["de", "en", "es"];

	var localizedString;

	switch (availableLanguages.indexOf(language)) {
		default: {
			if (typeof de_DE !== 'undefined') {
				localizedString = de_DE[key];
			}
			break;
		}
		case 1: {
			if (typeof en_EN !== 'undefined') {
				localizedString = en_EN[key];
			}
			break;
		}
		case 2: {
			if (typeof es_ES !== 'undefined') {
				localizedString = es_ES[key];
			}
			break;
		}
	}

	if(!localizedString) {
		localizedString = key;
	}

	return localizedString;
}
