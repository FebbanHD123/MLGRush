package de.febanhd.mlgrush.map.template;

import lombok.Getter;

@Getter
public class MapWorldSlot {

    private final int x;
    private State state;

    public MapWorldSlot(int x) {
        this.x = x;
        this.state = State.FREE;
    }

    public void setOccupied() {
        this.state = State.OCCUPIED;
    }

    public void setFree() {
        this.state = State.FREE;
    }

    public boolean isFree() {
        return this.state == State.FREE;
    }


    private enum State {
        FREE, OCCUPIED;
    }

}
