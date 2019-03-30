/*****************************************************************************
 * 
 * Copyright (c) 2007,2019 Ruslan Scherbakov (novosibman@gmail.com)
 * 
 *****************************************************************************/

package rus.timeliner;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

public class Utils {

    static public final int IMG_THREAD_RUNNING = 0;
    static public final int IMG_THREAD_SUSPENDED = 1;
    static public final int IMG_THREAD_STOPPED = 2;
    static public final int IMG_METHOD_RUNNING = 3;
    static public final int IMG_METHOD = 4;
    static public final int IMG_NUM = 5;

    static public Color mixColors(Color c1, Color c2, int w1, int w2) {
        return new Color((w1 * c1.getRed() + w2 * c2.getRed()) / (w1 + w2), (w1 * c1.getGreen() + w2 * c2.getGreen()) / (w1 + w2), (w1 * c1.getBlue() + w2
                * c2.getBlue())
                / (w1 + w2));
    }

    static public void fillStateRect(Graphics2D g, Rectangle rect, int paletteIndex, boolean rectBound) {
        drawState(g, rect, Defs.palette[paletteIndex].background, Defs.palette[paletteIndex].foreground, Defs.palette[paletteIndex].stroke,
                rectBound);
    }

    static public void drawState(Graphics2D g, Rectangle rect, Color colorBk, Color colorFg, Stroke bs, boolean rectBound) {
        if (rect.width < 0 || rect.height < 0)
            return;
        if (rect.width < 2)
            rect.width = 2;
        if (rect.width > 10000)
            rect.width = 10000;
        g.setColor(colorBk);
        g.fill(rect);
        g.setColor(colorFg);
        // draw decoration middle line
        if (bs != null) {
            int mindy = rect.y + rect.height / 2;
            Stroke bs0 = g.getStroke();
            g.setStroke(bs);
            //System.out.println("rect: " + rect + " mindy: " + mindy);
            g.drawLine(rect.x, mindy, rect.x + rect.width, mindy);
            g.setStroke(bs0);
        }
        // if (rectBound && rect.width >= 3) {
        if (rectBound) {
            g.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
        } else {
            g.drawLine(rect.x, rect.y, rect.x + rect.width - 1, rect.y);
            g.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width - 1, rect.y + rect.height - 1);
        }
        
    }

    static public void outlineStateRect(Graphics2D g, Rectangle rect, Color col, boolean thick) {
        if (rect.width < 0 || rect.height < 0)
            return;
        if (rect.width < 2)
            rect.width = 2;
        g.setColor(col);
        if (rect.width >= 3) {
            g.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
            if (thick)
            g.drawRect(rect.x + 1, rect.y + 1, rect.width - 3, rect.height - 3);
        } else {
            //if (thick)
            g.drawLine(rect.x, rect.y, rect.x + rect.width - 1, rect.y);
            g.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width - 1, rect.y + rect.height - 1);
        }
//        if (thick)
//        g.drawLine(rect.x, rect.y + 1, rect.x + rect.width - 1, rect.y + 1);
//        g.drawLine(rect.x, rect.y + rect.height - 2, rect.x + rect.width - 1, rect.y + rect.height - 2);
    }

    static public int drawText(Graphics2D g, String text, Rectangle rect, boolean transp) {
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D textBounds = fm.getStringBounds(text, g);
        int y = rect.y + (rect.height - (int) textBounds.getHeight()) / 2 + fm.getAscent();
        return drawText(g, text, rect.x, y, transp);
    }

    static public int drawText(Graphics2D g, String text, int x, int y, boolean transp) {
        FontMetrics fm = g.getFontMetrics();
        char[] chars = text.toCharArray();
        g.drawChars(chars, 0, chars.length, x, y);
        int len = fm.stringWidth(text);
        return len;
    }

    /**
     * Formats time in format: MM:SS:NNN
     * 
     * @param v
     * @return
     */
    static public String formatTime(double v) {
        StringBuffer str = new StringBuffer();
        boolean neg = v < 0;
        if (neg) {
            v = -v;
            str.append('-');
        }
        long sec = (long) v;
        if (sec / 60 < 10)
            str.append('0');
        str.append(sec / 60);
        str.append(':');
        sec %= 60;
        if (sec < 10)
            str.append('0');
        str.append(sec);
        str.append(':');
        long ms = (long) (v * 1000);
        ms %= 1000;
        if (ms < 10)
            str.append("00");
        else if (ms < 100)
            str.append('0');
        str.append(ms);
        return str.toString();
    }

    static public int compare(double d1, double d2) {
        if (d1 > d2)
            return 1;
        if (d1 < d2)
            return 1;
        return 0;
    }

    static public int compare(String s1, String s2) {
        if (s1 != null && s2 != null)
            return s1.compareToIgnoreCase(s2);
        if (s1 != null)
            return 1;
        if (s2 != null)
            return -1;
        return 0;
    }

    static public String formatPercent(int val, int max) {
        String s = max > 0 && max >= val ? "" + ((double) val / (double) max) : "";
        return s;
    }

    static public String adjustString(String text, Graphics2D g, int width) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        int cuts = 0;
        while (textWidth > width && text.length() > 1) {
            cuts++;
            text = text.substring(0, text.length() - 1);
            // size = gc.stringExtent(name + "...");
            textWidth = g.getFontMetrics().stringWidth(text + "...");
        }
        if (cuts > 0)
            text += "...";
        return text;
    }
}
