/*****************************************************************************
 * 
 * Copyright (c) 2007,2019 Ruslan Scherbakov (novosibman@gmail.com)
 * 
 *****************************************************************************/

package rus.timeliner.demo;

import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import rus.timeliner.Item;
import rus.timeliner.ItemEvent;
import rus.timeliner.Items;
import rus.timeliner.TimelineView;

public class Demo {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Test");
                TimelineView timeline = new TimelineView();
                frame.setContentPane(timeline);
                //frame.getContentPane().add(timeline);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1200, 800);
                frame.setVisible(true);
                timeline.setData(TestThread.makeItems(getThreads2(), false));
                timeline.requestFocusInWindow();
            }
        });
    }
    
    static TestThread[] getThreads1() {
        return new TestThread[] {
            new TestThread(10L, 40L, "a 10_40: 20-30", "AAA").add(20).add(30),
            //new TestThread(20L, 50L, "a 20_50: 25-35-40-45", "AAA").add(25).add(35).add(40).add(45),
        };
    }
    static TestThread[] getThreads2() {
        return new TestThread[] {
            new TestThread(10L, 40L, "a 10_40: 20-30", "AAA").add(20).add(30),
            new TestThread(20L, 50L, "a 20_50: 25-35-40-45", "AAA").add(25).add(35).add(40).add(45),
            new TestThread(30L, 50L, "aaaaaaaa3", "AAA"),
            new TestThread(30L, 50L, "aaaaaaaa4", "AAA"),
            new TestThread(30L, 50L, "aaaaaaaa5", "AAA"),
            new TestThread(30L, 500L, "bg1000-  13", "BB1"),
            new TestThread(30L, 500L, "bg1000-  13", "BB2"),
            new TestThread(30L, 500L, "bg1000-  13", "BB3"),
            new TestThread(30L, 500L, "bg1000-  13", "BB4"),
            new TestThread(30L, 500L, "bg1000-  13", "BB5"),
            new TestThread(30L, 500L, "bg1000-  13", "BB6"),
            new TestThread(30L, 500L, "bg1000-  13", "BB7"),
            new TestThread(30L, 500L, "bg1000-  13", "BB8"),
            new TestThread(30L, 500L, "bg1000-  13", "BB9"),
            new TestThread(10L, 500L, "bg1000-  13", "BB0"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 300L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(80L, 500L, "bg1000-  13", "BB"),
            new TestThread(90L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 200L, "bg1000-  13", "BB"),
            new TestThread(90L, 100L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  12", "BB"),
            new TestThread(30L, 500L, "bg1000-  11", "BB"),
            new TestThread(30L, 500L, "bg1000-  10", "BB"),
            new TestThread(30L, 100L, "bg1000-  09", "BB"),
            new TestThread(30L, 200L, "bg1000-  08", "BB"),
            new TestThread(30L, 300L, "bg1000-  07", "BB"),
            new TestThread(30L, 400L, "bg1000-  06", "BB"),
            new TestThread(30L, 500L, "bg1000-  05", "BB"),
            new TestThread(30L, 400L, "bg1000-  04", "BB"),
            new TestThread(30L, 300L, "bg1000-  03", "BB"),
            new TestThread(30L, 200L, "bg1000-  02", "BB"),
            new TestThread(30L, 100L, "bg1000-  01", "BB"),
            new TestThread(10L, 40L, "a 10_40: 20-30", "AAA").add(20).add(30),
            new TestThread(20L, 50L, "a 20_50: 25-35-40-45", "AAA").add(25).add(35).add(40).add(45),
            new TestThread(30L, 50L, "aaaaaaaa3", "AAA"),
            new TestThread(30L, 50L, "aaaaaaaa4", "AAA"),
            new TestThread(30L, 50L, "aaaaaaaa5", "AAA"),
            new TestThread(30L, 500L, "bg1000-  13", "BB1"),
            new TestThread(30L, 500L, "bg1000-  13", "BB2"),
            new TestThread(30L, 500L, "bg1000-  13", "BB3"),
            new TestThread(30L, 500L, "bg1000-  13", "BB4"),
            new TestThread(30L, 500L, "bg1000-  13", "BB5"),
            new TestThread(30L, 500L, "bg1000-  13", "BB6"),
            new TestThread(30L, 500L, "bg1000-  13", "BB7"),
            new TestThread(30L, 500L, "bg1000-  13", "BB8"),
            new TestThread(30L, 500L, "bg1000-  13", "BB9"),
            new TestThread(10L, 500L, "bg1000-  13", "BB0"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 300L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(80L, 500L, "bg1000-  13", "BB"),
            new TestThread(90L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 200L, "bg1000-  13", "BB"),
            new TestThread(90L, 100L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  12", "BB"),
            new TestThread(30L, 500L, "bg1000-  11", "BB"),
            new TestThread(30L, 500L, "bg1000-  10", "BB"),
            new TestThread(30L, 100L, "bg1000-  09", "BB"),
            new TestThread(30L, 200L, "bg1000-  08", "BB"),
            new TestThread(30L, 300L, "bg1000-  07", "BB"),
            new TestThread(30L, 400L, "bg1000-  06", "BB"),
            new TestThread(30L, 500L, "bg1000-  05", "BB"),
            new TestThread(30L, 400L, "bg1000-  04", "BB"),
            new TestThread(30L, 300L, "bg1000-  03", "BB"),
            new TestThread(30L, 200L, "bg1000-  02", "BB"),
            new TestThread(30L, 100L, "bg1000-  01", "BB"),
            new TestThread(10L, 40L, "a 10_40: 20-30", "AAA").add(20).add(30),
            new TestThread(20L, 50L, "a 20_50: 25-35-40-45", "AAA").add(25).add(35).add(40).add(45),
            new TestThread(30L, 50L, "aaaaaaaa3", "AAA"),
            new TestThread(30L, 50L, "aaaaaaaa4", "AAA"),
            new TestThread(30L, 50L, "aaaaaaaa5", "AAA"),
            new TestThread(30L, 500L, "bg1000-  13", "BB1"),
            new TestThread(30L, 500L, "bg1000-  13", "BB2"),
            new TestThread(30L, 500L, "bg1000-  13", "BB3"),
            new TestThread(30L, 500L, "bg1000-  13", "BB4"),
            new TestThread(30L, 500L, "bg1000-  13", "BB5"),
            new TestThread(30L, 500L, "bg1000-  13", "BB6"),
            new TestThread(30L, 500L, "bg1000-  13", "BB7"),
            new TestThread(30L, 500L, "bg1000-  13", "BB8"),
            new TestThread(30L, 500L, "bg1000-  13", "BB9"),
            new TestThread(10L, 500L, "bg1000-  13", "BB0"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 300L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  13", "BB"),
            new TestThread(80L, 500L, "bg1000-  13", "BB"),
            new TestThread(90L, 500L, "bg1000-  13", "BB"),
            new TestThread(30L, 200L, "bg1000-  13", "BB"),
            new TestThread(90L, 100L, "bg1000-  13", "BB"),
            new TestThread(30L, 500L, "bg1000-  12", "BB"),
            new TestThread(30L, 500L, "bg1000-  11", "BB"),
            new TestThread(30L, 500L, "bg1000-  10", "BB"),
            new TestThread(30L, 100L, "bg1000-  09", "BB"),
            new TestThread(30L, 200L, "bg1000-  08", "BB"),
            new TestThread(30L, 300L, "bg1000-  07", "BB"),
            new TestThread(30L, 400L, "bg1000-  06", "BB"),
            new TestThread(30L, 500L, "bg1000-  05", "BB"),
            new TestThread(30L, 400L, "bg1000-  04", "BB"),
            new TestThread(30L, 300L, "bg1000-  03", "BB"),
            new TestThread(30L, 200L, "bg1000-  02", "BB"),
            new TestThread(30L, 100L, "bg1000-  01", "BB"),
            new TestThread(10L, 40L, "a 10_40: 20-30", "AAA").add(20).add(30),
            new TestThread(20L, 50L, "a 20_50: 25-35-40-45", "AAA").add(25).add(35).add(40).add(45),
            new TestThread(30L, 50L, "aaaaaaaa3", "AAA"),
            new TestThread(30L, 50L, "aaaaaaaa4", "AAA"),
            new TestThread(30L, 50L, "aaaaaaaa5", "AAA"),
            new TestThread(30L, 500L, "bg1000-  13", "BB1"),
            new TestThread(30L, 500L, "bg1000-  13", "BB2"),
            new TestThread(30L, 500L, "bg1000-  13", "BB3"),
            new TestThread(30L, 500L, "bg1000-  13", "BB4"),
            new TestThread(30L, 500L, "bg1000-  13", "BB5"),
            new TestThread(30L, 500L, "bg1000-  13", "BB6"),
            new TestThread(30L, 500L, "bg1000-  13", "BB7"),
        };
    }
}

class TestThread {
    ArrayList<ItemEvent> events = new ArrayList<ItemEvent>();

    static protected int nextId;
    protected int id;
    protected long startTime;
    protected long stopTime;
    protected String name;
    protected String group;

    public TestThread(long startTime, long stopTime, String name, String group) {
        this.id = nextId++;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.name = name;
        this.group = group;
    }

    public TestThread add(ItemEvent e) {
        events.add(e);
        return this;
    }

    public TestThread add(long time) {
        events.add(new ItemEvent("event_" + (events.size() + 1), time));
        return this;
    }

    public int getId() {
        return id;
    }

    public long getStartTime() {
        return startTime;
    }

    public double getStopTime() {
        return stopTime;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    static public Items makeItems(TestThread threads[], boolean flat) {
        Items items = new Items();
        if (flat) {
            for (TestThread thread : threads) {
                Item t = new Item(thread.startTime, thread.stopTime, thread.name, thread.events);
                t.expanded = true;
                t.paletteIndex = 1;
                t.populateChildrenFromEvents();
                items.rootItems.add(t);
            }
        } else {
            TreeMap<String, Item> _groups = new TreeMap<String, Item>();
            for (TestThread thread : threads) {
                String groupName = thread.getGroup();
                if (null == groupName)
                    groupName = "n/a";
                Item group = _groups.get(groupName);
                if (null == group) {
                    group = new Item(groupName);
                    group.expanded = true;
                    _groups.put(groupName, group);
                }
                Item t = new Item(thread.startTime, thread.stopTime, thread.name, thread.events);
                t.paletteIndex = 1;
                t.populateChildrenFromEvents();
                group.add(t, true);
            }
            for (Item group : _groups.values()) {
                items.rootItems.add(group);
            }
        }
        items.updateItems();
        return items;
    }
}
