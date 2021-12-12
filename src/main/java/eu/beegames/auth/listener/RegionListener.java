package eu.beegames.auth.listener;

import eu.beegames.auth.AuthPlugin;
import eu.beegames.auth.data.VectorPair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegionListener implements Listener {
    private final AuthPlugin plugin;

    private final VectorPair vLoc;

    public RegionListener(AuthPlugin _plugin) {
        plugin = _plugin;

        vLoc = plugin.cfg.getTargetRegion();
    }

    @EventHandler
    public void on(PlayerMoveEvent ev) {
        Location from = ev.getFrom();
        Location to = ev.getTo();
        Player p = ev.getPlayer();

        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) return;

        if (to.toVector().isInAABB(vLoc.from, vLoc.to)) {
            if(!plugin.authmeApi.isAuthenticated(p)) {
                plugin.getLogger().info("Player " + p.getName() + " tried to teleport themselves without authentication! Are they hacking or is the server spawning the player somewhere else?");
                ev.setCancelled(true);
                return;
            }

            if (plugin.debounceJoin.contains(p)) plugin.getLogger().info("Debounced immediate lobby teleport after join for " + p.getName());

            // Debounce the spawn teleport and/or events from other tiles, just in case
            if (!plugin.debounceJoin.contains(p) && !plugin.debounceAuth.contains(p)) {
            
                // QUIRK: For some reason, authmebungee might sometimes consider you unauthed
                
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    try (DataOutputStream dos = new DataOutputStream(bos)) {
                        dos.writeUTF("Forward");
                        dos.writeUTF("ONLINE");
                        dos.writeUTF("AuthMe.v2.Broadcast");
                        
                        try (ByteArrayOutputStream _bos = new ByteArrayOutputStream()) {
                            try (DataOutputStream _dos = new DataOutputStream(_bos)) {
                                _dos.writeUTF("login");
                                _dos.writeUTF(p.getName());
                            }
                            
                            final byte[] dataBytes = _bos.toByteArray();
                            dos.writeShort(dataBytes.length);
                            dos.write(dataBytes);
                        }
                    }

                    p.sendPluginMessage(plugin, "BungeeCord", bos.toByteArray());
                } catch (IOException e) {
                    plugin.getLogger().severe("!!! Couldn't send quirk-fixing plugin message");
                }
                
                p.sendPluginMessage(plugin, "BungeeCord", plugin.connectionMessage);
                plugin.debounceAuth.add(p);
                plugin.sched.runTaskLater(plugin, () -> plugin.debounceAuth.remove(p), 60);
            }
        }
    }
}
