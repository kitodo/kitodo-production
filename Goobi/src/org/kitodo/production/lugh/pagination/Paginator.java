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
package org.kitodo.production.lugh.pagination;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Class to generate different sorts of paginations.
 *
 * @author Matthias Ronge
 */
public class Paginator implements Iterator<String> {

    /**
     * Type of character and state of the buffer.
     */
    private enum State {
        /**
         * A decimal number.
         */
        DECIMAL,

        /**
         * The buffer is currently empty. (Not a character class.)
         */
        EMPTY,

        /**
         * Final run, clear the buffer. (Fictitious character class and not a
         * buffer state.)
         */
        END,

        /**
         * The next element is only to display if the counter value is full (1,
         * 2, 3). (Not a buffer state.)
         */
        FULL_INTEGER,

        /**
         * The next element is only to display if the counter value is half
         * (1.5, 2.5, 3.5). (Not a buffer state.)
         */
        HALF_INTEGER,

        /**
         * Elements to set the counter increment.
         */
        INCREMENT,

        /**
         * An lower-case Roman numeral. This may still turn into a static text
         * if the next character is a letter.
         */
        LOWERCASE_ROMAN,

        /**
         * Other characters. If the next character is {@code LOWERCASE_ROMAN} or
         * {@code UPPERCASE_ROMAN}, it may be treated as Roman numeral.
         */
        SYMBOL,

        /**
         * Letters. If the next character is of type {@code LOWERCASE_ROMAN} or
         * {@code UPPERCASE_ROMAN}, it will be treated as text.
         */
        TEXT,

        /**
         * A text escape marker. Whatever the buffer contains, treat it as
         * text..
         */
        TEXT_ESCAPE_TRANSITION,

        /**
         * An upper-case Roman numeral. This may still turn into a static text
         * if the next character is a letter.
         */
        UPPERCASE_ROMAN
    }

    /**
     * Fragments a pagination is composed of (text elements, counters, …)
     */
    private final LinkedList<Fragment> fragments = new LinkedList<Fragment>();

    /**
     * Supports rigth-to-left double page counting (2 1, 4 3, 6 5, …)
     */
    private boolean operateReverse = false;

    /**
     * Current counter value.
     */
    private HalfInteger value;

    private final void parse(String initializer) {
        StringBuilder buffer = new StringBuilder();
        State bufferState = State.EMPTY;

        /* iterate through the code points of the initializer string plus one
         * more iteration to process the last content of the buffer */

        Boolean page = null;
        int length = initializer.length();
        for (int offset = 0; offset <= length;) {
            int codePoint;
            State codePointClass;
            if (offset == length) {
                codePointClass = State.END;
                codePoint = 0;
            } else {
                codePoint = initializer.codePointAt(offset);
                codePointClass = codePointClassOf(codePoint);
            }

            // Whatever is in back-ticks is not interpreted

            if (codePointClass.equals(State.TEXT_ESCAPE_TRANSITION)) {
                if (bufferState.equals(State.EMPTY)) {
                    bufferState = State.TEXT_ESCAPE_TRANSITION;
                } else {
                    createFragment(buffer, bufferState, page);
                    page = null;
                    bufferState = bufferState.equals(State.TEXT_ESCAPE_TRANSITION) ? State.EMPTY
                            : State.TEXT_ESCAPE_TRANSITION;
                }
            } else if (bufferState.equals(State.TEXT_ESCAPE_TRANSITION)) {
                buffer.appendCodePoint(codePoint);
            }

            /* Recto/verso-only symbols cause a buffer write (or they would be
             * applied to the current buffer content (modify their left side),
             * but they shall be applied on the next write (modify their right
             * side)). They set the page variable and are not written to the
             * buffer by themselves. */

            else if (codePointClass.equals(State.HALF_INTEGER) || codePointClass.equals(State.FULL_INTEGER)) {
                if (!bufferState.equals(State.EMPTY)) {
                    createFragment(buffer, bufferState, page);
                    bufferState = State.EMPTY;
                }
                page = codePointClass.equals(State.HALF_INTEGER);
            }

            /* If the buffer is empty or contains the same sort of content as
             * the current input, just write it to the buffer. If the buffer
             * contains text, we can write symbols as well, the same is true the
             * other way ‘round. */

            else if (bufferState.equals(codePointClass) || bufferState.equals(State.EMPTY)
                    || (bufferState.equals(State.TEXT) && codePointClass.equals(State.SYMBOL))
                    || (bufferState.equals(State.SYMBOL) && codePointClass.equals(State.TEXT))) {

                buffer.appendCodePoint(codePoint);
                bufferState = codePointClass;
            }

            /* If we got text, and the content of the buffer is a Roman numeral,
             * or the other way round, we can still write to the buffer, but the
             * result of the operation is always text. This is an important
             * catch in order to, for example, prevent the C in ‘Chapter’ start
             * counting. (Remember, Roman numeral C is 100.) */

            else if ((bufferState.equals(State.TEXT)
                    && (codePointClass.equals(State.LOWERCASE_ROMAN) || codePointClass.equals(State.UPPERCASE_ROMAN)))
                    || ((bufferState.equals(State.LOWERCASE_ROMAN) || bufferState.equals(State.UPPERCASE_ROMAN))
                            && codePointClass.equals(State.TEXT))) {

                buffer.appendCodePoint(codePoint);
                bufferState = State.TEXT;
            }

            // In any other case, we have to write out the buffer.

            else {
                createFragment(buffer, bufferState, page);
                page = null;
                buffer.appendCodePoint(codePoint);
                bufferState = codePointClass;
            }

            offset += Character.charCount(codePoint);
        }
    }

    /**
     * Stores the buffer as fragment and resets the buffer. The type of fragment
     * is derived from the the buffer’s state and the page directive. A page
     * directive causes what is immediately thereafter to be treated as text in
     * any case.
     *
     * @param buffer
     *            characters
     * @param fragmentType
     *            type of fragment to create
     * @param pageType
     *            page information
     */
    private final void createFragment(StringBuilder buffer, State fragmentType, Boolean pageType) {
        if (pageType == null && fragmentType.equals(State.DECIMAL)) {
            fragments.addLast(new DecimalNumeral(buffer.toString()));
        } else if (pageType == null && (fragmentType.equals(State.UPPERCASE_ROMAN)
                || fragmentType.equals(State.LOWERCASE_ROMAN))) {
            fragments.addLast(new RomanNumeral(buffer.toString(), fragmentType.equals(State.UPPERCASE_ROMAN)));
        } else if (fragmentType.equals(State.INCREMENT)) {
            fragments.peekLast().setIncrement(HalfInteger.valueOf(buffer.toString()));
        } else if (!fragmentType.equals(State.EMPTY)) {
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
         * increments the initial counter value by one half. This is to allow
         * starting with an interemdiate (verso) subpage.
         */
        boolean aHalf = !initializer.isEmpty() && initializer.codePointAt(0) == '½';

        parse(aHalf ? initializer.substring(1) : initializer);
        initializeIncrements(aHalf);
    }

    /**
     * Initialises missing increments, finds the initial value and, optional, a
     * reverse operation mode.
     */
    private final void initializeIncrements(boolean aHalf) {
        Fragment first = null;
        Fragment last = null;
        int valueFull;

        for (Fragment fragment : fragments) {
            if (fragment.intValue() == null)
                continue;
            if (first == null)
                first = fragment;
            last = fragment;
        }
        if (first == null) { // static text only
            valueFull = 0;
        } else if (first == last) { // only one counting element
            valueFull = first.intValue();
            if (first.getIncrement() == null)
                first.setIncrement(HalfInteger.ONE);
        } else if (first.intValue() <= last.intValue()) { /* more than one
                                                           * counting element in
                                                           * left-to-right order */
            valueFull = first.intValue();
            Fragment previous = null;
            int howMany = 0;
            for (Fragment fragment : fragments) {
                if (fragment.intValue() == null)
                    continue;

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

                if (fragment.intValue() == null)
                    continue;

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

    private static State codePointClassOf(int codePoint) {
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
            return State.DECIMAL;
        case 'C':
        case 'D':
        case 'I':
        case 'L':
        case 'M':
        case 'V':
        case 'X':
            return State.UPPERCASE_ROMAN;
        case '`':
            return State.TEXT_ESCAPE_TRANSITION;
        case 'c':
        case 'd':
        case 'i':
        case 'l':
        case 'm':
        case 'v':
        case 'x':
            return State.LOWERCASE_ROMAN;
        case '¡':
            return State.FULL_INTEGER;
        case '°':
        case '²':
        case '³':
        case '¹':
        case '½':
            return State.INCREMENT;
        case '¿':
            return State.HALF_INTEGER;
        default:
            switch (Character.getType(codePoint)) {
            case Character.UPPERCASE_LETTER:
            case Character.LOWERCASE_LETTER:
            case Character.TITLECASE_LETTER:
            case Character.MODIFIER_LETTER:
            case Character.OTHER_LETTER:
            case Character.NON_SPACING_MARK:
                return State.TEXT;
            default:
                return State.SYMBOL;
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

    /**
     * {@inheritDoc}
     */
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
