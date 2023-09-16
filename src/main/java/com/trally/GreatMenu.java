/*
 *  Copyright (c) 2023. Trally Chou (Zhou jiale, in Chinese)
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.trally;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/*
 * The main class
 */

public class GreatMenu extends JavaPlugin {
    static File menuFolder;
    static public GreatMenu plugin;
    public static HashMap<String, Inventory> menus = new HashMap<>();
    public static HashMap<String, List<String>[]> menusCommands = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;
        menuFolder = new File(this.getDataFolder().getPath() + "/menu");
        reLoadMenus();
        log("插件已加载");
        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);

    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 2) {
                if (args[0].equals("open")) {
                    if (menus.containsKey(args[1])) {
                        if (p.hasPermission("greatmenu." + args[1])) {
                            if (p.isOp()) {
                                MenuListener.editingMenu.put(p.getName(), args[1]);
                            }
                            p.openInventory(menus.get(args[1]));
                        } else {
                            p.sendMessage("§4无法为你打开这个菜单");
                        }

                        MenuListener.openAMenu.put(p.getName(), args[1]);
                    } else {
                        p.sendMessage("§4没有这个菜单");
                    }

                }

                if (args[0].equals("empty")) {

                    if (p.isOp()) {
                        int size;
                        try {
                            size = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            size = -1;
                            p.sendMessage("§4请不要输入非法字符");
                        }
                        if (size % 9 == 0) {
                            Inventory tmpInv = Bukkit.createInventory(() -> null, size, "Default");
                            p.openInventory(tmpInv);
                        } else {
                            p.sendMessage("§4无法打开一个这样的箱子");
                        }

                    }
                }

            }

            if (args.length == 1) {
                if (args[0].equals("off") && p.isOp()) {
                    MenuListener.opState.set(p.getName() + ".on", false);
                    p.sendMessage("§b关闭编辑模式");
                }
                if (args[0].equals("on") && p.isOp()) {
                    MenuListener.opState.set(p.getName() + ".on", true);
                    p.sendMessage("§a开启编辑模式");
                }
            }

            if (args.length == 0) {
                p.sendMessage("§a--------伟大菜单--------");
                p.sendMessage("§b/greatmenu open [菜单名]  打开菜单");
                p.sendMessage("§b/greatmenu on/off        开启/关闭编辑模式");
                p.sendMessage("§b/greatmenu empty [行数]   打开一个n行的匿名箱子");
                p.sendMessage("§a-----------------------");
            }

        }

        return true;
    }

    @Override
    public void onDisable() {
        log("插件已卸载");
    }

    static public void log(String s) {
        Bukkit.getConsoleSender().sendMessage("§b" + s);
    }

    static public void log(Object s) {
        System.out.println(s);
    }


    public static void reLoadMenus() {
        File[] menuFiles = menuFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        menus.clear();
        if (menuFiles != null) {
            FileConfiguration conf = plugin.getConfig();
            String prefix = "§4——请提醒腐竹缴纳插件费用，作者：TrallyChou";
            if (conf.getBoolean("I_PromiseThatIHavePayForThePluginAuthor,TrallyChou,AndIWillNotTransferThisOneTextToAnyone,ifIDoNotComplyThis,IWillBeADogAndMyMotherWillNotThinkThatIWasHerSonOrDaughter", false)) {
                prefix = "";
            }
            for (File menuFile : menuFiles) {
                YamlConfiguration tmpYml = YamlConfiguration.loadConfiguration(menuFile);
                Inventory tmpInv;
//                Bukkit.createInventory(null, InventoryType.valueOf(inv.getString("invType")), "Default");
                if (tmpYml.getString("invType", "Chest").equals("Chest")) {

                    tmpInv = Bukkit.createInventory(null, tmpYml.getInt("size"), tmpYml.getString("title") + prefix);
                } else {
                    tmpInv = Bukkit.createInventory(null, InventoryType.valueOf(tmpYml.getString("invType")), tmpYml.getString("title") + prefix);
                }

                tmpInv.setContents(MenuListener.getYmlItems(tmpYml));
                menus.put(menuFile.getName().replace(".yml", ""), tmpInv);
                menusCommands.put(menuFile.getName().replace(".yml", ""), getYmlItemsCommands(tmpYml));
            }
        }

    }

    public static List<String>[] getYmlItemsCommands(YamlConfiguration y) {
        List<String>[] res = new List[y.getInt("size")];
        for (int i = 0; i < y.getInt("size"); i++) {
            res[i] = (List<String>) y.getList("cmds." + i); //注意:可能为null
        }
        return res;
    }

}
