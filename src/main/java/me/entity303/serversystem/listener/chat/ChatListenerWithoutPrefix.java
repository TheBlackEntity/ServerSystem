package me.entity303.serversystem.listener.chat;

import me.entity303.serversystem.bansystem.ManagerMute;
import me.entity303.serversystem.bansystem.Mute;
import me.entity303.serversystem.main.ServerSystem;
import me.entity303.serversystem.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class ChatListenerWithoutPrefix extends MessageUtils implements Listener {

    public ChatListenerWithoutPrefix(ServerSystem plugin) {
        super(plugin);
    }

    @EventHandler
    public void onChatWithoutPrefix(AsyncPlayerChatEvent e) {
        ManagerMute muteManager = this.plugin.getMuteManager();
        if (muteManager.isMuted(e.getPlayer())) {
            Mute mute = muteManager.getMute(e.getPlayer());
            if (mute.getUNMUTE_TIME() > 0) if (mute.getUNMUTE_TIME() <= System.currentTimeMillis())
                muteManager.removeMute(e.getPlayer().getUniqueId());
            if (!mute.isSHADOW()) {
                String senderName = null;
                try {
                    senderName = Bukkit.getOfflinePlayer(UUID.fromString(mute.getSENDER_UUID())).getName();
                } catch (Exception ignored) {
                }
                if (senderName == null) senderName = mute.getSENDER_UUID();
                e.getPlayer().sendMessage(this.getPrefix() + this.getMessage("Mute.Muted", "mute", "mute", senderName, e.getPlayer()).replace("<UNMUTE_DATE>", mute.getUNMUTE_DATE()));
                e.setCancelled(true);
                return;
            }
            e.getRecipients().removeIf(all -> all != e.getPlayer());
        }

    }
}
