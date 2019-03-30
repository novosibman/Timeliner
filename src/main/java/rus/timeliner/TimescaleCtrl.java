/*****************************************************************************
 * 
 * Copyright (c) 2007,2019 Ruslan Scherbakov (novosibman@gmail.com)
 * 
 *****************************************************************************/

package rus.timeliner;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class TimescaleCtrl extends BaseCtrl implements MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;

    static public boolean DEBUG = true;

    public TimescaleCtrl() {
        super();
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    private ITimeDataProvider _timeProvider;
    private int _dragState = 0;
    private int _dragX0 = 0;
    private int _dragX = 0;
    private double _time0bak;
    private double _time1bak;
    private Rectangle rect0 = new Rectangle();

    public void setDataProvider(ITimeDataProvider timeProvider) {
        _timeProvider = timeProvider;
    }

    private double _timeDeltaD;
    private long _timeDelta;

    private void calcTimeDelta(int width, double K) {
        long D[] = { 1, 2, 5, };
        long pow = 1;
        double powD = 0.000000001;
        long td = pow;
        double tdD = powD;
        double dx = tdD * K;
        int i = 0;
        while (dx < width) {
            td = D[i] * pow;
            tdD = D[i] * powD;
            dx = tdD * K;
            i++;
            if (i == 3) {
                i = 0;
                pow *= 10;
                powD *= 10;
            }
        }
        _timeDeltaD = tdD;
        _timeDelta = td;
    }

    static private TimeDraw _tds[] = new TimeDraw[] {
        new TimeDrawSec(),
        new TimeDrawMillisec(),
        new TimeDrawMicrosec(),
        new TimeDrawNanosec(),
    };

    static TimeDraw getTimeDraw(long timeDelta) {
        TimeDraw timeDraw;
        if (timeDelta >= 1000000000)
            timeDraw = _tds[0];
        else if (timeDelta >= 1000000)
            timeDraw = _tds[1];
        else if (timeDelta >= 1000)
            timeDraw = _tds[2];
        else
            timeDraw = _tds[3];
        return timeDraw;
    }

    @Override
    void paint(Rectangle bound, Graphics2D g) {
        if (null == _timeProvider) {
            g.setColor(Color.blue);
            g.fill(bound);
            return;
        }
        double time0 = _timeProvider.getTime0();
        double time1 = _timeProvider.getTime1();
        double selectedTime = _timeProvider.getSelectedTime();
        int nameSpace = _timeProvider.getNameWidth();
        int timeSpace = _timeProvider.getTimeSpace();
        drawTimeScale(g, bound, time0, time1, selectedTime, nameSpace, timeSpace);
    }

    void drawTimeScale(Graphics2D g, Rectangle bound, double time0, double time1, double selectedTime, int nameWidth, int timeSpace) {
        // fill all area with default bk color
        g.setColor(Defs.header);
        g.fill(bound);

        g.setColor(Defs.foreground);

        rect0.setBounds(bound);
        // draw top left area
        rect0.width = nameWidth;
        rect0.x += 4;
        rect0.width -= 4;
        rect0.x += 2;
        rect0.height -= 4;

        // check and draw nothing if timing is not valid
        if (time1 > time0 && timeSpace >= 2) {

            // detect time scale precision
            double timeRange = time1 - time0;
            int numDigits = 8; // 11:222
            if (timeRange < .00001)
                numDigits = 16; // 11:222:333:444__
            else if (timeRange < .01)
                numDigits = 12; // 11:222:333__

            // detect time deltas for visualization
            int labelWidth = g.getFontMetrics().stringWidth("0") * numDigits;
            double K = 1;
            if (bound.width - nameWidth > 0) {
                K = (double) timeSpace / (time1 - time0);
                calcTimeDelta(labelWidth, K);
            }

            // get corresponding time painter
            TimeDraw timeDraw = getTimeDraw(_timeDelta);

            if (rect0.width > 0) {
                String name = Utils.adjustString("Time: " + timeDraw.hint(), g, rect0.width);
                Utils.drawText(g, name, rect0, true);
            }

            // prepare and draw right rect of the timescale
            rect0.setBounds(bound);
            rect0.x += nameWidth;
            rect0.width = bound.width - nameWidth;

            // draw bottom border and erase all other area
            g.drawLine(bound.x, bound.y + bound.height - 1, bound.x + bound.width - 1, bound.y + bound.height - 1);
            rect0.height--;
            if (rect0.isEmpty())
                return;

            // draw selected time
            int x = rect0.x + (int) ((selectedTime - time0) * K);
            if (x >= rect0.x && x < rect0.x + rect0.width) {
                g.setColor(Defs.selectedTime);
                g.drawLine(x, rect0.y + rect0.height - 6, x, rect0.y + rect0.height);
            }

            // draw time scale ticks
            rect0.y = bound.y;
            rect0.height = bound.height - 4;
            rect0.width = labelWidth;
            double time = Math.floor(time0 / _timeDeltaD) * _timeDeltaD;
            long t = (long) (time * 1000000000);
            int y = rect0.y + rect0.height;
            g.setColor(Defs.foreground);
            while (true) {
                x = bound.x + nameWidth + (int) ((time - time0) * K);
                if (x >= bound.x + nameWidth + bound.width - rect0.width) {
                    break;
                }
                if (x >= bound.x + nameWidth) {
                    g.drawLine(x, y, x, y + 4);
                    rect0.x = x;
                    if (x + rect0.width <= bound.x + bound.width)
                        timeDraw.draw(g, t, rect0);
                }
                time += _timeDeltaD;
                t += _timeDelta;
            }
        }

        g.setColor(Defs.foreground);
        g.drawLine(bound.x + nameWidth, bound.y + bound.height - 6, bound.x + nameWidth, bound.y + bound.height - 1);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (1 == e.getButton() && null != _timeProvider) {
            // System.out.println("mousePressed: " + e);
            if (e.getClickCount() > 1) {
                _timeProvider.resetStartFinishTime();
            } else {
                _dragState = 1;
                _dragX = _dragX0 = e.getX() - _timeProvider.getNameWidth();
                _time0bak = _timeProvider.getTime0();
                _time1bak = _timeProvider.getTime1();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (1 == _dragState) {
            // setCapture(false);
            _dragState = 0;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
        // System.out.println("mouseDragged: " + e);
        drag(e.getX());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    private void drag(int x) {
        int leftSpace = _timeProvider.getNameWidth();
        x = x - leftSpace;
        if (x > 0 && getWidth() > leftSpace && _dragX != x) {
            _dragX = x;
            double time1 = _time0bak + (_time1bak - _time0bak) * _dragX0 / _dragX;
            _timeProvider.setStartFinishTime(_time0bak, time1);
        }
    }
}

abstract class TimeDraw {
    static String S = ":";
    static String S0 = ":0";
    static String S00 = ":00";

    static String pad(long n) {
        String s = S;
        if (n < 10)
            s = S00;
        else if (n < 100)
            s = S0;
        return s + n;
    }

    public abstract void draw(Graphics2D gc, long time, Rectangle rect);

    public abstract String hint();
}

class TimeDrawSec extends TimeDraw {
    static String _hint = "sec";

    @Override
    public void draw(Graphics2D gc, long time, Rectangle rect) {
        time /= 1000000000;
        Utils.drawText(gc, time + "", rect, true);
    }

    @Override
    public String hint() {
        return _hint;
    }
}

class TimeDrawMillisec extends TimeDraw {
    static String _hint = "s:ms";

    @Override
    public void draw(Graphics2D gc, long time, Rectangle rect) {
        if (time == 0) {
            Utils.drawText(gc, "0", rect, true);
            return;
        }
        String sign = time < 0 ? "-" : "";
        if (time < 0)
            time = -time;
        time /= 1000000;
        long ms = time % 1000;
        time /= 1000;
        Utils.drawText(gc, sign + time + pad(ms), rect, true);
    }

    @Override
    public String hint() {
        return _hint;
    }
}

class TimeDrawMicrosec extends TimeDraw {
    static String _hint = "s:ms:us";

    @Override
    public void draw(Graphics2D gc, long time, Rectangle rect) {
        if (time == 0) {
            Utils.drawText(gc, "0", rect, true);
            return;
        }
        String sign = time < 0 ? "-" : "";
        if (time < 0)
            time = -time;
        time /= 1000;
        long us = time % 1000;
        time /= 1000;
        long ms = time % 1000;
        time /= 1000;
        Utils.drawText(gc, sign + time + pad(ms) + pad(us), rect, true);
    }

    @Override
    public String hint() {
        return _hint;
    }
}

class TimeDrawNanosec extends TimeDraw {
    static String _hint = "s:ms:us:ns";

    @Override
    public void draw(Graphics2D gc, long time, Rectangle rect) {
        if (time == 0) {
            Utils.drawText(gc, "0", rect, true);
            return;
        }
        String sign = time < 0 ? "-" : "";
        if (time < 0)
            time = -time;
        long ns = time % 1000L;
        time /= 1000L;
        long us = time % 1000L;
        time /= 1000L;
        long ms = time % 1000L;
        time /= 1000L;
        Utils.drawText(gc, sign + time + pad(ms) + pad(us) + pad(ns), rect, true);
    }

    @Override
    public String hint() {
        return _hint;
    }
}
