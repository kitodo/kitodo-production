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

package org.kitodo.dataeditor.pagination;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.kitodo.dataformat.metskitodo.DivType;

/**
 * Class to generate different sorts of paginations.
 *
 * @author Matthias Ronge
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

        StringBuilder buffer = new StringBuilder();
        PaginatorState bufferState = PaginatorState.EMPTY;

        /*
         * iterate through the code points of the initializer string plus one more
         * iteration to process the last content of the buffer
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
                if (bufferState.equals(PaginatorState.EMPTY)) {
                    bufferState = PaginatorState.TEXT_ESCAPE_TRANSITION;
                } else {
                    createFragment(buffer, bufferState, page);
                    page = null;
                    bufferState = bufferState.equals(PaginatorState.TEXT_ESCAPE_TRANSITION) ? PaginatorState.EMPTY
                            : PaginatorState.TEXT_ESCAPE_TRANSITION;
                }
            } else if (bufferState.equals(PaginatorState.TEXT_ESCAPE_TRANSITION)) {
                buffer.appendCodePoint(codePoint);
            } else if (codePointClass.equals(PaginatorState.HALF_INTEGER) || codePointClass.equals(PaginatorState.FULL_INTEGER)) {
                /*
                 * Recto/verso-only symbols cause a buffer write (or they would be applied to
                 * the current buffer content (modify their left side), but they shall be
                 * applied on the next write (modify their right side)). They set the page
                 * variable and are not written to the buffer by themselves.
                 */
                if (!bufferState.equals(PaginatorState.EMPTY)) {
                    createFragment(buffer, bufferState, page);
                    bufferState = PaginatorState.EMPTY;
                }
                page = codePointClass.equals(PaginatorState.HALF_INTEGER);
            } else if (bufferState.equals(codePointClass) || bufferState.equals(PaginatorState.EMPTY)
                    || (bufferState.equals(PaginatorState.TEXT) && codePointClass.equals(PaginatorState.SYMBOL))
                    || (bufferState.equals(PaginatorState.SYMBOL) && codePointClass.equals(PaginatorState.TEXT))) {
                /*
                 * If the buffer is empty or contains the same sort of content as the current
                 * input, just write it to the buffer. If the buffer contains text, we can write
                 * symbols as well, the same is true the other way ‘round.
                 */

                buffer.appendCodePoint(codePoint);
                bufferState = codePointClass;

            } else if ((bufferState.equals(PaginatorState.TEXT)
                    && (codePointClass.equals(PaginatorState.LOWERCASE_ROMAN) || codePointClass.equals(PaginatorState.UPPERCASE_ROMAN)))
                    || ((bufferState.equals(PaginatorState.LOWERCASE_ROMAN) || bufferState.equals(PaginatorState.UPPERCASE_ROMAN))
                            && codePointClass.equals(PaginatorState.TEXT))) {
                /*
                 * If we got text, and the content of the buffer is a Roman numeral, or the
                 * other way round, we can still write to the buffer, but the result of the
                 * operation is always text. This is an important catch in order to, for
                 * example, prevent the C in ‘Chapter’ start counting. (Remember, Roman numeral
                 * C is 100.)
                 */

                buffer.appendCodePoint(codePoint);
                bufferState = PaginatorState.TEXT;

            } else {
                // In any other case, we have to write out the buffer.
                createFragment(buffer, bufferState, page);
                page = null;
                buffer.appendCodePoint(codePoint);
                bufferState = codePointClass;
            }

            offset += Character.charCount(codePoint);
        }
    }

    /**
     * Stores the buffer as fragment and resets the buffer. The type of fragment is
     * derived from the the buffer’s PaginatorState and the page directive. A page directive
     * causes what is immediately thereafter to be treated as text in any case.
     *
     * @param buffer
     *            characters
     * @param fragmentType
     *            type of fragment to create
     * @param pageType
     *            page information
     */
    private void createFragment(StringBuilder buffer, PaginatorState fragmentType, Boolean pageType) {
        if (pageType == null && fragmentType.equals(PaginatorState.DECIMAL)) {
            fragments.addLast(new DecimalNumeral(buffer.toString()));
        } else if (pageType == null
                && (fragmentType.equals(PaginatorState.UPPERCASE_ROMAN) || fragmentType.equals(PaginatorState.LOWERCASE_ROMAN))) {
            fragments.addLast(new RomanNumeral(buffer.toString(), fragmentType.equals(PaginatorState.UPPERCASE_ROMAN)));
        } else if (fragmentType.equals(PaginatorState.INCREMENT)) {
            fragments.peekLast().setIncrement(HalfInteger.valueOf(buffer.toString()));
        } else if (!fragmentType.equals(PaginatorState.EMPTY)) {
            fragments.addLast(new StaticText(buffer.toString(), pageType));
        }
        buffer.setLength(0);
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
        boolean aHalf = !initializer.isEmpty() && initializer.codePointAt(0) == '½';

        parse(aHalf ? initializer.substring(1) : initializer);
        initializeIncrements(aHalf);
    }

    /**
     * Initialises missing increments, finds the initial value and, optional, a
     * reverse operation mode.
     */
    private void initializeIncrements(boolean aHalf) {
        Fragment first = null;
        Fragment last = null;
        int valueFull;

        for (Fragment fragment : fragments) {
            if (fragment.intValue() == null) {
                continue;
            }
            if (first == null) {
                first = fragment;
            }
            last = fragment;
        }
        if (first == null) { // static text only
            valueFull = 0;
        } else if (first == last) { // only one counting element
            valueFull = first.intValue();
            if (first.getIncrement() == null) {
                first.setIncrement(HalfInteger.ONE);
            }
        } else if (first.intValue() <= last.intValue()) { /*
                                                           * more than one counting element in left-to-right order
                                                           */
            valueFull = first.intValue();
            Fragment previous = null;
            int howMany = 0;
            for (Fragment fragment : fragments) {
                if (fragment.intValue() == null) {
                    continue;
                }

                if (previous != null && previous.getIncrement() == null) {
                    previous.setIncrement(new HalfInteger(fragment.intValue() - previous.intValue(), false));
                }

                previous = fragment;
                howMany++;
            }
            if (last.getIncrement() == null) {
                last.setIncrement(new HalfInteger((last.intValue() - first.intValue()) / (howMany - 1), false));
            }

        } else { // more than one counting element in right-to-left order
            this.operateReverse = true;
            valueFull = last.intValue();
            Fragment previous = null;
            int howMany = 0;
            for (Iterator<Fragment> iterator = fragments.descendingIterator(); iterator.hasNext();) {
                Fragment fragment = iterator.next();

                if (fragment.intValue() == null) {
                    continue;
                }

                if (previous != null && previous.getIncrement() == null) {
                    previous.setIncrement(new HalfInteger(fragment.intValue() - previous.intValue(), false));
                }
                previous = fragment;
                howMany++;
            }
            if (first.getIncrement() == null) {
                first.setIncrement(new HalfInteger((first.intValue() - last.intValue()) / (howMany - 1), false));
            }

        }
        value = new HalfInteger(valueFull, aHalf);
    }

    private static PaginatorState codePointClassOf(int codePoint) {
        switch (codePoint) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return PaginatorState.DECIMAL;
            case 'C':
            case 'D':
            case 'I':
            case 'L':
            case 'M':
            case 'V':
            case 'X':
                return PaginatorState.UPPERCASE_ROMAN;
            case '`':
                return PaginatorState.TEXT_ESCAPE_TRANSITION;
            case 'c':
            case 'd':
            case 'i':
            case 'l':
            case 'm':
            case 'v':
            case 'x':
                return PaginatorState.LOWERCASE_ROMAN;
            case '¡':
                return PaginatorState.FULL_INTEGER;
            case '°':
            case '²':
            case '³':
            case '¹':
            case '½':
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
     * Always true. We can count infinitely, there is always a higher number.
     */
    @Override
    public boolean hasNext() {
        return true;
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

    public void run(List<DivType> physicalDivTypes) {
        for(DivType div : physicalDivTypes) {
            div.setORDERLABEL(this.next());
        }
    }
}
