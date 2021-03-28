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

import StateEngine.CtrlCCtrlV.CallingContext;
import esl2.types.FatalException;
import esl2.types.StringValue;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public final class Fatal extends StandardUnaryFunction
{

    @Override
    public ValueType fun(CallingContext context, ValueType arg) throws TypedOperationException, FatalException
    {
        if (arg instanceof StringValue)
        {
            context.fileOut.message("FATAL: " + ((StringValue)arg).value);
            throw new FatalException(((StringValue)arg).value);
        }
        else
        {
            throw new TypedOperationException("Error logging non-String while trying to generate a Fatal message.");
        }
    }

}
