package de.sub.goobi.persistence.apache;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import org.goobi.io.SafeFile;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.InvalidImagesException;

public class FolderInformation {

	private int id;
	private String title;
	public static final String metadataPath = ConfigMain.getParameter("MetadatenVerzeichnis");
	public static String DIRECTORY_SUFFIX = ConfigMain.getParameter("DIRECTORY_SUFFIX", "tif");
	public static String DIRECTORY_PREFIX = ConfigMain.getParameter("DIRECTORY_PREFIX", "orig");

	public FolderInformation(int id, String goobititle) {
		this.id = id;
		this.title = goobititle;
	}


	public String getImagesTifDirectory(boolean useFallBack) {
		SafeFile dir = new SafeFile(getImagesDirectory());
		DIRECTORY_SUFFIX = ConfigMain.getParameter("DIRECTORY_SUFFIX", "tif");
		DIRECTORY_PREFIX = ConfigMain.getParameter("DIRECTORY_PREFIX", "orig");
		/* nur die _tif-Ordner anzeigen, die nicht mir orig_ anfangen */
		FilenameFilter filterVerz = new FilenameFilter() {
			@Override
			public boolean accept(java.io.File dir, String name) {
				return (name.endsWith("_" + DIRECTORY_SUFFIX) && !name.startsWith(DIRECTORY_PREFIX + "_"));
			}
		};

		String tifOrdner = "";
		String[] verzeichnisse = dir.list(filterVerz);

		if (verzeichnisse != null) {
			for (int i = 0; i < verzeichnisse.length; i++) {
				tifOrdner = verzeichnisse[i];
			}
		}

		if (tifOrdner.equals("") && useFallBack) {
			String suffix = ConfigMain.getParameter("MetsEditorDefaultSuffix", "");
			if (!suffix.equals("")) {
				String[] folderList = dir.list();
				for (String folder : folderList) {
					if (folder.endsWith(suffix)) {
						tifOrdner = folder;
						break;
					}
				}
			}
		}
		if (!tifOrdner.equals("") && useFallBack) {
			String suffix = ConfigMain.getParameter("MetsEditorDefaultSuffix", "");
			if (!suffix.equals("")) {
				SafeFile tif = new SafeFile(tifOrdner);
				String[] files = tif.list();
				if (files == null || files.length == 0) {
					String[] folderList = dir.list();
					for (String folder : folderList) {
						if (folder.endsWith(suffix)) {
							tifOrdner = folder;
							break;
						}
					}
				}
			}
		}

		if (tifOrdner.equals("")) {
			tifOrdner = this.title + "_" + DIRECTORY_SUFFIX;
		}

		String rueckgabe = getImagesDirectory() + tifOrdner;

		if (!rueckgabe.endsWith(File.separator)) {
			rueckgabe += File.separator;
		}

		return rueckgabe;
	}

	/*
	 * @return true if the Tif-Image-Directory exists, false if not
	 */
	public Boolean getTifDirectoryExists() {
		SafeFile testMe;

		testMe = new SafeFile(getImagesTifDirectory(true));

		if (testMe.list() == null) {
			return false;
		}
		if (testMe.exists() && testMe.list().length > 0) {
			return true;
		} else {
			return false;
		}
	}

	public String getImagesOrigDirectory(boolean useFallBack) {
		if (ConfigMain.getBooleanParameter("useOrigFolder", true)) {
			SafeFile dir = new SafeFile(getImagesDirectory());
			DIRECTORY_SUFFIX = ConfigMain.getParameter("DIRECTORY_SUFFIX", "tif");
			DIRECTORY_PREFIX = ConfigMain.getParameter("DIRECTORY_PREFIX", "orig");
			/* nur die _tif-Ordner anzeigen, die mit orig_ anfangen */
			FilenameFilter filterVerz = new FilenameFilter() {
				@Override
				public boolean accept(java.io.File dir, String name) {
					return (name.endsWith("_" + DIRECTORY_SUFFIX) && name.startsWith(DIRECTORY_PREFIX + "_"));
				}
			};

			String origOrdner = "";
			String[] verzeichnisse = dir.list(filterVerz);
			for (int i = 0; i < verzeichnisse.length; i++) {
				origOrdner = verzeichnisse[i];
			}

			if (origOrdner.equals("") && useFallBack) {
				String suffix = ConfigMain.getParameter("MetsEditorDefaultSuffix", "");
				if (!suffix.equals("")) {
					String[] folderList = dir.list();
					for (String folder : folderList) {
						if (folder.endsWith(suffix)) {
							origOrdner = folder;
							break;
						}
					}
				}
			}
			if (!origOrdner.equals("") && useFallBack) {
				String suffix = ConfigMain.getParameter("MetsEditorDefaultSuffix", "");
				if (!suffix.equals("")) {
					SafeFile tif = new SafeFile(origOrdner);
					String[] files = tif.list();
					if (files == null || files.length == 0) {
						String[] folderList = dir.list();
						for (String folder : folderList) {
							if (folder.endsWith(suffix)) {
								origOrdner = folder;
								break;
							}
						}
					}
				}
			}

			if (origOrdner.equals("")) {
				origOrdner = DIRECTORY_PREFIX + "_" + this.title + "_" + DIRECTORY_SUFFIX;
			}

			String rueckgabe = getImagesDirectory() + origOrdner + File.separator;

			return rueckgabe;
		} else {
			return getImagesTifDirectory(useFallBack);
		}
	}

	public String getImagesDirectory() {
		String pfad = getProcessDataDirectory() + "images" + File.separator;

		return pfad;
	}

	public String getProcessDataDirectory() {
		String pfad = metadataPath + this.id + File.separator;
		pfad = pfad.replaceAll(" ", "__");
		return pfad;
	}

	public String getOcrDirectory() {
		return getProcessDataDirectory() + "ocr" + File.separator;
	}

	public String getTxtDirectory() {
		return getOcrDirectory() + this.title + "_txt" + File.separator;
	}

	public String getWordDirectory() {
		return getOcrDirectory() + this.title + "_wc" + File.separator;
	}

	public String getPdfDirectory() {
		return getOcrDirectory() + this.title + "_pdf" + File.separator;
	}

	public String getAltoDirectory() {
		return getOcrDirectory() + this.title + "_alto" + File.separator;
	}

	public String getImportDirectory() {
		return getProcessDataDirectory() + "import" + File.separator;
	}

	public String getMetadataFilePath() {
		return getProcessDataDirectory() + "meta.xml";
	}

	public String getSourceDirectory() {
		SafeFile dir = new SafeFile(getImagesDirectory());
		FilenameFilter filterVerz = new FilenameFilter() {
			@Override
			public boolean accept(java.io.File dir, String name) {
				return (name.endsWith("_" + "source"));
			}
		};
		SafeFile sourceFolder = null;
		String[] verzeichnisse = dir.list(filterVerz);
		if (verzeichnisse == null || verzeichnisse.length == 0) {
			sourceFolder = new SafeFile(dir, title + "_source");
			if (ConfigMain.getBooleanParameter("createSourceFolder", false)) {
				sourceFolder.mkdir();
			}
		} else {
			sourceFolder = new SafeFile(dir, verzeichnisse[0]);
		}

		return sourceFolder.getAbsolutePath();
	}

	public Map<String, String> getFolderForProcess(boolean useFallBack) {
		Map<String, String> answer = new HashMap<String, String>();
		String processpath = getProcessDataDirectory().replace("\\", "/");
		String tifpath = getImagesTifDirectory(useFallBack).replace("\\", "/");
		String imagepath = getImagesDirectory().replace("\\", "/");
		String origpath = getImagesOrigDirectory(useFallBack).replace("\\", "/");
		String metaFile = getMetadataFilePath().replace("\\", "/");
		String ocrBasisPath = getOcrDirectory().replace("\\", "/");
		String ocrPlaintextPath = getTxtDirectory().replace("\\", "/");
		String sourcepath = getSourceDirectory().replace("\\", "/");
		String importpath = getImportDirectory().replace("\\", "/");
		if (tifpath.endsWith(File.separator)) {
			tifpath = tifpath.substring(0, tifpath.length() - File.separator.length()).replace("\\", "/");
		}
		if (imagepath.endsWith(File.separator)) {
			imagepath = imagepath.substring(0, imagepath.length() - File.separator.length()).replace("\\", "/");
		}
		if (origpath.endsWith(File.separator)) {
			origpath = origpath.substring(0, origpath.length() - File.separator.length()).replace("\\", "/");
		}
		if (processpath.endsWith(File.separator)) {
			processpath = processpath.substring(0, processpath.length() - File.separator.length()).replace("\\", "/");
		}
		if (sourcepath.endsWith(File.separator)) {
			sourcepath = sourcepath.substring(0, sourcepath.length() - File.separator.length()).replace("\\", "/");
		}
		if (ocrBasisPath.endsWith(File.separator)) {
			ocrBasisPath = ocrBasisPath.substring(0, ocrBasisPath.length() - File.separator.length()).replace("\\", "/");
		}
		if (ocrPlaintextPath.endsWith(File.separator)) {
			ocrPlaintextPath = ocrPlaintextPath.substring(0, ocrPlaintextPath.length() - File.separator.length()).replace("\\", "/");
		}
		if (SystemUtils.IS_OS_WINDOWS) {
			answer.put("(tifurl)", "file:/" + tifpath);
		} else {
			answer.put("(tifurl)", "file://" + tifpath);
		}
		if (SystemUtils.IS_OS_WINDOWS) {
			answer.put("(origurl)", "file:/" + origpath);
		} else {
			answer.put("(origurl)", "file://" + origpath);
		}
		if (SystemUtils.IS_OS_WINDOWS) {
			answer.put("(imageurl)", "file:/" + imagepath);
		} else {
			answer.put("(imageurl)", "file://" + imagepath);
		}
		answer.put("(tifpath)", tifpath);
		answer.put("(origpath)", origpath);
		answer.put("(imagepath)", imagepath);
		answer.put("(processpath)", processpath);
		answer.put("(sourcepath)", sourcepath);
		answer.put("(importpath)", importpath);
		answer.put("(ocrbasispath)", ocrBasisPath);
		answer.put("(ocrplaintextpath)", ocrPlaintextPath);
		answer.put("(metaFile)", metaFile);
		return answer;
	}

	public String getMethodFromName(String methodName) {
		java.lang.reflect.Method method;
		try {
			method = this.getClass().getMethod(methodName);
			Object o = method.invoke(this);
			return (String) o;
		} catch (SecurityException e) {

		} catch (NoSuchMethodException e) {

		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		String folder = this.getImagesTifDirectory(false);
		folder = folder.substring(0, folder.lastIndexOf("_"));
		folder = folder + "_" + methodName;
		if (new SafeFile(folder).exists()) {
			return folder;
		}
		return null;
	}

	public List<String> getDataFiles() throws InvalidImagesException {
		SafeFile dir;
		try {
			dir = new SafeFile(getImagesTifDirectory(true));
		} catch (Exception e) {
			throw new InvalidImagesException(e);
		}
		/* Verzeichnis einlesen */
		String[] dateien = dir.list(Helper.dataFilter);
		ArrayList<String> dataList = new ArrayList<String>();
		if (dateien != null && dateien.length > 0) {
			for (int i = 0; i < dateien.length; i++) {
				String s = dateien[i];
				dataList.add(s);
			}
			/* alle Dateien durchlaufen */
			if (dataList != null && dataList.size() != 0) {
				Collections.sort(dataList, new GoobiImageFileComparator());
			}
			return dataList;
		} else {
			return null;
		}
	}

	public static class GoobiImageFileComparator implements Comparator<String> {

		@Override
		public int compare(String s1, String s2) {
			String imageSorting = ConfigMain.getParameter("ImageSorting", "number");
			s1 = s1.substring(0, s1.lastIndexOf("."));
			s2 = s2.substring(0, s2.lastIndexOf("."));

			if (imageSorting.equalsIgnoreCase("number")) {
				try {
					Integer i1 = Integer.valueOf(s1);
					Integer i2 = Integer.valueOf(s2);
					return i1.compareTo(i2);
				} catch (NumberFormatException e) {
					return s1.compareToIgnoreCase(s2);
				}
			} else if (imageSorting.equalsIgnoreCase("alphanumeric")) {
				return s1.compareToIgnoreCase(s2);
			} else {
				return s1.compareToIgnoreCase(s2);
			}
		}

	}
}
