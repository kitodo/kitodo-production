package ugh.contentfileformats;

/*******************************************************************************
 * ugh.contentfileformats / TiffFormat.java
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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;

import ugh.dl.Metadata;

import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
import com.sun.media.imageio.plugins.tiff.TIFFField;
import com.sun.media.jai.codec.FileSeekableStream;

/*******************************************************************************
 * <p>
 * Class allows to read TIFF images as ContentFiles and is able to extract
 * technical metadata.
 * </p>
 * 
 * @author Markus Enders
 * @version 2010-02-13
 * @since 2008-10-31
 * @deprecated
 * 
 *             TODOLOG
 * 
 *             TODO Get rid of this dependency on contentLib or koLibRI! (Is
 *             this still a TODO??)
 * 
 *             CHANGELOG
 * 
 *             13.02.1020 --- Funk --- Minor changes.
 * 
 ******************************************************************************/
@Deprecated
public class TiffFormat implements ugh.dl.ContentFileFormat {

	// The logger.
	private static final Logger	logger			= Logger
														.getLogger(ugh.dl.DigitalDocument.class);

	FileSeekableStream			ffs				= null;
	String						imageFilename	= null;
	ImageReader					ioReader		= null;

	int							x_resolution;
	int							y_resolution;
	int							bitspersample;
	int							numberofsamples;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.ContentFileFormat#getMetadata(java.lang.String)
	 */
	public Metadata getMetadata(String typeName) {

		TIFFDirectory tiffDirectory = null;

		// Get reader first.
		if (this.ioReader == null) {
			// Get image reader.
			Iterator<ImageReader> it = ImageIO
					.getImageReadersByFormatName("tif");
			if (it.hasNext()) {
				this.ioReader = it.next();
			}
			if (this.ioReader == null) {
				logger.error("TiffFormat: Can't find ImageIo ImageReader");
				return null;
			}
		}

		// Open file.
		try {
			File rf = new File(this.imageFilename);
			ImageInputStream iis = ImageIO.createImageInputStream(rf);
			// Set file to ioReader.
			this.ioReader.setInput(iis, true);

			// Just get the metadata of first image.
			tiffDirectory = TIFFDirectory.createFromMetadata(this.ioReader
					.getImageMetadata(0));
		} catch (IOException ioe) {
			// TODO Handle exception!
		}

		// Get all metadata.
		//
		// Get resolution.
		TIFFField tf = tiffDirectory.getTIFFField(282);
		this.x_resolution = tf.getAsInt(0);
		this.y_resolution = tf.getAsInt(1);

		// Get resolution unit.
		tf = tiffDirectory.getTIFFField(296);
		int resolution_unit = tf.getAsInt(0);

		// Resolution unit is cm convert to inches.
		if (resolution_unit == 3) {
			this.x_resolution = (int) (this.x_resolution / 2.54);
			this.y_resolution = (int) (this.y_resolution / 2.54);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.ContentFileFormat#readFile(java.lang.String)
	 */
	public boolean readFile(String filename) {

		ImageReader ir = null;

		// Find an ImageIO reader first.
		Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName("tif");
		if (it.hasNext()) {
			ir = it.next();
		}
		if (ir == null) {
			logger.error("ERROR: TiffFormat: Can't find ImageIo ImageReader");
			return false;
		}
		this.ioReader = ir;

		// Open file.
		try {
			this.ffs = new FileSeekableStream(filename);
		} catch (IOException ioe) {
			logger.error("Can't read contentfile '" + filename + "'");
			this.ffs = null;
			return false;
		}
		this.imageFilename = filename;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.ContentFileFormat#convertTo(java.lang.String)
	 */
	public boolean convertTo(String format) {
		return false;
	}

	/***************************************************************************
	 * @throws IOException
	 **************************************************************************/
	public void closeFile() throws IOException {
		this.ffs.close();
	}

}
