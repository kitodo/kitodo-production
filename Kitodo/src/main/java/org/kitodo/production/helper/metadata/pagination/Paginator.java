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

package org.kitodo.production.helper.metadata.pagination;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Class to generate different sorts of paginations.
 */
public class Paginator implements Iterator<String> {

    /**
     * Fragments a pagination is composed of (text elements, counters, …).
     */
    private final LinkedList<Fragment> fragments = new LinkedList<>();

    /**
     * Supports rigth-to-left double page counting (2 1, 4 3, 6 5, …).
     */
    private boolean operateReverse = false;

    /**
     * Current counter value.
     */
    private HalfInteger value;

    private void parse(String initializer) {

        StringBuilder stringBuilder = new StringBuilder();
        PaginatorState paginatorState = PaginatorState.EMPTY;

        /*
         * iterate through the code points of the initializer string plus one more
         * iteration to process the last content of the stringBuilder
         */
        Boolean page = null;
        int length = initializer.length();
        for (int offset = 0; offset <= length;) {
            int codePoint;
            PaginatorState codePointClass;
            if (offset == length) {
                codePointClass = PaginatorState.END;
                codePoint = 0;
            } else {
                codePoint = initializer.codePointAt(offset);
                codePointClass = codePointClassOf(codePoint);
            }

            // Whatever is in back-ticks is not interpreted
            if (codePointClass.equals(PaginatorState.TEXT_ESCAPE_TRANSITION)) {
                if (paginatorState.equals(PaginatorState.EMPTY)) {
                    paginatorState = PaginatorState.TEXT_ESCAPE_TRANSITION;
                } else {
                    createFragment(stringBuilder, paginatorState, page);
                    page = null;
                    paginatorState = paginatorState.equals(PaginatorState.TEXT_ESCAPE_TRANSITION) ? PaginatorState.EMPTY
                            : PaginatorState.TEXT_ESCAPE_TRANSITION;
                }
            } else if (paginatorState.equals(PaginatorState.TEXT_ESCAPE_TRANSITION)) {
                stringBuilder.appendCodePoint(codePoint);
            } else if (codePointClass.equals(PaginatorState.HALF_INTEGER)
                    || codePointClass.equals(PaginatorState.FULL_INTEGER)) {
                /*
                 * Recto/verso-only symbols cause a stringBuilder write (or they would be
                 * applied to the current stringBuilder content (modify their left side), but
                 * they shall be applied on the next write (modify their right side)). They set
                 * the page variable and are not written to the stringBuilder by themselves.
                 */
                if (!paginatorState.equals(PaginatorState.EMPTY)) {
                    createFragment(stringBuilder, paginatorState, page);
                    paginatorState = PaginatorState.EMPTY;
                }
                page = codePointClass.equals(PaginatorState.HALF_INTEGER);
            } else if (paginatorState.equals(codePointClass) || paginatorState.equals(PaginatorState.EMPTY)
                    || paginatorState.equals(PaginatorState.TEXT) && codePointClass.equals(PaginatorState.SYMBOL)
                    || paginatorState.equals(PaginatorState.SYMBOL) && codePointClass.equals(PaginatorState.TEXT)) {
                /*
                 * If the stringBuilder is empty or contains the same sort of content as the
                 * current input, just write it to the stringBuilder. If the stringBuilder
                 * contains text, we can write symbols as well, the same is true the other way
                 * ‘round.
                 */
                stringBuilder.appendCodePoint(codePoint);
                paginatorState = codePointClass;

            } else if (paginatorState.equals(PaginatorState.TEXT)
                    && (codePointClass.equals(PaginatorState.LOWERCASE_ROMAN)
                            || codePointClass.equals(PaginatorState.UPPERCASE_ROMAN))
                    || (paginatorState.equals(PaginatorState.LOWERCASE_ROMAN)
                            || paginatorState.equals(PaginatorState.UPPERCASE_ROMAN))
                            && codePointClass.equals(PaginatorState.TEXT)) {
                /*
                 * If we got text, and the content of the stringBuilder is a Roman numeral, or
                 * the other way round, we can still write to the stringBuilder, but the result
                 * of the operation is always text. This is an important catch in order to, for
                 * example, prevent the C in ‘Chapter’ start counting. (Remember, Roman numeral
                 * C is 100.)
                 */
                stringBuilder.appendCodePoint(codePoint);
                paginatorState = PaginatorState.TEXT;

            } else {
                // In any other case, we have to write out the stringBuilder.
                createFragment(stringBuilder, paginatorState, page);
                page = null;
                stringBuilder.appendCodePoint(codePoint);
                paginatorState = codePointClass;
            }
            offset += Character.charCount(codePoint);
        }
    }

    /**
     * Stores the stringBuilder as fragment and resets the stringBuilder. The type
     * of fragment is derived from the the stringBuilder’s PaginatorState and the
     * page directive. A page directive causes what is immediately thereafter to be
     * treated as text in any case.
     *
     * @param stringBuilder
     *            characters
     * @param fragmentType
     *            type of fragment to create
     * @param pageType
     *            page information
     */
    private void createFragment(StringBuilder stringBuilder, PaginatorState fragmentType, Boolean pageType) {
        if (pageType == null && fragmentType.equals(PaginatorState.DECIMAL)) {
            fragments.addLast(new DecimalNumeral(stringBuilder.toString()));
        } else if (pageType == null && (fragmentType.equals(PaginatorState.UPPERCASE_ROMAN)
                || fragmentType.equals(PaginatorState.LOWERCASE_ROMAN))) {
            fragments.addLast(
                new RomanNumeral(stringBuilder.toString(), fragmentType.equals(PaginatorState.UPPERCASE_ROMAN)));
        } else if (fragmentType.equals(PaginatorState.INCREMENT)) {
            fragments.peekLast().setIncrement(HalfInteger.valueOf(stringBuilder.toString()));
        } else if (!fragmentType.equals(PaginatorState.EMPTY)) {
            fragments.addLast(new StaticText(stringBuilder.toString(), pageType));
        }
        stringBuilder.setLength(0);
    }

    /**
     * Creates a new paginator.
     *
     * @param initializer
     *            initial value
     */
    public Paginator(String initializer) {

        /*
         * If the initialisation string starts with a half increment marker this
         * increments the initial counter value by one half. This is to allow starting
         * with an interemdiate (verso) subpage.
         */
        boolean halfAboveValue = !initializer.isEmpty() && initializer.codePointAt(0) == '½';
        String paginatorInitializer = halfAboveValue ? initializer.substring(1) : initializer;
        parse(paginatorInitializer);
        initializeIncrements(halfAboveValue);
    }

    /**
     * Initializes missing increments, finds the initial value and, optional, a
     * reverse operation mode.
     */
    private void initializeIncrements(boolean aHalf) {
        Fragment firstFragment = null;
        Fragment lastFragment = null;
        int valueFull;

        for (Fragment fragment : fragments) {
            if (fragment.getInitialValue() == null) {
                continue;
            }
            if (firstFragment == null) {
                firstFragment = fragment;
            }
            lastFragment = fragment;
        }
        if (firstFragment == null) { // static text only
            valueFull = 0;
        } else if (firstFragment.equals(lastFragment)) { // only one counting element
            valueFull = firstFragment.getInitialValue();
            if (firstFragment.getIncrement() == null) {
                firstFragment.setIncrement(new HalfInteger(1, false));
            }
        } else if (firstFragment.getInitialValue() <= lastFragment.getInitialValue()) {
            valueFull = initializeLeftToRightMode(firstFragment, lastFragment);
        } else {
            valueFull = initializeRightToLeftMode(firstFragment, lastFragment);
        }
        value = new HalfInteger(valueFull, aHalf);
    }

    /**
     * More than one counting element in left-to-right order.
     */
    private int initializeLeftToRightMode(Fragment firstFragment, Fragment lastFragment) {
        int valueFull;
        valueFull = firstFragment.getInitialValue();
        Fragment previousFragment = null;
        int howMany = 0;
        for (Fragment fragment : fragments) {
            if (fragment.getInitialValue() == null) {
                continue;
            }

            if (previousFragment != null && previousFragment.getIncrement() == null) {
                previousFragment.setIncrement(
                    new HalfInteger(fragment.getInitialValue() - previousFragment.getInitialValue(), false));
            }

            previousFragment = fragment;
            howMany++;
        }
        if (lastFragment.getIncrement() == null) {
            lastFragment.setIncrement(new HalfInteger(
                    (lastFragment.getInitialValue() - firstFragment.getInitialValue()) / (howMany - 1), false));
        }
        return valueFull;
    }

    /**
     * More than one counting element in right-to-left order.
     */
    private int initializeRightToLeftMode(Fragment firstFragment, Fragment lastFragment) {
        int valueFull;
        this.operateReverse = true;
        valueFull = lastFragment.getInitialValue();
        Fragment previousFragment = null;
        int howMany = 0;
        for (Iterator<Fragment> iterator = fragments.descendingIterator(); iterator.hasNext();) {
            Fragment fragment = iterator.next();

            if (fragment.getInitialValue() == null) {
                continue;
            }

            if (previousFragment != null && previousFragment.getIncrement() == null) {
                previousFragment.setIncrement(
                    new HalfInteger(fragment.getInitialValue() - previousFragment.getInitialValue(), false));
            }
            previousFragment = fragment;
            howMany++;
        }
        if (firstFragment.getIncrement() == null) {
            firstFragment.setIncrement(new HalfInteger(
                    (firstFragment.getInitialValue() - lastFragment.getInitialValue()) / (howMany - 1), false));
        }
        return valueFull;
    }

    private static PaginatorState codePointClassOf(int codePoint) {
        switch (codePoint) {
            case '0': case '1': case '2': case '3': case '4': case '5':
            case '6': case '7': case '8': case '9':
                return PaginatorState.DECIMAL;
            case 'C': case 'D': case 'I': case 'L': case 'M': case 'V':
            case 'X':
                return PaginatorState.UPPERCASE_ROMAN;
            case '`':
                return PaginatorState.TEXT_ESCAPE_TRANSITION;
            case 'c': case 'd': case 'i': case 'l': case 'm': case 'v':
            case 'x':
                return PaginatorState.LOWERCASE_ROMAN;
            case '¡':
                return PaginatorState.FULL_INTEGER;
            case '°': case '²': case '³': case '¹': case '½':
                return PaginatorState.INCREMENT;
            case '¿':
                return PaginatorState.HALF_INTEGER;
            default:
                switch (Character.getType(codePoint)) {
                    case Character.UPPERCASE_LETTER:
                    case Character.LOWERCASE_LETTER:
                    case Character.TITLECASE_LETTER:
                    case Character.MODIFIER_LETTER:
                    case Character.OTHER_LETTER:
                    case Character.NON_SPACING_MARK:
                        return PaginatorState.TEXT;
                    default:
                        return PaginatorState.SYMBOL;
                }
        }
    }

    /**
     * To prevent infinite loops by using hasNext as condition, a
     * UnsupportedOperationException is thrown because there are always next
     * elements.
     */
    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException("Paginator.hasNext()");
    }

    @Override
    public String next() {
        StringBuilder result = new StringBuilder();
        if (operateReverse) {
            for (Iterator<Fragment> iterator = fragments.descendingIterator(); iterator.hasNext();) {
                Fragment fragment = iterator.next();
                result.insert(0, fragment.format(value));
                value = value.add(fragment.getIncrement());
            }
        } else {
            for (Fragment fragment : fragments) {
                result.append(fragment.format(value));
                value = value.add(fragment.getIncrement());
            }
        }
        return result.toString();
    }

    /**
     * The iterator does not support {@code remove()}.
     *
     * @throws UnsupportedOperationException
     *             if invoked.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Paginator.remove()");

    }

    /**
     * Returns a concise string representation of this instance.
     *
     * @return a string representing this instance
     */
    @Override
    public String toString() {
        return value + (operateReverse ? ", reversed, " : ", ") + fragments;
    }
}
