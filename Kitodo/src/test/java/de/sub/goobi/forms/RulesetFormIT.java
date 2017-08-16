package de.sub.goobi.forms;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.data.database.beans.Ruleset;

public class RulesetFormIT {

    private RulesetForm rulesetForm = new RulesetForm();
    private Ruleset ruleset;

    @Test
    public void shouldCreateRuleset(){
        ruleset = rulesetForm.getRuleset();
        Assert.assertNull("ruleset should be null",ruleset);
        rulesetForm.createNewRuleset();
        ruleset = rulesetForm.getRuleset();
        Assert.assertNotNull("ruleset should not be null",ruleset);
    }

    @Test
    public void shouldRedirectAtRulesetCreation(){
        String shouldRedirect = "/pages/RegelsaetzeBearbeiten?faces-redirect=true";
        String Redirect = rulesetForm.createNewRuleset();
        assertEquals("Should redirect to RegelsaetzeBearbeiten",shouldRedirect,Redirect);
    }

    @Test
    public void shouldReturnNullOnSaveRulesetIfXmlNotFound(){
        rulesetForm.createNewRuleset();
        ruleset = rulesetForm.getRuleset();
        ruleset.setFile("NotExisting.xml");
        ruleset.setTitle("NotExisting");
        rulesetForm.setRuleset(ruleset);
        Assert.assertNull("saveRuleset should return null",rulesetForm.saveRuleset());
    }
}
