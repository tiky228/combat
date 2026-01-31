package com.codex.battleground;

public class PlayerState {
    private long blockingUntil;
    private long stunnedUntil;

    public long getBlockingUntil() {
        return blockingUntil;
    }

    public void setBlockingUntil(long blockingUntil) {
        this.blockingUntil = blockingUntil;
    }

    public long getStunnedUntil() {
        return stunnedUntil;
    }

    public void setStunnedUntil(long stunnedUntil) {
        this.stunnedUntil = stunnedUntil;
    }
}
