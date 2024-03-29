package me.entity303.serversystem.utils.versions.offlineplayer.entityplayer;

import com.mojang.authlib.GameProfile;
import me.entity303.serversystem.main.ServerSystem;
import me.entity303.serversystem.utils.MessageUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.World;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

public class EntityPlayer_Latest extends MessageUtils implements EntityPlayer {
    private static Method getWorldServerMethod = null;
    private Constructor<net.minecraft.server.level.EntityPlayer> entityPlayerConstructor = null;

    public EntityPlayer_Latest(ServerSystem plugin) {
        super(plugin);
    }

    @Override
    public Object getEntityPlayer(OfflinePlayer offlinePlayer) {
        if (EntityPlayer_Latest.getWorldServerMethod == null)
            EntityPlayer_Latest.getWorldServerMethod = Arrays.stream(MinecraftServer.class.getDeclaredMethods())
                    .filter(method -> method.getParameters().length == 1)
                    .filter(method -> method.getParameters()[0].getType().getName().equalsIgnoreCase(ResourceKey.class.getName()))
                    .filter(method -> method.getReturnType().getName().toLowerCase(Locale.ROOT).contains("world"))
                    .findFirst().orElse(null);

        GameProfile gameProfile = new GameProfile(offlinePlayer.getUniqueId(), offlinePlayer.getName());

        Field worldResourceKeyField = Arrays.stream(World.class.getDeclaredFields()).filter(field -> {
            if (!field.getType().getName().equalsIgnoreCase("net.minecraft.resources.ResourceKey"))
                return false;

            try {
                String name = field.get(null).toString();

                return name.toLowerCase().contains("overworld");
            } catch (IllegalAccessException e) {
                return false;
            }

        }).findFirst().orElse(null);

        ResourceKey<World> worldResourceKey = null;
        try {
            worldResourceKey = (ResourceKey<World>) worldResourceKeyField.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        try {

            if (this.entityPlayerConstructor != null) {

                int lastParameter = this.entityPlayerConstructor.getParameterCount() - 1;

                if (this.entityPlayerConstructor.getParameterTypes()[lastParameter].getName().contains("ClientInformation"))
                    try {
                        Class clientInformationClass = Class.forName("net.minecraft.server.level.ClientInformation");

                        Method createDefaultMethod = Arrays.stream(clientInformationClass.getDeclaredMethods()).filter(method -> method.getReturnType().getName().contains("ClientInformation")).findFirst().orElse(null);

                        return this.entityPlayerConstructor.newInstance(MinecraftServer.getServer(), (WorldServer) EntityPlayer_Latest.getWorldServerMethod.invoke(MinecraftServer.getServer(), worldResourceKey), gameProfile, createDefaultMethod.invoke(null));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return null;
                    }

                return this.entityPlayerConstructor.newInstance(MinecraftServer.getServer(), (WorldServer) EntityPlayer_Latest.getWorldServerMethod.invoke(MinecraftServer.getServer(), worldResourceKey), gameProfile, null);
            }

            try {
                return new net.minecraft.server.level.EntityPlayer(MinecraftServer.getServer(), (WorldServer) EntityPlayer_Latest.getWorldServerMethod.invoke(MinecraftServer.getServer(), worldResourceKey), gameProfile);
            } catch (NoSuchMethodError ignored) {
                this.entityPlayerConstructor = (Constructor<net.minecraft.server.level.EntityPlayer>) net.minecraft.server.level.EntityPlayer.class.getConstructors()[0];

                int lastParameter = this.entityPlayerConstructor.getParameterCount() - 1;

                if (this.entityPlayerConstructor.getParameterTypes()[lastParameter].getName().contains("ClientInformation"))
                    try {
                        Class clientInformationClass = Class.forName("net.minecraft.server.level.ClientInformation");

                        Method createDefaultMethod = Arrays.stream(clientInformationClass.getDeclaredMethods()).filter(method -> method.getReturnType().getName().contains("ClientInformation")).findFirst().orElse(null);

                        return this.entityPlayerConstructor.newInstance(MinecraftServer.getServer(), (WorldServer) EntityPlayer_Latest.getWorldServerMethod.invoke(MinecraftServer.getServer(), worldResourceKey), gameProfile, createDefaultMethod.invoke(null));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return null;
                    }

                return this.entityPlayerConstructor.newInstance(MinecraftServer.getServer(), (WorldServer) EntityPlayer_Latest.getWorldServerMethod.invoke(MinecraftServer.getServer(), worldResourceKey), gameProfile, null);
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }
}
