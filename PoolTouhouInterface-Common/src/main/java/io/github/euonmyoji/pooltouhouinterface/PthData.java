package io.github.euonmyoji.pooltouhouinterface;


import io.github.euonmyoji.pooltouhouinterface.game.ICollide;
import io.github.euonmyoji.pooltouhouinterface.game.entity.Enemy;
import io.github.euonmyoji.pooltouhouinterface.game.entity.EnemyBullet;
import io.github.euonmyoji.pooltouhouinterface.game.entity.PthPlayer;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptContext;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptData;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptDataSupplier;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptRunner;
import io.github.euonmyoji.pooltouhouinterface.script.callback.IScriptCallback;
import io.github.euonmyoji.pooltouhouinterface.script.callback.Kill;
import io.github.euonmyoji.pooltouhouinterface.script.callback.MoveUp;
import io.github.euonmyoji.pooltouhouinterface.util.CalcStack;
import io.github.euonmyoji.pooltouhouinterface.util.UnsafeObjectPool;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author yinyangshi
 */
public abstract class PthData {
    public static final String VERSION = "0.1.0";
    public static final double DX = 0;
    public static final double DY = 36 * 2;
    public static final double DZ = 64 * 2;
    public static final double SCALE = 12.5;
    public static boolean debug = false;
    public CalcStack calcStack = new CalcStack();
    public LinkedList<IScriptCallback> scriptCallback = new LinkedList<>();
    public ScriptRunner runner = new ScriptRunner(this);
    public volatile boolean running = true;
    public double x;
    public double y;
    public double z;
    protected PthPlayer player;
    /**
     * the bullet in the game
     * do not tick if the tick num = 0
     */
    public UnsafeObjectPool<EnemyBullet> enemyBullets;
    /**
     * the enemies in the game
     * do not tick if the tick num = 0
     */
    public UnsafeObjectPool<Enemy> enemies;

    protected PthData() {
        setup();
    }

    protected abstract void setup();

    public abstract ScriptData getScript(String name);

    protected void tick() {
        player.tick();
        this.runner.px = player.x;
        this.runner.py = player.y;

        {
            Iterator<EnemyBullet> iterator = enemyBullets.allocatedObjectIterator();
            it:
            while (iterator.hasNext()) {
                EnemyBullet entity = iterator.next();
                entity.tick(this);
                for (IScriptCallback iScriptCallback : this.scriptCallback) {
                    //noinspection ChainOfInstanceofChecks vegetable (
                    if (iScriptCallback instanceof Kill) {
                        entity.destroy();
                        iterator.remove();
                        this.scriptCallback.clear();
                        continue it;
                    } else if (iScriptCallback instanceof MoveUp) {
                        MoveUp moveUp = ((MoveUp) iScriptCallback);
                        entity.moveUp(moveUp.v);
                    }
                }
                if (entity.collide.collidePlayer(entity.x, entity.y, player)) {
                    player.biu();
                    iterator.remove();
                    continue;
                }
                this.scriptCallback.clear();
            }
        }
        {
            Iterator<Enemy> iterator = enemies.allocatedObjectIterator();
            while (iterator.hasNext()) {
                Enemy enemy = iterator.next();
                enemy.tick(this);
                for (IScriptCallback iScriptCallback : this.scriptCallback) {
                    if (iScriptCallback instanceof Kill) {
                        enemy.destroy();
                        iterator.remove();
                    }
                }
                this.scriptCallback.clear();
            }
        }
    }


    public void summonEnemy(String name, double x, double y, double hp, int collideType, ScriptDataSupplier[] collideArgs, ScriptData scriptData, ScriptRunner runner, ScriptDataSupplier[] dataSuppliers) {
        //the enemy pool must be reading but no choice.
        Enemy enemy = enemies.allocateObject();
        enemy.tick = 0;
        enemy.name = name;
        enemy.x = x;
        enemy.y = y;
        enemy.hp = hp;
        if (enemy.collide == null) {
            enemy.collide = ICollide.getCollide(collideType, runner, collideArgs);
        } else {
            enemy.collide = enemy.collide.changeCollide(collideType, runner, collideArgs);
        }
        enemy.scriptData = scriptData;
        if (enemy.scriptContext == null) {
            enemy.scriptContext = new ScriptContext(scriptData.dataCount, scriptData.tickFunction.maxStack);
        } else {
            enemy.scriptContext.pointer = 0;
            if (enemy.scriptContext.data.length < scriptData.dataCount) {
                enemy.scriptContext.data = new double[scriptData.dataCount];
            }
            enemy.scriptContext.wait = 0;
        }
        if (dataSuppliers != null) {
            for (int i = 0; i < Math.min(enemy.scriptContext.data.length, dataSuppliers.length); i++) {
                enemy.scriptContext.data[i] = dataSuppliers[i].get(runner);
            }
        }
        enemy.summon();
    }

    public void summonBullet(String name, double x, double y, double _scale, double angle, int collideType, ScriptDataSupplier[] collideArgs, ScriptData scriptData, ScriptRunner runner, ScriptDataSupplier[] dataSuppliers) {
        //the bullet pool must be reading but no choice.
        EnemyBullet bullet = enemyBullets.allocateObject();
        bullet.tick = 0;
        bullet.name = name;
        bullet.x = x;
        bullet.y = y;
        bullet.setAngle(angle);
        if (bullet.collide == null) {
            bullet.collide = ICollide.getCollide(collideType, runner, collideArgs);
        } else {
            bullet.collide = bullet.collide.changeCollide(collideType, runner, collideArgs);
        }
        bullet.scriptData = scriptData;
        if (bullet.scriptContext == null) {
            bullet.scriptContext = new ScriptContext(scriptData.dataCount, scriptData.tickFunction.maxStack);
        } else {
            bullet.scriptContext.pointer = 0;
            bullet.scriptContext.wait = 0;
            if (bullet.scriptContext.data.length < scriptData.dataCount) {
                bullet.scriptContext.data = new double[scriptData.dataCount];
            }
        }
        bullet.scriptContext.pointer = 0;
        bullet.scriptContext.wait = 0;
        if (dataSuppliers != null) {
            for (int i = 0; i < Math.min(bullet.scriptContext.data.length, dataSuppliers.length); i++) {
                bullet.scriptContext.data[i] = dataSuppliers[i].get(runner);
            }
        }
        bullet.summon();
    }
}
