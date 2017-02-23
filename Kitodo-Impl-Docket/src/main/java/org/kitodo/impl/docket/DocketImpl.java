package org.kitodo.impl.docket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import org.kitodo.api.docket.DocketData;
import org.kitodo.api.docket.DocketInterface;

public class DocketImpl implements DocketInterface {

	@Override
	public File generateDocket(DocketData docketData, Path pathToXslFile) throws IOException {

		ExportDocket exportDocket = new ExportDocket();

		File file = new File("docket.pdf");
		FileOutputStream fileOutputStream = new FileOutputStream(file);

		exportDocket.startExport(docketData, fileOutputStream, pathToXslFile.toString());

		return file;

	}

	@Override
	public File generateMultipleDockets(ArrayList<DocketData> docketDataList, Path pathToXslFile)
			throws IOException {

		ExportDocket exportDocket = new ExportDocket();

		File file = new File("docket_multipage.pdf");
		FileOutputStream fileOutputStream = new FileOutputStream(file);

		exportDocket.startExport(docketDataList, fileOutputStream, pathToXslFile.toString());

		return file;
	}

}
