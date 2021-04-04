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
import StateEngine.State;
import StateEngine.CtrlCCtrlV.CallingContext;
import esl2.engine.ConstantsSingleton;
import esl2.engine.FlowControl;
import esl2.engine.statement.Statement;
import esl2.types.ProgrammingException;
import esl2.types.StringValue;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public final class Task extends Statement
{

    public Task()
    {
        super(null);
    }

    public static ValueType fun(CallingContext context, ValueType arg1, ValueType arg2) throws TypedOperationException
    {
        State state = null;
        if (arg1 instanceof StringValue)
        {
            String name = ((StringValue)arg1).value;
            state = context.environment.stateArchitypes.get(name);
            if (null == state)
            {
                throw new TypedOperationException("No state with name '" + name + "'.");
            }
        }
        else
        {
            throw new TypedOperationException("Task called without string name of state.");
        }
        Color toFind = Color.BLUE;
        if (Color.BLUE == context.cell.color)
        {
            toFind = Color.GREEN;
        }
        Cell blue = null;
        for (Cell cell : context.cell.parent.resources)
        {
            // We can only task cells that aren't actively tasked.
            if ((toFind == cell.color) && (false == cell.active))
            {
                blue = cell;
                break;
            }
        }
        if (null == blue)
        {
            return ConstantsSingleton.getInstance().DOUBLE_ZERO;
        }
        context.cell.machine.setCommand(new StateEngine.Commands.Task(blue, state, arg2));
        return ConstantsSingleton.getInstance().DOUBLE_ONE;
    }

    @Override
    public FlowControl execute(esl2.engine.CallingContext context) throws TypedOperationException
    {
        try
        {
            ValueType arg1 = context.currentFrame.args.get(0);
            ValueType arg2 = context.currentFrame.args.get(1);

            return new FlowControl(FlowControl.Type.RETURN, FlowControl.NO_TARGET, fun((StateEngine.CtrlCCtrlV.CallingContext)context, arg1, arg2), token);
        }
        catch(ClassCastException e)
        {
            throw new ProgrammingException("ESL2 Context was not StateEngine Context");
        }
    }

}
