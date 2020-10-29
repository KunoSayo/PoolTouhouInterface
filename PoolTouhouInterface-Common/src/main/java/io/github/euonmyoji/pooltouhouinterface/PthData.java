package io.github.euonmyoji.pooltouhouinterface;


import io.github.euonmyoji.pooltouhouinterface.entity.Enemy;
import io.github.euonmyoji.pooltouhouinterface.entity.EnemyBullet;
import io.github.euonmyoji.pooltouhouinterface.game.ICollide;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptData;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptDataSupplier;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptRunner;
import io.github.euonmyoji.pooltouhouinterface.script.callback.IScriptCallback;
import io.github.euonmyoji.pooltouhouinterface.util.CalcStack;
import io.github.euonmyoji.pooltouhouinterface.util.UnsafeObjectPool;

import java.util.LinkedList;

/**
 * @author yinyangshi
 */
public abstract class PthData {
    public static final String VERSION = "0.1.0";
    public static final double DX = 0;
    public static final double DY = 36 * 2;
    public static final double DZ = 64 * 2;
    public CalcStack calcStack = new CalcStack();
    public LinkedList<IScriptCallback> scriptCallback;
    public double x;
    public double y;
    public double z;
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
    public volatile boolean running = true;
    protected double py;
    protected double pz;

    public abstract ScriptData getScript(String name);

    /*
     * the (yaw, pitch) in the minecraft F3 (1.16.3 with OPTIFINE)

     * ^x (yaw = -90)
     * |
     * |  .(z, x)
     * |
     * O------->z   (yaw = 0)
     * |
     * |
     * |
     * V-x (yaw = 90)
     *
     * see top (pitch = -90)
     */

    public void tick(double x, double y, double z, double yaw, double pitch) {
        if (Math.abs(pitch) < 90.0) {
            double delta = x - this.x;
            boolean calc = false;
            if (delta < 0) {
                if (yaw <= 0 || yaw >= 180) {
                    calc = true;
                }
            } else {
                if (yaw >= 0) {
                    calc = true;
                }
            }
            if (calc) {
                double vx = Math.sin(yaw * Math.PI / 180.0);
                double vy = -Math.sin(pitch * Math.PI / 180.0);
                double vz = Math.cos(yaw * Math.PI / 180.0);
//                double len = Math.sqrt(vx * vx + vy * vy + vz * vz);
//                vx /= len;
//                vy /= len;
//                vz /= len;
                double k = (delta / vx);
                double py = y + vy * k;
                double pz = z + vz * k;
                changeIfInGame(py, pz);
            }
        }
        onPlayerLocationCallback(this.x, py, pz);
        //todo: not done yet && logic
    }

    public abstract void onPlayerLocationCallback(double x, double y, double z);

    private void changeIfInGame(double y, double z) {
        if (y >= this.y && y <= this.y + DY && z >= this.z && z <= this.z + DZ) {
            this.py = y;
            this.pz = z;
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
        if (enemy.scriptContext.data.length < scriptData.dataCount) {
            enemy.scriptContext.data = new double[scriptData.dataCount];
        }
        for (int i = 0; i < Math.min(enemy.scriptContext.data.length, dataSuppliers.length); i++) {
            enemy.scriptContext.data[i] = dataSuppliers[i].get(runner);
        }
    }

    public void summonBullet(String name, double x, double y, double scale, double angle, int collideType, ScriptDataSupplier[] collideArgs, ScriptData scriptData, ScriptRunner runner, ScriptDataSupplier[] dataSuppliers) {
        //the bullet pool must be reading but no choice.
        EnemyBullet bullet = enemyBullets.allocateObject();
        bullet.tick = 0;
        bullet.name = name;
        bullet.x = x;
        bullet.y = y;
        bullet.angle = angle;
        if (bullet.collide == null) {
            bullet.collide = ICollide.getCollide(collideType, runner, collideArgs);
        } else {
            bullet.collide = bullet.collide.changeCollide(collideType, runner, collideArgs);
        }
        bullet.scriptData = scriptData;
        if (bullet.scriptContext.data.length < scriptData.dataCount) {
            bullet.scriptContext.data = new double[scriptData.dataCount];
        }
        for (int i = 0; i < Math.min(bullet.scriptContext.data.length, dataSuppliers.length); i++) {
            bullet.scriptContext.data[i] = dataSuppliers[i].get(runner);
        }
    }
}
