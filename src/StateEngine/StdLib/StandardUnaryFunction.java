 /*
   Copyright (C) 2017 Thomas DiModica <ricinwich@yahoo.com>

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

import esl2.engine.CallingContext;
import esl2.engine.FlowControl;
import esl2.engine.statement.Statement;
import esl2.types.FatalException;
import esl2.types.ProgrammingException;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public abstract class StandardUnaryFunction extends Statement
{

    public StandardUnaryFunction()
    {
        super(null);
    }

    @Override
    public FlowControl execute(CallingContext context) throws TypedOperationException, FatalException
    {
        try
        {
            ValueType arg = context.currentFrame.args.get(0);

            return new FlowControl(FlowControl.Type.RETURN, FlowControl.NO_TARGET, fun((StateEngine.CtrlCCtrlV.CallingContext)context, arg), token);
        }
        catch(ClassCastException e)
        {
            throw new ProgrammingException("ESL2 Context was not StateEngine Context");
        }
    }

    public abstract ValueType fun(StateEngine.CtrlCCtrlV.CallingContext context, ValueType arg) throws TypedOperationException, FatalException;

}
