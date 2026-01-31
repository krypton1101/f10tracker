package krypton1101.f10tracker;

/**
 * Data structure to hold lap event information with nickname, lap count, timestamp, and start flag
 */
public class LapEvent {
    private final String nickname;
    private final int lapCount;
    private final long timestamp;
    private final boolean isStart;
    
    public LapEvent(String nickname, int lapCount, long timestamp, boolean isStart) {
        this.nickname = nickname;
        this.lapCount = lapCount;
        this.timestamp = timestamp;
        this.isStart = isStart;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public int getLapCount() {
        return lapCount;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isStart() {
        return isStart;
    }
    
    @Override
    public String toString() {
        return String.format("LapEvent{nickname=%s, lapCount=%d, timestamp=%d, isStart=%b}",
                nickname, lapCount, timestamp, isStart);
    }
}