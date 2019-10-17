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

/**
 *  Simple toggle visibility script
 *
 *  Every HTML-Element with the class "toggle" has a click event attached to change the visibility of another
 *  HTML-Element, according to the specified "data-for" attribute.
 *
 *  Usage:
 *  Create an HTML-Element which will function as the trigger for the visibility change of the target HTML-Element.
 *  Give it the class "toggle" and specify a target element with the "data-for" attribute. Valid inputs are
 *  classnames.
 *
 *  Example:
 *  <span class="toggle" data-for="toggle-1">Click me</span>
 *
 *  Then create the HTML element whose visibility will be toggled and give it the classname that you specified in
 *  the "data-for" attribute of your trigger HTML-Element
 *
 *  Example:
 *  <div class="toggle-1">Content</div>
 *
 *  Further usage:
 *  Initially set the visibility status of the target HTML-Element with the css attribute "display"
 *
 *  Example:
 *  <div class="toggle-1" style="display: none;">I'm invisible when loaded</div>
 *
 */
document.addEventListener("DOMContentLoaded", function() {
    [].forEach.call(document.querySelectorAll(".toggle"), function(el) {
      el.addEventListener("click", function() {
        try {
            var toggleEl = document.querySelectorAll("." + el.getAttribute('data-for'))[0];
            if (toggleEl.style.display == 'none') {
                toggleEl.style.display = '';
            } else if (toggleEl.style.display == '' ){
                toggleEl.style.display = 'none';
            }
        }
        catch (err) {
        }
      });
    });
});

