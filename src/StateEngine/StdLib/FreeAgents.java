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
import esl2.types.DoubleValue;
import esl2.types.ValueType;

public final class FreeAgents extends StandardConstantFunction
{

    @Override
    public ValueType fun(CallingContext context)
    {
        int free = 0;
        Color toFind = Color.BLUE;
        if (Color.BLUE == context.cell.color)
        {
            toFind = Color.GREEN;
        }
        for (Cell cell : context.cell.parent.resources)
        {
            if ((toFind == cell.color) && (false == cell.active))
            {
                ++free;
            }
        }
        return new DoubleValue(free);
    }

}
