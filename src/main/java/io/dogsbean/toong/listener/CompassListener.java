package io.dogsbean.toong.listener;

import io.dogsbean.toong.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CompassListener implements Listener {

    private final Main plugin;
    private final Map<UUID, BukkitRunnable> activeTrails;
    private final Map<UUID, Long> lastCompassUse;

    public CompassListener(Main plugin) {
        this.plugin = plugin;
        this.activeTrails = new HashMap<>();
        this.lastCompassUse = new HashMap<>();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (player.getInventory().getItemInMainHand().getType() != Material.COMPASS) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (lastCompassUse.containsKey(player.getUniqueId())) {
            if (currentTime - lastCompassUse.get(player.getUniqueId()) < 1000) {
                return;
            }
        }
        lastCompassUse.put(player.getUniqueId(), currentTime);

        Location nearestTemple = plugin.getTempleManager().getNearestAltar(player.getLocation());

        if (nearestTemple == null) {
            player.sendMessage("§c근처에 신전이 없습니다!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);
            return;
        }

        double distance = player.getLocation().distance(nearestTemple);

        if (distance < 5) {
            player.sendMessage("§a§l이미 신전 근처에 있습니다!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 2.0f);
            return;
        }

        player.sendMessage("§6§l✦ §e신전의 기운이 느껴집니다...");
        player.sendMessage("§7거리: §a" + String.format("%.1f", distance) + "m");
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.2f);

        startPathTrail(player, nearestTemple);
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Material newItem = player.getInventory().getItem(event.getNewSlot()) != null
                ? player.getInventory().getItem(event.getNewSlot()).getType()
                : Material.AIR;

        if (newItem != Material.COMPASS) {
            stopPathTrail(player);
        }
    }

    private void startPathTrail(Player player, Location target) {
        stopPathTrail(player);

        BukkitRunnable trail = new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.getInventory().getItemInMainHand().getType() != Material.COMPASS) {
                    cancel();
                    activeTrails.remove(player.getUniqueId());
                    return;
                }

                Location playerLoc = player.getLocation().clone();
                Location targetLoc = target.clone();

                if (playerLoc.distance(targetLoc) < 5) {
                    player.sendMessage("§a§l✦ 신전에 도착했습니다! ✦");
                    player.playSound(playerLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);

                    for (int i = 0; i < 30; i++) {
                        double angle = i * 12;
                        double radian = Math.toRadians(angle);
                        double x = Math.cos(radian) * 2;
                        double z = Math.sin(radian) * 2;
                        Location particleLoc = playerLoc.clone().add(x, 0.5, z);
                        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 3, 0.2, 0.2, 0.2, 0);
                        player.getWorld().spawnParticle(Particle.GLOW, particleLoc, 2, 0.1, 0.1, 0.1, 0);
                    }

                    cancel();
                    activeTrails.remove(player.getUniqueId());
                    return;
                }

                Vector direction = targetLoc.toVector().subtract(playerLoc.toVector()).normalize();

                for (int i = 1; i <= 8; i++) {
                    Location pathPoint = playerLoc.clone().add(direction.clone().multiply(i * 1.5));
                    pathPoint.add(0, 0.3 + Math.sin(tick * 0.3 + i * 0.5) * 0.4, 0);

                    player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, pathPoint, 1, 0.1, 0.1, 0.1, 0);
                    player.getWorld().spawnParticle(Particle.GLOW, pathPoint, 2, 0.1, 0.1, 0.1, 0);
                    player.getWorld().spawnParticle(Particle.COMPOSTER, pathPoint, 1, 0.05, 0.05, 0.05, 0);
                }

                Location arrowLoc = playerLoc.clone().add(direction.clone().multiply(12));
                arrowLoc.setY(playerLoc.getY() + 1.5);

                if (tick % 60 == 0) {
                    double remainingDistance = playerLoc.distance(targetLoc);
                    player.sendActionBar("§6§l신전까지 §a§l" + String.format("%.1f", remainingDistance) + "m");
                }

                tick++;
            }
        };

        trail.runTaskTimer(plugin, 0L, 3L);
        activeTrails.put(player.getUniqueId(), trail);
    }

    private void stopPathTrail(Player player) {
        if (activeTrails.containsKey(player.getUniqueId())) {
            activeTrails.get(player.getUniqueId()).cancel();
            activeTrails.remove(player.getUniqueId());
        }
    }

    public void stopAllTrails() {
        for (BukkitRunnable trail : activeTrails.values()) {
            trail.cancel();
        }
        activeTrails.clear();
    }
}