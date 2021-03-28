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
import esl2.types.StringValue;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public final class Rewind extends StandardUnaryFunction
{

    @Override
    public ValueType fun(CallingContext context, ValueType arg) throws TypedOperationException
    {
        if (arg instanceof StringValue)
        {
            String name = ((StringValue)arg).value;
            context.cell.machine.states.removeLast();
            while ((false == context.cell.machine.states.isEmpty()) &&
                (false == name.equals(context.cell.machine.states.getLast().getFirst().name)))
            {
                context.cell.machine.states.removeLast();
            }
            if (true == context.cell.machine.states.isEmpty())
            {
                throw new TypedOperationException("Error rewinding stack to state '" + name + "' as state is not in stack.");
            }
        }
        else
        {
            throw new TypedOperationException("Rewind called without string name of state.");
        }
        return ConstantsSingleton.getInstance().DOUBLE_ONE;
    }

}
