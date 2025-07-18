package com.github.ipecter.rtustudio.limbo.handler;

import com.github.ipecter.rtustudio.limbo.LimboContinues;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboSessionHandler;
import net.elytrium.limboapi.api.player.LimboPlayer;

public abstract class QueuedHandler implements LimboSessionHandler {

    protected final LimboContinues plugin;
    protected final RegisteredServer server;
    protected boolean tick = false;

    protected LimboPlayer player;

    public QueuedHandler(LimboContinues plugin, RegisteredServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    protected boolean debug() {
        return plugin.getSettingConfig().getConfig().isDebug();
    }

    @Override
    public void onSpawn(Limbo server, LimboPlayer player) {
        this.tick = true;
        this.player = player;
        plugin.verbose("Player '" + player.getProxyPlayer().getUsername() + "' spawned queue.");
        if (player.getProxyPlayer().hasPermission("limbocontinues.priority")) {
            if (plugin.getPriority().add(player)) {
                plugin.verbose("Added player '" + player.getProxyPlayer().getUsername() + "' to priority queue.");
            } else
                plugin.verbose("Not added player '" + player.getProxyPlayer().getUsername() + "' to priority queue.");
        } else {
            if (plugin.getPlayers().add(player)) {
                plugin.verbose("Added player '" + player.getProxyPlayer().getUsername() + "' to queue.");
            } else plugin.verbose("Not added player '" + player.getProxyPlayer().getUsername() + "' to queue.");
        }
        onJoin(server, player);
    }

    public void onJoin(Limbo server, LimboPlayer player) {
    }

    @Override
    public void onDisconnect() {
        this.tick = false;
        player.getProxyPlayer().resetTitle();
        onQuit();
        plugin.getPriority().remove(player);
        plugin.getPlayers().remove(player);
    }

    public void onQuit() {
    }

}
