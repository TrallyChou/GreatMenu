/*
 *  Copyright (c) 2023. Trally Chou (Zhou jiale, in Chinese)
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.trally;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class CommandExecute extends BukkitRunnable {
    public List<String> commands;
    public Player p;
    public int index;

    public CommandExecute(List<String> commands, Player p, int index) {
        this.commands = commands;
        this.p = p;
        this.index = index;
    }

    @Override
    public void run() {
        for (int i = index; i < commands.size(); i++) {
            String tmpCmd = commands.get(i);

            tmpCmd = tmpCmd.replace("<Player>", p.getName()).replace("<World>", p.getWorld().getName());
            if (GreatMenu.econ != null) {
                tmpCmd = tmpCmd.replace("<Money>", String.valueOf(GreatMenu.econ.getBalance(p)));
            }
            if (tmpCmd.startsWith("$c1")) {  //命令
                tmpCmd = tmpCmd.substring(3);
                String finalTmpCmd = tmpCmd;
                p.chat("/" + finalTmpCmd);
                continue;
            }

            if (tmpCmd.startsWith("$c2")) {  //控制台命令
                tmpCmd = tmpCmd.substring(3);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), tmpCmd);
                continue;
            }

            if (tmpCmd.startsWith("$c3")) {  //op命令
                tmpCmd = tmpCmd.substring(3);
                boolean isOp = p.isOp();
                p.setOp(true);
                String finalTmpCmd = tmpCmd;
                p.chat("/" + finalTmpCmd);
                p.setOp(isOp);
                continue;
            }


            if (tmpCmd.equals("!c")) {  //关闭
                p.closeInventory();
                continue;
            }

            if (tmpCmd.startsWith("$c")) { //chat说话
                tmpCmd = tmpCmd.substring(2);
                p.chat(tmpCmd);
                continue;
            }

            if (tmpCmd.startsWith("$m")) { //message给玩家发消息
                tmpCmd = tmpCmd.substring(2);
                p.sendMessage(tmpCmd);
                continue;
            }

            if (tmpCmd.startsWith("$t")) { //title给玩家发标题
                tmpCmd = tmpCmd.substring(2);
                p.sendTitle(tmpCmd, "");
                continue;
            }

            if (tmpCmd.startsWith("$eg")) {   //give
                tmpCmd = tmpCmd.substring(3);
                double e = Integer.parseInt(tmpCmd);
                if (GreatMenu.econ != null) {
                    EconomyResponse r = GreatMenu.econ.depositPlayer(p, e);
                    if (!r.transactionSuccess()) {
                        p.sendMessage("出现问题，请联系服务器管理员");
                    }
                } else {
                    p.sendMessage("出现问题，请联系服务器管理员");
                }

                continue;
            }

            if (tmpCmd.startsWith("$et")) {  //take
                tmpCmd = tmpCmd.substring(3);
                double e = Integer.parseInt(tmpCmd);
                if (GreatMenu.econ != null) {
                    if (GreatMenu.econ.getBalance(p) >= e) {
                        EconomyResponse r = GreatMenu.econ.withdrawPlayer(p, e);
                        if (!r.transactionSuccess()) {
                            p.sendMessage("出现问题，请联系服务器管理员");
                        }
                    } else {
                        p.sendMessage("你没有这么多钱");
                        break;
                    }

                } else {
                    p.sendMessage("出现问题，请联系服务器管理员");
                }

                continue;
            }

            if (tmpCmd.startsWith("$d")) { //delay延迟
                tmpCmd = tmpCmd.substring(2);
                int t = Integer.parseInt(tmpCmd);
                new CommandExecute(commands, p, i + 1).runTaskLater(GreatMenu.plugin, t);
                break;
            }

        }
    }
}
