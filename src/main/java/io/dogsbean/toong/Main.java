package io.dogsbean.toong;

import io.dogsbean.toong.listener.CompassListener;
import io.dogsbean.toong.listener.SkillListener;
import io.dogsbean.toong.listener.TempleListener;
import io.dogsbean.toong.manager.ClassManager;
import io.dogsbean.toong.manager.SkillManager;
import io.dogsbean.toong.manager.TempleManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

@Getter
public class Main extends JavaPlugin {

    private TempleManager templeManager;
    private ClassManager classManager;
    private SkillManager skillManager;

    @Override
    public void onEnable() {
        getLogger().info("퉁퉁퉁 사후루스 플러그인이 활성화되었습니다!");

        templeManager = new TempleManager(this);
        classManager = new ClassManager(this);
        skillManager = new SkillManager(this);

        Bukkit.getPluginManager().registerEvents(new TempleListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SkillListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CompassListener(this), this);

        saveDefaultConfig();
        classManager.loadPlayerData();
    }

    @Override
    public void onDisable() {
        getLogger().info("퉁퉁퉁 사후루스 플러그인이 비활성화되었습니다!");
        classManager.savePlayerData();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c이 명령어는 플레이어만 사용할 수 있습니다!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("사후루스") || command.getName().equalsIgnoreCase("사후")) {
            if (args.length == 0) {
                showHelp(player);
                return true;
            }

            switch (args[0]) {
                case "신전생성":
                    if (!player.hasPermission("toongsaurus.admin")) {
                        player.sendMessage("§c권한이 없습니다!");
                        return true;
                    }
                    templeManager.createTemple(player.getLocation());
                    player.sendMessage("§6§l✦ §e퉁퉁퉁 사후루스 신전이 생성되었습니다! §6§l✦");
                    break;

                case "정보":
                    classManager.showPlayerInfo(player);
                    break;

                case "스킬":
                    skillManager.showSkillList(player);
                    break;

                default:
                    showHelp(player);
                    break;
            }
            return true;
        }

        return false;
    }

    private void showHelp(Player player) {
        player.sendMessage("§6§l════════════════════════════");
        player.sendMessage("§e§l  🪵 퉁퉁퉁 사후루스 명령어 🪵");
        player.sendMessage("§6§l════════════════════════════");
        player.sendMessage("§a▸ /사후루스 정보 §7- 내 전직 정보 확인");
        player.sendMessage("§a▸ /사후루스 스킬 §7- 사용 가능한 스킬 확인");
        if (player.hasPermission("toongsaurus.admin")) {
            player.sendMessage("§c▸ /사후루스 신전생성 §7- 신전 생성 (관리자)");
        }
        player.sendMessage("§6§l════════════════════════════");
    }
}