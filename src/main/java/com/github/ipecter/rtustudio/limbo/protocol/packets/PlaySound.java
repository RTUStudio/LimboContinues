package com.github.ipecter.rtustudio.limbo.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.elytrium.limboapi.api.protocol.packets.PacketMapping;

@Getter
public class PlaySound implements MinecraftPacket {

    public static final PacketMapping[] MAPPINGS = new PacketMapping[]{
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
            new PacketMapping(0x6e, ProtocolVersion.MINECRAFT_1_21_5, true),
    };

    private final String soundName;
    private final float volume;
    private final float pitch;
    private int playerX;
    private int playerY;
    private int playerZ;

    public PlaySound(String soundName, double x, double y, double z, float volume, float pitch) {
        this.soundName = soundName;
        this.setPosition(x, y, z);
        this.volume = volume;
        this.pitch = pitch;
    }

    public PlaySound(String soundName, float volume, float pitch) {
        this.soundName = soundName;
        this.setPosition(0, 0, 0);
        this.volume = volume;
        this.pitch = pitch;
    }

    public PlaySound() {
        throw new IllegalStateException();
    }

    @Override
    public void decode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new IllegalStateException();
    }

    @Override
    public void encode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_19_3)) {
            ProtocolUtils.writeVarInt(byteBuf, 0);
            ProtocolUtils.writeString(byteBuf, this.soundName);
            byteBuf.writeBoolean(false);
        } else {
            ProtocolUtils.writeString(byteBuf, this.soundName);
        }
        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_9)) {
            ProtocolUtils.writeVarInt(byteBuf, 0);
        }
        byteBuf.writeInt(this.playerX);
        byteBuf.writeInt(this.playerY);
        byteBuf.writeInt(this.playerZ);
        byteBuf.writeFloat(this.volume);
        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_10)) {
            byteBuf.writeFloat(this.pitch);
        } else {
            byteBuf.writeByte((int) (this.pitch * 63.5F));
        }
        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_19)) {
            byteBuf.writeLong(0);
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler minecraftSessionHandler) {
        return false;
    }

    public PlaySound setPosition(double x, double y, double z) {
        this.playerX = (int) (x * 8);
        this.playerY = (int) (y * 8);
        this.playerZ = (int) (z * 8);
        return this;
    }
}
