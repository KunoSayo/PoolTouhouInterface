package io.github.euonmyoji.pooltouhouinterface.bukkit.game;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import io.github.euonmyoji.pooltouhouinterface.PthData;
import io.github.euonmyoji.pooltouhouinterface.bukkit.PoolTouhouInterface;
import io.github.euonmyoji.pooltouhouinterface.game.entity.EnemyBullet;
import org.bukkit.entity.EntityType;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class EnemyBulletImpl extends EnemyBullet {
    private final BukkitPthData pthData;
    private final UUID uuid;
    private final int eId;


    public EnemyBulletImpl(BukkitPthData pthData) {
        this.pthData = pthData;
        eId = pthData.random.nextInt(Integer.MAX_VALUE);
        uuid = UUID.randomUUID();
    }

    @Override
    public void summon() {
        if (pthData.mcPlayer != null) {
            PacketContainer stgPlayerSpawn = PoolTouhouInterface.protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
            stgPlayerSpawn.getEntityTypeModifier().write(0, EntityType.FIREBALL);

            stgPlayerSpawn.getIntegers()
                    .write(0, eId)
                    .write(1, 0)
                    .write(2, 0)
                    .write(3, 0)
                    .write(4, 0)
                    .write(5, 0)
                    .write(6, 0);
            stgPlayerSpawn.getUUIDs().write(0, uuid);
            stgPlayerSpawn.getDoubles().write(0, pthData.x)
                    .write(1, (this.x / PthData.SCALE) + pthData.y)
                    .write(2, (this.y / PthData.SCALE) + pthData.z);
            try {
                PoolTouhouInterface.protocolManager.sendServerPacket(pthData.mcPlayer, stgPlayerSpawn);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void tick(PthData pthData) {
        super.tick(pthData);
        if (this.pthData.mcPlayer != null) {
            PacketContainer move = PoolTouhouInterface.protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
            move.getBytes().writeDefaults();
            move.getIntegers().write(0, eId);
            move.getBooleans().write(0, false);
            move.getDoubles().write(0, pthData.x).write(1, (this.x / PthData.SCALE) + pthData.y).write(2, (this.y / PthData.SCALE) + pthData.z);
            try {
                PoolTouhouInterface.protocolManager.sendServerPacket(this.pthData.mcPlayer, move);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroy() {
        if (pthData.mcPlayer != null) {
            PacketContainer des = PoolTouhouInterface.protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            des.getIntegerArrays().write(0, new int[]{eId});
            try {
                PoolTouhouInterface.protocolManager.sendServerPacket(pthData.mcPlayer, des);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
