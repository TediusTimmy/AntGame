 /*
   Copyright (C) 2021 Thomas DiModica <ricinwich@yahoo.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package AntGUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import AntUtil.JSONIO;
import AntWorld.World;
import StateEngine.Environment;
import StateEngine.StdLib.Look;
import esl2.parser.ParserLogger;
import esl2.types.FatalException;

public final class WorldViewer extends JPanel
{

    private World world;
    private int x;
    private int y;
    private int wx;
    private int wy;

    public int seed;
    public int energy;
    public int look;
    public double density;
    public int scale;

    // Java 8 has a bug, wherein the size of the titlebar changes depending if the
    // frame is resizable or not. It is a look-and-feel issue with the default Windows
    // look-and-feel. We're going to hack around that.
    private boolean hack;

    public WorldViewer()
    {
        x = -1;
        y = -1;
        seed = 1024;
        energy = 100;
        look = 20;
        density = 0.01;
        scale = 10;

        hack = false;

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                // Do nothing
            }
            @Override
            public void mouseMoved(MouseEvent e)
            {
                int mx = (e.getX() / scale) - 2;
                int my = (e.getY() / scale) - 2;
                Color color = world.getAt(mx, my);
                String name;
                if (Color.PINK == color)
                {
                    name = "ORANGE";
                }
                else if ((Color.BLACK == color) || (Color.WHITE == color) || (Color.GRAY == color))
                {
                    name = Look.convertBaseColorToString(color);
                }
                else
                {
                    name = Look.convertResourceColorToString(color);
                }
                WorldViewer.this.setToolTipText(name);
            }
        });
    }

    public void reset()
    {
        x = -1;
        y = -1;
        hack = false;
    }

    public World.RESULT update()
    {
        if (null != world)
        {
            return world.update();
        }
        return World.RESULT.NONE;
    }

    public void updateScreen()
    {
        repaint();
    }

    public long getTurns()
    {
        if (null != world)
        {
            return world.numUpdates;
        }
        return 0;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Rectangle rect = this.getBounds(null);
        if ((x != rect.getWidth()) && (y != rect.getHeight()) && (false == hack))
        {
            x = (int)rect.getWidth();
            y = (int)rect.getHeight();
            wx = x / scale - 4;
            wy = y / scale - 4;
            world = new World(seed, wx, wy, energy, look, density);
        }
        g.setColor(Color.ORANGE);
        g.fillRect(0, 0, (wx + 4) * scale, (wy + 4) * scale);
        for (int i = 0; i < wy; ++i)
        {
            for (int j = 0; j < wx; ++j)
            {
                g.setColor(world.getAt(j, i));
                g.fillRect((j + 2) * scale, (i + 2) * scale, scale, scale);
            }
        }
    }
   
    @Override
    public boolean isFocusable()
    {
        return true;
    }

    public boolean loadFile(String fileName, ParserLogger logger)
    {
        try
        {
            return loadData(JSONIO.transform(JSONIO.initialize(JSONIO.readFile(fileName, logger), logger), logger), logger);
        }
        catch (FatalException e)
        {
            logger.message(e.getLocalizedMessage());
            return false;
        }
    }

    public boolean loadData(Environment env, ParserLogger logger)
    {
        Rectangle rect = this.getBounds(null);
        x = (int)rect.getWidth();
        y = (int)rect.getHeight();
        wx = x / scale - 4;
        wy = y / scale - 4;
        world = new World(seed, wx, wy, energy, look, density);

        world.initialize(env, logger);
        hack = true;
        return true;
    }

}
