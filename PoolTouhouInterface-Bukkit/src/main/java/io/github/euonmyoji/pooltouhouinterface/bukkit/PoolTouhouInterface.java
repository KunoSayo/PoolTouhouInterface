package io.github.euonmyoji.pooltouhouinterface.bukkit;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.github.euonmyoji.pooltouhouinterface.PthData;
import io.github.euonmyoji.pooltouhouinterface.bukkit.game.BukkitPthData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class PoolTouhouInterface extends JavaPlugin implements Listener {
    public static PoolTouhouInterface plugin;
    public static Path scriptDir;
    public static HashMap<UUID, PthData> runningGame = new HashMap<>();
    public static ProtocolManager protocolManager;

    @Override
    public void onLoad() {
        plugin = this;
        Path cfgDir = getDataFolder().toPath();
        scriptDir = cfgDir.resolve("script");
        try {
            Files.createDirectories(scriptDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (args.length == 0 || !(sender instanceof Player)) {
            sender.sendMessage("PTHI: " + PthData.VERSION);
            return true;
        } else {
            switch (args[0]) {
                case "start": {
                    if (args.length < 2) {
                        //I d o n o t w a n t t o t e l l y o u t h e a r g s.
                        return false;
                    }
                    String scriptToInit = args[1];
                    Player player = ((Player) sender);

                    Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                        PthData old = runningGame.put(player.getUniqueId(), new BukkitPthData(player, scriptToInit));
                        if (old != null) {
                            old.running = false;
                        }
                    });
                    return true;
                }
                case "stop": {
                    PthData pthData = runningGame.remove(((Player) sender).getUniqueId());
                    if (pthData != null) {
                        pthData.running = false;
                        return true;
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PthData pthData = runningGame.remove(event.getPlayer().getUniqueId());
        if (pthData != null) {
            pthData.running = false;
        }
    }

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
    }
}
