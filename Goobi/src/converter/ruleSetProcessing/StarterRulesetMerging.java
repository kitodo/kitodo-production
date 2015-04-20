package converter.ruleSetProcessing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class StarterRulesetMerging {

	
	protected final Logger myLogger = Logger
	.getLogger(StarterRulesetMerging.class);

	static SlubReader sr = null;
	static ManuscriptReader mr = null;

	/**
	 * @param args
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static void main(String[] args) throws JDOMException, IOException {

		String myBasePath = null;
		try {
			myBasePath = getInput("Please enter the file path  to 'slub.xml' and 'manuscript.xml' (both files need to be in the same folder):");
			if (myBasePath == null || myBasePath.length() == 0) {
				myBasePath = "./input/";
			}
			if (!myBasePath.endsWith("/")) {
				myBasePath = myBasePath + "/";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		//initialize SlubReader and ManuscriptReader classes with respective filepath
		sr = new SlubReader(myBasePath + "slub.xml");
		//mr = new ManuscriptReader(myBasePath + "slubTest.xml");
		mr = new ManuscriptReader(myBasePath + "manuscript.xml");

		//check if SlubReader and ManuscriptReader are readable
		System.out.println(sr);
		System.out.println(mr);

		XMLOutputter so = new XMLOutputter();
		so.setFormat(Format.getPrettyFormat());

		mr.cleanOutComments();
		so.output(mr.getDoubleElements(), System.out);
		SlubIntegrity.resetObjectsOnWatch();

		//initiate the compare mechanism
		sr.compare(mr);
		//System.out.println(sr.getDiffDocument().getRootElement().getChildren());
		so.output(sr.getDiffDocument(), System.out);

	}

	private static String getInput(String message) throws IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		System.out.print(message);
		input = in.readLine();
		return input;
	}

}
