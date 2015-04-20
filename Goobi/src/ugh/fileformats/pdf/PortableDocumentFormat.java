package ugh.fileformats.pdf;

/*******************************************************************************
 * ugh.fileformats.pdf / PortableDocumentFormat.java
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

import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import ugh.dl.ContentFile;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.Prefs;

import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;
import com.sun.media.jai.codec.FileSeekableStream;

/*******************************************************************************
 * @author Markus Enders
 * @version 2010-01-23
 * @since 2004-12-17
 * @deprecated
 * 
 *             CHANGELOG
 * 
 *             09.10.2009 --- Funk --- Changed the deprecated anotations.
 * 
 ******************************************************************************/

@Deprecated
@SuppressWarnings("unused")
public class PortableDocumentFormat implements ugh.dl.Fileformat {

	private final boolean			exportable	= true;
	private final boolean			importable	= false;
	private final boolean			updateable	= false;

	private ugh.dl.DigitalDocument	mydoc;
	private ugh.dl.FileSet			myImageset;
	private ugh.dl.Prefs			myPreferences;

	/***************************************************************************
	 * @param inPrefs
	 **************************************************************************/
	public PortableDocumentFormat(Prefs inPrefs) {
		this.myPreferences = inPrefs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#getDigitalDocument()
	 */
	public ugh.dl.DigitalDocument getDigitalDocument() {
		return this.mydoc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#setDigitalDocument(ugh.dl.DigitalDocument)
	 */
	public boolean setDigitalDocument(ugh.dl.DigitalDocument inDoc) {
		this.mydoc = inDoc;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#read(java.lang.String)
	 */
	public boolean read(String filename) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#update(java.lang.String)
	 */
	public boolean update(String filename) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#write(java.lang.String)
	 */
	public boolean write(String filename) {
		// Create instance for PDF object.
		com.lowagie.text.Document document = new com.lowagie.text.Document();
		// Open file.
		try {
			PdfWriter.getInstance(document, new FileOutputStream(filename));
		} catch (IOException ioe) {
			System.err.println("Can't open file " + filename
					+ " for writing the PDF file");
			return false;
		} catch (DocumentException de) {
			return false;
		}

		// Set common metadata for PDF, open PDF document.
		document.open();

		// Write all pages.
		DocStruct physicaldocStruct = this.mydoc.getPhysicalDocStruct();
		List<DocStruct> allChildren = physicaldocStruct.getAllChildren();
		Iterator<DocStruct> it = allChildren.iterator();
		while (it.hasNext()) {
			DocStruct child = it.next();
			// Only for pages we check this.
			if (child.getType().getName().equals("page")) {
				List<ContentFile> allContentFiles = child.getAllContentFiles();
				writePDFSinglePage(allContentFiles, document);
			}
		}

		document.close();

		return true;
	}

	/***************************************************************************
	 * @param allFiles
	 * @param document
	 * @return
	 **************************************************************************/
	private boolean writePDFSinglePage(List<ContentFile> allFiles,
			com.lowagie.text.Document document) {
		// Internal format name for ImageIO.
		String formatname = null;

		// Get the first contentfile used.
		Iterator<ContentFile> it = allFiles.iterator();
		while (it.hasNext()) {
			ContentFile cf = it.next();
			String mimetype = cf.getMimetype();

			// Get a reader for a specific mimetype first get ImageIO
			// formatname.
			if (mimetype.equals("image/tiff")) {
				formatname = "tif";
			}

			if (formatname == null) {
				System.err
						.println("PortableDocumentFormat: Can't find ImageIO-format for mime-type"
								+ mimetype);
				return false;
			}
			Iterator<ImageReader> it2 = ImageIO
					.getImageReadersByFormatName(formatname);
			ImageReader ir = null;
			while (it2.hasNext()) {
				ir = it2.next();
			}
			if (ir == null) {
				System.err
						.println("ERROR: no plug-in for contentfile with mimetype "
								+ mimetype + " available!");
				return false;
			}

			if (mimetype.equals("image/tiff")) {
				writeTIFFImage(document, cf);
			}
			// Get out of loop (we only want to get the first image).
			break;
		}

		return true;
	}

	/***************************************************************************
	 * @param document
	 * @param cf
	 * @return
	 **************************************************************************/
	private boolean writeTIFFImage(com.lowagie.text.Document document,
			ContentFile cf) {
		RenderedOp inImage = null;
		RenderedOp outImage = null;
		Image itextimg = null;

		// Read metadata.
		String xresolution_string = null;
		String yresolution_string = null;
		String samples_string = null;
		String bitspersamples_string = null;
		String height_string = null;
		String width_string = null;

		long orig_width = 0;
		long orig_height = 0;

		List<Metadata> allMD = cf.getAllMetadata();
		// No metadata available.
		if (allMD == null) {
			return false;
		}
		Iterator<Metadata> it = allMD.iterator();
		while (it.hasNext()) {
			Metadata md = it.next();
			if (md.getType().getName().equals("ugh_height")) {
				height_string = md.getValue();
			}
			if (md.getType().getName().equals("ugh_width")) {
				width_string = md.getValue();
			}
			if (md.getType().getName().equals("ugh_xresolution")) {
				xresolution_string = md.getValue();
			}
			if (md.getType().getName().equals("ugh_yresolution")) {
				yresolution_string = md.getValue();
			}
			if (md.getType().getName().equals("ugh_samples")) {
				samples_string = md.getValue();
			}
			if (md.getType().getName().equals("ugh_bitspersamples")) {
				bitspersamples_string = md.getValue();
			}
		}
		if (xresolution_string == null || yresolution_string == null
				|| height_string == null || width_string == null
				|| samples_string == null || bitspersamples_string == null) {
			System.err
					.println("ERROR: Can't get all needed technical metadata for contentfile "
							+ cf.getLocation());
			return false;
		}

		int xresolution = Integer.parseInt(xresolution_string);
		int yresolution = Integer.parseInt(yresolution_string);

		// Read tiff file and convert it to Buffered Images.
		try {
			FileSeekableStream ffs = new FileSeekableStream(cf.getLocation());
			// Create an image for Java Advanced Imaging.
			inImage = JAI.create("stream", ffs);
			orig_width = inImage.getWidth();
			orig_height = inImage.getHeight();
		} catch (IOException ioe) {
			System.err.println("Can't read contentfile " + cf.getLocation());
			return false;
		}

		// Set scaling factor; usually we had to calculate it.
		int scale = 100;
		ParameterBlock params = new ParameterBlock();
		params.addSource(inImage);
		params.add(scale);
		params.add(scale);
		params.add(0.0F);
		params.add(0.0F);
		params.add(new InterpolationNearest());

		outImage = JAI.create("scale", params);
		// Create buffered image.
		BufferedImage newbi = outImage.getAsBufferedImage();

		try {
			itextimg = Image.getInstance(newbi, null, true);
		} catch (IOException ioe) {
		} catch (BadElementException bee) {
		}

		// Define page size.
		int page_w = 210;
		int page_h = 297;

		// Scale image, if it doesn't fit on page.
		float page_w_pixel = (float) (page_w * xresolution / 25.4);
		float page_h_pixel = (float) (page_h * yresolution / 25.4);

		// Calculate actual scale factor.
		//
		// Check, if image will fit on page.
		if ((orig_width > page_w_pixel) || (orig_height > page_h_pixel)) {
			// Does not fit on page.
			float scalefactor = 0;
			float scalefactor_w = page_w_pixel / orig_width;
			float scalefactor_h = page_h_pixel / orig_height;

			if (scalefactor_h < scalefactor_w) {
				scalefactor = scalefactor_h;
			} else {
				scalefactor = scalefactor_w;
			}
			// orig_width = (long) (orig_width * scalefactor);
			// orig_height = (long) (orig_height * scalefactor);
			// Do scaling.
			itextimg.scalePercent((72f / xresolution * 100) * scalefactor,
					(72f / yresolution * 100) * scalefactor);
		} else {
			// Do scaling.
			itextimg.scalePercent((72f / xresolution * 100),
					(72f / yresolution * 100));
		}

		// Add page.
		try {
			document.add(itextimg);
			// Create new page.
			document.newPage();
		} catch (DocumentException de) {
			System.err
					.println("PortableDocumentFormat: DocumentException for Contentfile "
							+ cf.getLocation());
		}

		return true;
	}

}
