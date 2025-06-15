package com.github.ipecter.rtustudio.limbo.configuration.serializer;

import com.github.ipecter.rtustudio.limbo.protocol.packets.PlaySound;
import net.elytrium.serializer.custom.ClassSerializer;

import java.util.LinkedHashMap;
import java.util.Map;

public class PlaySoundSerializer extends ClassSerializer<PlaySound, Map<String, Object>> {

    @SuppressWarnings("unchecked")
    protected PlaySoundSerializer() {
        super(PlaySound.class, (Class<Map<String, Object>>) (Class<?>) Map.class);
    }

    @Override
    public Map<String, Object> serialize(PlaySound from) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", from.getSoundName());
        map.put("volume", from.getVolume());
        map.put("pitch", from.getPitch());
        return map;
    }

    @Override
    public PlaySound deserialize(Map<String, Object> from) {
        return new PlaySound((String) from.get("name"), (float) (double) from.get("volume"), (float) (double) from.get("pitch"));
    }

}
