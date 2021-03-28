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

import AntWorld.World;
import JSON.JSONValue;
import JSON.Lexer;
import JSON.Parser;
import StateEngine.StdLib.Look;
import esl2.input.FileInput;
import esl2.parser.ParserException;
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

    public WorldViewer()
    {
        x = -1;
        y = -1;
        seed = 1024;
        energy = 100;
        look = 20;
        density = 0.01;
        scale = 10;

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
        if ((x != rect.getWidth()) && (y != rect.getHeight()))
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
        boolean result = true;
        Rectangle rect = this.getBounds(null);
        x = (int)rect.getWidth();
        y = (int)rect.getHeight();
        wx = x / scale - 4;
        wy = y / scale - 4;
        world = new World(seed, wx, wy, energy, look, density);

        try
        {
            FileInput file = new FileInput(fileName);
            Lexer lexer = new Lexer(file, fileName, 1, 1);
            JSONValue val = Parser.parse(lexer, logger);
            world.initializeFrom(val, logger);
        }
        catch (FatalException | ParserException e)
        {
            logger.message(e.getLocalizedMessage());
            result = false;
        }
        return result;
    }

}
