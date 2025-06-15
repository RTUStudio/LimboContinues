package com.github.ipecter.rtustudio.limbo.configuration;

import com.github.ipecter.rtustudio.limbo.LimboContinues;
import lombok.Getter;
import net.elytrium.commons.kyori.serialization.Serializers;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.nio.file.Path;

@Getter
public class SettingConfig {

    private final LimboContinues plugin;
    private final Path path;
    private final Config config = new Config();

    public SettingConfig(LimboContinues plugin) {
        this.plugin = plugin;
        this.path = plugin.getDir().resolve("Configs").resolve("Setting.yml");
    }

    public void reload() {
        config.reload(path);
    }

    @Getter
    public static class Config extends YamlSerializable {

        private static final SerializerConfig CONFIG = new SerializerConfig.Builder().setCommentValueIndent(1).build();
        @Comment(value = {
                @CommentValue("Available serializers:"),
                @CommentValue("LEGACY_AMPERSAND - \"&c&lExample &c&9Text\"."),
                @CommentValue("LEGACY_SECTION - \"§c§lExample §c§9Text\"."),
                @CommentValue("MINIMESSAGE - \"<bold><red>Example</red> <blue>Text</blue></bold>\". (https://webui.adventure.kyori.net/)"),
                @CommentValue("GSON - \"[{\"text\":\"Example\",\"bold\":true,\"color\":\"red\"},{\"text\":\" \",\"bold\":true},{\"text\":\"Text\",\"bold\":true,\"color\":\"blue\"}]\". (https://minecraft.tools/en/json_text.php/)"),
                @CommentValue("GSON_COLOR_DOWNSAMPLING - Same as GSON, but uses downsampling.")
        })
        private final Serializers serializer = Serializers.MINIMESSAGE;
        @Getter
        private boolean debug = false;

        public Config() {
            super(CONFIG);
        }
    }
}
