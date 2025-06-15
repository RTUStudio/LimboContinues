package com.github.ipecter.rtustudio.limbo.listener;

import com.github.ipecter.rtustudio.limbo.LimboContinues;
import com.github.ipecter.rtustudio.limbo.configuration.QueueConfig;
import com.github.ipecter.rtustudio.limbo.configuration.ReconnectConfig;
import com.github.ipecter.rtustudio.limbo.configuration.SettingConfig;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class PlayerLogin {

    public static final PlainTextComponentSerializer SERIALIZER = PlainTextComponentSerializer.builder().flattener(
            ComponentFlattener.basic()
    ).build();

    private final LimboContinues plugin;
    private final SettingConfig.Config settingConfig;
    private final ReconnectConfig.Config reconnectConfig;
    private final QueueConfig.Config queueConfig;

    public PlayerLogin(LimboContinues plugin) {
        this.plugin = plugin;
        this.settingConfig = plugin.getSettingConfig().getConfig();
        this.reconnectConfig = plugin.getReconnect().getConfig();
        this.queueConfig = plugin.getQueue().getConfig();
    }

    @Subscribe
    public void onLoginLimboRegister(LoginLimboRegisterEvent e) {
        e.setOnKickCallback((callback) -> {
            if (callback.kickedDuringServerConnect()) return false;
            Player player = callback.getPlayer();
            RegisteredServer server = callback.getServer();
            Component component = callback.getServerKickReason().isPresent() ? callback.getServerKickReason().get() : Component.empty();
            String reason = SERIALIZER.serialize(component);
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
