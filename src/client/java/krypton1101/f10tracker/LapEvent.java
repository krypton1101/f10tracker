package krypton1101.f10tracker;

/**
 * Data structure to hold lap event information with UUID, timestamp, and start flag
 */
public class LapEvent {
    private final String uuid;
    private final long timestamp;
    private final boolean isStart;
    
    public LapEvent(String uuid, long timestamp, boolean isStart) {
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.isStart = isStart;
    }
    
    public String getUuid() {
        return uuid;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isStart() {
        return isStart;
    }
    
    @Override
    public String toString() {
        return String.format("LapEvent{UUID=%s, timestamp=%d, isStart=%b}", 
                uuid, timestamp, isStart);
    }
}