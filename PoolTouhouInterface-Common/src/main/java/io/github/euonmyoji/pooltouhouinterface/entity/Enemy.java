package io.github.euonmyoji.pooltouhouinterface.entity;

import io.github.euonmyoji.pooltouhouinterface.game.ICollide;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptContext;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptData;

/**
 * @author yinyangshi
 */
public class Enemy {
    public String name;
    public double x;
    public double y;
    public double hp;
    public ICollide collide;
    public ScriptData scriptData;
    public ScriptContext scriptContext;

    public int tick;
}
