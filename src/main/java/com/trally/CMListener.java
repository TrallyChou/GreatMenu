/*
 *  Copyright (c) 2023. Trally Chou (Zhou jiale, in Chinese)
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.trally;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class CMListener implements Listener {

    @EventHandler
    void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String msg = e.getMessage();
        msg = msg.replace('&', '§');
        msg = msg.replace("§§", "&");

        if (p.isOp()) {
            switch (MenuListener.getEditingMode(p.getName())) {
                case 0:
                    e.setCancelled(true);
                    MenuListener.changeDisplayName(p, msg);
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    break;

                case 1:
                    e.setCancelled(true);
                    MenuListener.addLore(p, msg);
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    break;

                case 2:


                    break;
                case 3:
                    e.setCancelled(true);
                    MenuListener.addCmd(p,msg);
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    break;

                case 6:
                    e.setCancelled(true);
                    MenuListener.changeAMenuName(p,msg);
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    break;

                case 7:
                    e.setCancelled(true);
                    MenuListener.createAMenu(p,msg);
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    break;
            }
        }
    }
}
