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

package org.kitodo.security.password;

import com.ibm.icu.impl.locale.XCldrStub;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.kitodo.config.PasswordConfig;
import org.passay.CharacterRule;
import org.passay.DictionaryRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.Rule;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;
import org.passay.dictionary.ArrayWordList;
import org.passay.dictionary.WordListDictionary;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public void initialize(ValidPassword argument) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        PasswordValidator validator = new PasswordValidator(getRulesFromConfigFile());

        RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                XCldrStub.Joiner.on(",").join(validator.getMessages(result)))
                .addConstraintViolation();
        return false;
    }

    private List<Rule> getRulesFromConfigFile() {
        List<Rule> rules = new ArrayList<>();

        rules.add(new LengthRule(PasswordConfig.getLengthMin(), PasswordConfig.getLengthMax()));
        rules.add(new CharacterRule(EnglishCharacterData.Digit,PasswordConfig.getNumberOfDigitCharacters()));
        rules.add(new CharacterRule(EnglishCharacterData.Special,PasswordConfig.getNumberOfSpecialCharacters()));
        rules.add(new CharacterRule(EnglishCharacterData.UpperCase,PasswordConfig.getNumberOfUppercaseCharacters()));
        rules.add(new DictionaryRule(new WordListDictionary(new ArrayWordList(PasswordConfig.getNotAllowedWords()))));

        if (!PasswordConfig.isWhitespaceAllowed()) {
            rules.add(new WhitespaceRule());
        }

        return rules;
    }
}
