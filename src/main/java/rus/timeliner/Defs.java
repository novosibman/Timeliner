/*****************************************************************************
 * 
 * Copyright (c) 2007,2019 Ruslan Scherbakov (novosibman@gmail.com)
 * 
 *****************************************************************************/

package rus.timeliner;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

public class Defs {

    static public Color merge(Color c, Color c2, int weight2) {
        int weight = 255 - weight2;
        int r = (c.getRed() * weight + c2.getRed() * weight2) / 255;
        int g = (c.getGreen() * weight + c2.getGreen() * weight2) / 255;
        int b = (c.getBlue() * weight + c2.getBlue() * weight2) / 255;
        return new Color(r, g, b);
    }

    static final int mergeBy = 100;
    static final public Color sel = new Color(80, 80, 80);
    static final public Color darken = new Color(40, 40, 40);
    static final public Color shadow = new Color(0, 0, 0, 30);
    static final public Color rangeSelection = new Color(0, 0, 250, 50);
    static final public Color threadBackground = new Color(230, 245, 255, 200);
    static final public Color threadForeground = new Color(120, 120, 120, 200);
    static final public Color selectedTime = new Color(100, 100, 255);
    static final public Color selectedTimeInFocus = new Color(10, 10, 255);
    static final public Color selectedItem = new Color(0, 0, 200);
    static final public Color foreground = new Color(0, 0, 0);
    static final public Color group = new Color(200, 200, 100);
    static final public Color header = new Color(200, 200, 100);
    static final public Color headerSel = merge(header, sel, mergeBy);
    static final public Color background2 = new Color(240, 250, 235);
    static final public Color background = new Color(250, 255, 245);
    static final public Color expander = foreground;
    static final public Color backgroundSel = merge(background, sel, mergeBy);
    static final public Color midline = Color.LIGHT_GRAY;

    static public class PaletteItem {
        public Color background;
        public Color foreground;
        public Stroke stroke;
        public PaletteItem(Color background, Color foreground, Stroke stroke) {
            this.background = background;
            this.foreground = foreground;
            this.stroke = stroke;
        }
    }

    static final public BasicStroke bsThick = new BasicStroke(2f);
    static final public BasicStroke bsDotted = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] { 3f }, 0f);
    static final public BasicStroke bsDottedThick = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[] { 3f }, 0f);

    static public PaletteItem palette[] = new PaletteItem[] {
        new PaletteItem(new Color(133, 199, 255), new Color(120, 120, 120, 200), bsDotted),
        new PaletteItem(new Color(255, 244, 199), new Color(122, 105, 69, 200), bsDotted),
        new PaletteItem(new Color(189, 245, 255), new Color(97, 125, 131, 200), bsDotted),
        new PaletteItem(new Color(227, 185, 255), new Color(160, 160, 120, 200), bsDotted),
        new PaletteItem(new Color(214, 255, 230), new Color(160, 160, 120, 200), bsDotted),
        new PaletteItem(new Color(255, 240, 192), new Color(160, 160, 120, 200), bsDotted),
    };
}
