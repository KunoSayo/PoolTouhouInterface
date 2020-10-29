package io.github.euonmyoji.pooltouhouinterface.bukkit.game;

import io.github.euonmyoji.pooltouhouinterface.PthData;
import io.github.euonmyoji.pooltouhouinterface.bukkit.PoolTouhouInterface;
import io.github.euonmyoji.pooltouhouinterface.bukkit.util.PlayerDataGetter;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptData;
import net.minecraft.server.v1_16_R2.Block;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.PacketPlayOutBlockChange;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;

import java.util.UUID;

/**
 * @author yinyangshi
 */
public class BukkitPthData extends PthData implements Runnable {
    private final UUID uuid;
    private final PlayerDataGetter pd;
    private BlockPosition last;

    public BukkitPthData(CraftPlayer player) {
        Location location = player.getLocation();
        this.x = location.getX() + 32;
        this.y = location.getY() - 36;
        this.z = location.getZ() - 64;
        this.uuid = player.getUniqueId();
        this.py = this.y + 36;
        this.pz = this.z;
        pd = new PlayerDataGetter(uuid, this);
        new Thread(this).start();
    }

    @Override
    public ScriptData getScript(String name) {
        return null;
    }

    @Override
    public void onPlayerLocationCallback(double x, double y, double z) {
        CraftPlayer player = ((CraftPlayer) Bukkit.getServer().getPlayer(uuid));
        if (player != null) {
            if(last != null) {
                player.getHandle().playerConnection
                        .sendPacket(new PacketPlayOutBlockChange(last,
                                Block.REGISTRY_ID.fromId(0)));
            }
            player.getHandle().playerConnection
                    .sendPacket(new PacketPlayOutBlockChange(last = new BlockPosition(x, y, z),
                            Block.REGISTRY_ID.fromId(66)));
        }
    }

    @Override
    public void run() {
        while (running) {
            super.tick(pd.x, pd.y, pd.z, pd.yaw, pd.pitch);
            //todo:not done yet
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
