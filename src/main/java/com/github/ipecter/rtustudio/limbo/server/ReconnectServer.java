package com.github.ipecter.rtustudio.limbo.server;

import com.github.ipecter.rtustudio.limbo.LimboContinues;
import com.github.ipecter.rtustudio.limbo.configuration.ReconnectConfig;
import com.github.ipecter.rtustudio.limbo.handler.ReconnectHandler;
import com.github.ipecter.rtustudio.limbo.protocol.packets.PlaySound;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.ClientPlaySessionHandler;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.BossBarPacket;
import lombok.Getter;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;
import net.elytrium.limboapi.api.file.WorldFile;
import net.elytrium.limboapi.api.protocol.PacketDirection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class ReconnectServer {

    private final LimboContinues plugin;
    private final ReconnectConfig config;
    private final boolean debug = false;
    private final Path schematicPath;

    private final LimboFactory factory;
    @Getter
    private Limbo limbo;

    public ReconnectServer(LimboContinues plugin) {
        this.plugin = plugin;
        this.config = new ReconnectConfig(plugin);
        this.schematicPath = plugin.getDir().resolve("Schematics");
        this.factory = (LimboFactory) plugin.getServer().getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).orElseThrow();
        init();
    }

    public ReconnectConfig.Config getConfig() {
        return config.getConfig();
    }

    private void init() {
        ReconnectConfig.Config.World wc = getConfig().getWorld();
        ReconnectConfig.Config.World.Location loc = wc.getLocation();
        VirtualWorld world = this.factory.createVirtualWorld(
                Dimension.valueOf(getConfig().getWorld().getDimension()), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        if (wc.getSchematic().isLoad()) {
            try {
                Path path = this.schematicPath.resolve(wc.getSchematic().getFile());
                WorldFile file = this.factory.openWorldFile(wc.getSchematic().getType(), path);
                ReconnectConfig.Config.World.Schematic.Offset offset = wc.getSchematic().getOffset();
                file.toWorld(this.factory, world, offset.getX(), offset.getY(), offset.getZ(), wc.getLightLevel());
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
        this.limbo = this.factory.createLimbo(world)
                .setName("LimboContinuous_Reconnect")
                .setShouldRejoin(true)
                .setShouldRespawn(true)
                .setGameMode(wc.getGamemode())
                .registerPacket(PacketDirection.CLIENTBOUND, PlaySound.class, PlaySound::new, PlaySound.MAPPINGS);
    }


    public void reload() {
        config.reload();
        init();
    }

    public void send(Player player, RegisteredServer server) {
        ConnectedPlayer connectedPlayer = (ConnectedPlayer) player;
        MinecraftConnection connection = connectedPlayer.getConnection();
        MinecraftSessionHandler minecraftSessionHandler = connection.getActiveSessionHandler();
        if (minecraftSessionHandler != null) {
            if (minecraftSessionHandler instanceof ClientPlaySessionHandler sessionHandler) {
                for (UUID bossBar : sessionHandler.getServerBossBars()) {
                    BossBarPacket deletePacket = new BossBarPacket();
                    deletePacket.setUuid(bossBar);
                    deletePacket.setAction(BossBarPacket.REMOVE);
                    connectedPlayer.getConnection().delayedWrite(deletePacket);
                }
                sessionHandler.getServerBossBars().clear();
            }
        }
        connectedPlayer.getTabList().clearAll();
        limbo.spawnPlayer(player, new ReconnectHandler(plugin, server));
    }
}
