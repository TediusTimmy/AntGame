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

import StateEngine.State;
import StateEngine.CtrlCCtrlV.CallingContext;
import esl2.engine.ConstantsSingleton;
import esl2.types.StringValue;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public final class Transition extends StandardUnaryFunction
{

    @Override
    public ValueType fun(CallingContext context, ValueType arg) throws TypedOperationException
    {
        if (arg instanceof StringValue)
        {
            String name = ((StringValue)arg).value;
            State state = context.environment.stateArchitypes.get(name);
            if (null == state)
            {
                throw new TypedOperationException("No state with name '" + name + "'.");
            }
            context.cell.machine.states.getLast().set(0, new State(state));
        }
        else
        {
            throw new TypedOperationException("Non-string passed to Transition for state name.");
        }
        return ConstantsSingleton.getInstance().DOUBLE_ONE;
    }

}
