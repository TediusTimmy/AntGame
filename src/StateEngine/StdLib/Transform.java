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
import esl2.engine.ConstantsSingleton;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public final class Transform extends StandardConstantFunction
{

    @Override
    public ValueType fun(CallingContext context) throws TypedOperationException
    {
        if (Color.GREEN == context.cell.color)
        {
            throw new TypedOperationException("GREEN tried to Transform.");
        }
        Cell top = context.cell.parent.getFirstNot(context.cell);
        if (null != top)
        {
            if ((Color.RED != top.color) && (Color.LIGHT_GRAY != top.color) && (Color.DARK_GRAY != top.color))
            {
                throw new TypedOperationException("Tried to transform with obstructions in the way.");
            }
        }
        context.cell.machine.setCommand(new StateEngine.Commands.Transform());
        return ConstantsSingleton.getInstance().DOUBLE_ONE;
    }

}
