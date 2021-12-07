package me.Entity303.ServerSystem.Utils.versions.offlineplayer.teleport;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Teleport_v1_8_R3 implements Teleport {

    @Override
    public void teleport(Player player, Location location) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.locX = location.getX();
        entityPlayer.locY = location.getY();
        entityPlayer.locZ = location.getZ();
        entityPlayer.yaw = location.getYaw();
        entityPlayer.pitch = location.getPitch();
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        WorldServer worldServer = craftWorld.getHandle();
        entityPlayer.world = worldServer;
    }
}
