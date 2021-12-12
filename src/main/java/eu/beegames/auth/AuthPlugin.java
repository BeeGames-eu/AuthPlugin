package eu.beegames.auth;

import eu.beegames.auth.listener.PortalListener;
import eu.beegames.auth.listener.RegionListener;
import fr.xephi.authme.api.v3.AuthMeApi;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashSet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@SuppressWarnings("unused")
public class AuthPlugin extends JavaPlugin implements Listener {
    public BukkitScheduler sched;
    public byte [] connectionMessage;
    private Location spawnLoc;

    public ConfigWrapper cfg;
    
    public HashSet<Player> debounceAuth = new HashSet<>();
    public HashSet<Player> debounceJoin = new HashSet<>();
    
    public AuthMeApi authmeApi;
    

    @Override
    public void onEnable() {
        Server s = getServer();
        sched = s.getScheduler();
        Messenger m = s.getMessenger();
        m.registerOutgoingPluginChannel(this, "BungeeCord");

        saveDefaultConfig();

        cfg = new ConfigWrapper(getConfig());

        switch (cfg.getTargetMode()) {
            case PORTAL:
                s.getPluginManager().registerEvents(new PortalListener(this), this);
                break;
            case REGION:
                s.getPluginManager().registerEvents(new RegionListener(this), this);
                break;
            default: {
                getLogger().severe("Don't know how to handle " + cfg.getTargetMode() + " yet");
                s.getPluginManager().disablePlugin(this);
                return;
            }
        }

        s.getPluginManager().registerEvents(this, this);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (DataOutputStream dos = new DataOutputStream(bos)) {
                dos.writeUTF("Connect");
                dos.writeUTF(cfg.getDestination());
            }

            connectionMessage = bos.toByteArray();
        } catch (IOException e) {
            getLogger().severe("!!! Couldn't prepare connection message");
        }


        spawnLoc = s.getWorld(cfg.getWorldName()).getSpawnLocation().add(.5, 0, .5);

        spawnLoc.setYaw(cfg.getSpawnYaw());
        spawnLoc.setPitch(cfg.getSpawnPitch());
        
        authmeApi = AuthMeApi.getInstance();
        getLogger().info("AuthPlugin is enabled");
    }


    
    @EventHandler
    public void on(PlayerRespawnEvent ev) {
        ev.setRespawnLocation(spawnLoc);
    }

    @EventHandler
    public void on(PlayerJoinEvent ev) {
        ev.setJoinMessage("");

        Player p = ev.getPlayer();
        debounceJoin.add(p);
        sched.runTaskLater(this, () -> p.teleport(spawnLoc), 3);
        
        sched.runTaskLater(this, () -> debounceJoin.remove(p), 40);

        if (!p.hasPermission("eu.beegames.auth.bypass_kick_timeout")) {
            sched.runTaskLater(this, () ->
                sched.runTaskLater(this, new Runnable() {
                    private byte left = 5;
                    @Override
                    public void run() {
                        if (left == 0) {
                            p.kickPlayer("§cByl jsi vyhozen za neaktivitu na auth serveru!");
                            return;
                        }

                        if (left == 5) p.sendMessage("§cBudes vyhozen za neaktivitu za " + left + " sekund!");

                        left--;
                        sched.runTaskLater(AuthPlugin.this, this, 20);
                    }
                }, 20), 2300);
        }
    }

    @EventHandler
    public void on(AsyncPlayerChatEvent ev) {
        ev.setCancelled(true);
    }

    // Just in case, adventure mode might not be enough
    @EventHandler
    public void on(BlockBreakEvent ev) {
        ev.setCancelled(!ev.getPlayer().isOp());
    }
    
    @EventHandler
    public void on(PlayerQuitEvent ev) {
        ev.setQuitMessage("");
    
        // Prevent players from spawning in the portal when joining
        ev.getPlayer().teleport(spawnLoc);
    }
    
    @EventHandler
    public void on(EntityDamageEvent ev) {
        ev.setCancelled(true);
    }
}
