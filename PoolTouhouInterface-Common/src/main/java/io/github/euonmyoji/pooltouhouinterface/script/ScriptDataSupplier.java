package io.github.euonmyoji.pooltouhouinterface.script;

/**
 * @author yinyangshi
 */
@FunctionalInterface
public interface ScriptDataSupplier {
    double get(ScriptRunner runner);
}

@FunctionalInterface
interface ScriptDataSetter {
    void set(ScriptRunner runner);
}