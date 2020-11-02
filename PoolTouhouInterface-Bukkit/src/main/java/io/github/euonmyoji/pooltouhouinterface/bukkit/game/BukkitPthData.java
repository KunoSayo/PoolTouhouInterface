package io.github.euonmyoji.pooltouhouinterface.bukkit.game;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import io.github.euonmyoji.pooltouhouinterface.PthData;
import io.github.euonmyoji.pooltouhouinterface.bukkit.PoolTouhouInterface;
import io.github.euonmyoji.pooltouhouinterface.game.entity.Enemy;
import io.github.euonmyoji.pooltouhouinterface.game.entity.EnemyBullet;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptContext;
import io.github.euonmyoji.pooltouhouinterface.script.ScriptData;
import io.github.euonmyoji.pooltouhouinterface.util.UnsafeObjectPool;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class BukkitPthData extends PthData implements Runnable {
    public final UUID uuid;
    public Player mcPlayer;
    public Random random;
    public HashMap<String, ScriptData> scripts = new HashMap<>();


    public BukkitPthData(Player player, String main) {
        super();
        this.mcPlayer = player;
        Location location = player.getLocation();
        this.x = location.getBlockX() + 32;
        this.y = location.getBlockY() - 36;
        this.z = location.getBlockZ() - 64;
        this.uuid = player.getUniqueId();

        this.player = new PlayerImpl(this, player);

        for (int by = (int) this.y; by <= this.y + DY; by += 1) {
            for (int bz = (int) this.z; bz <= this.z + DZ; bz += 1) {
                PacketContainer block = PoolTouhouInterface.protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
                block.getBlockPositionModifier().write(0, new BlockPosition((int) this.x + 1, by, bz));
                block.getBlockData().write(0, WrappedBlockData.createData(Material.BLUE_WOOL));
                try {
                    PoolTouhouInterface.protocolManager.sendServerPacket(player, block);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            ScriptData data = new ScriptData(this, main, PoolTouhouInterface.scriptDir.resolve(main + ".pthpsb"));
            runner.function = data.functions.get("start");
            runner.context = new ScriptContext(data.dataCount, runner.function.maxStack);
            runner.run();
        } catch (IOException e) {
            player.sendMessage("Load main script failed.");
            e.printStackTrace();
        }

        new Thread(this).start();
    }

    @Override
    protected void setup() {
        random = new Random();

        EnemyBullet[] enemyBullets = new EnemyBullet[4096];
        for (int i = 0; i < enemyBullets.length; i++) {
            enemyBullets[i] = new EnemyBulletImpl(this);
        }
        this.enemyBullets = new UnsafeObjectPool<>(enemyBullets);

        Enemy[] enemies = new Enemy[1024];
        for (int i = 0; i < enemies.length; i++) {
            enemies[i] = new EnemyImpl(this);
        }
        this.enemies = new UnsafeObjectPool<>(enemies);
    }

    @Override
    public ScriptData getScript(String name) {
        ScriptData scriptData = scripts.get(name);
        if (scriptData == null) {
            try {
                scripts.put(name, scriptData = new ScriptData(this, name, PoolTouhouInterface.scriptDir.resolve(name + ".pthpsb")));
            } catch (IOException e) {
                System.out.println("loading script " + name + " failed.");
                e.printStackTrace();
            }
        }
        return scriptData;
    }

    @Override
    public void run() {
        final double msPer = 1000.0 / 20.0;
        long last = System.currentTimeMillis();
        while (running) {
            this.mcPlayer = Bukkit.getPlayer(this.uuid);
            super.tick();
            long now = System.currentTimeMillis();
            long delta = now - last;
            if (delta < msPer) {
                try {
                    //noinspection BusyWait :qp: (vegetable)
                    Thread.sleep((long) (msPer - delta));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            last = now;
        }

        mcPlayer = Bukkit.getServer().getPlayer(uuid);
        if (mcPlayer != null) {
            for (int by = (int) this.y; by <= this.y + DY; by += 1) {
                for (int bz = (int) this.z; bz <= this.z + DZ; bz += 1) {
                    PacketContainer block = PoolTouhouInterface.protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
                    block.getBlockPositionModifier().write(0, new BlockPosition((int) this.x + 1, by, bz));
                    block.getBlockData().write(0, WrappedBlockData.createData(Material.AIR));
                    try {
                        PoolTouhouInterface.protocolManager.sendServerPacket(mcPlayer, block);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        this.player.destroy();
        this.mcPlayer = null;
    }
}
