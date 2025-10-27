package krypton1101.f10tracker;

import net.minecraft.util.math.Vec3d;
import com.google.gson.JsonObject;

public class Checkpoint {
    private final int id;
    private final String name;
    private final boolean isStartFinish;
    private final double minX, minY, minZ;
    private final double maxX, maxY, maxZ;
    private final int orderIndex;
    
    public Checkpoint(JsonObject json) {
        this.id = json.get("id").getAsInt();
        this.name = json.get("name").getAsString();
        this.isStartFinish = json.get("is_start_finish").getAsInt() == 1;
        this.minX = json.get("min_x").getAsDouble();
        this.minY = json.get("min_y").getAsDouble();
        this.minZ = json.get("min_z").getAsDouble();
        this.maxX = json.get("max_x").getAsDouble();
        this.maxY = json.get("max_y").getAsDouble();
        this.maxZ = json.get("max_z").getAsDouble();
        this.orderIndex = json.get("order_index").getAsInt();
    }
    
    public boolean isPlayerInCheckpoint(Vec3d playerPos) {
        return playerPos.x >= minX && playerPos.x <= maxX &&
               playerPos.y >= minY && playerPos.y <= maxY &&
               playerPos.z >= minZ && playerPos.z <= maxZ;
    }
    
    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isStartFinish() { return isStartFinish; }
    public int getOrderIndex() { return orderIndex; }
    public double getMinX() { return minX; }
    public double getMinY() { return minY; }
    public double getMinZ() { return minZ; }
    public double getMaxX() { return maxX; }
    public double getMaxY() { return maxY; }
    public double getMaxZ() { return maxZ; }
}