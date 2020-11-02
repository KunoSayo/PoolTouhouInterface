package io.github.euonmyoji.pooltouhouinterface.bukkit.util;

import io.github.euonmyoji.pooltouhouinterface.PthData;
import io.github.euonmyoji.pooltouhouinterface.bukkit.PoolTouhouInterface;
import io.github.euonmyoji.pooltouhouinterface.bukkit.game.BukkitPthData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;

import java.util.UUID;

/**
 * @author yinyangshi
 */
public class PlayerDataGetter implements Runnable {
    private final PthData pthData;
    public UUID uuid;
    public volatile double x;
    public volatile double y;
    public volatile double z;
    public volatile double yaw;
    public volatile double pitch;

    public PlayerDataGetter(UUID uuid, BukkitPthData pthData) {
        this.uuid = uuid;
        this.pthData = pthData;
        Bukkit.getScheduler().runTaskLater(PoolTouhouInterface.plugin, this, 1);
    }

    @Override
    public void run() {
        CraftPlayer player = ((CraftPlayer) Bukkit.getServer().getPlayer(uuid));
        if (player != null) {
            Location location = player.getEyeLocation();

            x = location.getX();
            y = location.getY();
            z = location.getZ();
            yaw = location.getYaw();
            pitch = location.getPitch();
        }
        if (pthData.running) {
            Bukkit.getScheduler().runTaskLater(PoolTouhouInterface.plugin, this, 1);
        }
    }
}
