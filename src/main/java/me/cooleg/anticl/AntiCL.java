package me.cooleg.anticl;

import net.minecraft.network.protocol.game.PacketPlayOutKeepAlive;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

public final class AntiCL extends JavaPlugin implements Listener {

    final int logoutTimer = 30;

    HashMap<UUID, Long> taggedList = new HashMap<>();
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        startRunnable();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) {return;}
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) {
            return;
        }
        UUID victim = (e.getEntity()).getUniqueId();

        if (taggedList.containsKey(victim)) {
            taggedList.put(victim, Instant.now().getEpochSecond() + logoutTimer);
            return;
        }
        taggedList.put(victim, Instant.now().getEpochSecond() + logoutTimer);
        e.getEntity().sendMessage(ChatColor.RED + "You have been combat tagged!");
        e.getEntity().sendMessage(ChatColor.RED + "Do not log out until told it is safe to do so.");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (taggedList.containsKey(e.getEntity().getUniqueId())) {
            taggedList.remove(e.getEntity().getUniqueId());
            String victim = e.getEntity().getName();
            e.setDeathMessage(victim + " wimped out of a fight and died ");
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (!taggedList.containsKey(e.getPlayer().getUniqueId())) {return;}
        e.getPlayer().setHealth(0);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!taggedList.containsKey(e.getPlayer().getUniqueId())) {return;}
        e.getPlayer().sendMessage(ChatColor.RED + "You may not run commands when combat tagged!");
        e.setCancelled(true);
        return;
    }

    private void startRunnable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!taggedList.containsKey(p.getUniqueId())) {continue;}
                    if (Instant.now().getEpochSecond() > taggedList.get(p.getUniqueId())) {
                        taggedList.remove(p.getUniqueId());
                        p.sendMessage(ChatColor.GREEN + "You can now safely log out.");
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }


}
