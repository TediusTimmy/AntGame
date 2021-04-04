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

import java.util.LinkedList;

import AntWorld.Cell;
import AntWorld.World;
import StateEngine.State;
import esl2.types.ValueType;

public final class Task extends Command
{

    private Cell victim;
    private State state;
    private ValueType orders;

    public Task(Cell toTask, State state, ValueType orders)
    {
        victim = toTask;
        this.state = state;
        this.orders = orders;
    }

    @Override
    public boolean act(Cell cell, World world) throws CommandFailed
    {
        victim.active = true;
        victim.energy = world.ENERGY;
        victim.machine.states.add(new LinkedList<State>());
        victim.machine.states.getFirst().add(new State(state));
        victim.machine.last = orders;
        handleCost(cell, 1);
        return true;
    }

}
