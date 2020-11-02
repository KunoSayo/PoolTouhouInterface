package io.github.euonmyoji.pooltouhouinterface.bukkit.game;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import io.github.euonmyoji.pooltouhouinterface.PthData;
import io.github.euonmyoji.pooltouhouinterface.bukkit.PoolTouhouInterface;
import io.github.euonmyoji.pooltouhouinterface.bukkit.util.PlayerDataGetter;
import io.github.euonmyoji.pooltouhouinterface.game.entity.PthPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class PlayerImpl extends PthPlayer {
    private final PlayerDataGetter pd;
    private final UUID uuid;
    private final UUID entityUUID;
    private final int eId;


    public PlayerImpl(BukkitPthData pthData, Player mcPlayer) {
        super(pthData);
        this.x = 1600 >> 1;
        this.y = 0;
        this.radius = 3.0;
        pd = new PlayerDataGetter(mcPlayer.getUniqueId(), pthData);
        eId = pthData.random.nextInt();
        uuid = mcPlayer.getUniqueId();
        entityUUID = UUID.randomUUID();

        PacketContainer stgPlayerSpawn = PoolTouhouInterface.protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        stgPlayerSpawn.getEntityTypeModifier().write(0, EntityType.MINECART);

        stgPlayerSpawn.getIntegers()
                .write(0, eId)
                .write(1, 0)
                .write(2, 0)
                .write(3, 0)
                .write(4, 0)
                .write(5, 0)
                .write(6, 0);
        stgPlayerSpawn.getUUIDs().write(0, entityUUID);
        stgPlayerSpawn.getDoubles().write(0, pthData.x)
                .write(1, this.x * PthData.SCALE + pthData.y)
                .write(2, this.y * PthData.SCALE + pthData.z);
        try {
            PoolTouhouInterface.protocolManager.sendServerPacket(mcPlayer, stgPlayerSpawn);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tick() {
        calcPos(pd.x, pd.y, pd.z, pd.yaw, pd.pitch);
        Player player = Bukkit.getServer().getPlayer(uuid);
        if (player != null) {
            PacketContainer move = PoolTouhouInterface.protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
            move.getBytes().writeDefaults();
            move.getIntegers().write(0, eId);
            move.getDoubles().write(0, pthData.x).write(1, this.x / PthData.SCALE + pthData.y).write(2, this.y / PthData.SCALE + pthData.z);
            move.getBooleans().write(0, false);
            try {
                PoolTouhouInterface.protocolManager.sendServerPacket(player, move);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void biu() {
//        pthData.running = false;
    }

    @Override
    public void destroy() {
        Player player = Bukkit.getServer().getPlayer(uuid);
        if (player != null) {
            PacketContainer des = PoolTouhouInterface.protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            des.getIntegerArrays().write(0, new int[]{eId});
            try {
                PoolTouhouInterface.protocolManager.sendServerPacket(player, des);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
