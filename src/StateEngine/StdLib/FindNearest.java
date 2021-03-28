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

package StateEngine.StdLib;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;

import AntWorld.Cell;
import AntWorld.World;
import StateEngine.CtrlCCtrlV.CallingContext;
import esl2.types.ArrayValue;
import esl2.types.FatalException;
import esl2.types.StringValue;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;
import esl2.types.Vector;
import esl2.types.VectorValue;

public final class FindNearest extends StandardUnaryFunction
{

    @Override
    public ValueType fun(CallingContext context, ValueType arg) throws TypedOperationException, FatalException
    {
        if (arg instanceof StringValue)
        {
            String name = ((StringValue)arg).value;
            Color color = convertColorNameToColor(name);
            if (null == color)
            {
                throw new FatalException("Name '" + name + "' is invalid to find.");
            }
            LinkedList<Cell> nearest = findNearest(context.world, context.cell.x, context.cell.y, color, context.world.LOOK, false);
            ArrayList<ValueType> result = new ArrayList<ValueType>();
            for (Cell cell : nearest)
            {
                // Yes, the Y values should be flipped. Up is negative.
                result.add(new VectorValue(new Vector(cell.x - context.cell.x, context.cell.y - cell.y, 0)));
            }
            return new ArrayValue(result);
        }
        else
        {
            throw new TypedOperationException("FindNearest called without string name of type to find.");
        }
    }

    public static Color convertColorNameToColor(String name)
    {
        Color color = null;
        switch (name)
        {
        case "BLUE":
            color = Color.BLUE;
            break;
        case "GREEN":
            color = Color.GREEN;
            break;
        case "RED":
            color = Color.RED;
            break;
        case "CYAN":
            color = Color.CYAN;
            break;
        case "YELLOW":
            color = Color.YELLOW;
            break;
        case "MAGENTA":
            color = Color.MAGENTA;
            break;
        case "LIGHT GRAY":
            color = Color.LIGHT_GRAY;
            break;
        case "DARK GRAY":
            color = Color.DARK_GRAY;
            break;
        case "BLACK":
            color = Color.BLACK;
            break;
        case "WHITE":
            color = Color.WHITE;
            break;
        case "GRAY":
            color = Color.GRAY;
            break;
        default:
            // Handle this in the caller.
        }
        return color;
    }

    public static int ManhattanDistance(int x1, int y1, int x2, int y2)
    {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static void addNeighborMaybe(Cell temp, LinkedList<Cell> known, LinkedList<Cell> frontier)
    {
        if (null != temp)
        {
            boolean contains = false;
            for (Cell cell : known)
            {
                if (cell == temp) // These should all be unique, so reference comparison should be right.
                {
                    contains = true;
                    break;
                }
            }
            if (false == contains)
            {
                known.add(temp);
                frontier.add(temp);
            }
        }
    }

    public static void addNeighbors(World world, LinkedList<Cell> known, LinkedList<Cell> frontier, int x, int y)
    {
        Cell temp = world.getCellAt(x + 1, y);
        addNeighborMaybe(temp, known, frontier);
        temp = world.getCellAt(x - 1, y);
        addNeighborMaybe(temp, known, frontier);
        temp = world.getCellAt(x, y + 1);
        addNeighborMaybe(temp, known, frontier);
        temp = world.getCellAt(x, y - 1);
        addNeighborMaybe(temp, known, frontier);
    }

    public static boolean globalLook(Cell chello, Color color)
    {
        boolean contains = false;
        for (Cell cell : chello.resources)
        {
            if (color == cell.color)
            {
                contains = true;
                break;
            }
        }
        if (color == chello.color)
        {
            contains = true;
        }
        return contains;
    }

    public static boolean localLook(Cell chello, Color color)
    {
        boolean contains = false;
        if (true == chello.resources.isEmpty())
        {
            if (color == chello.color)
            {
                contains = true;
            }
        }
        else
        {
            if (color == chello.resources.get(0).color)
            {
                contains = true;
            }
        }
        return contains;
    }

    /**
     * Breadth-first search for closest cells.
     * @param world The world.
     * @param x The x location to start at.
     * @param y The y location to start at.
     * @param color The color to find. The Cell must be this color, or have a resource of this color.
     * @param max The maximum distance to search.
     * @param global If true, look at more than the top-most thing.
     * @return The list of cells, all equally near, that are nearest to the start.
     */
    public static LinkedList<Cell> findNearest(World world, int x, int y, Color color, int max, boolean global)
    {
        LinkedList<Cell> known = new LinkedList<Cell>();
        LinkedList<Cell> frontier = new LinkedList<Cell>();
        LinkedList<Cell> result = new LinkedList<Cell>();
        int closest = max;

        known.add(world.getCellAt(x, y));
        addNeighbors(world, known, frontier, x, y);

        while (false == frontier.isEmpty())
        {
            Cell front = frontier.removeFirst();
            int dist = ManhattanDistance(x, y, front.x, front.y);
            boolean present;
            if (true == global)
            {
                present = globalLook(front, color);
            }
            else
            {
                present = localLook(front, color);
            }
            if (true == present)
            {
                if (dist < closest)
                {
                    result.clear();
                    result.add(front);
                    closest = dist;
                }
                else if (dist == closest)
                {
                    result.add(front);
                }
            }
            if (dist < closest)
            {
                addNeighbors(world, known, frontier, front.x, front.y);
            }
        }

        return result;
    }

}
