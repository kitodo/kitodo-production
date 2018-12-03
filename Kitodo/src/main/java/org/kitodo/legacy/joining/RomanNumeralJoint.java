package org.kitodo.legacy.joining;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.RomanNumeralInterface;

public class RomanNumeralJoint implements RomanNumeralInterface {
    private static final Logger logger = LogManager.getLogger(RomanNumeralJoint.class);

    @Override
    public String getNumber() {
        logger.log(Level.TRACE, "getNumber()");
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public int intValue() {
        logger.log(Level.TRACE, "intValue()");
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setValue(int value) {
        logger.log(Level.TRACE, "setValue(value: {})", value);
        // TODO Auto-generated method stub
    }

    @Override
    public void setValue(String value) {
        logger.log(Level.TRACE, "setValue(value: \"{}\")");
        // TODO Auto-generated method stub
    }
}
