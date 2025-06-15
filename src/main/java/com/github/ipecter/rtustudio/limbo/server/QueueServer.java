package com.github.ipecter.rtustudio.limbo.server;

import com.github.ipecter.rtustudio.limbo.LimboContinues;
import com.github.ipecter.rtustudio.limbo.configuration.QueueConfig;
import com.github.ipecter.rtustudio.limbo.handler.QueueHandler;
import com.github.ipecter.rtustudio.limbo.protocol.packets.PlaySound;
import com.velocitypowered.api.network.ProtocolVersion;
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
import net.elytrium.limboapi.api.protocol.packets.PacketMapping;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class QueueServer {

    private final LimboContinues plugin;
    private final QueueConfig config;
    private final boolean debug = false;
    private final Path schematicPath;

    private final LimboFactory factory;
    @Getter
    private Limbo limbo;

    public QueueServer(LimboContinues plugin) {
        this.plugin = plugin;
        this.config = new QueueConfig(plugin);
        this.schematicPath = plugin.getDir().resolve("Schematics");
        this.factory = (LimboFactory) plugin.getServer().getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).orElseThrow();
        init();
    }

    public QueueConfig.Config getConfig() {
        return config.getConfig();
    }

    private void init() {
        QueueConfig.Config.World wc = getConfig().getWorld();
        QueueConfig.Config.World.Location loc = wc.getLocation();
        VirtualWorld world = this.factory.createVirtualWorld(
                Dimension.valueOf(getConfig().getWorld().getDimension()), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        if (wc.getSchematic().isLoad()) {
            try {
                Path path = this.schematicPath.resolve(wc.getSchematic().getFile());
                WorldFile file = this.factory.openWorldFile(wc.getSchematic().getType(), path);
                QueueConfig.Config.World.Schematic.Offset offset = wc.getSchematic().getOffset();
                file.toWorld(this.factory, world, offset.getX(), offset.getY(), offset.getZ(), wc.getLightLevel());
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
        this.limbo = this.factory.createLimbo(world)
                .setName("LimboContinuous_Queue")
                .setShouldRejoin(true)
                .setShouldRespawn(true)
                .setGameMode(wc.getGamemode())
                .registerPacket(PacketDirection.CLIENTBOUND, PlaySound.class, PlaySound::new, new PacketMapping[]{
                        new PacketMapping(0x29, ProtocolVersion.MINECRAFT_1_7_2, true),
                        new PacketMapping(0x19, ProtocolVersion.MINECRAFT_1_9, true),
                        new PacketMapping(0x1a, ProtocolVersion.MINECRAFT_1_13, true),
                        new PacketMapping(0x19, ProtocolVersion.MINECRAFT_1_14, true),
                        new PacketMapping(0x1a, ProtocolVersion.MINECRAFT_1_15, true),
                        new PacketMapping(0x19, ProtocolVersion.MINECRAFT_1_16, true),
                        new PacketMapping(0x18, ProtocolVersion.MINECRAFT_1_16_2, true),
                        new PacketMapping(0x19, ProtocolVersion.MINECRAFT_1_17, true),
                        new PacketMapping(0x16, ProtocolVersion.MINECRAFT_1_19, true),
                        new PacketMapping(0x17, ProtocolVersion.MINECRAFT_1_19_1, true),
                        new PacketMapping(0x5e, ProtocolVersion.MINECRAFT_1_19_3, true),
                        new PacketMapping(0x62, ProtocolVersion.MINECRAFT_1_19_4, true),
                        new PacketMapping(0x64, ProtocolVersion.MINECRAFT_1_20_2, true),
                        new PacketMapping(0x66, ProtocolVersion.MINECRAFT_1_20_3, true),
                        new PacketMapping(0x68, ProtocolVersion.MINECRAFT_1_20_5, true),
                        new PacketMapping(0x6f, ProtocolVersion.MINECRAFT_1_21_2, true),
                });
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
        limbo.spawnPlayer(player, new QueueHandler(plugin, server));
    }
}
