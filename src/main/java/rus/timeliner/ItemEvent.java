/*****************************************************************************
 * 
 * Copyright (c) 2007,2019 Ruslan Scherbakov (novosibman@gmail.com)
 * 
 *****************************************************************************/

package rus.timeliner;

public class ItemEvent {
    public String name;
    public double time;
    public int paletteIndex = 2;

    public ItemEvent(String name, double time, int paletteIndex) {
        this.name = name;
        this.time = time;
        this.paletteIndex = paletteIndex;
    }

    public ItemEvent(String name, double time) {
        this.name = name;
        this.time = time;
    }

    public double getTime() {
        return time;
    }
}