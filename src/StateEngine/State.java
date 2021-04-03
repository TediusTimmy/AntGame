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

package StateEngine;

import java.util.ArrayList;

import StateEngine.Commands.Command;
import StateEngine.CtrlCCtrlV.CallWrapper;
import StateEngine.CtrlCCtrlV.CallingContext;
import esl2.types.FatalException;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public final class State
{

    public String name;
    public final ArrayList<ValueType> data;
    public CallWrapper update;
    public CallWrapper onUpdate;
    public Command next;

    public State()
    {
        data = new ArrayList<ValueType>();
    }

    public State(State src)
    {
        name = src.name;
        data = new ArrayList<ValueType>(src.data);
        update = src.update;
        onUpdate = src.onUpdate;
    }

    public ValueType update(CallingContext context, ValueType arg) throws FatalException, TypedOperationException
    {
        try
        {
            next = null;
            context.pushState(this);
            return update.execute(context, arg);
        }
        finally
        {
            context.popState();
        }
    }

    public void setCommand(Command newCommand) throws TypedOperationException
    {
        if (null != next)
        {
            throw new TypedOperationException("Tried to perform two commands in one move.");
        }
        next = newCommand;
    }

}
