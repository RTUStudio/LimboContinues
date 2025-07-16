package com.github.ipecter.rtustudio.limbo.listener;

import com.github.ipecter.rtustudio.limbo.LimboContinues;
import com.github.ipecter.rtustudio.limbo.configuration.QueueConfig;
import com.github.ipecter.rtustudio.limbo.configuration.ReconnectConfig;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import lombok.extern.slf4j.Slf4j;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;

@Slf4j
public class PlayerLogin {

    public static final PlainTextComponentSerializer SERIALIZER = PlainTextComponentSerializer.builder().flattener(
            ComponentFlattener.basic()
    ).build();

    private final LimboContinues plugin;
    private final ReconnectConfig.Config reconnectConfig;
    private final QueueConfig.Config queueConfig;

    public PlayerLogin(LimboContinues plugin) {
        this.plugin = plugin;
        this.reconnectConfig = plugin.getReconnect().getConfig();
        this.queueConfig = plugin.getQueue().getConfig();
    }

    @Subscribe
    private void onLogin(ServerPreConnectEvent e, Continuation continuation) {
        if (e.getPreviousServer() != null) {
            continuation.resume();
            return;
        }
        Player player = e.getPlayer();
        RegisteredServer server = e.getOriginalServer();
        server.ping().whenCompleteAsync((pong, throwable) -> {
            if (pong != null) {
                boolean isFull = false;
                QueueConfig.Config.Queue.MaxPlayer mp = queueConfig.getQueue().getMaxPlayer();
                if (pong.getPlayers().isPresent() && !mp.getBypass().contains(player.getUsername())) {
                    ServerPing.Players players = pong.getPlayers().get();
                    int max = mp.isEnabled() ? Math.min(mp.getSize(), players.getMax()) : players.getMax();
                    plugin.verbose("online: " + players.getOnline() + ", max: " + max);
                    isFull = players.getOnline() >= max;
                }
                if (isFull) {
                    List<String> bypass = queueConfig.getQueue().getMaxPlayer().getBypass();
                    if (!bypass.contains(player.getUsername())) {
                        e.setResult(ServerPreConnectEvent.ServerResult.denied());
                        plugin.getQueue().send(player, server);
                    }
                }
            } else {
                e.setResult(ServerPreConnectEvent.ServerResult.denied());
                plugin.getReconnect().send(player, server);
            }
            continuation.resume();
        });
    }

    @Subscribe
    private void onLoginLimboRegister(LoginLimboRegisterEvent e) {
        e.setOnKickCallback((callback) -> {
            if (callback.kickedDuringServerConnect()) return false;
            Player player = callback.getPlayer();
            RegisteredServer server = callback.getServer();
            Component component = callback.getServerKickReason().isPresent() ? callback.getServerKickReason().get() : Component.empty();
            String reason = SERIALIZER.serialize(component);
            plugin.verbose("reason: " + reason);
            if (player.getCurrentServer().isEmpty()) {
                if (reason.isEmpty() || reason.matches(queueConfig.getTrigger())) {
                    plugin.getQueue().send(player, server);
                    return true;
                }
            } else {
                if (reason.matches(reconnectConfig.getTrigger())) {
                    plugin.getReconnect().send(player, server);
                    return true;
                }
            }
            return false;
        });
    }
}
