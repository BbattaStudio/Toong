package io.dogsbean.toong.manager;

import io.dogsbean.toong.Main;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TempleManager {

    private final Main plugin;
    private final List<Location> templeLocations;
    private final List<Location> altarLocations;
    private File templeFile;
    private FileConfiguration templeConfig;

    public TempleManager(Main plugin) {
        this.plugin = plugin;
        this.templeLocations = new ArrayList<>();
        this.altarLocations = new ArrayList<>();
        this.templeFile = new File(plugin.getDataFolder(), "temples.yml");
        loadTemples();
    }

    public void createTemple(Location center) {
        int baseX = center.getBlockX();
        int baseY = center.getBlockY();
        int baseZ = center.getBlockZ();

        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= 6) {
                    Block block = center.getWorld().getBlockAt(baseX + x, baseY, baseZ + z);
                    if (distance <= 4) {
                        block.setType(Material.MOSS_BLOCK);
                    } else {
                        block.setType(Material.ROOTED_DIRT);
                    }
                }
            }
        }

        int[][] treePillars = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int[] pillar : treePillars) {
            for (int y = 1; y <= 6; y++) {
                Block block = center.getWorld().getBlockAt(baseX + pillar[0], baseY + y, baseZ + pillar[1]);
                if (y <= 5) {
                    block.setType(Material.OAK_LOG);
                } else {
                    block.setType(Material.OAK_LEAVES);
                    Leaves leaves = (Leaves) block.getBlockData();
                    leaves.setPersistent(true);
                    block.setBlockData(leaves);
                }
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    Block leafBlock = center.getWorld().getBlockAt(baseX + pillar[0] + dx, baseY + 6, baseZ + pillar[1] + dz);
                    leafBlock.setType(Material.OAK_LEAVES);
                    Leaves leaves = (Leaves) leafBlock.getBlockData();
                    leaves.setPersistent(true);
                    leafBlock.setBlockData(leaves);
                }
            }
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                Block block = center.getWorld().getBlockAt(baseX + x, baseY + 1, baseZ + z);
                block.setType(Material.STRIPPED_OAK_LOG);
            }
        }

        Block altarBase = center.getWorld().getBlockAt(baseX, baseY + 1, baseZ);
        altarBase.setType(Material.CHISELED_BOOKSHELF);

        Block altarTop = center.getWorld().getBlockAt(baseX, baseY + 2, baseZ);
        altarTop.setType(Material.ENCHANTING_TABLE);

        int[][] glowPositions = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}, {-2, -2}, {2, -2}, {-2, 2}, {2, 2}};
        for (int[] pos : glowPositions) {
            Block glow = center.getWorld().getBlockAt(baseX + pos[0], baseY + 1, baseZ + pos[1]);
            glow.setType(Material.GLOW_LICHEN);
        }

        int[][] lanternPositions = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        for (int[] pos : lanternPositions) {
            Block lantern = center.getWorld().getBlockAt(baseX + pos[0], baseY + 1, baseZ + pos[1]);
            lantern.setType(Material.LANTERN);
        }

        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            int flowerX = (int) (Math.cos(angle) * 3);
            int flowerZ = (int) (Math.sin(angle) * 3);
            Block flower = center.getWorld().getBlockAt(baseX + flowerX, baseY + 1, baseZ + flowerZ);
            if (i % 2 == 0) {
                flower.setType(Material.POPPY);
            } else {
                flower.setType(Material.DANDELION);
            }
        }

        templeLocations.add(center);
        altarLocations.add(altarTop.getLocation());

        saveTemples();

        plugin.getLogger().info("신전이 생성되었습니다: " + center.toString());
    }

    public boolean isNearAltar(Location location) {
        for (Location altar : altarLocations) {
            if (altar.getWorld().equals(location.getWorld())) {
                if (altar.distance(location) <= 3.0) {
                    return true;
                }
            }
        }
        return false;
    }

    public Location getNearestAltar(Location location) {
        Location nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Location altar : altarLocations) {
            if (altar.getWorld().equals(location.getWorld())) {
                double distance = altar.distance(location);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = altar;
                }
            }
        }

        return nearest;
    }

    public void saveTemples() {
        try {
            if (!templeFile.exists()) {
                templeFile.getParentFile().mkdirs();
                templeFile.createNewFile();
            }

            templeConfig = YamlConfiguration.loadConfiguration(templeFile);

            templeConfig.set("altars", null);

            for (int i = 0; i < altarLocations.size(); i++) {
                Location altar = altarLocations.get(i);
                String path = "altars." + i;
                templeConfig.set(path + ".world", altar.getWorld().getName());
                templeConfig.set(path + ".x", altar.getX());
                templeConfig.set(path + ".y", altar.getY());
                templeConfig.set(path + ".z", altar.getZ());
            }

            templeConfig.save(templeFile);
            plugin.getLogger().info("신전 위치가 저장되었습니다. (총 " + altarLocations.size() + "개)");
        } catch (IOException e) {
            plugin.getLogger().warning("신전 데이터 저장 실패!");
            e.printStackTrace();
        }
    }

    public void loadTemples() {
        if (!templeFile.exists()) {
            plugin.getLogger().info("저장된 신전이 없습니다.");
            return;
        }

        templeConfig = YamlConfiguration.loadConfiguration(templeFile);

        if (!templeConfig.contains("altars")) {
            plugin.getLogger().info("저장된 신전이 없습니다.");
            return;
        }

        altarLocations.clear();

        for (String key : templeConfig.getConfigurationSection("altars").getKeys(false)) {
            try {
                String path = "altars." + key;
                String worldName = templeConfig.getString(path + ".world");
                double x = templeConfig.getDouble(path + ".x");
                double y = templeConfig.getDouble(path + ".y");
                double z = templeConfig.getDouble(path + ".z");

                org.bukkit.World world = plugin.getServer().getWorld(worldName);
                if (world != null) {
                    Location altar = new Location(world, x, y, z);
                    altarLocations.add(altar);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("신전 로드 실패: " + key);
                e.printStackTrace();
            }
        }

        plugin.getLogger().info("신전 위치가 로드되었습니다. (총 " + altarLocations.size() + "개)");
    }
}