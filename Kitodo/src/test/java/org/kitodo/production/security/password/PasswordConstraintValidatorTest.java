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

import java.util.Locale;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
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

    @Test
    public void violationMessagesShouldBeInSpanishWhenUiLanguageIsSpanish() {
        FacesContext facesContext = mockFacesContext(Locale.of("es"));
        try (MockedStatic<FacesContext> facesContextMock = Mockito.mockStatic(FacesContext.class)) {
            facesContextMock.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            assertFalse(passwordConstraintValidator.isValid("short", context), "Password 'short' was correct!");

            // at least one violation message must be in Spanish (independent of how many
            // violations are emitted, i.e. one per rule or all messages joined into one)
            ArgumentCaptor<String> template = ArgumentCaptor.forClass(String.class);
            Mockito.verify(context, Mockito.atLeastOnce()).buildConstraintViolationWithTemplate(template.capture());
            assertTrue(template.getAllValues().stream().anyMatch(m -> m.contains("contraseña")),
                    "Expected a Spanish violation message, got: " + template.getAllValues());
        }
    }

    @Test
    public void violationMessagesShouldBeInGermanWhenUiLanguageIsGerman() {
        FacesContext facesContext = mockFacesContext(Locale.GERMAN);
        try (MockedStatic<FacesContext> facesContextMock = Mockito.mockStatic(FacesContext.class)) {
            facesContextMock.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            assertFalse(passwordConstraintValidator.isValid("short", context), "Password 'short' was correct!");

            // at least one violation message must be in German (independent of how many
            // violations are emitted, i.e. one per rule or all messages joined into one)
            ArgumentCaptor<String> template = ArgumentCaptor.forClass(String.class);
            Mockito.verify(context, Mockito.atLeastOnce()).buildConstraintViolationWithTemplate(template.capture());
            assertTrue(template.getAllValues().stream().anyMatch(m -> m.contains("Großbuchstaben")),
                    "Expected a German violation message, got: " + template.getAllValues());
        }
    }

    private FacesContext mockFacesContext(Locale locale) {
        UIViewRoot viewRoot = Mockito.mock(UIViewRoot.class);
        Mockito.when(viewRoot.getLocale()).thenReturn(locale);
        FacesContext facesContext = Mockito.mock(FacesContext.class);
        Mockito.when(facesContext.getViewRoot()).thenReturn(viewRoot);
        return facesContext;
    }
}
