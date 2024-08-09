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

package org.kitodo.selenium.testframework.pages;

import org.kitodo.selenium.testframework.Browser;

public class StartPage extends Page<StartPage> {

    public StartPage() {
        super("pages/desktop.jsf");
    }

    @Override
    public StartPage goTo() {
        Browser.goTo(this.getUrl());
        return this;
    }
}
