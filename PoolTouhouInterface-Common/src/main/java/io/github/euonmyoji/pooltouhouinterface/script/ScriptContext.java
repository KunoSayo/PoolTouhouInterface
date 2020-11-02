package io.github.euonmyoji.pooltouhouinterface.script;

/**
 * @author yinyangshi
 */
public class ScriptContext {
    public int pointer = 0;
    public double[] data;
    public double[] stack;
    public int wait = 0;

    public ScriptContext(int dataCount, int maxStack) {
        this.data = new double[dataCount];
        this.stack = new double[maxStack];
    }
}
