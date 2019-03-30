/*****************************************************************************
 * 
 * Copyright (c) 2007,2019 Ruslan Scherbakov (novosibman@gmail.com)
 * 
 *****************************************************************************/

package rus.timeliner;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPanel;

public abstract class BaseCtrl extends JPanel {

    private static final long serialVersionUID = 1L;

    static public final int MARGIN = 4;
    static public final int SMALL_ICON_SIZE = 16;

    public BaseCtrl() {
        setIgnoreRepaint(true);
        setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle bound = getBounds();
        bound.setLocation(0, 0);
        paint(bound, (Graphics2D) g);
    }

    abstract void paint(Rectangle bound, Graphics2D g);
}
