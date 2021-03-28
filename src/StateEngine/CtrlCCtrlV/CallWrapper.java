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

package StateEngine.CtrlCCtrlV;

import java.util.ArrayList;

import esl2.engine.CallingContext;
import esl2.engine.expression.Constant;
import esl2.engine.expression.Expression;
import esl2.engine.expression.FunctionCall;
import esl2.input.Token;
import esl2.types.FatalException;
import esl2.types.FunctionPointerValue;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public final class CallWrapper
{

    public final int location;
    public final Token token;

    public CallWrapper(int location, Token token)
    {
        this.location = location;
        this.token = token;
    }

    public ValueType execute(CallingContext context, ValueType arg) throws FatalException, TypedOperationException
    {
        ArrayList<Expression> args = new ArrayList<Expression>();
        args.add(new Constant(token, arg));
        FunctionCall call = new FunctionCall(token, new Constant(token, new FunctionPointerValue(location)), args);
        return call.evaluate(context);
    }

}
