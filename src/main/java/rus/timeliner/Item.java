/*****************************************************************************
 * 
 * Copyright (c) 2007,2019 Ruslan Scherbakov (novosibman@gmail.com)
 * 
 *****************************************************************************/

package rus.timeliner;

import java.util.ArrayList;

public class Item {
    public String name;
    public Object data;
    public int nesting;
    public int paletteIndex;
    public double startTime;
    public double endTime;
    public boolean expanded;
    public boolean selected;
    public final ArrayList<Item> children = new ArrayList<Item>();
    public final ArrayList<ItemEvent> events = new ArrayList<ItemEvent>();

    public Item(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public Item(double startTime, double endTime, String name) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Item(double startTime, double endTime, String name, ArrayList<ItemEvent> events) {
        this(startTime, endTime, name);
        if (events != null) {
            this.events.addAll(events);
            // populateChildrenFromEvents();
        }
    }

    public void populateChildrenFromEvents() {
        children.clear();
        int count = events.size();
        for (int i = 0; i < count - 1; i++) {
            Item e = new Item(events.get(i).time, events.get(i + 1).time, events.get(i).name);
            e.paletteIndex = events.get(i).paletteIndex;
            children.add(e);
        }
        // children.add(new Item(endTime, events.get(i + 1).time, events.get(i).name));
    }

    public void populateEventsFromChildren(int rec) {
        events.clear();
        if (children.size() == 0)
            return;
        for (Item c : children) {
            events.add(new ItemEvent(c.name, c.startTime, c.paletteIndex));
            events.add(new ItemEvent(c.name, c.endTime, -1));
            if (rec > 0)
                c.populateEventsFromChildren(rec - 1);
        }
    }

    boolean hasChildren() {
        return children.size() > 0;
    }

    public Item add(Item e, boolean adjustTime) {
        children.add(e);
        if (adjustTime) {
            if (children.size() == 0) {
                startTime = e.startTime;
                endTime = e.endTime;
            } else {
                if (startTime > e.startTime)
                    startTime = e.startTime;
                if (endTime < e.endTime)
                    endTime = e.endTime;
            }
        }
        return this;
    }

    public Item add(ItemEvent e) {
        events.add(e);
        return this;
    }

    public Item add(String name, double time) {
        events.add(new ItemEvent(name, time));
        return this;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public int firstEventAfter(double selectedTime) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getTime() >= selectedTime)
                return i;
        }
        return -1;
    }
}
