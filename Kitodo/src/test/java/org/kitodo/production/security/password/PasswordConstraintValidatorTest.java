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

package org.kitodo.production.security.password;

import javax.validation.ConstraintValidatorContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordConstraintValidatorTest {

    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;
    private PasswordConstraintValidator passwordConstraintValidator = new PasswordConstraintValidator();

    @Before
    public void setup() {
        // mock the context
        context = Mockito.mock(ConstraintValidatorContext.class);

        // context.buildConstraintViolationWithTemplate returns
        // ConstraintValidatorContext.ConstraintViolationBuilder
        // so we mock that too as you will be calling one of it's methods
        builder = Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        // when the context.buildConstraintViolationWithTemplate is called,
        // the mock should return the builder.
        Mockito.when(context.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(builder);
    }

    @Test
    public void passwordsShouldBeValid() {
        assertTrue("Password 'Random_2425!' was incorrect!",
            passwordConstraintValidator.isValid("Random_2425!", context));
        assertTrue("Password 'teRand3435!' was incorrect!",
            passwordConstraintValidator.isValid("teRand3435!", context));
    }

    @Test
    public void passwordsShouldBeInvalid() {
        assertFalse("Password 'password' was correct!", passwordConstraintValidator.isValid("password", context));
        assertFalse("Password 'white space' was correct!", passwordConstraintValidator.isValid("white space", context));
        assertFalse("Password 'short' was correct!", passwordConstraintValidator.isValid("short", context));
    }
}
