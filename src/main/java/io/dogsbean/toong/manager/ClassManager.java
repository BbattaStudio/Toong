package io.dogsbean.toong.manager;

import io.dogsbean.toong.Main;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class ClassManager {

    private Main plugin;
    private Map<UUID, PlayerClass> playerClasses;
    private File dataFile;
    private FileConfiguration dataConfig;

    public ClassManager(Main plugin) {
        this.plugin = plugin;
        this.playerClasses = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
    }

    public PlayerClass getPlayerClass(Player player) {
        return playerClasses.getOrDefault(player.getUniqueId(), new PlayerClass());
    }

    public boolean tryAdvanceClass(Player player) {
        PlayerClass pClass = getPlayerClass(player);
        int currentTier = pClass.getClassTier();

        if (currentTier >= 3) {
            player.sendMessage("§c넌 이미 퉁퉁퉁 사후루스다!");
            return false;
        }

        pClass.setClassTier(currentTier + 1);
        playerClasses.put(player.getUniqueId(), pClass);

        String tierName = getTierName(currentTier + 1);

        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§e§l        ⚡ 전직 성공! ⚡");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
        player.sendMessage("  §6✦ §e퉁퉁퉁 사후루스의 가호를 받았습니다!");
        player.sendMessage("  §a§l" + tierName + " §f등급으로 승급!");
        player.sendMessage("");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        player.getWorld().strikeLightningEffect(player.getLocation());
        return true;
    }

    private String getTierName(int tier) {
        return switch (tier) {
            case 1 -> "🌱 나무 견습생";
            case 2 -> "🪓 나무 전사";
            case 3 -> "🌲 고대 숲의 수호자";
            default -> "일반인";
        };
    }

    public void showPlayerInfo(Player player) {
        PlayerClass pClass = getPlayerClass(player);
        String tierName = getTierName(pClass.getClassTier());
        int tier = pClass.getClassTier();

        player.sendMessage("§6§l════════════════════════════");
        player.sendMessage("§e§l      🪵 내 전직 정보 🪵");
        player.sendMessage("§6§l════════════════════════════");
        player.sendMessage("");
        player.sendMessage("  §7등급: §a" + tierName);
        player.sendMessage("  §7전직 단계: §b" + tier + "차");
        player.sendMessage("");

        if (tier < 3) {
            String nextOffering = getNextOffering(tier + 1);
            player.sendMessage("  §7다음 전직 제물: §e" + nextOffering);
            player.sendMessage("  §7▸ 신전에서 제물을 바치세요!");
        } else {
            player.sendMessage("  §6§l★ 최고 등급 달성! ★");
        }

        player.sendMessage("");
        player.sendMessage("§6§l════════════════════════════");
    }

    private String getNextOffering(int tier) {
        return switch (tier) {
            case 1 -> "철 3개";
            case 2 -> "다이아몬드 2개";
            case 3 -> "엔더 드래곤 알 1개";
            default -> "없음";
        };
    }

    public void savePlayerData() {
        try {
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            }

            dataConfig = YamlConfiguration.loadConfiguration(dataFile);

            for (Map.Entry<UUID, PlayerClass> entry : playerClasses.entrySet()) {
                String path = entry.getKey().toString();
                dataConfig.set(path + ".tier", entry.getValue().getClassTier());
            }

            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("플레이어 데이터 저장 실패!");
            e.printStackTrace();
        }
    }

    public void loadPlayerData() {
        if (!dataFile.exists()) {
            return;
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int tier = dataConfig.getInt(key + ".tier", 0);

                PlayerClass pClass = new PlayerClass();
                pClass.setClassTier(tier);
                playerClasses.put(uuid, pClass);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("잘못된 UUID: " + key);
            }
        }
    }

    @Setter
    @Getter
    public static class PlayerClass {
        private int classTier = 0;
    }
}