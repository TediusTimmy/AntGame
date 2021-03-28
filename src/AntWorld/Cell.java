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

package AntWorld;

import java.awt.Color;
import java.util.LinkedList;

import AntUtil.Rand48;
import StateEngine.StateMachine;
import StateEngine.CtrlCCtrlV.CallingContext;
import esl2.types.FatalException;
import esl2.types.TypedOperationException;

public final class Cell
{

    public Cell(World world, Rand48 rand, int x, int y)
    {
        this.x = x;
        this.y = y;
        resources = new LinkedList<Cell>();
        this.world = world;
        draw_rand = rand;
        machine = new StateMachine();

        active = false;
        energy = 0;
    }

    // Location
    public int x;
    public int y;

    // Color of this Cell
    public Color color;
    public int prior;

    // Resources in this Cell
    public final LinkedList<Cell> resources;

    // For a resource
    public final World world;
    public Cell parent;
    public final Rand48 draw_rand;

    // For a blue
    public Cell held;
    public boolean active;
    public StateMachine machine;
    public int energy;

    public void addResource(Cell resource)
    {
        for (int i = 0; i < resources.size(); ++i)
        {
            if (resource.prior <= resources.get(i).prior)
            {
                resources.add(i, resource);
                return;
            }
        }
        resources.add(resource);
    }

    public Cell getFirstNot(Cell cell)
    {
        for (Cell chello : resources)
        {
            if (chello != cell)
            {
                return chello;
            }
        }
        return null;
    }

    public boolean update(CallingContext context) throws FatalException, TypedOperationException
    {
        context.cell = this;
        active = machine.update(context);
        return active;
    }

}
