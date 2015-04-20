package ugh.dl;

/*******************************************************************************
 * ugh.dl / ContentFileFormat.java
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
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 ******************************************************************************/

/***************************************************************************
 * <p>
 * Get special technical metadata about a file. Not all metadata are available
 * for any kind of filetype. E.g. resolution etc. makes only sense for still
 * images.
 * </p>
 * 
 * @author Markus Enders
 * @since 2004-12-18
 * @version 2010-02-13
 * @param typeName
 *            Internal name of metadata; this metadata is NOT user configurable.
 *            The following metadata types exists:
 *            <ul>
 *            _ugh_resolution_x resolution in x direction (in inch)
 *            <ul>
 *            _ugh_resolution_y resolution in y direction (in inch)
 *            <ul>
 *            _ugh_bitspersample
 *            <ul>
 *            _ugh_numberofsamples
 *            <ul>
 *            _ugh_height height in pixels
 *            <ul>
 *            _ugh_width width in pixels
 * @return Metadata object or null, if metadata is not available.
 * @deprecated
 * 
 *             TODOLOG
 * 
 *             TODO Get rid of this dependency on contentLib or koLibRI or use
 *             Saselan (http://commons.apache.org/sanselan/)! (Is this still a
 *             TODO??)
 * 
 *             CHANGELOG
 * 
 *             13.02.2010 --- Funk --- Minor changes.
 * 
 **************************************************************************/
@Deprecated
public interface ContentFileFormat {

	/***************************************************************************
	 * @param The
	 *            typeName.
	 * @return
	 **************************************************************************/
	public Metadata getMetadata(String typeName);

	/***************************************************************************
	 * <p>
	 * Read a file with the given filename.
	 * </p>
	 * 
	 * @param The
	 *            filename.
	 * @return TRUE if successful.
	 **************************************************************************/
	public boolean readFile(String filename);

	/***************************************************************************
	 * <p>
	 * Convert file to a special format; What kind of formats are available can
	 * be obtained from the ConverterFactory (still not implemented).
	 * </p>
	 * 
	 * @param The
	 *            format.
	 * @return TRUE if successful.
	 **************************************************************************/
	public boolean convertTo(String format);

}
