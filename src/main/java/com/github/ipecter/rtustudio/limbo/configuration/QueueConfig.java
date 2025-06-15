package com.github.ipecter.rtustudio.limbo.configuration;

import com.github.ipecter.rtustudio.limbo.LimboContinues;
import com.github.ipecter.rtustudio.limbo.configuration.serializer.PlaySoundSerializer;
import com.github.ipecter.rtustudio.limbo.protocol.packets.PlaySound;
import lombok.Getter;
import net.elytrium.limboapi.api.file.BuiltInWorldFileType;
import net.elytrium.limboapi.api.player.GameMode;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.Serializer;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.nio.file.Path;

@Getter
public class QueueConfig {

    private final Path path;
    private final Config config;

    public QueueConfig(LimboContinues plugin) {
        this.path = plugin.getDir().resolve("Configs").resolve("Queue.yml");
        this.config = new Config();
    }

    public void reload() {
        config.reload(path);
    }

    @Getter
    public static class Config extends YamlSerializable {

        private static final SerializerConfig CONFIG = new SerializerConfig.Builder().setCommentValueIndent(1).build();
        @Comment(value = @CommentValue("Send player to the limbo, if disconnect reason contains this text (using regex)"))
        public String trigger = "((?i)^(server closed|server is restarting|multiplayer\\.disconnect\\.server_shutdown))+$";
        private final Server server = new Server();
        private final World world = new World();
        private final Offline offline = new Offline();
        private final Queue queue = new Queue();
        private final Connect connect = new Connect();

        public Config() {
            super(CONFIG);
        }

        @Getter
        public static class Offline {

            private final String message = "Server is offline";

            private final Title title = new Title();
            @Serializer(PlaySoundSerializer.class)
            private final PlaySound sound = new PlaySound("entity.experience_orb.pickup", 0.5f, 0.5f);

            @Getter
            public static class Title {
                private final String title = "";
                private final String subtitle = "<red>Server is offline</red>";
            }

        }

        @Getter
        public static class Queue {

            private final String message = "Queue: {0}";

            private final Title title = new Title();
            @Serializer(PlaySoundSerializer.class)
            private final PlaySound sound = new PlaySound("entity.experience_orb.pickup", 0.5f, 0.5f);

            @Getter
            public static class Title {
                private final String title = "";
                private final String subtitle = "Queue: {0}";
            }

        }

        @Getter
        public static class Connect {

            private final String message = "Connecting!";

            private final Title title = new Title();
            @Serializer(PlaySoundSerializer.class)
            private final PlaySound sound = new PlaySound("entity.player.levelup", 1, 1);

            @Getter
            public static class Title {
                private final String title = "";
                private final String subtitle = "<green>Connecting...</green>";
            }

        }

        @Getter
        public static class Server {
            @Comment(value = @CommentValue("Server status check interval in milliseconds"))
            private final long check = 1000;
            @Comment(value = @CommentValue("Server status check timeout in milliseconds"))
            private final long timeout = 500;
            @Comment(value = @CommentValue("Connect delay after server startup"))
            private final long delay = 2000;
        }


        @Getter
        public static class World {

            @Comment(value = @CommentValue("Dimensions: OVERWORLD, NETHER, THE_END"))
            private final String dimension = "OVERWORLD";
            private final World.Schematic schematic = new World.Schematic();
            private final int lightLevel = 15;
            private final GameMode gamemode = GameMode.SPECTATOR;
            private final World.Location location = new World.Location();

            @Getter
            public static class Location {
                private final int x = 0;
                private final int y = 100;
                private final int z = 0;
                private final float pitch = 0;
                private final float yaw = 90;
            }

            @Getter
            public static class Schematic {
                @Comment(value = @CommentValue("Load world from file"))
                private final boolean load = false;
                @Comment(value = @CommentValue("Type: SCHEMATIC, WORLDEDIT_SCHEM, STRUCTURE"))
                private final BuiltInWorldFileType type = BuiltInWorldFileType.WORLDEDIT_SCHEM;
                @Comment(value = @CommentValue("Schematic file name"))
                private final String file = "world.schem";
                private final World.Schematic.Offset offset = new World.Schematic.Offset();

                @Getter
                public static class Offset {
                    private final int x = 0;
                    private final int y = 100;
                    private final int z = 0;
                }
            }
        }
    }
}
