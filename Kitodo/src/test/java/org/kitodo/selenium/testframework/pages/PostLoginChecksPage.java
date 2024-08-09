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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class PostLoginChecksPage extends Page<PostLoginChecksPage> {

    @FindBy(id = "retryForm:logoutButton")
    private WebElement logoutButton;

    public PostLoginChecksPage() {
        super("pages/checks.jsf");
    }

    @Override
    public PostLoginChecksPage goTo() {
        // this page is never navigated to directly by the user
        return null;
    }

    public void logout() {
        clickElement(logoutButton);
    }

}
