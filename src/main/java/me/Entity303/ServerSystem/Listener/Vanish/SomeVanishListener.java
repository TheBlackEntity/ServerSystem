package me.Entity303.ServerSystem.Listener.Vanish;

import me.Entity303.ServerSystem.Main.ss;
import me.Entity303.ServerSystem.Utils.ServerSystemCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import java.util.List;
import java.util.UUID;

public class SomeVanishListener extends ServerSystemCommand implements Listener {

    public SomeVanishListener(ss plugin) {
        super(plugin);
    }

    @EventHandler
    public void onVehicleCollision(VehicleEntityCollisionEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        if (this.plugin.getVanish().isVanish(player)) {
            e.setCancelled(true);
            e.setPickupCancelled(true);
            e.setCollisionCancelled(true);
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        Entity entity = e.getRemover();
        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;
        if (this.plugin.getVanish().isVanish(player) && this.plugin.getVanish().getAllowInteract().contains(player) && this.plugin.getCommandManager().isInteractActive())
            e.setCancelled(true);

    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
        if (this.plugin.getVanish().isVanish(e.getPlayer())) this.plugin.getVanish().setVanish(true, e.getPlayer());
        else {
            List<UUID> vanished = this.plugin.getVanish().getVanishList();
            for (UUID uuid : vanished)
                if (Bukkit.getOfflinePlayer(uuid).isOnline())
                    this.plugin.getVanish().setVanish(true, Bukkit.getPlayer(uuid));
        }

    }
}