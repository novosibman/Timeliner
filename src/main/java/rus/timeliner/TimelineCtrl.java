/*****************************************************************************
 * 
 * Copyright (c) 2007,2019 Ruslan Scherbakov (novosibman@gmail.com)
 * 
 *****************************************************************************/

package rus.timeliner;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.JScrollBar;
import javax.swing.JToolTip;

public class TimelineCtrl extends BaseCtrl implements MouseMotionListener, MouseListener, MouseWheelListener {

    private static final long serialVersionUID = 1L;

    private final double zoomCoeff = 1.5;
    
    enum Drag {
        None,
        NameWidth,
        Time,
        TimeRange,
    }

    private ITimeDataProvider _timeProvider;
    private boolean _isInFocus = false;
    private boolean _isDragCursor3 = false;
    private boolean _mouseHover = false;
    private boolean adjustingScrolls;
    private boolean shiftPressed;
    private int _topItem0 = 0;
    private int _topItem = 0;
    private int _itemHeight = 18;
    private Drag _dragState = Drag.None;
    private int _hitIdx = 0;
    private int _dragX0 = 0;
    private int _dragX = 0;
    private int _dragY0 = 0;
    private int _dragY = 0;
    private int _idealNameWidth = 0;
    private double _timeStep = 0.001;
    private double _time0bak;
    private double _time1bak;
    private double _time0sel;
    private double _time1sel;
    private double hitTime;
    private Items _data = new Items();
    private final Rectangle _rect0 = new Rectangle(0, 0, 0, 0);
    private final Rectangle _rect1 = new Rectangle(0, 0, 0, 0);
    private final Rectangle _rect2 = new Rectangle(0, 0, 0, 0);
    private Cursor _dragCursor3;
    protected JScrollBar horzScrollBar;
    protected JScrollBar vertScrollBar;

    JToolTip tip;

    public TimelineCtrl() {
        super();
        _dragCursor3 = new Cursor(Cursor.W_RESIZE_CURSOR);
        tip = new JToolTip();
        tip.setTipText("TEST");
        addListeners();
    }

    protected void addListeners() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {
            }
            @Override public void keyPressed(KeyEvent e) {
                if (processKey(e.getKeyCode(), true))
                    e.consume();
            }
            @Override public void keyReleased(KeyEvent e) {
                if (processKey(e.getKeyCode(), false))
                    e.consume();
            }
        });
    }

    public void setDataProvider(ITimeDataProvider timeProvider) {
        _timeProvider = timeProvider;
        adjustScrolls();
        repaint();
    }

    public void fireSelectionChanged() {
        // if (null != _selectionListeners) {
        // Iterator it = _selectionListeners.iterator();
        // while (it.hasNext()) {
        // SelectionListener listener = (SelectionListener) it.next();
        // listener.widgetSelected(null);
        // }
        // }
    }

    public void fireDefaultSelection() {
        // if (null != _selectionListeners) {
        // Iterator it = _selectionListeners.iterator();
        // while (it.hasNext()) {
        // SelectionListener listener = (SelectionListener) it.next();
        // listener.widgetDefaultSelected(null);
        // }
        // }
    }

    public void setData(Items threads) {
        _data = threads;
        adjustScrolls();
        repaint();
    }

    public void adjustScrolls() {
        if (null == _timeProvider || horzScrollBar == null || vertScrollBar == null) {
            return;
        }
        adjustingScrolls = true;
        int pageCount = countPerPage();
        if (_topItem + pageCount > _data.allItems.length)
            _topItem = _data.allItems.length - pageCount;
        if (_topItem < 0)
            _topItem = 0;
        if (_data.allItems.length - 1 - pageCount >= 0) {
            vertScrollBar.setValues(_topItem, pageCount, 0, _data.allItems.length);
            vertScrollBar.setBlockIncrement(pageCount);
            vertScrollBar.setEnabled(true);
        } else {
            vertScrollBar.setEnabled(false);
        }
        double time0 = _timeProvider.getTime0();
        double time1 = _timeProvider.getTime1();
        double timeMin = _timeProvider.getMinTime();
        double timeMax = _timeProvider.getMaxTime();
        int timePage = (int) ((time1 - time0) / _timeStep);
        int timePos = (int) (time0 / _timeStep);
        horzScrollBar.setValues(timePos, timePage, (int) (timeMin / _timeStep), (int) (timeMax / _timeStep));
        horzScrollBar.setBlockIncrement(timePage);
        adjustingScrolls = false;
    }

    boolean ensureVisibleItem(int idx, boolean redraw) {
        boolean changed = false;
        if (idx < 0) {
            for (idx = 0; idx < _data.allItems.length; idx++) {
                if (_data.allItems[idx].selected)
                    break;
            }
        }
        if (idx >= _data.allItems.length)
            return changed;
        if (idx < _topItem) {
            _topItem = idx;
            vertScrollBar.setValue(_topItem);
            if (redraw)
                repaint();
            changed = true;
        } else {
            int page = countPerPage();
            if (idx >= _topItem + page) {
                _topItem = idx - page + 1;
                vertScrollBar.setValue(_topItem);
                if (redraw)
                    repaint();
                changed = true;
            }
        }
        return changed;
    }

    boolean fixTop() {
        boolean changed = false;
        if (_topItem < 0) {
            _topItem = 0;
            changed = true;
        } else {
            int page = countPerPage();
            if (_topItem + page >= _data.allItems.length) {
                _topItem = _data.allItems.length - page + 1;
                changed = true;
            }
        }
        return changed;
    }

    public void selectThread(int n) {
        if (n != 1 && n != -1)
            return;
        boolean changed = false;
        int lastSelection = -1;
        for (int i = 0; i < _data.allItems.length; i++) {
            Item item = _data.allItems[i];
            if (item.selected) {
                lastSelection = i;
                if (1 == n && i < _data.allItems.length - 1) {
                    item.selected = false;
                    if (item.hasChildren())
                        _data.expandItem(i, true);
                    item = _data.allItems[i + 1];
                    if (item.hasChildren()) {
                        _data.expandItem(i + 1, true);
                        item = _data.allItems[i + 2];
                    }
                    item.selected = true;
                    changed = true;
                } else if (-1 == n && i > 0) {
                    i--;
                    Item prevItem = _data.allItems[i];
                    if (prevItem.hasChildren()) {
                        if (prevItem.expanded) {
                            if (i > 0) {
                                i--;
                                prevItem = _data.allItems[i];
                            }
                        }
                        if (!prevItem.expanded) {
                            int added = _data.expandItem(i, true);
                            prevItem = _data.allItems[i + added];
                            item.selected = false;
                            prevItem.selected = true;
                            changed = true;
                        }
                    } else {
                        item.selected = false;
                        prevItem.selected = true;
                        changed = true;
                    }
                }
                break;
            }
        }
        if (lastSelection < 0 && _data.allItems.length > 0) {
            Item item = _data.allItems[0];
            if (item.hasChildren()) {
                _data.expandItem(0, true);
                item = _data.allItems[1];
                item.selected = true;
                changed = true;
            } else {
                item.selected = true;
                changed = true;
            }
        }
        if (changed) {
            ensureVisibleItem(-1, false);
            repaint();
            fireSelectionChanged();
        }
    }

    public void selectEvent(int n) {
        if (null == _timeProvider)
            return;
        Item item = getSelectedItem();
        if (item == null)
            return;
        double selectedTime = _timeProvider.getSelectedTime();
        int idx = item.firstEventAfter(selectedTime);
        if (-1 == n) {
            if (idx < 0)
                idx = item.events.size() - 1;
            else
                idx--;
            if (selectedTime > item.getEndTime())
                selectedTime = item.getEndTime();
            else if (idx >= 0 && idx < item.events.size())
                selectedTime = item.events.get(idx).getTime();
            else
                selectedTime = item.getStartTime();
        } else {
            if (selectedTime < item.getStartTime())
                selectedTime = item.getStartTime();
            else if (item.events.size() == 0) {
                if (selectedTime < item.getEndTime())
                    selectedTime = item.getEndTime();
            } else if (selectedTime < item.events.get(0).getTime()) {
                selectedTime = item.events.get(0).getTime();
            } else if (idx >= 0) {
                idx++;
                if (idx >= 0 && idx < item.events.size())
                    selectedTime = item.events.get(idx).getTime();
                else
                    selectedTime = item.getEndTime();
            }
        }
        _timeProvider.setSelectedTime(selectedTime, true);
        fireSelectionChanged();
    }

    public void selectNextEvent() {
        selectEvent(1);
    }

    public void selectPrevEvent() {
        selectEvent(-1);
    }

    public void zoomIn() {
        double _time0 = _timeProvider.getTime0();
        double _time1 = _timeProvider.getTime1();
        double _range = _time1 - _time0;
        double selTime = _timeProvider.getSelectedTime();
        if (selTime <= _time0 || selTime >= _time1) {
            selTime = (_time0 + _time1) / 2;
        }
        double time0 = selTime - (selTime - _time0) / zoomCoeff;
        double time1 = selTime + (_time1 - selTime) / zoomCoeff;

        double inaccuracy = (_timeProvider.getMaxTime() - _timeProvider.getMinTime()) - (time1 - time0);
        if (inaccuracy > 0 && inaccuracy < 0.3) {
            _timeProvider.setStartFinishTime(_timeProvider.getMinTime(), _timeProvider.getMaxTime());
            return;
        }

        double m = _timeProvider.getMinTimeInterval();
        if ((time1 - time0) < m) {
            time0 = selTime - (selTime - _time0) * m / _range;
            time1 = time0 + m;
        }

        _timeProvider.setStartFinishTime(time0, time1);
    }

    public void zoomOut() {
        double _time0 = _timeProvider.getTime0();
        double _time1 = _timeProvider.getTime1();
        double selTime = _timeProvider.getSelectedTime();
        if (selTime <= _time0 || selTime >= _time1) {
            selTime = (_time0 + _time1) / 2;
        }
        double time0 = selTime - (selTime - _time0) * zoomCoeff;
        double time1 = selTime + (_time1 - selTime) * zoomCoeff;

        double inaccuracy = (_timeProvider.getMaxTime() - _timeProvider.getMinTime()) - (time1 - time0);
        if (inaccuracy > 0 && inaccuracy < 0.3) {
            _timeProvider.setStartFinishTime(_timeProvider.getMinTime(), _timeProvider.getMaxTime());
            return;
        }

        _timeProvider.setStartFinishTime(time0, time1);
    }

    public Item getSelectedItem() {
        Item item = null;
        int idx = getSelectedIndex();
        if (idx >= 0)
            item = _data.allItems[idx];
        return item;
    }

    public int getSelectedIndex() {
        int idx = -1;
        for (int i = 0; i < _data.allItems.length; i++) {
            Item item = _data.allItems[i];
            if (item.selected) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    boolean toggle(int idx) {
        boolean toggled = false;
        if (idx >= 0 && idx < _data.allItems.length) {
            Item item = _data.allItems[idx];
            if (item.hasChildren()) {
                item.expanded = !item.expanded;
                _data.updateItems();
                adjustScrolls();
                repaint();
                toggled = true;
            }
        }
        return toggled;
    }

    int hitItemTest(int x, int y) {
        if (x < 0 || y < 0)
            return -1;
        int hit = -1;
        int idx = y / _itemHeight;
        idx += _topItem;
        if (idx < _data.allItems.length)
            hit = idx;
        return hit;
    }

    int hitExpanderTest(int x, int y) {
        if (x < 0 || y < 0)
            return -1;
        int hit = -1;
        int idx = y / _itemHeight;
        idx += _topItem;
        if (idx < _data.allItems.length)
            hit = idx;
        if (hit >= 0) {
            Rectangle bound = getBounds();
            bound.setLocation(0, 0);
            getExpanderRect(_rect0, bound, idx);
            if (!_rect0.contains(x, y))
                hit = -1;
        }
        return hit;
    }

    int hitSplitTest(int x, int y) {
        if (x < 0 || y < 0 || null == _timeProvider)
            return -1;
        int w = 4;
        int hit = -1;
        int nameWidth = _timeProvider.getNameWidth();
        if (x > nameWidth - w && x < nameWidth + w)
            hit = 1;
        return hit;
    }

    public Item getItem(Point pt) {
        int idx = hitItemTest(pt.x, pt.y);
        return idx >= 0 ? _data.allItems[idx] : null;
    }

    double hitTimeTest(int x) {
        if (null == _timeProvider)
            return 0;
        double time = -1;
        double time0 = _timeProvider.getTime0();
        double time1 = _timeProvider.getTime1();
        int nameWidth = _timeProvider.getNameWidth();
        x -= nameWidth;
        if (x >= 0 && getWidth() > nameWidth) {
            time = time0 + (time1 - time0) * x / (getWidth() - nameWidth);
        }
        return time;
    }

    protected void selectItem(int idx, boolean addSelection) {
        if (addSelection) {
            if (idx >= 0 && idx < _data.allItems.length) {
                Item item = _data.allItems[idx];
                item.selected = true;
            }
        } else {
            for (int i = 0; i < _data.allItems.length; i++) {
                Item item = _data.allItems[i];
                item.selected = i == idx;
            }
        }
        boolean changed = ensureVisibleItem(idx, true);
        if (!changed)
            repaint();
    }

    public int countPerPage() {
        int height = getHeight();
        int count = 0;
        if (height > 0)
            count = height / _itemHeight;
        return count;
    }

    public int getTopIndex() {
        int idx = -1;
        if (_data.allItems.length > 0)
            idx = 0;
        return idx;
    }

    public int getBottomIndex() {
        int idx = _data.allItems.length - 1;
        return idx;
    }

    private void getItemRect(Rectangle rect, Rectangle bound, int idx) {
        idx -= _topItem;
        rect.x = bound.x;
        rect.y = bound.y + idx * _itemHeight;
        rect.width = bound.width - bound.x;
        rect.height = _itemHeight;
    }

    private void getExpanderRect(Rectangle rect, Rectangle bound, int idx) {
        Item item = _data.allItems[idx];
        int s = _itemHeight >> 1;
        idx -= _topItem;
        rect.x = bound.x + MARGIN + item.nesting * s;
        rect.y = bound.y + idx * _itemHeight + _itemHeight / 4;
        s &= ~1;
        rect.width = rect.height = s;
    }

    private void getItemNameRect(Rectangle rect, Rectangle bound, int idx, int nameWidth) {
        idx -= _topItem;
        rect.x = bound.x;
        rect.y = bound.y + idx * _itemHeight;
        rect.width = nameWidth;
        rect.height = _itemHeight;
    }

    private void getItemLabelRect(Rectangle rect, Rectangle bound, int idx, int nameWidth) {
        getItemNameRect(rect, bound, idx, nameWidth);
        Item item = _data.allItems[idx];
        int s = _itemHeight >> 1;
        int xoffset = MARGIN + s + item.nesting * s + MARGIN;
        rect.x += xoffset;
        rect.width -= xoffset;
    }

    private void getItemTimelineRect(Rectangle rect, Rectangle bound, int idx, int nameWidth) {
        idx -= _topItem;
        rect.x = bound.x + nameWidth;
        rect.y = bound.y + idx * _itemHeight;
        rect.width = bound.width - rect.x;
        rect.height = _itemHeight;
    }

    @Override
    public void paint(Rectangle bound, Graphics2D g) {
        drawContent(bound, g);
    }

    private void drawContent(Rectangle bound, Graphics2D g) {
        _itemHeight = g.getFontMetrics().getHeight() + 6;
        if (bound.width < 2 || bound.height < 2 || null == _timeProvider) {
            g.setColor(Color.RED);
            g.fill(bound);
            return;
        }

        g.setColor(Defs.background);
        g.fill(bound);

        _idealNameWidth = 0;
        int nameWidth = _timeProvider.getNameWidth();
        double time0 = _timeProvider.getTime0();
        double time1 = _timeProvider.getTime1();
        double endTime = _timeProvider.getEndTime();
        double selectedTime = _timeProvider.getSelectedTime();

        for (int i = _topItem; i < _data.allItems.length; i++) {
            Item item = _data.allItems[i];

            getItemRect(_rect0, bound, i);
            if (_rect0.y >= bound.y + bound.height)
                break;

            getItemNameRect(_rect0, bound, i, nameWidth);
            g.setColor(Defs.background2);
            g.fill(_rect0);
            // drawItemName(item, _rect0, g);

            if (item.hasChildren()) {
                getExpanderRect(_rect0, bound, i);
                g.setColor(Defs.expander);
                drawExpander(item, _rect0, g);
            }

            int textWidth = g.getFontMetrics().stringWidth(item.name);
            if (_idealNameWidth < textWidth)
                _idealNameWidth = textWidth;

            getItemLabelRect(_rect0, bound, i, nameWidth);
            g.setColor(Defs.foreground);
            String name = Utils.adjustString(item.name, g, _rect0.width);
            int textX = Utils.drawText(g, name, _rect0, true) + 8;
            textX += _rect0.x;

            getItemTimelineRect(_rect0, bound, i, nameWidth);
            g.setColor(Defs.background);
            g.fill(_rect0);

            if (textX > _rect0.x)
                textX = _rect0.x;

            int midy = _rect0.y + _rect0.height / 2;
            g.setColor(Defs.midline);
            g.drawLine(textX, midy, _rect0.x + _rect0.width, midy);

            drawItemTimeline(item, _rect0, time0, time1, endTime, selectedTime, g);

            // shade selected item
            if (item.selected) {
                getItemRect(_rect0, bound, i);
                g.setColor(Defs.shadow);
                g.fill(_rect0);
            }
        }

        // draw drag line
        Color dragColor = Color.LIGHT_GRAY;
        if (_dragState == Drag.NameWidth) {
            dragColor = Color.BLACK;
        } else if (_dragState == Drag.None && _mouseHover) {
            dragColor = Color.RED;
        }
        g.setColor(dragColor);
        g.drawLine(bound.x + nameWidth - 1, bound.y, bound.x + nameWidth - 1, bound.y + bound.height - 1);

        double K = (bound.width - nameWidth) / (time1 - time0);

        // draw time range
        if (_dragState == Drag.TimeRange) {
            g.setColor(Defs.rangeSelection);
            int x0 = bound.x + nameWidth + (int) ((_time0sel - time0) * K);
            int x1 = bound.x + nameWidth + (int) ((_time1sel - time0) * K);
            if (x1 > x0)
                g.fillRect(x0, bound.y, x1 - x0, bound.height);
            else
                g.fillRect(x1, bound.y, x0 - x1, bound.height);
        }

        // draw time mark
        int x = bound.x + nameWidth + (int) ((selectedTime - time0) * K);
        if (x >= bound.x + nameWidth && x < bound.x + bound.width) {
            g.setColor(Defs.selectedTime);
            g.drawLine(x, bound.y, x, bound.y + bound.height);
        }
    }

    private void drawExpander(Item item, Rectangle rect, Graphics2D g) {
        g.draw(rect);
        int p = rect.y + rect.height / 2;
        g.drawLine(rect.x + 2, p, rect.x + rect.width - 2, p);
        if (!item.expanded) {
            p = rect.x + rect.width / 2;
            g.drawLine(p, rect.y + 2, p, rect.y + rect.height - 2);
        }
    }

    static public boolean prepareRect(Rectangle toRect, Rectangle boundRect, double time0, double time1, double eventTime0, double eventTime1) {
        if (eventTime0 > time1 || eventTime1 < time0 || eventTime0 >= eventTime1) {
            return false;
        }
        double timeDelta = time1 - time0;
        //double K = boundRect.width / dt;
        int xEnd = boundRect.x + boundRect.width;
        if (eventTime0 < time0)
            eventTime0 = time0;
        if (eventTime1 > time1)
            eventTime1 = time1;
        int startX = boundRect.x + (int) ((eventTime0 - time0) * boundRect.width / timeDelta);
        int endX = boundRect.x + (int) ((eventTime1 - time0) * boundRect.width / timeDelta);
        toRect.x = startX >= boundRect.x ? startX : boundRect.x;
        toRect.width = (endX <= xEnd ? endX : xEnd) - toRect.x;
        if (toRect.x + toRect.width > boundRect.x + boundRect.width)
            toRect.width = boundRect.width - toRect.x;
        toRect.y = boundRect.y;
        toRect.height = boundRect.height;
        return true;
    }

    private void drawItemTimeline(Item item, Rectangle rect, double time0, double time1, double endTime, double selectedTime, Graphics2D g) {
        double dt = time1 - time0;
        if (rect.width <= 0 || rect.height <= 0 || dt <= 0) {
            return;
        }
        _rect1.setBounds(rect);
        _rect1.y += 2;
        _rect1.height -= 4;
        boolean selected = item.selected;
        Color colSel = _isInFocus ? Defs.selectedTimeInFocus : Defs.selectedTime;
        if (prepareRect(_rect2, _rect1, time0, time1, item.getStartTime(), item.getEndTime())) {
            Utils.fillStateRect(g, _rect2, item.paletteIndex, true);
        }
        if (item.expanded && item.hasChildren()) {
            if (selected) {
                Utils.outlineStateRect(g, _rect1, colSel, true);
            }
            return;
        }
        _rect1.setBounds(rect);
        _rect1.y += 4;
        _rect1.height -= 8;
        int count = item.events.size();
        ArrayList<ItemEvent> list = item.events;
        if (count > 0) {
            int paletteIndex = list.get(0).paletteIndex;
            double eventStartTime = list.get(0).getTime();
            int idx = 1;
            while (eventStartTime <= time1) {
                if (idx == count)
                    break;
                ItemEvent event = list.get(idx);
                double eventEndTime = event.getTime();
                idx++;
                if (paletteIndex >= 0 && prepareRect(_rect2, _rect1, time0, time1, eventStartTime, eventEndTime)) {
                    Utils.fillStateRect(g, _rect2, paletteIndex, true);
                    boolean timeSelected = eventStartTime <= selectedTime && selectedTime < eventEndTime;
                    if (selected && timeSelected) {
//                        _rect2.y -= 1;
//                        _rect2.height += 2;
                        Utils.outlineStateRect(g, _rect2, colSel, true);
                    }
                }
                eventStartTime = eventEndTime;
                paletteIndex = event.paletteIndex;
            }
        }
        if (selected) {
            _rect1.setBounds(rect);
            _rect1.y += 2;
            _rect1.height -= 4;
            Utils.outlineStateRect(g, _rect1, colSel, true);
        }
    }

    public boolean processKey(int keyCode, boolean pressed) {
        if (KeyEvent.VK_SHIFT == keyCode) {
            shiftPressed = pressed;
            //System.out.println("KeyEvent.VK_SHIFT: " + pressed + " - _dragState: " + _dragState);
            if (!shiftPressed && _dragState == Drag.TimeRange) {
                _dragState = Drag.None;
                repaint();
            }
            return true;
        }
        if (!pressed) {
            return false;
        }
        int idx = -1;
        if (KeyEvent.VK_HOME == keyCode) {
            idx = getTopIndex();
        } else if (KeyEvent.VK_END == keyCode) {
            idx = getBottomIndex();
        } else if (KeyEvent.VK_DOWN == keyCode) {
            idx = getSelectedIndex();
            if (idx < 0)
                idx = 0;
            else if (idx < _data.allItems.length - 1)
                idx++;
        } else if (KeyEvent.VK_UP == keyCode) {
            idx = getSelectedIndex();
            if (idx < 0)
                idx = 0;
            else if (idx > 0)
                idx--;
        } else if (KeyEvent.VK_LEFT == keyCode) {
            selectPrevEvent();
        } else if (KeyEvent.VK_RIGHT == keyCode) {
            selectNextEvent();
        } else if (KeyEvent.VK_PAGE_DOWN == keyCode) {
            int page = countPerPage();
            idx = getSelectedIndex();
            if (idx < 0)
                idx = 0;
            idx += page;
            if (idx >= _data.allItems.length)
                idx = _data.allItems.length - 1;
        } else if (KeyEvent.VK_PAGE_UP == keyCode) {
            int page = countPerPage();
            idx = getSelectedIndex();
            if (idx < 0)
                idx = 0;
            idx -= page;
            if (idx < 0)
                idx = 0;
        } else if (KeyEvent.VK_ENTER == keyCode) {
            idx = getSelectedIndex();
            if (idx >= 0) {
                toggle(idx);
            }
            idx = -1;
        } else {
            return false;
        }
        if (idx >= 0) {
            selectItem(idx, false);
            fireSelectionChanged();
        }
        return true;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (_dragState == Drag.None) {
            boolean mouseHover = hitSplitTest(e.getX(), e.getY()) > 0;
            if (_mouseHover != mouseHover)
                repaint();
            _mouseHover = mouseHover;
        }
        updateCursor(e.getX(), e.getY());
        updateToolTipText(e.getX(), e.getY());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        drag(e.getX(), e.getY());
        updateCursor(e.getX(), e.getY());
        updateToolTipText(e.getX(), e.getY());
    }

    private void drag(int x, int y) {
        if (null == _timeProvider)
            return;
        int size = getWidth() - _timeProvider.getNameWidth();
        if (_dragState == Drag.Time) {
            //if (x > 0 && size > 0) {
            if (size > 0) {
                _dragX = x;
                _dragY = y;
                double K = size / (_time1bak - _time0bak);
                double timeDelta = (_dragX - _dragX0) / K;
                double time1 = _time1bak - timeDelta;
                double maxTime = _timeProvider.getMaxTime();
                if (time1 > maxTime)
                    time1 = maxTime;
                double time0 = time1 - (_time1bak - _time0bak);
                if (time0 < _timeProvider.getMinTime()) {
                    time0 = _timeProvider.getMinTime();
                    time1 = time0 + (_time1bak - _time0bak);
                }
                int idxDelta = (_dragY - _dragY0) / _itemHeight;
                //System.out.println("idxDelta: " + idxDelta);
                _topItem = _topItem0 - idxDelta;
                fixTop();
                _timeProvider.setStartFinishTime(time0, time1);
            }
        } else if (_dragState == Drag.NameWidth) {
            _dragX = x;
            _timeProvider.setNameWidth(_hitIdx + _dragX - _dragX0);
        } else if (_dragState == Drag.TimeRange) {
             _time1sel = hitTimeTest(x);
             repaint();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (null == _timeProvider)
            return;
        if (1 == e.getButton()) {
            _topItem0 = _topItem;
            _dragX = _dragX0 = e.getX();
            _dragY = _dragY0 = e.getY();
            int idx = hitSplitTest(e.getX(), e.getY());
            if (idx > 0) {
                if (e.getClickCount() > 1) {
                    _timeProvider.setNameWidth(_idealNameWidth + 3 * MARGIN + _itemHeight / 2);
                    return;
                }
                _dragState = Drag.NameWidth;
                _hitIdx = _timeProvider.getNameWidth();
                _time0bak = _timeProvider.getTime0();
                _time1bak = _timeProvider.getTime1();
                repaint();
                return;
            }
            idx = hitItemTest(e.getX(), e.getY());
            if (idx >= 0 && hitExpanderTest(e.getX(), e.getY()) == idx) {
                toggle(idx);
                return;
            }
            if (e.getClickCount() > 1) {
                if (idx >= 0) {
                    selectItem(idx, false);
                    toggle(idx);
                    fireDefaultSelection();
                }
                return;
            }
            if (shiftPressed) {
                _dragState = Drag.TimeRange;
                _time0sel = _time1sel = hitTimeTest(e.getX());
                repaint();
                return;
            }
            _hitIdx = idx;
            hitTime = hitTimeTest(e.getX());
            _dragState = Drag.Time;
            _time0bak = _timeProvider.getTime0();
            _time1bak = _timeProvider.getTime1();
            return;
        }
    }

    public void updateCursor(int x, int y) {
        int idx = hitSplitTest(x, y);
        if (_dragState == Drag.None && idx >= 0 || _dragState == Drag.NameWidth) {
            if (!_isDragCursor3)
                setCursor(_dragCursor3);
            _isDragCursor3 = true;
        } else {
            if (_isDragCursor3)
                setCursor(null);
            _isDragCursor3 = false;
        }
    }

    @Override
    public Point getToolTipLocation(MouseEvent e) {
        System.out.println("getToolTipLocation..." + e);
        return new Point(e.getX(), e.getY());//super.getToolTipLocation(e);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        System.out.println("getToolTipText..." + e);
        int idx = hitItemTest(e.getX(), e.getY());
        return idx >= 0 ? _data.allItems[idx].getName() : null;
    }

    public void updateToolTipText(int x, int y) {
//        int idx = hitItemTest(x, y);
//        setToolTipText(idx >= 0 ? _data.allItems[idx].getName() : null);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (_dragState == Drag.Time) {
            _dragState = Drag.None;
            if (Math.abs(e.getX() - _dragX0) < 5 && Math.abs(e.getY() - _dragY0) < 5) {
                _timeProvider.setSelectedTime(hitTime, false);
                selectItem(_hitIdx, false);
            }
        } else if (_dragState == Drag.TimeRange) {
            _dragState = Drag.None;
            if (_time0sel < _time1sel)
                _timeProvider.setStartFinishTime(_time0sel, _time1sel);
            else if (_time0sel > _time1sel)
                _timeProvider.setStartFinishTime(_time1sel, _time0sel);
        } else {
            _dragState = Drag.None;
            repaint();
        }
        updateCursor(e.getX(), e.getY());
        //updateToolTipText(e.getX(), e.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() > 0) {
            zoomIn();
        } else if (e.getWheelRotation() < 0) {
            zoomOut();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (_mouseHover) {
            _mouseHover = false;
            repaint();
        }
        // _dragState = 0;
        // updateCursor(e.getX(), e.getY());
    }

    public void scrollChanged(JScrollBar sb) {
        if (adjustingScrolls)
            return;
        if (sb == vertScrollBar) {
            _topItem = vertScrollBar.getValue();
            if (_topItem < 0)
                _topItem = 0;
            repaint();
        } else if (sb == horzScrollBar && null != _timeProvider) {
            int startTime = horzScrollBar.getValue();
            double time0 = _timeProvider.getTime0();
            double time1 = _timeProvider.getTime1();
            double range = time1 - time0;
            // _timeRangeFixed = true;
            time0 = _timeStep * startTime;
            time1 = time0 + range;
            //System.out.println("scrollChanged...");
            _timeProvider.setStartFinishTime(time0, time1);
        }
    }
}
