package io.github.euonmyoji.pooltouhouinterface.game.entity;

/**
 * @author yinyangshi
 */
public abstract class EnemyBullet extends PthEntity {
    private double angle = 0;
    private double dx = 1;
    private double dy = 0;

    public void setAngle(double angle) {
        this.angle = angle;
        double a = (angle + 90) * Math.PI / 180.0;
        dx = Math.sin(a);
        dy = Math.cos(a);
    }

    public void moveUp(double v) {
        this.x += dx * v;
        this.y += dy * v;
    }
}
