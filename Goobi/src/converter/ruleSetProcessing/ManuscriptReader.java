package converter.ruleSetProcessing;


public class ManuscriptReader extends RulesetReader {

	
	
	public ManuscriptReader(String filePath){
		super(filePath);
		super.myLogger.info("Manuscript ruleset path set as '" + filePath + "'");
	}

}
