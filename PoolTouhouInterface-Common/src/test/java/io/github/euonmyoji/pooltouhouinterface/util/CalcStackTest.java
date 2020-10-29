package io.github.euonmyoji.pooltouhouinterface.util;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * @author yinyangshi
 */
public class CalcStackTest {

    @Test
    public void testOperator() {
        CalcStack calcStack = new CalcStack();
        calcStack.push(3.0);
        calcStack.push(2.0);
        calcStack.sub();
        TestCase.assertEquals(1.0, calcStack.pop());
    }
}
