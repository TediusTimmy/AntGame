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

package StateEngine.Commands;

import AntWorld.Cell;
import AntWorld.World;
import esl2.engine.stdlib.PushBack;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public final class Inform extends Command
{

    private ValueType arg;

    public Inform(ValueType arg)
    {
        this.arg = arg;
    }

    @Override
    public boolean act(Cell cell, World world) throws CommandFailed
    {
        for (Cell child : cell.parent.resources)
        {
            if (true == child.active)
            {
                try
                {
                    child.machine.reports = new PushBack().fun(child.machine.reports, arg);
                }
                catch (TypedOperationException e)
                {
                    // This should NEVER be executed.
                    throw new CommandFailed(e.getLocalizedMessage());
                }
            }
        }
        handleCost(cell, world, 1);
        return true;
    }

}
