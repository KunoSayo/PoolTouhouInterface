package io.github.euonmyoji.pooltouhouinterface.script;

/**
 * @author yinyangshi
 */
public class Loop {
    public Type type;
    public int pointer;
    public Loop(Type type, int pointer) {
        this.type = type;
        this.pointer = pointer;
    }

    public enum Type {
        START, END
    }
}
