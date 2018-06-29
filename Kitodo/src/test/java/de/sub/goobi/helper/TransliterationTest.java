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

package de.sub.goobi.helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransliterationTest {

    private Transliteration transliteration = new Transliteration();

    @Test
    public void shouldTransliterateISO() {
        String input = "Лорем ипсум долор сит амет, путент персиус фацилиси меи те, еам лорем нихил цоммуне ат, дицо аццусамус витуператорибус ет нец. Но пер виде магна омнес, дуо ат аперири витуперата. Дуо путент малорум яуаеяуе ет.";
        String transliterated = transliteration.transliterateISO(input);
        String expected = "Lorem ipsum dolor sit amet, putent persius facilisi mei te, eam lorem nihil commune at, dico accusamus vituperatoribus et nec. No per vide magna omnes, duo at aperiri vituperata. Duo putent malorum âuaeâue et.";

        assertEquals("String was transliterated incorrectly!", expected, transliterated);
    }

    @Test
    public void shouldTransliterateISOAlphabet() {
        String input = "А а Б б В в Г г Д д Е е Ж ж З з И и Й й К к Л л М м Н н О о П п Р р С с Т т У у Ф ф Х х Ц ц Ч ч Ш ш Щ щ Ь ь Ю ю Я я";
        String transliterated = transliteration.transliterateISO(input);
        String  expected = "A a B b V v G g D d E e Ž ž Z z I i J j K k L l M m N n O o P p R r S s T t U u F f H h C c Č č Š š Ŝ ŝ ʹ̱ ʹ Û û Â â";

        assertEquals("String was transliterated incorrectly!", expected, transliterated);
    }

    @Test
    public void shouldTransliterateDIN() {
        String input = "Лорем ипсум долор сит амет, путент персиус фацилиси меи те, еам лорем нихил цоммуне ат, дицо аццусамус витуператорибус ет нец. Но пер виде магна омнес, дуо ат аперири витуперата. Дуо путент малорум яуаеяуе ет.";
        String transliterated = transliteration.transliterateDIN(input);
        String expected = "Lorem ipsum dolor sit amet, putent persius facilisi mei te, eam lorem nichil commune at, dico accusamus vituperatoribus et nec. No per vide magna omnes, duo at aperiri vituperata. Duo putent malorum jauaejaue et.";

        assertEquals("String was transliterated incorrectly!", expected, transliterated);
    }

    @Test
    public void shouldTransliterateDINAlphabet() {
        String input = "А а Б б В в Г г Д д Е е Ж ж З з И и Й й К к Л л М м Н н О о П п Р р С с Т т У у Ф ф Х х Ц ц Ч ч Ш ш Щ щ Ь ь Ю ю Я я";
        String transliterated = transliteration.transliterateDIN(input);
        String expected = "A a B b V v G g D d E e Ž ž Z z I i J j K k L l M m N n O o P p R r S s T t U u F f Ch ch C c Č č Š š Šč šč ʹ̱ ʹ Ju ju Ja ja";

        assertEquals("String was transliterated incorrectly!", expected, transliterated);
    }
}
