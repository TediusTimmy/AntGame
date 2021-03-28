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

import java.util.ArrayList;

import AntWorld.Cell;
import StateEngine.CtrlCCtrlV.CallingContext;
import esl2.types.ArrayValue;
import esl2.types.FatalException;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public final class Inform extends StandardUnaryFunction
{

    @Override
    public ValueType fun(CallingContext context, ValueType arg) throws TypedOperationException, FatalException
    {
        ArrayList<ValueType> result = new ArrayList<ValueType>();
        for (Cell cell : context.cell.parent.resources)
        {
            if (true == cell.active)
            {
                result.add(cell.machine.states.getLast().getFirst().onUpdate.execute(context, arg));
            }
        }
        return new ArrayValue(result);
    }

}
