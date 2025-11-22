package io.dogsbean.toong.listener;

import io.dogsbean.toong.Main;
import io.dogsbean.toong.manager.ClassManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import java.util.HashMap;
import java.util.Map;

public class TempleListener implements Listener {

    private final Main plugin;

    private static final Map<Integer, OfferingRequirement> OFFERINGS = new HashMap<>();

    static {
        OFFERINGS.put(1, new OfferingRequirement(Material.IRON_INGOT, 3, "철 3개"));
        OFFERINGS.put(2, new OfferingRequirement(Material.DIAMOND, 2, "다이아몬드 2개"));
        OFFERINGS.put(3, new OfferingRequirement(Material.DRAGON_EGG, 1, "엔더 드래곤 알 1개"));
    }

    public TempleListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        if (clickedBlock.getType() != Material.ENCHANTING_TABLE) {
            return;
        }

        Location clickLocation = clickedBlock.getLocation();
        if (!plugin.getTempleManager().isNearAltar(clickLocation)) {
            return;
        }

        event.setCancelled(true);

        ClassManager.PlayerClass pClass = plugin.getClassManager().getPlayerClass(player);
        int currentTier = pClass.getClassTier();

        if (currentTier >= 3) {
            player.sendMessage("§6§l✦ §e이미 최고 등급에 도달했습니다! §6§l✦");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int nextTier = currentTier + 1;
        OfferingRequirement requirement = OFFERINGS.get(nextTier);
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (handItem.getType() != requirement.material) {
            player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§c  ⚠ 잘못된 제물입니다!");
            player.sendMessage("§e  " + nextTier + "차 전직 필요 제물:");
            player.sendMessage("§a  ▸ " + requirement.displayName);
            player.sendMessage("§7  현재 들고 있는 아이템:");
            player.sendMessage("§f  ▸ " + getKoreanName(handItem.getType()));
            player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (handItem.getAmount() < requirement.amount) {
            player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§c  ⚠ 제물이 부족합니다!");
            player.sendMessage("§7  필요: §e" + requirement.amount + "개");
            player.sendMessage("§7  보유: §c" + handItem.getAmount() + "개");
            player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        handItem.setAmount(handItem.getAmount() - requirement.amount);
        player.getInventory().setItemInMainHand(handItem);

        Location particleLoc = clickLocation.clone().add(0.5, 1.5, 0.5);

        for (int i = 0; i < 40; i++) {
            int delay = i;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                double angle = (delay * 20) * Math.PI / 180;
                double radius = 1.5;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                double y = Math.sin(delay * 0.3) * 0.5;

                Location spiralLoc = particleLoc.clone().add(x, y, z);
                particleLoc.getWorld().spawnParticle(Particle.ENCHANTED_HIT, spiralLoc, 1, 0, 0, 0, 0);
                particleLoc.getWorld().spawnParticle(Particle.END_ROD, spiralLoc, 1, 0, 0, 0, 0);

                if (delay % 10 == 0) {
                    particleLoc.getWorld().spawnParticle(Particle.GLOW, particleLoc, 15, 0.5, 0.5, 0.5, 0.05);
                    particleLoc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 10, 0.3, 0.3, 0.3, 0.1);
                }
            }, delay);
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            particleLoc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, particleLoc, 50, 0.5, 1, 0.5, 0.1);
            particleLoc.getWorld().spawnParticle(Particle.FIREWORK, particleLoc, 30, 0.5, 0.5, 0.5, 0.15);
            particleLoc.getWorld().spawnParticle(Particle.ENCHANT, particleLoc, 100, 1, 1, 1, 0.5);

            plugin.getClassManager().tryAdvanceClass(player);
        }, 40);

        player.playSound(clickLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.5f, 0.8f);
        player.playSound(clickLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.playSound(clickLocation, Sound.ITEM_TOTEM_USE, 1.0f, 1.2f);
        }, 20);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.playSound(clickLocation, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }, 40);
    }

    private String getKoreanName(Material material) {
        return switch (material) {
            case DIAMOND -> "철";
            case DRAGON_EGG -> "엔더 드래곤 알";
            case EMERALD -> "에메랄드";
            case GOLD_INGOT -> "금 주괴";
            case IRON_INGOT -> "철 주괴";
            case AIR -> "없음";
            default -> material.name();
        };
    }

    private static class OfferingRequirement {
        Material material;
        int amount;
        String displayName;

        OfferingRequirement(Material material, int amount, String displayName) {
            this.material = material;
            this.amount = amount;
            this.displayName = displayName;
        }
    }
}