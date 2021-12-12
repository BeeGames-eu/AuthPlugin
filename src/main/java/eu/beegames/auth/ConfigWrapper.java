package eu.beegames.auth;

import eu.beegames.auth.data.VectorPair;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.util.Locale;

public class ConfigWrapper {
    private final FileConfiguration cfg;

    private String wn;
    private TargetMode tm;
    private String dest;
    private VectorPair tReg;
    private float sy;
    private float sp;

    public ConfigWrapper(FileConfiguration _cfg) {
        cfg = _cfg;
    }

    public String getWorldName() {
        if (wn != null) return wn;

        return wn = cfg.getString("world_name", "lobby");
    }


    public TargetMode getTargetMode() {
        if (tm != null) return tm;

        try {
            return tm = TargetMode.valueOf(cfg.getString("target_mode", "portal").toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            return tm = TargetMode.PORTAL;
        }
    }

    public String getDestination() {
        if (dest != null) return dest;

        return dest = cfg.getString("target_server", "lobby");
    }

    public VectorPair getTargetRegion() {
        if (tReg != null) return tReg;

        return tReg = new VectorPair(
                cfg.getVector("target.from", new Vector(0, 0, 0)),
                cfg.getVector("target.to", new Vector(0, 0, 0))
        );
    }

    public float getSpawnYaw() {
        if (sy != 0.0) return sy;

        return sy = (float)cfg.getDouble("spawn.yaw", 0.0);
    }

    public float getSpawnPitch() {
        if (sp != 0.0) return sp;

        return sp = (float)cfg.getDouble("spawn.pitch", 0.0);
    }
}
