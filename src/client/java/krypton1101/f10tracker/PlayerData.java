package krypton1101.f10tracker;

import net.minecraft.util.math.Vec3d;
import java.util.UUID;

/**
 * Data structure to hold player position and velocity information with timestamp
 */
public class PlayerData {
    private final long timestamp;
    private final Vec3d position;
    private final Vec3d velocity;
    private final float yaw;
    private final float pitch;
    private final UUID playerUuid;
    
    public PlayerData(long timestamp, Vec3d position, Vec3d velocity, float yaw, float pitch, UUID playerUuid) {
        this.timestamp = timestamp;
        this.position = position;
        this.velocity = velocity;
        this.yaw = yaw;
        this.pitch = pitch;
        this.playerUuid = playerUuid;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public Vec3d getPosition() {
        return position;
    }
    
    public Vec3d getVelocity() {
        return velocity;
    }
    
    public float getYaw() {
        return yaw;
    }
    
    public float getPitch() {
        return pitch;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    @Override
    public String toString() {
        return String.format("PlayerData{UUID=%s, timestamp=%d, pos=(%.3f,%.3f,%.3f), vel=(%.3f,%.3f,%.3f), yaw=%.1f, pitch=%.1f}",
                playerUuid.toString(), timestamp, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z, yaw, pitch);
    }
}
