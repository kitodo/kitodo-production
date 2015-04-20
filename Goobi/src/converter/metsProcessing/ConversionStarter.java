package converter.metsProcessing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


import converter.Conversion.StarterMetaDataConversion;
import converter.processing.ProcessStarter;

public class ConversionStarter {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String answer = getInput("Sie sind dabei die Umwandlung aller Ihrer digitalen Dokumente \n" +
				"in das interne Mets Speicherformat zu starten. Wegen einer Änderung in UGH \n" +
				"sollten Sie für die automatische Validierung vorher \n" +
				" '<AGORA:PageNotAccountedStart>0</AGORA:PageNotAccountedStart>' gegen '<AGORA:PageNotAccountedStart>-1</AGORA:PageNotAccountedStart>' und \n" +
				" '<AGORA:PageNotAccountedEnd>0</AGORA:PageNotAccountedEnd>' gegen '<AGORA:PageNotAccountedEnd>-1</AGORA:PageNotAccountedEnd>' \n" +
				"in allen meta.xml Dateien ersetzen. \n" +
				"Dazu genügt ein einfacher Text Editor wie z.B. PSPad (http://www.pspad.com/de/). \n" +
				"Wenn Sie die bereits erledigt haben und nun starten möchten tun Sie dieses mit der Eingabe von 'y'");
		if (answer.toLowerCase().equals("y")) {
			ProcessStarter.main(null);
			StarterMetaDataConversion.main(null);
		}else{
			System.out.println("Programmausführung abgebrochen");
		}
	}
	
	private static String getInput(String message) throws IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		System.out.print(message);
		input = in.readLine();
		return input;
	}

}
