package io.github.euonmyoji.pooltouhouinterface.bukkit.game;

import io.github.euonmyoji.pooltouhouinterface.game.entity.Enemy;

import java.util.UUID;

/**
 * @author yinyangshi
 */
public class EnemyImpl extends Enemy {
    private final UUID uuid;
    private final int eId;


    public EnemyImpl(BukkitPthData pthData) {
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
