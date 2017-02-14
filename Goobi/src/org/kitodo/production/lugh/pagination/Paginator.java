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

import com.sharkysoft.util.UnreachableCodeException;

/**
 * Class to generate different sorts of paginations.
 * 
 * @author Matthias Ronge
 */
public class Paginator implements Iterator<String> {

    private enum State {
        /**
         * A decimal number.
         */
        DECIMAL,

        /**
         * The buffer is currently empty.
         */
        EMPTY,

        /**
         * Final run, clear the buffer.
         */
        END,

        /**
         * The next element is only to display if the counter value is full (1,
         * 2, 3).
         */
        FULL_INTEGER,

        /**
         * The next element is only to display if the counter value is half
         * (1.5, 2.5, 3.5).
         */
        HALF_INTEGER,

        /**
         * Elements to set the counter increment.
         */
        INCREMENT,

        /**
         * Initial state of the parser.
         */
        INITIAL,

        /**
         * A lowercase roman number.
         */
        LOWERCASE_ROMAN,

        /**
         * Plain text
         */
        TEXT,

        /**
         * A text escape marker.
         */
        TEXT_ESCAPE_TRANSITION,

        /**
         * An uppercase roman number.
         */
        UPPERCASE_ROMAN
    }

    /**
     * Fragments a pagination is composed of (text elements, counters, …)
     */
    private LinkedList<Fragment> fragments = new LinkedList<Fragment>();

    /**
     * Supports rigth-to-left double page counting (2 1, 4 3, 6 5, …)
     */
    private boolean operateReverse = false;

    /**
     * Current counter value.
     */
    private HalfInteger value;

    /**
     * Creates a new paginator.
     * 
     * @param initializer
     *            initial value
     */
    public Paginator(String initializer) {
        State currentState = State.INITIAL;
        boolean valueHalf = false;
        int valueFull;
        boolean textEscapeMode = false;
        StringBuilder buffer = new StringBuilder();

        /* iterate through the code points of the initializer string plus one
         * more iteration to process the last content of the buffer */

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

            // Whatever is in back-ticks is not interpreted as number

            if (codePointClass.equals(State.TEXT_ESCAPE_TRANSITION)) {
                if (currentState.equals(State.DECIMAL)) {
                    fragments.addLast(new DecimalNumeral(buffer.toString()));
                    buffer.setLength(0);
                } else if (currentState.equals(State.UPPERCASE_ROMAN) || currentState.equals(State.LOWERCASE_ROMAN)) {
                    fragments.addLast(new RomanNumeral(buffer.toString(), currentState.equals(State.UPPERCASE_ROMAN)));
                    buffer.setLength(0);
                } else if (currentState.equals(State.INCREMENT)) {
                    fragments.peekLast().setIncrement(HalfInteger.valueOf(buffer.toString()));
                    buffer.setLength(0);
                }
                currentState = State.TEXT;
                textEscapeMode = !textEscapeMode;
                offset += Character.charCount(codePoint);
                continue;
            }
            if (textEscapeMode) {
                buffer.appendCodePoint(codePoint);
            }

            else {
                switch (currentState) {
                case INITIAL:

                    /* If the initialisation string starts with a half
                     * increment marker this increments the initial counter
                     * value by one half. This is to allow starting with an
                     * even subpage */

                    if (codePoint == '½') {
                        valueHalf = true;
                        break;
                    }
                    //$FALL-THROUGH$

                    /* Parse different types of fragments from the
                     * initialiser string */

                case EMPTY:
                    if (!codePointClass.equals(State.FULL_INTEGER) && !codePointClass.equals(State.HALF_INTEGER)) {
                        buffer.appendCodePoint(codePoint);
                    }
                    if (codePointClass.equals(State.INCREMENT))
                        fragments.addLast(new StaticText(""));
                    currentState = codePointClass;
                    break;

                case DECIMAL:
                    if (codePointClass.equals(State.DECIMAL)) {
                        buffer.appendCodePoint(codePoint);
                    } else {
                        fragments.addLast(new DecimalNumeral(buffer.toString()));
                        buffer.setLength(0);
                        if (!codePointClass.equals(State.FULL_INTEGER) || !codePointClass.equals(State.HALF_INTEGER)) {
                            buffer.appendCodePoint(codePoint);
                        }
                        currentState = codePointClass;
                    }
                    break;

                case UPPERCASE_ROMAN:
                case LOWERCASE_ROMAN:
                    if (codePointClass.equals(currentState)) {
                        buffer.appendCodePoint(codePoint);
                    } else {
                        fragments.addLast(
                                new RomanNumeral(buffer.toString(), currentState.equals(State.UPPERCASE_ROMAN)));
                        buffer.setLength(0);
                        if (!codePointClass.equals(State.FULL_INTEGER) || !codePointClass.equals(State.HALF_INTEGER)) {
                            buffer.appendCodePoint(codePoint);
                        }
                        currentState = codePointClass;
                    }
                    break;

                case TEXT:
                    if (codePointClass.equals(State.UPPERCASE_ROMAN) || codePointClass.equals(State.LOWERCASE_ROMAN)
                            || codePointClass.equals(State.TEXT)) {
                        buffer.appendCodePoint(codePoint);
                    } else {
                        fragments.addLast(new StaticText(buffer.toString()));
                        buffer.setLength(0);
                        if (!codePointClass.equals(State.FULL_INTEGER) && !codePointClass.equals(State.HALF_INTEGER)) {
                            buffer.appendCodePoint(codePoint);
                        }
                        currentState = codePointClass;
                    }
                    break;

                case FULL_INTEGER:
                case HALF_INTEGER:
                    buffer.appendCodePoint(codePoint);
                    fragments.addLast(new StaticText(buffer.toString(), currentState.equals(State.FULL_INTEGER)));
                    buffer.setLength(0);
                    currentState = State.EMPTY;
                    break;

                case INCREMENT:
                    if (codePointClass.equals(State.INCREMENT)) {
                        buffer.appendCodePoint(codePoint);
                    } else {
                        fragments.peekLast().setIncrement(HalfInteger.valueOf(buffer.toString()));
                        buffer.setLength(0);
                        if (!codePointClass.equals(State.FULL_INTEGER) || !codePointClass.equals(State.FULL_INTEGER)) {
                            buffer.appendCodePoint(codePoint);
                        }
                        currentState = codePointClass;
                    }
                    break;

                default: throw new UnreachableCodeException("Complete switch");
                }
            }
            offset += Character.charCount(codePoint);
        }

        /* Initialise missing increments, find initial value and optional
         * reverse operation mode */

        Fragment first = null;
        Fragment last = null;
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
        this.value = new HalfInteger(valueFull, valueHalf);
    }

    private State codePointClassOf(int codePoint) {
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
            return State.TEXT;
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
