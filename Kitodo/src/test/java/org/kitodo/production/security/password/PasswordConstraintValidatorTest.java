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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PasswordConstraintValidatorTest {

    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;
    private PasswordConstraintValidator passwordConstraintValidator = new PasswordConstraintValidator();

    @BeforeEach
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
        assertTrue(passwordConstraintValidator.isValid("Random_2425!", context), "Password 'Random_2425!' was incorrect!");
        assertTrue(passwordConstraintValidator.isValid("teRand3435!", context), "Password 'teRand3435!' was incorrect!");
    }

    @Test
    public void passwordsShouldBeInvalid() {
        assertFalse(passwordConstraintValidator.isValid("password", context), "Password 'password' was correct!");
        assertFalse(passwordConstraintValidator.isValid("white space", context), "Password 'white space' was correct!");
        assertFalse(passwordConstraintValidator.isValid("short", context), "Password 'short' was correct!");
    }
}
