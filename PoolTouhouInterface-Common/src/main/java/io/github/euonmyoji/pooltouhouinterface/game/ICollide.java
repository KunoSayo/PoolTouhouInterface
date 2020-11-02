package io.github.euonmyoji.pooltouhouinterface.game;

import io.github.euonmyoji.pooltouhouinterface.game.entity.PthPlayer;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptDataSupplier;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptRunner;

/**
 * @author yinyangshi
 */
public interface ICollide {

    static int getCollideArgCount(int arg) {
        switch (arg) {
            case 10: {
                return 1;
            }
            default: {
                throw new IllegalArgumentException("No such collide type: " + arg);
            }
        }
    }

    static ICollide getCollide(int collideType, ScriptRunner runner, ScriptDataSupplier[] aiArgsArrayS) {
        switch (collideType) {
            case 10: {
                return new Circle(aiArgsArrayS[0].get(runner));
            }
            default: {
                throw new IllegalArgumentException("No such collide type: " + collideType);
            }
        }
    }

    ICollide changeCollide(int collideType, ScriptRunner runner, ScriptDataSupplier[] args);

    boolean collidePlayer(double x, double y, PthPlayer player);

    class Circle implements ICollide {
        public double radius;

        private Circle(double radius) {
            this.radius = radius;
        }

        @Override
        public ICollide changeCollide(int collideType, ScriptRunner runner, ScriptDataSupplier[] args) {
            if (collideType == 10) {
                this.radius = args[0].get(runner);
            }
            return this;
        }

        @Override
        public boolean collidePlayer(double x, double y, PthPlayer player) {
            double dx = player.x - x;
            double dy = player.y - y;
            return dx * dx + dy * dy < player.radius * player.radius + this.radius * this.radius;
        }
    }
}
