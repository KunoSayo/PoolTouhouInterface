package io.github.euonmyoji.pooltouhouinterface.bukkit.game;

import io.github.euonmyoji.pooltouhouinterface.game.entity.EnemyBullet;

import java.util.UUID;

public class EnemyBulletImpl extends EnemyBullet {
    private final UUID uuid;
    private final int eId;


    public EnemyBulletImpl(BukkitPthData pthData) {
        eId = pthData.random.nextInt(Integer.MAX_VALUE);
        uuid = UUID.randomUUID();
    }

    @Override
    public void summon() {

    }

    @Override
    public void destroy() {

    }
}
