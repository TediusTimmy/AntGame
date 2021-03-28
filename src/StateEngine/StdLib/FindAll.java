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

public final class FindAll extends StandardUnaryFunction
{

    @Override
    public ValueType fun(CallingContext context, ValueType arg) throws TypedOperationException, FatalException
    {
        if (arg instanceof StringValue)
        {
            String name = ((StringValue)arg).value;
            Color color = FindNearest.convertColorNameToColor(name);
            if (null == color)
            {
                throw new FatalException("Name '" + name + "' is invalid to find.");
            }
            LinkedList<Cell> nearest = findAll(context.world, context.cell.x, context.cell.y, color, context.world.LOOK, false);
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
            throw new TypedOperationException("FindAll called without string name of type to find.");
        }
    }

    /**
     * Breadth-first search all cells.
     * @param world The world.
     * @param x The x location to start at.
     * @param y The y location to start at.
     * @param color The color to find. The Cell must be this color, or have a resource of this color.
     * @param max The maximum distance to search.
     * @param global If true, look at more than the top-most thing.
     * @return The list of cells that are visible.
     */
    public static LinkedList<Cell> findAll(World world, int x, int y, Color color, int max, boolean global)
    {
        LinkedList<Cell> known = new LinkedList<Cell>();
        LinkedList<Cell> frontier = new LinkedList<Cell>();
        LinkedList<Cell> result = new LinkedList<Cell>();

        known.add(world.getCellAt(x, y));
        FindNearest.addNeighbors(world, known, frontier, x, y);

        while (false == frontier.isEmpty())
        {
            Cell front = frontier.removeFirst();
            int dist = FindNearest.ManhattanDistance(x, y, front.x, front.y);
            boolean present;
            if (true == global)
            {
                present = FindNearest.globalLook(front, color);
            }
            else
            {
                present = FindNearest.localLook(front, color);
            }
            if (true == present)
            {
                result.add(front);
            }
            if (dist < max)
            {
                FindNearest.addNeighbors(world, known, frontier, front.x, front.y);
            }
        }

        return result;
    }

}
