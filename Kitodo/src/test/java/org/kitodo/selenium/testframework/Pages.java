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

package org.kitodo.selenium.testframework;

import org.kitodo.selenium.testframework.pages.LoginPage;
import org.openqa.selenium.support.PageFactory;

public class Pages {

    private static <T> T getPage(Class<T> clazz) throws IllegalAccessException, InstantiationException {
        System.out.println("T page = clazz.newInstance();");
        System.out.println(clazz.getCanonicalName());
        T page = clazz.newInstance();
        System.out.println("PageFactory.initElements(Browser.getDriver(), page);");
        System.out.println("page = " + page.toString());
        PageFactory.initElements(Browser.getDriver(), page);
        System.out.println("return page;");
        return page;
    }

    public static LoginPage login() throws InstantiationException, IllegalAccessException {
        return getPage(LoginPage.class);
    }

}
