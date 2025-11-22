package io.dogsbean.toong.manager;

import io.dogsbean.toong.Main;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class SkillManager {

    private Main plugin;
    private Map<UUID, Long> skillCooldowns;

    public SkillManager(Main plugin) {
        this.plugin = plugin;
        this.skillCooldowns = new HashMap<>();
    }

    public void useSkill(Player player, int skillNumber) {
        ClassManager.PlayerClass pClass = plugin.getClassManager().getPlayerClass(player);
        int tier = pClass.getClassTier();

        if (tier == 0) {
            player.sendTitle("§c§l힘이 부족합니다.", "§7§o아직 너무 약해..", 0, 25, 30);
            player.sendMessage("§c§l전직을 해야 스킬을 사용할 수 있습니다!");
            return;
        }

        if (skillNumber > tier) {
            player.sendTitle("§c§l힘이 부족합니다.", "§7§o아직 너무 약해..", 0, 25, 30);
            player.sendMessage("§c§l해당 스킬을 사용하려면 " + skillNumber + "차 전직이 필요합니다!");
            return;
        }

        if (isOnCooldown(player, skillNumber)) {
            long remainingTime = getRemainingCooldown(player, skillNumber);
            player.sendMessage("§c§l⏱ 스킬 쿨다운: §e" + remainingTime + "초 §c§l남음");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        switch (skillNumber) {
            case 1:
                useTier1Skill(player);
                setCooldown(player, 1, 25);
                break;
            case 2:
                useTier2Skill(player);
                setCooldown(player, 2, 40);
                break;
            case 3:
                useTier3Skill(player);
                setCooldown(player, 3, 50);
                break;
        }
    }

    private void useTier1Skill(Player player) {
        player.sendMessage("§a§l[스킬 발동] §6✦ 퉁퉁퉁 사후루스의 축복 ✦");

        player.sendTitle("§6§l✦ 퉁퉁퉁 사후루스의 축복 ✦", "§a자연의 힘이 깃들다", 5, 40, 15);

        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 240, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 240, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 140, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100, 0));

        Location loc = player.getLocation();

        for (int i = 0; i < 80; i++) {
            int delay = i;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                double angle = (delay * 30) * Math.PI / 180;
                double radius = 1.5 + Math.sin(delay * 0.2) * 0.5;

                for (int ring = 0; ring < 2; ring++) {
                    double ringRadius = radius + (ring * 0.5);
                    double x = Math.cos(angle) * ringRadius;
                    double z = Math.sin(angle) * ringRadius;
                    double y = 0.3 + Math.sin(delay * 0.15) * 1.5;

                    Location particleLoc = loc.clone().add(x, y, z);
                    player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 3, 0.1, 0.1, 0.1, 0);
                    player.getWorld().spawnParticle(Particle.GLOW, particleLoc, 2, 0.05, 0.05, 0.05, 0);
                }

                if (delay % 8 == 0) {
                    player.getWorld().spawnParticle(Particle.COMPOSTER, loc.clone().add(0, 0.5, 0), 25, 0.8, 0.3, 0.8, 0.15);
                    player.getWorld().spawnParticle(Particle.WAX_ON, loc.clone().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
                    player.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, loc.clone().add(0, 1.5, 0), 10, 0.3, 0.3, 0.3, 0.05);
                }

                if (delay % 15 == 0) {
                    player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc.clone().add(0, 1, 0), 30, 1, 1, 1, 0.2);
                    player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 1.5, 0), 5, 0.3, 0.3, 0.3, 0.05);
                }
            }, delay);
        }

        for (int i = 0; i < 12; i++) {
            int delay = i * 3;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                double angle = (delay * 60) * Math.PI / 180;
                for (int j = 0; j < 6; j++) {
                    double offsetAngle = angle + (j * 60 * Math.PI / 180);
                    double x = Math.cos(offsetAngle) * 2.5;
                    double z = Math.sin(offsetAngle) * 2.5;
                    Location burstLoc = loc.clone().add(x, 0.2, z);
                    player.getWorld().spawnParticle(Particle.HEART, burstLoc, 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, burstLoc, 5, 0.2, 0.2, 0.2, 0);
                }
            }, delay);
        }

        player.playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.5f, 1.5f);
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
        player.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.5f, 1.2f);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);
            player.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.5f);
        }, 10);
    }

    private void useTier2Skill(Player player) {
        player.sendMessage("§a§l[스킬 발동] §c⚡ 나무망치 강타 ⚡");

        player.sendTitle("§c§l⚡ 나무망치 강타 ⚡", "§6대지를 뒤흔들어라!", 5, 35, 10);

        Location center = player.getLocation();
        Vector direction = center.getDirection().normalize();

        for (int i = 0; i < 8; i++) {
            int delay = i;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Location trailLoc = player.getLocation();
                player.getWorld().spawnParticle(Particle.FLAME, trailLoc, 15, 0.3, 0.1, 0.3, 0.05);
                player.getWorld().spawnParticle(Particle.LAVA, trailLoc, 5, 0.2, 0.1, 0.2, 0);
                player.getWorld().spawnParticle(Particle.LARGE_SMOKE, trailLoc, 10, 0.4, 0.2, 0.4, 0.02);
                player.getWorld().spawnParticle(Particle.CRIT, trailLoc.clone().add(0, 0.5, 0), 8, 0.3, 0.3, 0.3, 0.1);
            }, delay);
        }

        player.setVelocity(direction.multiply(1.2).setY(0.3));

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Location impactLoc = player.getLocation();

            player.sendTitle("§4§l💥 충격파! 💥", "", 0, 25, 5);

            for (int i = 0; i < 40; i++) {
                int delay = i;
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    double radius = delay * 0.15;
                    for (int angle = 0; angle < 360; angle += 30) {
                        double radian = Math.toRadians(angle);
                        double x = Math.cos(radian) * radius;
                        double z = Math.sin(radian) * radius;
                        Location ringLoc = impactLoc.clone().add(x, 0.1, z);
                        impactLoc.getWorld().spawnParticle(Particle.FLAME, ringLoc, 2, 0.1, 0.1, 0.1, 0);
                        impactLoc.getWorld().spawnParticle(Particle.LARGE_SMOKE, ringLoc, 1, 0.1, 0.1, 0.1, 0);
                    }

                    if (delay % 5 == 0) {
                        for (int j = 0; j < 8; j++) {
                            double angle = j * 45;
                            double radian = Math.toRadians(angle);
                            double x = Math.cos(radian) * radius * 1.2;
                            double z = Math.sin(radian) * radius * 1.2;
                            Location burstLoc = impactLoc.clone().add(x, 0.2, z);
                            impactLoc.getWorld().spawnParticle(Particle.LAVA, burstLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }, delay);
            }

            for (org.bukkit.entity.Entity entity : player.getNearbyEntities(6, 4, 6)) {
                if (entity instanceof LivingEntity living && !(entity instanceof Player)) {
                    living.damage(12.0, player);

                    Vector knockback = living.getLocation().toVector().subtract(impactLoc.toVector()).normalize();
                    living.setVelocity(knockback.multiply(2.0).setY(1.2));

                    for (int i = 0; i < 20; i++) {
                        int delay = i;
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            living.getWorld().spawnParticle(Particle.CRIT, living.getLocation().add(0, 1, 0), 3, 0.3, 0.5, 0.3, 0);
                            living.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, living.getLocation().add(0, 1.5, 0), 2, 0.2, 0.2, 0.2, 0);
                        }, delay);
                    }
                }
            }

            impactLoc.getWorld().spawnParticle(Particle.EXPLOSION, impactLoc.clone().add(0, 0.5, 0), 15, 1.5, 0.3, 1.5, 0.1);
            impactLoc.getWorld().spawnParticle(Particle.ITEM, impactLoc.clone().add(0, 0.2, 0), 150, 2.5, 0.5, 2.5, 0.2, new ItemStack(Material.OAK_LOG));
            impactLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, impactLoc.clone().add(0, 1, 0), 10, 2.5, 0.5, 2.5, 0);
            impactLoc.getWorld().spawnParticle(Particle.CLOUD, impactLoc, 40, 2.5, 0.3, 2.5, 0.1);
            impactLoc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, impactLoc.clone().add(0, 0.5, 0), 30, 2, 1, 2, 0.05);

            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    if (Math.abs(x) + Math.abs(z) <= 4) {
                        Block block = impactLoc.getWorld().getBlockAt(impactLoc.getBlockX() + x, impactLoc.getBlockY() - 1, impactLoc.getBlockZ() + z);
                        if (block.getType().isSolid()) {
                            int randomDelay = (int)(Math.random() * 15);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                impactLoc.getWorld().spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 1, 0.5), 5, 0.4, 0.2, 0.4, 0.15, block.getBlockData());
                            }, randomDelay);
                        }
                    }
                }
            }

            impactLoc.getWorld().playSound(impactLoc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 2.0f, 0.5f);
            impactLoc.getWorld().playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
            impactLoc.getWorld().playSound(impactLoc, Sound.BLOCK_ANVIL_LAND, 2.0f, 0.7f);
            impactLoc.getWorld().playSound(impactLoc, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.5f, 0.5f);
            impactLoc.getWorld().playSound(impactLoc, Sound.ITEM_TRIDENT_THUNDER, 0.8f, 0.6f);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                impactLoc.getWorld().playSound(impactLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 0.8f);
            }, 5);

        }, 8);

        player.playSound(center, Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.8f);
        player.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.5f);
        player.playSound(center, Sound.ENTITY_RAVAGER_ROAR, 1.0f, 1.2f);
    }

    private void useTier3Skill(Player player) {
        player.sendMessage("§a§l[스킬 발동] §5§l🌲 고대 숲의 수호자 🌲");

        player.sendTitle("§5§l✧ 고대 숲의 수호자 ✧", "§d§l절대적인 힘이 깨어나다", 10, 60, 20);

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 260, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 260, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 260, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 260, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 260, 0));

        Location loc = player.getLocation();

        for (int wave = 0; wave < 5; wave++) {
            int waveDelay = wave * 12;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (int angle = 0; angle < 360; angle += 10) {
                    double radian = Math.toRadians(angle);
                    double radius = 4.0;
                    double x = Math.cos(radian) * radius;
                    double z = Math.sin(radian) * radius;
                    Location burstLoc = loc.clone().add(x, 0.3, z);
                    loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, burstLoc, 3, 0.1, 0.5, 0.1, 0.02);
                    loc.getWorld().spawnParticle(Particle.END_ROD, burstLoc, 2, 0.1, 0.3, 0.1, 0.01);
                    loc.getWorld().spawnParticle(Particle.ENCHANTED_HIT, burstLoc, 1, 0, 0, 0, 0);
                }
            }, waveDelay);
        }

        for (int i = 0; i < 100; i++) {
            int delay = i;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                double angle = (delay * 15) * Math.PI / 180;
                double radius = 2.5 + (delay * 0.015);

                for (int j = 0; j < 4; j++) {
                    double offsetAngle = angle + (j * 90 * Math.PI / 180);
                    double x = Math.cos(offsetAngle) * radius;
                    double z = Math.sin(offsetAngle) * radius;
                    double y = Math.sin(delay * 0.2) * 0.8 + 1.5;

                    Location particleLoc = loc.clone().add(x, y, z);
                    player.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, particleLoc, 2, 0.05, 0.05, 0.05, 0);
                    player.getWorld().spawnParticle(Particle.GLOW, particleLoc, 1, 0, 0, 0, 0);
                }

                for (int j = 0; j < 3; j++) {
                    double spiralAngle = angle * 2 + (j * 120 * Math.PI / 180);
                    double spiralRadius = 1.5;
                    double x = Math.cos(spiralAngle) * spiralRadius;
                    double z = Math.sin(spiralAngle) * spiralRadius;
                    double y = (delay * 0.05) % 3;

                    Location spiralLoc = loc.clone().add(x, y, z);
                    player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, spiralLoc, 2, 0.05, 0.05, 0.05, 0.01);
                    player.getWorld().spawnParticle(Particle.DRAGON_BREATH, spiralLoc, 1, 0.05, 0.05, 0.05, 0);
                }

                if (delay % 8 == 0) {
                    player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 2, 0), 20, 0.8, 0.8, 0.8, 0.1);
                    player.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, loc.clone().add(0, 1.5, 0), 15, 1, 0.5, 1, 0.08);
                    player.getWorld().spawnParticle(Particle.WAX_ON, loc.clone().add(0, 1, 0), 10, 0.6, 0.6, 0.6, 0.05);
                }

                if (delay % 12 == 0) {
                    player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.clone().add(0, 0.5, 0), 30, 1.8, 0.5, 1.8, 0.1);
                    player.getWorld().spawnParticle(Particle.ENCHANT, loc.clone().add(0, 1, 0), 25, 1.5, 1, 1.5, 0.5);

                    for (int k = 0; k < 8; k++) {
                        double burstAngle = k * 45;
                        double burstRadian = Math.toRadians(burstAngle);
                        double burstX = Math.cos(burstRadian) * 2;
                        double burstZ = Math.sin(burstRadian) * 2;
                        Location burstLoc = loc.clone().add(burstX, 1.5, burstZ);
                        player.getWorld().spawnParticle(Particle.FIREWORK, burstLoc, 3, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                if (delay % 20 == 0) {
                    player.sendTitle("§5§l✧ §d수호자 §5§l✧", "§6§l무적의 힘!", 0, 20, 5);
                }
            }, delay);
        }

        for (int pillar = 0; pillar < 6; pillar++) {
            int pillarDelay = pillar * 10;
            double pillarAngle = pillar * 60;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                double radian = Math.toRadians(pillarAngle);
                double x = Math.cos(radian) * 3;
                double z = Math.sin(radian) * 3;
                Location pillarBase = loc.clone().add(x, 0, z);

                for (int h = 0; h < 30; h++) {
                    int heightDelay = h;
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        Location pillarLoc = pillarBase.clone().add(0, heightDelay * 0.15, 0);
                        pillarBase.getWorld().spawnParticle(Particle.END_ROD, pillarLoc, 3, 0.1, 0.1, 0.1, 0);
                        pillarBase.getWorld().spawnParticle(Particle.DRAGON_BREATH, pillarLoc, 2, 0.15, 0.15, 0.15, 0.01);
                        pillarBase.getWorld().spawnParticle(Particle.GLOW, pillarLoc, 2, 0.1, 0.1, 0.1, 0);
                    }, heightDelay);
                }
            }, pillarDelay);
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc.clone().add(0, 1, 0), 200, 3, 3, 3, 0.08);
            loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc.clone().add(0, 1, 0), 100, 2, 2, 2, 0.8);
            loc.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, loc.clone().add(0, 1.5, 0), 50, 2, 2, 2, 0.1);
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 2, 0), 80, 1.5, 1.5, 1.5, 0.15);
            loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0, 0.5, 0), 60, 2.5, 0.5, 2.5, 0.2);

            player.sendTitle("§d§l⚡ 각성 완료! ⚡", "§5§l넌 이제 퉁퉁퉁 사후루스다", 5, 30, 10);
        }, 50);

        loc.getWorld().strikeLightningEffect(loc);
        loc.getWorld().strikeLightningEffect(loc.clone().add(2, 0, 2));
        loc.getWorld().strikeLightningEffect(loc.clone().add(-2, 0, -2));

        player.playSound(loc, Sound.ITEM_TOTEM_USE, 2.0f, 1.0f);
        player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 1.5f);
        player.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.2f);
        player.playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2.0f, 0.5f);
        player.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.8f);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.5f, 2.0f);
            player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1.0f);
            player.playSound(loc, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.0f, 0.8f);
        }, 15);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);
            player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.7f);
        }, 30);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.playSound(loc, Sound.BLOCK_BELL_USE, 1.5f, 0.5f);
            player.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
        }, 50);
    }

    private void setCooldown(Player player, int skillNumber, int seconds) {
        String key = player.getUniqueId() + "_skill" + skillNumber;
        skillCooldowns.put(UUID.nameUUIDFromBytes(key.getBytes()), System.currentTimeMillis() + (seconds * 1000L));
    }

    private boolean isOnCooldown(Player player, int skillNumber) {
        String key = player.getUniqueId() + "_skill" + skillNumber;
        UUID cooldownKey = UUID.nameUUIDFromBytes(key.getBytes());

        if (!skillCooldowns.containsKey(cooldownKey)) {
            return false;
        }

        return skillCooldowns.get(cooldownKey) > System.currentTimeMillis();
    }

    private long getRemainingCooldown(Player player, int skillNumber) {
        String key = player.getUniqueId() + "_skill" + skillNumber;
        UUID cooldownKey = UUID.nameUUIDFromBytes(key.getBytes());

        if (!skillCooldowns.containsKey(cooldownKey)) {
            return 0;
        }

        long remaining = (skillCooldowns.get(cooldownKey) - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    public void showSkillList(Player player) {
        ClassManager.PlayerClass pClass = plugin.getClassManager().getPlayerClass(player);
        int tier = pClass.getClassTier();

        player.sendMessage("§6§l════════════════════════════");
        player.sendMessage("§e§l    🪵 사용 가능한 스킬 🪵");
        player.sendMessage("§6§l════════════════════════════");

        if (tier == 0) {
            player.sendMessage("§c전직을 해야 스킬을 사용할 수 있습니다!");
            player.sendMessage("§7신전에서 제물을 바치세요!");
            player.sendMessage("§6§l════════════════════════════");
            return;
        }

        player.sendMessage("§a§l[1차] §6퉁퉁퉁 사후루스의 축복");
        player.sendMessage("§7  ▸ 사용법: §e빈손 우클릭");
        player.sendMessage("§7  ▸ 효과: 힘/속도/재생/포만감");
        player.sendMessage("§7  ▸ 쿨다운: §e25초");

        if (tier >= 2) {
            player.sendMessage("");
            player.sendMessage("§a§l[2차] §c나무망치 강타");
            player.sendMessage("§7  ▸ 사용법: §eShift + 빈손 우클릭");
            player.sendMessage("§7  ▸ 효과: 돌진 + 범위 공격 + 넉백");
            player.sendMessage("§7  ▸ 쿨다운: §e40초");
        }

        if (tier >= 3) {
            player.sendMessage("");
            player.sendMessage("§a§l[3차] §5§l고대 숲의 수호자");
            player.sendMessage("§7  ▸ 사용법: §e양손 비운 상태로 F키");
            player.sendMessage("§7  ▸ 효과: 저항/힘/속도/재생/화염저항");
            player.sendMessage("§7  ▸ 쿨다운: §e50초");
        }

        player.sendMessage("§6§l════════════════════════════");
    }
}