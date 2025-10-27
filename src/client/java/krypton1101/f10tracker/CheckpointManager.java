package krypton1101.f10tracker;

import net.minecraft.util.math.Vec3d;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckpointManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("F10Tracker-CheckpointManager");
    
    private final List<Checkpoint> checkpoints;
    
    public CheckpointManager() {
        this.checkpoints = new ArrayList<>();
    }
    
    public void updateCheckpoints(String json) {
        try {
            JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
            checkpoints.clear();
            
            for (JsonElement element : jsonArray) {
                checkpoints.add(new Checkpoint(element.getAsJsonObject()));
            }
            
            LOGGER.info("Updated checkpoints. Total: {}", checkpoints.size());
        } catch (Exception e) {
            LOGGER.error("Failed to parse checkpoint data: {}", e.getMessage(), e);
        }
    }
    
    public boolean checkPlayerPosition(Vec3d playerPos) {
        for (Checkpoint checkpoint : checkpoints) {
            boolean isInside = checkpoint.isPlayerInCheckpoint(playerPos);
            
            // If player just entered the checkpoint and we haven't sent data for it yet
            if (isInside) {
                LOGGER.debug("Player entered checkpoint {}: {}", checkpoint.getId(), checkpoint.getName());
            }
            // If player left the checkpoint, remove from entered set
            else if (!isInside) {
                LOGGER.debug("Player left checkpoint {}: {}", checkpoint.getId(), checkpoint.getName());
            }
            return isInside;
        }
        return false;
    }
    
    public List<Checkpoint> getCheckpoints() {
        return new ArrayList<>(checkpoints);
    }
    
    public int getCheckpointCount() {
        return checkpoints.size();
    }
}