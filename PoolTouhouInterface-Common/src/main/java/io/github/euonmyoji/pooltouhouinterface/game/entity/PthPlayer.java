package io.github.euonmyoji.pooltouhouinterface.game.entity;

import io.github.euonmyoji.pooltouhouinterface.PthData;

/**
 * the x is in pth x
 * the y is in pth y
 * @author yinyangshi
 */
public abstract class PthPlayer {
    public double x;
    public double y;
    public double radius;
    protected PthData pthData;

    public PthPlayer(PthData pthData) {
        this.pthData = pthData;
    }

    public abstract void tick();

    public abstract void biu();

    public abstract void destroy();

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

    protected void calcPos(double x, double y, double z, double yaw, double pitch) {
        if (Math.abs(pitch) < 90.0) {
            double delta = x - pthData.x;
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
                double k = (delta / vx);
                double py = y + vy * k;
                double pz = z + vz * k;
                changeIfInGame(py, pz);
            }
        }
    }

    private void changeIfInGame(double y, double z) {
        if (y >= pthData.y && y <= pthData.y + PthData.DY && z >= pthData.z && z <= pthData.z + PthData.DZ) {
            this.x = (y - pthData.y) * 12.5;
            this.y = (z - pthData.z) * 12.5;
            pthData.runner.px = this.x;
            pthData.runner.py = this.y;
        }
    }

}
