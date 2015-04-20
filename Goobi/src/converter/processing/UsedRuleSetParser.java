package converter.processing;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

public class UsedRuleSetParser implements Validatable {

	private static final Logger logger = Logger
			.getLogger(UsedRuleSetParser.class);

	public static List<String> getIds(String inPath)
			throws ConfigurationException {
		logger.debug("getIds(String) - start");

		List<String> myList = new ArrayList<String>();
		XMLConfiguration config = new XMLConfiguration(inPath);

		/*
		 * find correct ruleset
		 */

		String rulesetpath = null;
		int maxsize = config.getMaxIndex("ruleset");
		for (int i = 0; i <= maxsize; i++) {
			String name = config.getString("ruleset(" + i + ")[@title]");
			if (name.equals("manuscript_test")) {
				rulesetpath = "ruleset(" + i + ")";
			}
		}

		/*
		 * check if ruleset exists
		 */
		if (rulesetpath == null) {
			logger.debug("Rulessetpath=" + rulesetpath);
			throw new ConfigurationException("ruleset not found");
		}

		/*
		 * run through all processes of found ruleset
		 */
		maxsize = config.getMaxIndex(rulesetpath + ".process");
		for (int i = 0; i <= maxsize; i++) {
			boolean isTemplate = config.getBoolean(rulesetpath + ".process("
					+ i + ")[@isTemplate]");
			if (!isTemplate) {
				myList.add(config.getString(rulesetpath + ".process(" + i
						+ ")[@id]"));
			}
		}

		logger.debug("getIds(String) - end");
		return myList;
	}

	public void validate() throws ContentLibException {
		// TODO Auto-generated method stub

	}

	public void setBaseFolder(String path) {
		// TODO Auto-generated method stub

	}

	public void setID(String id) {
		// TODO Auto-generated method stub

	}

	public void setSearchString(String searchExpression) {
		// TODO Auto-generated method stub

	}

}
