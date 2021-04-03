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

import StateEngine.CtrlCCtrlV.CallingContext;
import esl2.engine.ConstantsSingleton;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public final class Report extends StandardConstantFunction
{

    @Override
    public ValueType fun(CallingContext context) throws TypedOperationException
    {
        if ((Color.BLUE == context.cell.color) &&
            ((true == context.cell.parent.resources.isEmpty()) || (Color.GREEN != context.cell.parent.resources.getFirst().color)))
        {
            throw new TypedOperationException("BLUE tried to Report for Orders with no GREEN.");
        }
        context.top().setCommand(new StateEngine.Commands.Report());
        return ConstantsSingleton.getInstance().DOUBLE_ONE;
    }

}
