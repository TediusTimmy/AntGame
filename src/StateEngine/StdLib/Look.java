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

import AntWorld.Cell;
import StateEngine.CtrlCCtrlV.CallingContext;
import esl2.types.FatalException;
import esl2.types.StringValue;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;
import esl2.types.Vector;
import esl2.types.VectorValue;

public final class Look extends StandardUnaryFunction
{

    @Override
    public ValueType fun(CallingContext context, ValueType arg) throws TypedOperationException, FatalException
    {
        if (arg instanceof VectorValue)
        {
            Vector value = ((VectorValue)arg).value;
            Cell at = context.world.getCellAt(context.cell.x + (int)value.x, context.cell.y - (int)value.y);
            if (null == at)
            {
                return new StringValue("ORANGE"); // ORANGE is beyond the edge of the universe
            }
            if (FindNearest.ManhattanDistance(context.cell.x, context.cell.y, at.x, at.y) > context.world.LOOK)
            {
                return new StringValue("PINK"); // PINK is beyond the current range of sight
            }
            // If the cell has no obstructions, see the base color.
            if (true == at.resources.isEmpty())
            {
                return new StringValue(convertBaseColorToString(at.color));
            }
            // Looking at the cell the blue/green is at has special handling.
            if ((0.0 == value.x) && (0.0 == value.y))
            {
                // If there is a resource other than us here, we see it.
                for (Cell cell : at.resources)
                {
                    if (cell != context.cell)
                    {
                        return new StringValue(convertResourceColorToString(cell.color));
                    }
                }
                // Else, we see the base color.
                return new StringValue(convertBaseColorToString(at.color));
            }
            else // If not, just report the top-most resource. Priority: GREEN, BLUE, EVERYTHING ELSE
            {
                return new StringValue(convertResourceColorToString(at.resources.getFirst().color));
            }
        }
        else
        {
            throw new TypedOperationException("Look called with non-vector of where.");
        }
    }

    public static String convertBaseColorToString(Color color)
    {
        if (color == Color.WHITE)
        {
            return "WHITE";
        }
        else if (color == Color.BLACK)
        {
            return "BLACK";
        }
        else if (color == Color.GRAY)
        {
            return "GRAY";
        }
        return "PLEASE REPORT THIS BUG";
    }

    public static String convertResourceColorToString(Color color)
    {
        if (color == Color.GREEN)
        {
            return "GREEN";
        }
        else if (color == Color.BLUE)
        {
            return "BLUE";
        }
        else if (color == Color.YELLOW)
        {
            return "YELLOW";
        }
        else if (color == Color.RED)
        {
            return "RED";
        }
        else if (color == Color.CYAN)
        {
            return "CYAN";
        }
        else if (color == Color.MAGENTA)
        {
            return "MAGENTA";
        }
        else if (color == Color.LIGHT_GRAY)
        {
            return "LIGHT GRAY";
        }
        else if (color == Color.DARK_GRAY)
        {
            return "DARK GRAY";
        }
        return "PLEASE REPORT THIS BUG";
    }

}
