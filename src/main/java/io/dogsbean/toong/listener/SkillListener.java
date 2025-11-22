package io.dogsbean.toong.listener;

import io.dogsbean.toong.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillListener implements Listener {

    private final Main plugin;
    private final Map<UUID, Long> lastInteract;
    private final Map<UUID, Long> lastSwap;

    public SkillListener(Main plugin) {
        this.plugin = plugin;
        this.lastInteract = new HashMap<>();
        this.lastSwap = new HashMap<>();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Material item = player.getInventory().getItemInMainHand().getType();
        if (item != Material.WOODEN_SWORD &&
                item != Material.STONE_SWORD &&
                item != Material.IRON_SWORD &&
                item != Material.GOLDEN_SWORD &&
                item != Material.DIAMOND_SWORD) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (lastInteract.containsKey(player.getUniqueId())) {
            if (currentTime - lastInteract.get(player.getUniqueId()) < 300) {
                return;
            }
        }
        lastInteract.put(player.getUniqueId(), currentTime);

        if (player.isSneaking()) {
            plugin.getSkillManager().useSkill(player, 2);
        } else {
            plugin.getSkillManager().useSkill(player, 1);
        }
    }

    @EventHandler
    public void onPlayerSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand().getType() != Material.AIR ||
                player.getInventory().getItemInOffHand().getType() != Material.AIR) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (lastSwap.containsKey(player.getUniqueId())) {
            if (currentTime - lastSwap.get(player.getUniqueId()) < 300) {
                return;
            }
        }
        lastSwap.put(player.getUniqueId(), currentTime);

        event.setCancelled(true);
        plugin.getSkillManager().useSkill(player, 3);
    }
}