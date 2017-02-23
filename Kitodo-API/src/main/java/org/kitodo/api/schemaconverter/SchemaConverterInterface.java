/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the GPL3-License.txt file that was
 * distributed with this source code.
 */

package org.kitodo.api.schemaconverter;

import java.io.File;

public interface SchemaConverterInterface {

	/**
	 * Converts a given file with given Format to a result format.
	 * 
	 * @param inputFile
	 *            The file to convert.
	 * @param baseFormat
	 *            The Format of the given File.
	 * @param resultFormat
	 *            The Format of the resultFile.
	 * @return The converted File.
	 */
	public File convert(File inputFile, SchemaFormat baseFormat, SchemaFormat resultFormat);

}
