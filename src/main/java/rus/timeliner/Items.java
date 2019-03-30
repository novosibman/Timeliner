/*****************************************************************************
 * 
 * Copyright (c) 2007,2019 Ruslan Scherbakov (novosibman@gmail.com)
 * 
 *****************************************************************************/

package rus.timeliner;

import java.util.ArrayList;

public class Items {
    public ArrayList<Item> rootItems = new ArrayList<Item>();
    public Item[] allItems = new Item[0];

    public Items() {
    }

    static public void addVisibleItems(Item parentItem, ArrayList<Item> itemList) {
        for (Item item : parentItem.children) {
            item.nesting = parentItem.nesting + 1;
            itemList.add(item);
            if (item.expanded) {
                addVisibleItems(item, itemList);
            }
        }
    }

    public void updateItems() {
        ArrayList<Item> itemList = new ArrayList<Item>();
        for (Item item : rootItems) {
            item.nesting = 0;
            itemList.add(item);
            if (item.expanded) {
                addVisibleItems(item, itemList);
            }
        }
        allItems = itemList.toArray(new Item[0]);
    }

    public int expandItem(int idx, boolean expand) {
        if (idx < 0 || idx >= allItems.length)
            return 0;
        int ret = 0;
        Item item = allItems[idx];
        if (item.hasChildren() && !item.expanded) {
            item.expanded = expand;
            ret = allItems.length;
            updateItems();
            ret = allItems.length - ret;
        }
        return ret;
    }
}
