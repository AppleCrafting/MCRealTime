package ing.applecraft.mcrealtime.time;

import java.time.Instant;

/**
 * Maps a real-world instant to one Minecraft day tick in the range 0..23999.
 */
public interface TimeProvider {
    long getMinecraftTime(Instant instant);
}
