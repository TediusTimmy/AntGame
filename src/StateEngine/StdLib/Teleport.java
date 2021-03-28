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
import java.util.LinkedList;

import AntWorld.Cell;
import StateEngine.CtrlCCtrlV.CallingContext;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;
import esl2.types.Vector;
import esl2.types.VectorValue;

public final class Teleport extends StandardUnaryFunction
{

    @Override
    public ValueType fun(CallingContext context, ValueType arg) throws TypedOperationException
    {
        if (Color.GREEN == context.cell.color)
        {
            throw new TypedOperationException("GREEN tried to teleport.");
        }
        boolean contains = false;
        for (Cell cell : context.cell.parent.resources)
        {
            if (Color.YELLOW == cell.color)
            {
                contains = true;
                break;
            }
        }
        if (false == contains)
        {
            throw new TypedOperationException("Tried to teleport with no teleporter.");
        }
        if (arg instanceof VectorValue)
        {
            Vector value = ((VectorValue)arg).value;
            int destx = context.cell.x + (int)value.x;
            int desty = context.cell.y - (int)value.y; // Yes, y is backwards.
            if (null == context.world.getCellAt(destx, desty))
            {
                throw new TypedOperationException("Tried to teleport out of the universe.");
            }
            LinkedList<Cell> nearest = FindNearest.findNearest(context.world, destx, desty, Color.YELLOW, Integer.MAX_VALUE, true);
            // The search is a global search, so it should never return empty: it just may return the portal that you're on.
            Cell to = nearest.getFirst();
            context.cell.parent.resources.remove(context.cell);
            context.cell.parent = to;
            to.addResource(context.cell);
            context.cell.x = to.x;
            context.cell.y = to.y;
            return new VectorValue(new Vector(destx - to.x, to.y - desty, 0.0));
        }
        else
        {
            throw new TypedOperationException("Teleport called with non-vector of where.");
        }
    }

}
