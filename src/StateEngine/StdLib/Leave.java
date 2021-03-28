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

import StateEngine.CtrlCCtrlV.CallingContext;
import esl2.engine.ConstantsSingleton;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public final class Leave extends StandardConstantFunction
{

    @Override
    public ValueType fun(CallingContext context) throws TypedOperationException
    {
        context.cell.machine.states.getLast().removeFirst();
        if (true == context.cell.machine.states.getLast().isEmpty())
        {
            context.cell.machine.states.removeLast();
        }
        if (true == context.cell.machine.states.isEmpty())
        {
            throw new TypedOperationException("Error leaving state: stack is now empty.");
        }
        return ConstantsSingleton.getInstance().DOUBLE_ONE;
    }

}
