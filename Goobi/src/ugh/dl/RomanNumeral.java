package ugh.dl;

/*******************************************************************************
 * ugh.dl / RomanNumberal.java
 * 
 * Copyright 2010 Center for Retrospective Digitization, Göttingen (GDZ)
 * 
 * http://gdz.sub.uni-goettingen.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

/*******************************************************************************
 * <p>
 * A class that represents a number as a Roman numeral.
 * </p>
 * 
 * <p>
 * A RomanNumeral only represents natural numbers, that is integers greater than
 * 0. The maximum integer value of a RomanNumeral is 4999. The RomanNumeral can
 * be displayed either in archaic style (4 = IIII) or modern style (4 = IV). The
 * default display is modern.
 * </p>
 * 
 * @author Lara Burton
 * @version 2009-11-17
 * @since 2004-05-21
 * 
 *        CHANGELOG
 * 
 *        17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *        30.10.2009 --- Funk --- Added generated serialVersionUID.
 * 
 ******************************************************************************/

public class RomanNumeral extends Number {

	private static final long	serialVersionUID	= -245445201389499368L;

	/***************************************************************************
	 * <p>
	 * Divisors used in converting an integer to a Roman numeral.
	 * </p>
	 **************************************************************************/
	private static final int[]	DIVISORS			= { 1000, 500, 100, 50, 10,
			5, 1									};

	/***************************************************************************
	 * <p>
	 * Characters ("digits") that make up a Roman numeral.
	 * </p>
	 **************************************************************************/
	private static final char[]	ROMAN_DIGITS		= { 'M', 'D', 'C', 'L',
			'X', 'V', 'I'							};

	/***************************************************************************
	 * <p>
	 * The number of elements in the divisors array.
	 * </p>
	 **************************************************************************/
	private static final int	NUM_DIVISORS		= 7;

	/***************************************************************************
	 * <p>
	 * The maximum integer value supported by RomanNumeral.
	 * </p>
	 **************************************************************************/
	public static final int		MAX_NUM				= 4999;

	/***************************************************************************
	 * <p>
	 * Flags whether or not the RomanNumeral style is archaic (if not archaic,
	 * the style is modern.
	 * </p>
	 **************************************************************************/
	private boolean				styleIsArchaic;

	/***************************************************************************
	 * <p>
	 * The buffer used to compute and store the string representation of the
	 * RomanNumeral.
	 * </p>
	 **************************************************************************/
	private StringBuffer		num;

	/***************************************************************************
	 * <p>
	 * The int value of the RomanNumeral.
	 **************************************************************************/
	private int					intValue;

	/***************************************************************************
	 * <p>
	 * Creates new RomanNumeral with style set to modern (4 = IV) and value of 1
	 * (I).
	 * </p>
	 **************************************************************************/
	public RomanNumeral() {
		this.styleIsArchaic = false;
		this.intValue = 1;
		this.num = new StringBuffer();
		this.num.append('I');
	}

	/***************************************************************************
	 * <p>
	 * Creates new RomanNumeral with style set to modern (4 = IV) with the value
	 * of the number equal to int.
	 * </p>
	 * 
	 * @param value
	 *            An int value to convert to a Roman numeral
	 * @exception NumberFormatException
	 *                if the parameter is out of range
	 **************************************************************************/
	public RomanNumeral(int value) throws NumberFormatException {
		this.setValue(value);
	}

	/***************************************************************************
	 * <p>
	 * Creates new RomanNumeral with style set to modern (4 = IV) with the value
	 * of the number equal to the RomanNumeral represented by the String value.
	 * </p>
	 * 
	 * @param value
	 *            A String representing a Roman numeral
	 * @exception NumberFormatException
	 *                if the parameter is not a valid representation of a Roman
	 *                numeral
	 **************************************************************************/
	public RomanNumeral(String value) throws NumberFormatException {
		this.setValue(value);
	}

	/***************************************************************************
	 * <p>
	 * Determines the integer value of a Roman digit, such as 'M', 'C', 'D', and
	 * so forth.
	 * </p>
	 * 
	 * @param digit
	 *            A character representing a Roman digit.
	 * @return The integer value of the Roman digit parameter.
	 * @exception NumberFormatException
	 *                if a digit is not a valid Roman digit.
	 **************************************************************************/
	private int getIntValue(char digit) throws NumberFormatException {

		for (int i = 0; i < NUM_DIVISORS; i++) {
			if (ROMAN_DIGITS[i] == digit) {
				return (DIVISORS[i]);
			}
		}

		throw new NumberFormatException("Number contains invalid digits");
	}

	/***************************************************************************
	 * <p>
	 * Converts a Roman numeral to an integer.
	 * </p>
	 * 
	 * @param romanNum
	 *            A string representing a Roman numeral
	 * @return An int value of the string parameter
	 * @exception NumberFormatException
	 *                If the string parameter is not a valid Roman numeral
	 **************************************************************************/
	private int convertRomanToInt(String romanNum) throws NumberFormatException {

		int digitCount = 0;
		int curr;
		int prev = DIVISORS[0];
		int sum = 0;

		for (int i = 0; i < romanNum.length(); i++) {
			curr = getIntValue(romanNum.charAt(i));
			if (prev < curr) {
				if (digitCount > 0) {
					throw new NumberFormatException("Not a valid Roman numeral");
				}
				if (prev * 5 == curr || prev * 10 == curr) {
					sum -= prev;
					sum += curr - prev;
				} else {
					throw new NumberFormatException("Not a valid Roman numeral");
				}
			} else {
				if (curr == prev) {
					digitCount++;
				} else {
					digitCount = 0;
				}
				if (digitCount >= 4) {
					throw new NumberFormatException("Not a valid Roman numeral");
				}
				sum += curr;
			}
			prev = curr;
		}

		return sum;
	}

	/***************************************************************************
	 * <p>
	 * Converts an int into an Archaic style Roman numeral (4 = IIII).
	 * </p>
	 * 
	 * @param number
	 *            the int value to convert to a Roman numeral
	 **************************************************************************/
	private void convertIntArchaic(int number) {

		int div;
		this.num = new StringBuffer();

		for (int i = 0; i < NUM_DIVISORS && number > 0; i++) {
			div = number / DIVISORS[i];
			for (int j = 0; j < div; j++) {
				this.num.append(ROMAN_DIGITS[i]);
			}

			number = number - DIVISORS[i] * div;
		}
	}

	/***************************************************************************
	 * <p>
	 * Converts an int into a Modern style Roman numeral (4 = IV).
	 * </p>
	 * 
	 * @param number
	 *            the int value to convert to a Roman numeral
	 **************************************************************************/
	private void convertInt(int number) {
		int div;

		this.num = new StringBuffer();
		for (int i = 0; i < NUM_DIVISORS && number > 0; i++) {
			div = number / DIVISORS[i];
			if (div == 4) {
				if (this.num.length() == 0
						|| this.num.charAt(this.num.length() - 1) != ROMAN_DIGITS[i - 1]) {
					if (i == 0) {
						for (int j = 0; j < div; j++) {
							this.num.append(ROMAN_DIGITS[i]);
						}
					} else {
						this.num.append(ROMAN_DIGITS[i]);
						this.num.append(ROMAN_DIGITS[i - 1]);
					}
				} else {
					this.num.setCharAt(this.num.length() - 1, ROMAN_DIGITS[i]);
					this.num.append(ROMAN_DIGITS[i - 2]);
				}
			} else {
				for (int j = 0; j < div; j++) {
					this.num.append(ROMAN_DIGITS[i]);
				}
			}

			number = number - DIVISORS[i] * div;
		}
	}

	/***************************************************************************
	 * <p>
	 * Accepts an integer and sets RomanNumeral to that value.
	 * </p>
	 * 
	 * @param v
	 *            the value
	 * @exception NumberFormatException
	 *                If the parameter is out of range.
	 **************************************************************************/
	public void setValue(int v) throws NumberFormatException {

		if (v > MAX_NUM) {
			throw new NumberFormatException("Value out of range - too high");
		}
		if (v < 1) {
			throw new NumberFormatException("Value out of range - too low");
		}

		this.intValue = v;

		if (this.styleIsArchaic) {
			convertIntArchaic(v);
		} else {
			convertInt(v);
		}
	}

	/***************************************************************************
	 *<p>
	 * Sets the value of the RomanNumeral.
	 * </p>
	 * 
	 * @param value
	 *            A string representation of a Roman numeral
	 * @exception NumberFormatException
	 *                If the string parameter is not a valid RomanNumeral
	 **************************************************************************/
	public void setValue(String value) throws NumberFormatException {

		this.intValue = convertRomanToInt(value);
		this.num = new StringBuffer(value);
	}

	/***************************************************************************
	 * <p>
	 * Sets the style of the RomanNumeral to modern (4 = IV).
	 * </p>
	 **************************************************************************/
	public void setStyleModern() {

		if (this.styleIsArchaic) {
			this.styleIsArchaic = false;
			convertInt(this.intValue);
		}
	}

	/***************************************************************************
	 * <p>
	 * Sets the style of the RomanNumeral to archaic (4 = IIII).
	 * </p>
	 **************************************************************************/
	public void setStyleArchaic() {

		if (!this.styleIsArchaic) {
			this.styleIsArchaic = true;
			convertIntArchaic(this.intValue);
		}
	}

	/***************************************************************************
	 * <p>
	 * Allows the user to learn if the style of the Roman numeral is archaic (4
	 * = IIII).
	 * </p>
	 * 
	 * <p>
	 * If it is not archaic, it is modern (4 = IV).
	 * </p>
	 * 
	 * @return <code>true</code> if the style of the Roman numeral is archaic,
	 *         else return <code>false</code>
	 **************************************************************************/
	public boolean isArchaic() {
		return this.styleIsArchaic;
	}

	/***************************************************************************
	 * <p>
	 * Allows the user to learn if the style of the Roman numeral is modern (4 =
	 * IV).
	 * </p>
	 * 
	 * <p>
	 * If it is not modern, it is archaic (4 = IIII).
	 * </p>
	 * 
	 * @return <code>true</code> if the style of the Roman numeral is modern,
	 *         else return <code>false</code>
	 **************************************************************************/
	public boolean isModern() {
		return !this.styleIsArchaic;
	}

	/***************************************************************************
	 *<p>
	 * Returns the string representation of the Roman numeral.
	 * </p>
	 * 
	 * @return a string representation of the Roman numeral
	 **************************************************************************/
	public String getNumber() {
		return this.num.toString();
	}

	/***************************************************************************
	 * <p>
	 * Determines if two RomanNumerals have equal values.
	 * </p>
	 * 
	 * <p>
	 * The style of the RomanNumeral (modern or archaic) is irrelevant. Only the
	 * actual integer values of the objects are compared.
	 * </p>
	 * 
	 * @param rn
	 *            a RomanNumeral
	 * @return <code>true</code> if the parameter has a value equal to the
	 *         RomanNumeral object, <code>false</code> if the values of the two
	 *         RomanNumerals are not equal.
	 **************************************************************************/
	public boolean equals(RomanNumeral rn) {

		if (rn.intValue() == this.intValue) {
			return true;
		}

		return false;
	}

	/***************************************************************************
	 * <p>
	 * An abstract method of Number.
	 * </p>
	 * 
	 * <p>
	 * Converts the RomanNumeral to a double.
	 * </p>
	 * 
	 * @return the double value of the Roman numeral
	 **************************************************************************/
	public double doubleValue() {
		return this.intValue;
	}

	/***************************************************************************
	 * <p>
	 * An abstract method of Number.
	 * </p>
	 * 
	 *<p>
	 * Converts the RomanNumeral to a float.
	 * </p>
	 * 
	 * @return the float value of the Roman numeral
	 **************************************************************************/
	public float floatValue() {
		return this.intValue;
	}

	/***************************************************************************
	 * <p>
	 * An abstract method of Number.
	 * </p>
	 * 
	 * <p>
	 * Converts the RomanNumeral to an int.
	 * </p>
	 * 
	 * @return the int value of the Roman numeral
	 **************************************************************************/
	public int intValue() {
		return this.intValue;
	}

	/***************************************************************************
	 * <p>
	 * An abstract method of Number.
	 * </p>
	 * 
	 * <p>
	 * Converts the RomanNumeral to a long.
	 * </p>
	 * 
	 * @return the long value of the Roman numeral
	 **************************************************************************/
	public long longValue() {
		return this.intValue;
	}

	/***************************************************************************
	 * <p>
	 * An overridden method of Number.
	 * </p>
	 * 
	 * <p>
	 * Converts the RomanNumeral to a short.
	 * </p>
	 * 
	 * @return the short value of the Roman numeral
	 **************************************************************************/
	public short shortValue() {
		return (short) this.intValue;
	}

	/***************************************************************************
	 * <p>
	 * An overridden method of Number.
	 * </p>
	 * 
	 * Converts the RomanNumeral to a byte.
	 * 
	 * @return the byte value of the Roman numeral
	 **************************************************************************/
	public byte byteValue() {
		return (byte) this.intValue;
	}

	/***************************************************************************
	 * <p>
	 * An overridden method from Object.
	 * </p>
	 * 
	 * The method getNumber is identical to this method.
	 * 
	 * @return a string representation of the Roman numeral.
	 **************************************************************************/
	public String toString() {
		return this.num.toString();
	}

}
