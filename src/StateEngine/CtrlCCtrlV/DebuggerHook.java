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

import StateEngine.StdLib.EnterDebugger;
import esl2.engine.DebugFunctor;
import esl2.types.ProgrammingException;

public final class DebuggerHook extends DebugFunctor
{

    @Override
    public void EnterDebugger(String message, esl2.engine.CallingContext rawContext)
    {
        try
        {
            CallingContext context = (CallingContext)rawContext;
            context.fileOut.message("Caught exception: " + message + "\nEntering debugger.");
            EnterDebugger.function(context);
        }
        catch(ClassCastException e)
        {
            throw new ProgrammingException("ESL2 Context was not StateEngine Context");
        }
    }

}
