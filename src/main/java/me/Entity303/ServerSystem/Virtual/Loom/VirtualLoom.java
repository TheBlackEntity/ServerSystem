package me.Entity303.ServerSystem.Virtual.Loom;

import me.Entity303.ServerSystem.Virtual.ContainerAccess.ContainerAccessWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public abstract class VirtualLoom {

    String version = null;

    public abstract void openLoom(Player player);

    protected ContainerAccessWrapper getWrapper(Player player) {
        try {
            Class clazz = Class.forName("me.Entity303.ServerSystem.Virtual.ContainerAccess.ContainerAccess_" + this.getVersion());
            return (ContainerAccessWrapper) clazz.getConstructor(Player.class).newInstance(player);
        } catch (ClassNotFoundException ignored) {
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            Class clazz = Class.forName("me.Entity303.ServerSystem.Virtual.ContainerAccess.ContainerAccess_Latest");
            return (ContainerAccessWrapper) clazz.getConstructor(Player.class).newInstance(player);
        } catch (ClassNotFoundException ignored) {
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getVersion() {
        if (this.version == null) try {
            this.version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
        return this.version;
    }
}