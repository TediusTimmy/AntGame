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
import esl2.types.FatalException;
import esl2.types.StringValue;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public final class Skip extends StandardUnaryFunction
{

    @Override
    public ValueType fun(CallingContext context, ValueType arg) throws TypedOperationException, FatalException
    {
        if (arg instanceof StringValue)
        {
            String name = ((StringValue)arg).value;
            while ((false == context.cell.machine.states.getLast().isEmpty()) &&
                (false == name.equals(context.cell.machine.states.getLast().getFirst().name)))
            {
                context.cell.machine.states.getLast().removeFirst();
            }
            if (true == context.cell.machine.states.getLast().isEmpty())
            {
                throw new FatalException("Error skipping to state '" + name + "' as state is not in current queue.");
            }
        }
        else
        {
            throw new TypedOperationException("Skip called without string name of state.");
        }
        return ConstantsSingleton.getInstance().DOUBLE_ONE;
    }

}
