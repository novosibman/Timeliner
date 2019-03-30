/*****************************************************************************
 * 
 * Copyright (c) 2007,2019 Ruslan Scherbakov (novosibman@gmail.com)
 * 
 *****************************************************************************/

package rus.timeliner;

import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

public class TimelineView extends JComponent implements ITimeDataProvider {

    private static final long serialVersionUID = 1L;

    private double _minTimeInterval = .000001;
    private double _selectedTime = -1;
    private double _startTime;
    private double _endTime;
    private double _time0;
    private double _time1;
    private double _timeMinTime;
    private double _timeMaxTime;
    private boolean _timeRangeFixed;
    private int _nameWidth = 120;
    private int _timeHeight = 24;
    private int _scrollWidth = 20;
    protected TimelineCtrl timeStates;
    protected TimescaleCtrl timeScale;
    protected JScrollBar horzScrollBar;
    protected JScrollBar vertScrollBar;

    public TimelineView() {
        setOpaque(true);
        setIgnoreRepaint(true);
        timeScale = new TimescaleCtrl();
        timeScale.setDataProvider(this);
        timeStates = new TimelineCtrl();
        timeStates.setDataProvider(this);
        horzScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        vertScrollBar = new JScrollBar(JScrollBar.VERTICAL);
        timeStates.horzScrollBar = horzScrollBar;
        timeStates.vertScrollBar = vertScrollBar;
        add(timeScale);
        add(timeStates);
        add(horzScrollBar);
        add(vertScrollBar);
        setPreferredSize(new Dimension(600, 300));
        addListeners();
    }

    public void dump(String tag) {
        System.out.println("timeline " + tag + "> " + _time0 + " " + _time1 + " " + _timeMinTime + " " + _timeMaxTime);
    }

    protected void addListeners() {
        addComponentListener(new ComponentListener() {
            @Override
            public void componentShown(ComponentEvent arg0) {
                System.out.println("componentShown: " + getSize() + " - " + getWidth() + "x" + getHeight());
            }

            @Override
            public void componentResized(ComponentEvent arg0) {
                // System.out.println("componentResized: " + getSize() + " - " + getWidth() + "x" + getHeight());
                updateSize();
            }

            @Override
            public void componentMoved(ComponentEvent arg0) {
                // System.out.println("componentMoved: " + getSize() + " - " + getWidth() + "x" + getHeight());
                updateSize();
            }

            @Override
            public void componentHidden(ComponentEvent arg0) {
                System.out.println("componentHidden: " + getSize() + " - " + getWidth() + "x" + getHeight());
            }
        });
        addContainerListener(new ContainerListener() {
            @Override
            public void componentRemoved(ContainerEvent arg0) {
                System.out.println("componentRemoved: " + getSize() + " - " + getWidth() + "x" + getHeight());
            }

            @Override
            public void componentAdded(ContainerEvent arg0) {
                System.out.println("componentAdded: " + getSize() + " - " + getWidth() + "x" + getHeight());
            }
        });
        horzScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                timeStates.scrollChanged(horzScrollBar);
            }
        });
        vertScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                timeStates.scrollChanged(vertScrollBar);
            }
        });
    }

    public void updateSize() {
        updateSize(getWidth(), getHeight());
    }

    public void updateSize(int width, int height) {
        int stateWidth = width - _scrollWidth;
        int stateHeight = height - _timeHeight - _scrollWidth;
        if (_nameWidth > stateWidth - 6)
            _nameWidth = stateWidth - 6;
        if (_nameWidth < 6)
            _nameWidth = 6;
        timeScale.setBounds(0, 0, width, _timeHeight);
        timeStates.setBounds(0, _timeHeight, stateWidth, stateHeight);
        updateScrollSize(width, height);
        redrawLater();
    }

    public void updateScrollSize() {
        updateScrollSize(getWidth(), getHeight());
    }

    public void updateScrollSize(int width, int height) {
        int stateWidth = width - _scrollWidth;
        int stateHeight = height - _timeHeight - _scrollWidth;
        horzScrollBar.setBounds(_nameWidth, _timeHeight + stateHeight, stateWidth - _nameWidth, _scrollWidth);
        vertScrollBar.setBounds(width - _scrollWidth, _timeHeight, _scrollWidth, stateHeight);
    }

    /**
     * Tries to set most convenient time range for display
     * 
     * @param items
     */
    public void setTimeRange(Items items) {
        _startTime = -1;
        _endTime = 0;
        for (int i = 0; i < items.rootItems.size(); i++) {
            Item item = items.rootItems.get(i);
            if (_startTime < 0 || _startTime > item.getStartTime())
                _startTime = item.getStartTime();
            if (item.getEndTime() > item.getStartTime() && item.getEndTime() > _endTime)
                _endTime = item.getEndTime();
            int len = item.events.size();
            if (len > 0) {
                ItemEvent lastEvent = item.events.get(item.events.size() - 1);
                if (lastEvent.getTime() > _endTime)
                    _endTime = lastEvent.getTime();
                lastEvent = item.events.get(0);
                if (_startTime < 0 || _startTime > lastEvent.getTime())
                    _startTime = lastEvent.getTime();
            }
        }
        // _startTime -= 0.000001;
        // _endTime += 0.000001;
        // if (_startTime < 0)
        // _startTime = 0;
        double dt = (_endTime - _startTime) * 0.01;
        _timeMinTime = _startTime - dt;
        // if (_timeMinTime < 0)
        // _timeMinTime = 0;
        // _time1_ = _time0_ + (_endTime - _time0_) * 1.05;
        _timeMaxTime = _endTime + dt;
        // _timeMinTime = Math.floor(_timeMinTime);
        // _timeMaxTime = Math.ceil(_timeMaxTime);
        if (!_timeRangeFixed) {
            _time0 = _timeMinTime;
            _time1 = _timeMaxTime;
        }
    }

    public void setData(Items items) {
        setTimeRange(items);
        if (_selectedTime < 0 || _selectedTime > _endTime)
            _selectedTime = (_startTime + _endTime) / 2;
        timeStates.setData(items);
        redrawLater();
    }

    private Runnable redrawer = new Runnable() {
        @Override
        public void run() {
            timeStates.adjustScrolls();
            timeStates.repaint();
            timeScale.repaint();
            horzScrollBar.repaint();
            vertScrollBar.repaint();
        }
    };
    
    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            int width = timeStates.getWidth();
            if (_nameWidth > width - 6)
                _nameWidth = width - 6;
            if (_nameWidth < 6)
                _nameWidth = 6;
            updateScrollSize();
            redrawLater();
        }
    };

    public void redrawLater() {
        SwingUtilities.invokeLater(redrawer);
    }

    public void setFocus() {
        if (null != timeStates)
            timeStates.requestFocus();
    }

    public double getTime0() {
        return _time0;
    }

    public double getTime1() {
        return _time1;
    }

    public double getMinTimeInterval() {
        return _minTimeInterval;
    }

    public int getNameWidth() {
        return _nameWidth;
    }

    @Override
    public boolean requestFocusInWindow() {
        return timeStates.requestFocusInWindow();
    }

    public void setNameWidth(int width) {
        _nameWidth = width;
        SwingUtilities.invokeLater(updater);
    }

    public int getTimeSpace() {
        int w = timeStates.getWidth();
        return w - _nameWidth;
    }

    public double getSelectedTime() {
        return _selectedTime;
    }

    public double getBeginTime() {
        return _startTime;
    }

    public double getEndTime() {
        return _endTime;
    }

    public double getMaxTime() {
        return _timeMaxTime;
    }

    public double getMinTime() {
        return _timeMinTime;
    }

    public void setStartFinishTime(double time0, double time1) {
        _time0 = time0;
//        if (_time0 < _timeMinTime)
//            _time0 = _timeMinTime;
        _time1 = time1;
//        if (_time1 - _time0 < _minTimeInterval)
//            _time1 = _time0 + _minTimeInterval;
//        if (_time1 > _timeMaxTime)
//            _time1 = _timeMaxTime;
        _timeRangeFixed = true;
        //dump("set");
        redrawLater();
    }

    public void resetStartFinishTime() {
        _timeRangeFixed = false;
        _time0 = _timeMinTime;
        _time1 = _timeMaxTime;
        //dump("reset");
        redrawLater();
    }

    public void setSelectedTime(double time, boolean ensureVisible) {
        _selectedTime = time;
//        if (_selectedTime > _endTime)
//            _selectedTime = _endTime;
//        if (_selectedTime < _startTime)
//            _selectedTime = _startTime;
        if (ensureVisible) {
            double timeSpace = (_time1 - _time0) * .02;
            double timeMid = (_time1 - _time0) * .1;
            if (_selectedTime < _time0 + timeSpace) {
                double dt = _time0 - _selectedTime + timeMid;
                _time0 -= dt;
                _time1 -= dt;
            } else if (_selectedTime > _time1 - timeSpace) {
                double dt = _selectedTime - _time1 + timeMid;
                _time0 += dt;
                _time1 += dt;
            }
//            if (_time0 < 0) {
//                _time1 -= _time0;
//                _time0 = 0;
//            } else if (_time1 > _timeMaxTime) {
//                _time0 -= _time1 - _timeMaxTime;
//                _time1 = _timeMaxTime;
//            }
        }
        redrawLater();
    }

    public void selectNextEvent() {
        timeStates.selectNextEvent();
    }

    public void selectPrevEvent() {
        timeStates.selectPrevEvent();
    }

    public void zoomIn() {
        timeStates.zoomIn();
    }

    public void zoomOut() {
        timeStates.zoomOut();
    }
}
