package io.github.euonmyoji.pooltouhouinterface.game.entity;

import io.github.euonmyoji.pooltouhouinterface.PthData;
import io.github.euonmyoji.pooltouhouinterface.game.ICollide;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptContext;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptData;

/**
 * @author yinyangshi
 */
public abstract class PthEntity {
    public String name;
    public double x;
    public double y;
    public ICollide collide;
    public ScriptData scriptData;
    public ScriptContext scriptContext;

    public int tick;

    public void tick(PthData pthData) {
        if (tick++ != 0) {
            if (scriptContext.wait == 0) {
                pthData.runner.context = scriptContext;
                pthData.runner.function = scriptData.tickFunction;
                pthData.runner.x = this.x;
                pthData.runner.y = this.y;
                pthData.runner.run();
                this.x = pthData.runner.x;
                this.y = pthData.runner.y;
            } else {
                scriptContext.wait -= 1;
            }
        }
    }

    public abstract void summon();

    public abstract void destroy();
}
