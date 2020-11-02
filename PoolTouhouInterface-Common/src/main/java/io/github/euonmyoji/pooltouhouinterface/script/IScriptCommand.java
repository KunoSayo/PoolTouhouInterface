package io.github.euonmyoji.pooltouhouinterface.script;

/**
 * @author yinyangshi
 */
@FunctionalInterface
public interface IScriptCommand {
    /**
     * 执行相关操作
     *
     * @param runner 环境
     * @return true if do not continue run
     */
    void exe(ScriptRunner runner);
}

