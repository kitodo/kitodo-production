package org.goobi.production.Import;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.goobi.production.enums.ImportFormat;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.helper.exceptions.DAOException;

public class ImportTest {

	public static void main(String[] args) throws DAOException, IOException {

		ProzessDAO dao = new ProzessDAO();
		Prozess digiwu = dao.get(944);
		String records = "";
		FileInputStream fis = new FileInputStream(new File("c:/Temp/temp_opac.xml"));

		records = new ImportTest().convertStreamToString(fis);
		
		OpacMassImport omi = new OpacMassImport(records, digiwu, ImportFormat.PICA);
		omi.convertData();
	}

	public String convertStreamToString(InputStream is) throws IOException {

		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
}
