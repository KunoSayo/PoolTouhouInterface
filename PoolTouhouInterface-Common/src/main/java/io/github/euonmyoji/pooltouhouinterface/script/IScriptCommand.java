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
     */
    void exe(ScriptRunner runner);
}

