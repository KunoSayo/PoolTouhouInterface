package io.github.euonmyoji.pooltouhouinterface.script;

import io.github.euonmyoji.pooltouhouinterface.PthData;

/**
 * @author yinyangshi
 */
public class ScriptRunner {
    public ScriptContext context;
    public ScriptFunction function;
    public PthData pthData;
    public double x;
    public double y;

    public double px;
    public double py;

    public ScriptRunner(PthData pthData) {
        this.pthData = pthData;
    }

    public void run() {
        int start = pthData.calcStack.getTop();
        while (context.pointer < function.commands.length) {
            function.commands[context.pointer].exe(this);
            context.pointer += 1;
            if (context.wait > 0) {
                context.wait -= 1;
                return;
            }
        }
        if(pthData.calcStack.getTop() - start > 0) {
            System.out.println("not balance!");
        }
        context.pointer = 0;
    }
}
