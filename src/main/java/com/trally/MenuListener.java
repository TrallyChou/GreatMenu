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
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MenuListener implements Listener {

    static YamlConfiguration opState = new YamlConfiguration();
    static HashMap<String, ItemStack[]> playerInventory = new HashMap<>();
    static HashMap<String, Inventory> invEditing = new HashMap<>();
    static HashMap<String, ItemStack> itemEditing = new HashMap<>();
    static HashMap<String, String> editingMenu = new HashMap<>();

    static HashMap<String, String> openAMenu = new HashMap<>();

    static HashMap<String, List<String>> clipboard = new HashMap<>();

    /*
     * state:
     * 0: 无
     * 1: 打开箱子
     * 2: 进入编辑模式后
     */

    /*
     * editingMode:
     * -1: 无
     * 0: 名称工具
     * 1: lore工具
     * 2: 尚无
     * 3: 命令工具
     * 4: 光标工具
     * 5: 指令光标工具
     * 6: 更改容器名
     * 7: 设置菜单id
     */

    @EventHandler
    public void onOpenAInv(InventoryOpenEvent e) {

        Player p = (Player) e.getPlayer();  //傻逼Bukkit 这还要转
        if (p.isOp() && opState.getBoolean(p.getName() + ".on")) {

            InventoryType t = e.getInventory().getType();
            if (e.getInventory().getHolder() != null || (t == InventoryType.ENCHANTING) || (t == InventoryType.ANVIL) || (t == InventoryType.ENDER_CHEST)) {
                editingMenu.remove(p.getName());
                if (opState.getInt(p.getName() + ".editingMode") == 3) {  //防止对普通箱子进行命令编辑
                    opState.set(p.getName() + ".editingMode", 4);
                }
                if (opState.getInt(p.getName() + ".state") == 2) {
                    p.getInventory().setItem(3, new ItemStack(Material.AIR));
                    p.getInventory().setItem(6, new ItemStack(Material.AIR));
                    p.getInventory().setItem(7, getAItemNamedAndLored(Material.LEVER, "§r创建为菜单"));
                }

            }

            if (editingMenu.containsKey(p.getName())) {
                showCmdsOnLore(p);
            }

            if (e.getInventory().getType() != InventoryType.PLAYER && opState.getInt(p.getName() + ".state", 0) == 0) {
                opState.set(p.getName() + ".state", 1);
                opState.set(p.getName() + ".editingMode", -1);
                ItemStack menuMenu = new ItemStack(Material.LADDER);
                ItemMeta menuMenuMeta = menuMenu.getItemMeta();
                ItemStack oriItem = p.getInventory().getItem(8);
                if (oriItem == null) {
                    opState.set(p.getName() + ".itemAt8", new ItemStack(Material.AIR));
                } else {
                    opState.set(p.getName() + ".itemAt8", oriItem);
                }


                menuMenuMeta.setDisplayName("§c编辑模式");
                menuMenuMeta.setLore(Collections.singletonList("§a点击进入编辑模式"));
                menuMenu.setItemMeta(menuMenuMeta);
                p.getInventory().setItem(8, menuMenu);
            }

            if (opState.getInt(p.getName() + ".state", 0) == 2) {
                if (editingMenu.get(p.getName()) != null) {
                    p.getInventory().setItem(3, getAItemNamedAndLored(Material.COMMAND, "§r命令工具", Arrays.asList("§a左键添加", "§4右键删除")));
                    p.getInventory().setItem(5, getAItemNamedAndLored(Material.FISHING_ROD, "§r命令光标工具", Arrays.asList("§a左键复制", "§4右键粘贴", "§4Q键删除")));
                    p.getInventory().setItem(6, getAItemNamedAndLored(Material.NAME_TAG, "§r更改菜单标题"));
                    p.getInventory().setItem(7, new ItemStack(Material.AIR));
                }
            }

            return;  //编辑模式无法运行命令
        }

        if (e.getInventory().getHolder() != null) {
            openAMenu.remove(p.getName());
        }


    }

    @EventHandler
    public void onCloseTheChest(InventoryCloseEvent e) {


        Player p = (Player) e.getPlayer();
        if (p.isOp() && opState.getBoolean(p.getName() + ".on")) {


            if (opState.getBoolean(p.getName() + ".showCmds", false)) {
                hideCmdsOnLore(p);
            }
            if (opState.getInt(p.getName() + ".state") == 1) {
                p.getInventory().setItem(8, (ItemStack) opState.get(p.getName() + ".itemAt8"));
                opState.set(p.getName() + ".itemAt8", null);
                opState.set(p.getName() + ".state", 0);
            }

            if (editingMenu.get(p.getName()) != null && e.getInventory().getHolder() == null) {
                File invFile = new File(GreatMenu.menuFolder, editingMenu.get(p.getName()) + ".yml");
                YamlConfiguration inv = YamlConfiguration.loadConfiguration(invFile);
                //inv.set("size", e.getInventory().getSize());
                setYmlItems(e.getInventory().getContents(), inv);
                //inv.set("title", e.getInventory().getName());
                menuSave(p, invFile, inv);
                //管箱子意味着保存，保存意味着重载，重载意味着invEditing中存的箱子仍然是原来的，而不是重载后重新创建的，导致bug。所以，执行下一条语句
                invEditing.put(p.getName(), GreatMenu.menus.get(editingMenu.get(p.getName())));
                hideCmdsOnLore(p);
//                try {
//                    inv.save(invFile);
//                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                }

            }
            return;
        }

        openAMenu.remove(p.getName());

    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickTheItemInMenu(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (p.isOp() && opState.getBoolean(p.getName() + ".on")) {
            //初始化UI
            if (opState.getInt(p.getName() + ".state") == 1) {
                if (e.getClickedInventory() == null) {
                    return;
                }
                if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
                    if (e.getSlot() == 8) {
                        e.setCancelled(true);

                        //进行物品栏初始化


                        e.getClickedInventory().setItem(8, new ItemStack(Material.AIR));
                        playerInventory.put(p.getName(), p.getInventory().getContents());

                        e.getClickedInventory().clear();
                        e.getClickedInventory().setItem(8, getAItemNamedAndLored(Material.BARRIER, "§c退出菜单编辑模式"));
                        opState.set(p.getName() + ".state", 2);


                        e.getClickedInventory().setItem(0, getAItemNamedAndLored(Material.SIGN, "§r名称工具"));
                        e.getClickedInventory().setItem(1, getAItemNamedAndLored(Material.BOOK_AND_QUILL, "§rlore工具", Arrays.asList("§a左键添加", "§4右键删除")));
                        e.getClickedInventory().setItem(4, getAItemNamedAndLored(Material.BUCKET, "§r光标工具", Collections.singletonList("§a可以移动物品")));

                        if (e.getInventory().getHolder() == null && editingMenu.containsKey(p.getName())) {
                            e.getClickedInventory().setItem(3, getAItemNamedAndLored(Material.COMMAND, "§r命令工具", Arrays.asList("§a左键添加", "§4右键删除")));
                            e.getClickedInventory().setItem(5, getAItemNamedAndLored(Material.FISHING_ROD, "§r命令光标工具", Arrays.asList("§a左键复制", "§4右键粘贴", "§4Q键删除")));
                            e.getClickedInventory().setItem(6, getAItemNamedAndLored(Material.NAME_TAG, "§r更改菜单标题"));
                        } else {
                            e.getClickedInventory().setItem(7, getAItemNamedAndLored(Material.LEVER, "§r创建为菜单"));
                        }

                        opState.set(p.getName() + ".editingMode", 4); //默认为4光标工具

                    }


                }
                return;
            }


            //UI中操作
            if (opState.getInt(p.getName() + ".state") == 2) {


                Inventory clickedInv = e.getClickedInventory();
                if (clickedInv == null) {
                    //否则会报错
                    return;
                }

                //背包中操作
                if (clickedInv.getType() == InventoryType.PLAYER) {
                    if (e.getSlot() < 9) {
                        e.setCancelled(true);
                        if (e.getCurrentItem().getType() != Material.AIR) {
                            opState.set(p.getName() + ".editingMode", e.getSlot());
                        }

                    }

                    if (opState.getInt(p.getName() + ".editingMode") != 4) {
                        e.setCancelled(true);
                    }

                    if (opState.getInt(p.getName() + ".editingMode") == 6) {
                        opState.set(p.getName() + ".nowEditingSlot", e.getSlot());
                        invEditing.put(p.getName(), e.getInventory());
                        p.sendTitle("请输入新的显示名", null);
                        Bukkit.getPluginManager().registerEvents(new CMListener(), GreatMenu.plugin);
                        Bukkit.getScheduler().runTask(GreatMenu.plugin, p::closeInventory);

                    }

                    if (opState.getInt(p.getName() + ".editingMode") == 7) {
                        opState.set(p.getName() + ".nowEditingSlot", e.getSlot());
                        invEditing.put(p.getName(), e.getInventory());
                        p.sendTitle("请为菜单分配一个内部名", null);
                        Bukkit.getPluginManager().registerEvents(new CMListener(), GreatMenu.plugin);
                        Bukkit.getScheduler().runTask(GreatMenu.plugin, p::closeInventory);

                    }


                    //退出事件
                    if (e.getSlot() == 8) {

                        opState.set(p.getName() + ".state", 0);
                        e.setCancelled(true);   //必要步骤，否则最终会掉出原来8位置的物品
                        clickedInv.clear();
                        clickedInv.setContents(playerInventory.get(p.getName()));
                        clickedInv.setItem(8, (ItemStack) opState.get(p.getName() + ".itemAt8"));
                        opState.set(p.getName() + ".itemAt8", null);
                        Bukkit.getScheduler().runTask(GreatMenu.plugin, p::closeInventory);
                        playerInventory.remove(p.getName());

                    }


                }


                //箱子中操作
                if (clickedInv.getType() == InventoryType.CHEST) {
                    if (opState.getInt(p.getName() + ".editingMode") == -1 || opState.getInt(p.getName() + ".editingMode") == 4) {
                        return;
                    }
                    //GreatMenu.log((e.getCurrentItem()));
                    if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
                        return;
                    }
                    e.setCancelled(true);
                    opState.set(p.getName() + ".nowEditingSlot", e.getSlot());
                    invEditing.put(p.getName(), clickedInv);
                    if (opState.getInt(p.getName() + ".editingMode") == 0) {
                        itemEditing.put(p.getName(), e.getCurrentItem());
                        p.sendTitle("请输入新名称", null);
                        Bukkit.getPluginManager().registerEvents(new CMListener(), GreatMenu.plugin);
                        Bukkit.getScheduler().runTask(GreatMenu.plugin, p::closeInventory);
                    }

                    if (opState.getInt(p.getName() + ".editingMode") == 1) {
                        if (e.getClick().isLeftClick()) {
                            itemEditing.put(p.getName(), e.getCurrentItem());
                            p.sendTitle("请输入要增加的lore", null);
                            Bukkit.getPluginManager().registerEvents(new CMListener(), GreatMenu.plugin);
                            Bukkit.getScheduler().runTask(GreatMenu.plugin, p::closeInventory);
                        } else if (e.getClick().isRightClick()) {
                            hideCmdsOnLore(p);
                            clickedInv.setItem(e.getSlot(), removeAItemLore(e.getCurrentItem()));
                            showCmdsOnLore(p);
                        }
                    }

                    if (opState.getInt(p.getName() + ".editingMode") == 3) {
                        if (e.getClick().isLeftClick()) {
                            p.sendTitle("请输入要增加的指令，不含/", null);
                            Bukkit.getPluginManager().registerEvents(new CMListener(), GreatMenu.plugin);
                            Bukkit.getScheduler().runTask(GreatMenu.plugin, p::closeInventory);
                        } else if (e.getClick().isRightClick()) {
                            removeCmd(p);
                        }
                    }

                    if (opState.getInt(p.getName() + ".editingMode") == 5) {
                        if (e.getClick().isLeftClick()) {
                            copyCmds(p);
                        } else if (e.getClick().isRightClick()) {
                            pasteCmds(p);
                        } else if (e.getClick() == ClickType.DROP) {
                            removeAllCmds(p);
                        }


                    }


                }


                return;
            }

            return;
        }

        if (openAMenu.containsKey(p.getName())) {
            if (e.getClickedInventory() != null) {
                if (e.getClickedInventory().getType() != InventoryType.PLAYER) {
                    e.setCancelled(true);
                    List<String> preExecuteCmds = GreatMenu.menusCommands.get(openAMenu.get(p.getName()))[e.getSlot()];
                    if (preExecuteCmds != null) {
                        for (int i = 0; i < preExecuteCmds.size(); i++) {
                            String tmpCmd = preExecuteCmds.get(i);
                            tmpCmd = tmpCmd.replace("<Player>", p.getName());
                            if (tmpCmd.startsWith("$c1")) {
                                tmpCmd = tmpCmd.substring(3);
                                p.chat("/" + tmpCmd);
                                continue;
                            }

                            if (tmpCmd.startsWith("$c2")) {
                                tmpCmd = tmpCmd.substring(3);
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), tmpCmd);
                                continue;
                            }

                            if (tmpCmd.startsWith("$c")) {
                                tmpCmd = tmpCmd.substring(2);
                                p.chat(tmpCmd);
                                continue;
                            }

                            if (tmpCmd.startsWith("$m")) {
                                tmpCmd = tmpCmd.substring(2);
                                p.sendMessage(tmpCmd);
                                continue;
                            }

                        }
                    }
                } else {
                    if (e.getClick().isShiftClick() || e.getClick() == ClickType.DOUBLE_CLICK || e.getClick() == ClickType.UNKNOWN)
                        e.setCancelled(true);
                }


            }
        }

    }


    @EventHandler
    public void dragEventInMenu(InventoryDragEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (p.isOp() && opState.getBoolean(p.getName() + ".on")) {
            return;
        }

        if (openAMenu.containsKey(p.getName())) {
            e.setCancelled(true);
        }
    }


    private ItemStack getAItemNamedAndLored(Material m, String s) {
        ItemStack tmpItem = new ItemStack(m);
        ItemMeta tmpMeta = tmpItem.getItemMeta();
        tmpMeta.setDisplayName(s);
        tmpItem.setItemMeta(tmpMeta);
        return tmpItem;
    }

    private ItemStack getAItemNamedAndLored(Material m, String s, List<String> l) {
        ItemStack tmpItem = new ItemStack(m);
        ItemMeta tmpMeta = tmpItem.getItemMeta();
        tmpMeta.setDisplayName(s);
        tmpMeta.setLore(l);
        tmpItem.setItemMeta(tmpMeta);
        return tmpItem;
    }

    static private ItemStack changeAItem(ItemStack oriItem, String s) {
        ItemMeta tmpMeta = oriItem.getItemMeta();
        tmpMeta.setDisplayName(s);
        oriItem.setItemMeta(tmpMeta);
        return oriItem;
    }

    static private ItemStack changeAItem(ItemStack oriItem, String s, List<String> l) {
        ItemMeta tmpMeta = oriItem.getItemMeta();
        tmpMeta.setDisplayName(s);
        tmpMeta.setLore(l);
        oriItem.setItemMeta(tmpMeta);
        return oriItem;
    }

    static private ItemStack addAItemLore(ItemStack oriItem, String l) {
        ItemMeta tmpMeta = oriItem.getItemMeta();
        List<String> tmpLore = tmpMeta.getLore();
        if (tmpLore == null) {
            tmpLore = new ArrayList<>();
        }
        tmpLore.add(l);
        tmpMeta.setLore(tmpLore);
        oriItem.setItemMeta(tmpMeta);
        return oriItem;
    }


    static private ItemStack removeAItemLore(ItemStack oriItem) {
        //安全的。
        ItemMeta tmpMeta = oriItem.getItemMeta();
        List<String> tmpLore = tmpMeta.getLore();
        if (tmpLore == null) {
            return oriItem;
        }
        tmpLore.remove(tmpLore.size() - 1);
        tmpMeta.setLore(tmpLore);
        oriItem.setItemMeta(tmpMeta);
        return oriItem;
    }

    static public int getEditingMode(String p) {
        return opState.getInt(p + ".editingMode");
    }

    static public void changeDisplayName(Player p, String n) {
        Inventory inv = invEditing.get(p.getName());
        inv.setItem(opState.getInt(p.getName() + ".nowEditingSlot"), changeAItem(itemEditing.get(p.getName()), n));
        p.openInventory(inv);

    }

    static public void addLore(Player p, String l) {
        Inventory inv = invEditing.get(p.getName());
        inv.setItem(opState.getInt(p.getName() + ".nowEditingSlot"), addAItemLore(itemEditing.get(p.getName()), l));
        p.openInventory(inv);

    }

    static public void addCmd(Player p, String c) {
        String n = editingMenu.get(p.getName());
        File invFile = new File(GreatMenu.menuFolder, n + ".yml");
        YamlConfiguration inv = YamlConfiguration.loadConfiguration(invFile);
        List<String> cmds = inv.getStringList("cmds." + opState.get(p.getName() + ".nowEditingSlot"));
        if (cmds == null) {
            cmds = new ArrayList<>();
        }
        cmds.add(c);
        inv.set("cmds." + opState.get(p.getName() + ".nowEditingSlot"), cmds);
        menuSave(p, invFile, inv);
        invEditing.put(p.getName(), GreatMenu.menus.get(n));
//        GreatMenu.reLoadMenus();
        p.openInventory(invEditing.get(p.getName()));
        editingMenu.put(p.getName(), n);
    }

    static void removeCmd(Player p) {
        String n = editingMenu.get(p.getName());
        File invFile = new File(GreatMenu.menuFolder, n + ".yml");
        YamlConfiguration inv = YamlConfiguration.loadConfiguration(invFile);
        List<String> cmds = inv.getStringList("cmds." + opState.get(p.getName() + ".nowEditingSlot"));
        if (cmds == null) {
            cmds = new ArrayList<>();
        }
        if (cmds.isEmpty()) {
            return;
        }
        cmds.remove(cmds.size() - 1);
        inv.set("cmds." + opState.get(p.getName() + ".nowEditingSlot"), cmds);
        menuSave(p, invFile, inv);
        Bukkit.getScheduler().runTask(GreatMenu.plugin, () -> {
            p.closeInventory();
            editingMenu.put(p.getName(), n);
            p.openInventory(GreatMenu.menus.get(n));
        });
    }

    static void removeAllCmds(Player p) {
        String n = editingMenu.get(p.getName());
        File invFile = new File(GreatMenu.menuFolder, n + ".yml");
        YamlConfiguration inv = YamlConfiguration.loadConfiguration(invFile);
        inv.set("cmds." + opState.get(p.getName() + ".nowEditingSlot"), null);
        menuSave(p, invFile, inv);
        Bukkit.getScheduler().runTask(GreatMenu.plugin, () -> {
            p.closeInventory();
            editingMenu.put(p.getName(), n);
            p.openInventory(GreatMenu.menus.get(n));
        });
    }

    static void copyCmds(Player p) {
        String n = editingMenu.get(p.getName());
        File invFile = new File(GreatMenu.menuFolder, n + ".yml");
        YamlConfiguration inv = YamlConfiguration.loadConfiguration(invFile);
        List<String> cmds = inv.getStringList("cmds." + opState.get(p.getName() + ".nowEditingSlot"));
        if (cmds == null) {
            return;
        }
        clipboard.put(p.getName(), cmds);
    }

    static void pasteCmds(Player p) {
        String n = editingMenu.get(p.getName());
        File invFile = new File(GreatMenu.menuFolder, n + ".yml");
        YamlConfiguration inv = YamlConfiguration.loadConfiguration(invFile);
        List<String> cmds = inv.getStringList("cmds." + opState.get(p.getName() + ".nowEditingSlot"));
        if (cmds == null) {
            cmds = new ArrayList<>();
        }
        if (clipboard.get(p.getName()) != null) {
            cmds.addAll(clipboard.get(p.getName()));
        }
        inv.set("cmds." + opState.get(p.getName() + ".nowEditingSlot"), cmds);
        menuSave(p, invFile, inv);
        invEditing.put(p.getName(), GreatMenu.menus.get(n));
        Bukkit.getScheduler().runTask(GreatMenu.plugin, () -> {
            p.closeInventory();
            editingMenu.put(p.getName(), n);
            p.openInventory(GreatMenu.menus.get(n));
        });
    }


    //这里需要实现其它类型的箱子
    static public void createAMenu(Player p, String n) {
        YamlConfiguration inv = new YamlConfiguration();
        File invFile = new File(GreatMenu.menuFolder, n + ".yml");
        inv.set("size", invEditing.get(p.getName()).getSize());
        inv.set("title", "Default");
        setYmlItems(invEditing.get(p.getName()).getContents(), inv);
        editingMenu.put(p.getName(), n);
        Inventory tmpInv = Bukkit.createInventory(null, invEditing.get(p.getName()).getSize(), "Default");
        menuSave(p, invFile, inv);
        tmpInv.setContents(invEditing.get(p.getName()).getContents());
        p.openInventory(tmpInv);

    }

    public static void setYmlItems(ItemStack[] items, YamlConfiguration inv) {
        for (int i = 0; i < items.length; i++) {
            inv.set("items." + i, items[i]);
        }
    }

    public static ItemStack[] getYmlItems(YamlConfiguration inv) {
        ItemStack[] res = new ItemStack[inv.getInt("size")];
        for (int i = 0; i < inv.getInt("size"); i++) {
            res[i] = inv.getItemStack("items." + i);
        }
        return res;
    }

    public static void changeAMenuName(Player p, String n) {
        File tmpFile = new File(GreatMenu.menuFolder, editingMenu.get(p.getName()) + ".yml");
        YamlConfiguration tmpYml = YamlConfiguration.loadConfiguration(tmpFile);
        tmpYml.set("title", n);
        menuSave(p, tmpFile, tmpYml);
        p.openInventory(GreatMenu.menus.get(editingMenu.get(p.getName())));
    }


    public static void showCmdsOnLore(Player p) {
        if (!opState.getBoolean(p.getName() + ".showCmds", false) && editingMenu.containsKey(p.getName())) {
            String nowMenu = editingMenu.get(p.getName());
            Inventory tmpMenu = GreatMenu.menus.get(nowMenu);
            List<String>[] cmdListsOfThisMenu = GreatMenu.menusCommands.get(nowMenu);

            for (int i = 0; i < cmdListsOfThisMenu.length; i++) {
                if (cmdListsOfThisMenu[i] != null && !cmdListsOfThisMenu[i].isEmpty()) {
                    if (tmpMenu.getItem(i) == null) {
                        tmpMenu.setItem(i, changeAItem(new ItemStack(Material.COMMAND), "§4这个位置没有方块，但是有命令§g§m§c"));
                    }
                    tmpMenu.setItem(i, addAItemLore(addAItemLore(tmpMenu.getItem(i), "§c§l运行："), cmdListsOfThisMenu[i]));
                }
            }
            opState.set(p.getName() + ".showCmds", true);
        }
    }


    public static void hideCmdsOnLore(Player p) {

        if (opState.getBoolean(p.getName() + ".showCmds", false)) {
            if (editingMenu.containsKey(p.getName())) {
                String nowMenu = editingMenu.get(p.getName());
                Inventory tmpMenu = GreatMenu.menus.get(nowMenu);
                List<String>[] cmdListsOfThisMenu = GreatMenu.menusCommands.get(nowMenu);
                for (int i = 0; i < cmdListsOfThisMenu.length; i++) {
                    if (cmdListsOfThisMenu[i] != null && !cmdListsOfThisMenu[i].isEmpty() && tmpMenu.getItem(i) != null) {
                        if (tmpMenu.getItem(i).getItemMeta().getDisplayName() != null && tmpMenu.getItem(i).getItemMeta().getDisplayName().endsWith("§g§m§c")) {
                            tmpMenu.setItem(i, new ItemStack(Material.AIR));
                        } else {
                            tmpMenu.setItem(i, removeAItemLore(tmpMenu.getItem(i), 1 + cmdListsOfThisMenu[i].size()));
                        }

                    }
                }
            }

            opState.set(p.getName() + ".showCmds", false);
        }
    }


    public static void menuSave(Player p, File f, YamlConfiguration y) {
        hideCmdsOnLore(p);
        try {
            y.save(f);
        } catch (IOException e) {
            GreatMenu.log("存储文件失败");
            throw new RuntimeException(e);
        }
        GreatMenu.reLoadMenus();
    }


    public static ItemStack addAItemLore(ItemStack oriItem, List<String> l) {
        ItemMeta tmpMeta = oriItem.getItemMeta();
        List<String> tmpLore = tmpMeta.getLore();
        if (tmpLore == null) {
            tmpLore = new ArrayList<>();
        }
        tmpLore.addAll(l);
        tmpMeta.setLore(tmpLore);
        oriItem.setItemMeta(tmpMeta);
        return oriItem;
    }

    public static ItemStack removeAItemLore(ItemStack oriItem, int i) {
        ItemMeta tmpMeta = oriItem.getItemMeta();
        List<String> tmpLore = tmpMeta.getLore();
        if (tmpLore == null) {
            return oriItem;
        }
        if (tmpLore.size() - 1 - i > -1) {
            tmpLore = tmpLore.subList(0, tmpLore.size() - i);//因为subList取左闭右开区间，自动+1
        } else {
            tmpLore = new ArrayList<>();
        }

        tmpMeta.setLore(tmpLore);
        oriItem.setItemMeta(tmpMeta);
        return oriItem;
    }


}
