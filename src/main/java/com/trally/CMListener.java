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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class CMListener implements Listener {
    //我已经忘记”CM“是什么意思了QAQ，大概是CloseMenu  (?)
    @EventHandler
    void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String msg = e.getMessage();
        if (p.isOp()) {
            msg = msg.replace('&', '§');
            msg = msg.replace("§§", "&");
            String finalMsg = msg;
            switch (MenuListener.getEditingMode(p.getName())) {
                case 0:
                    e.setCancelled(true);
                    Bukkit.getScheduler().runTask(GreatMenu.plugin, () -> MenuListener.changeDisplayName(p, finalMsg));
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    break;

                case 1:
                    e.setCancelled(true);
                    Bukkit.getScheduler().runTask(GreatMenu.plugin, () -> MenuListener.addLore(p, finalMsg));
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    break;

                case 2:


                    break;
                case 3:
                    e.setCancelled(true);
                    Bukkit.getScheduler().runTask(GreatMenu.plugin, () -> MenuListener.addCmd(p, finalMsg));
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    break;

                case 6:
                    e.setCancelled(true);
                    Bukkit.getScheduler().runTask(GreatMenu.plugin, () -> MenuListener.changeAMenuName(p, finalMsg));
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    break;

                case 7:
                    e.setCancelled(true);
                    Bukkit.getScheduler().runTask(GreatMenu.plugin, () -> MenuListener.createAMenu(p, finalMsg));
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    break;
            }
        }
    }
}
