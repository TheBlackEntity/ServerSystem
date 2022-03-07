package me.entity303.serversystem.virtual.containeraccess;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.ContainerAccess;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.World;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.function.BiFunction;

public class ContainerAccess_v1_15_R1 extends ContainerAccessWrapper implements ContainerAccess {
    private final EntityPlayer human;
    private final Player player;

    public ContainerAccess_v1_15_R1(Player player) {
        this.player = player;
        this.human = ((CraftPlayer) player).getHandle();
    }

    @Override
    public World getWorld() {
        return this.human.getWorld();
    }

    @Override
    public BlockPosition getPosition() {
        return new BlockPosition(this.player.getLocation().getX(), this.player.getLocation().getY(), this.player.getLocation().getZ());
    }

    @Override
    public <T> Optional<T> a(BiFunction<World, BlockPosition, T> biFunction) {
        return Optional.of(biFunction.apply(this.human.getWorld(), new BlockPosition(this.player.getLocation().getX(), this.player.getLocation().getY(), this.player.getLocation().getZ())));
    }
}