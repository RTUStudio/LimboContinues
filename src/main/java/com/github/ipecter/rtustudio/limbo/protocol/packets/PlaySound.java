package com.github.ipecter.rtustudio.limbo.protocol.packets;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class PlaySound implements MinecraftPacket {

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
