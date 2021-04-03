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

import java.awt.Color;

import AntWorld.Cell;
import AntWorld.World;

public final class Grab extends Command
{

    @Override
    public boolean act(Cell cell, World world) throws CommandFailed
    {
        int cost = 1;
        Cell top = cell.parent.getFirstNot(cell);
        if (null != top)
        {
            if (Color.DARK_GRAY == top.color)
            {
                cell.parent.resources.remove(top);
                // RULES: DARK GREY has a 1/4 chance of destroying a BLUE that interacts with it.
                if (top.draw_rand.getNext() < 0.25)
                {
                    cell.active = false;
                    cell.energy = -world.ENERGY;
                }
            }
            else if (Color.LIGHT_GRAY == top.color)
            {
                cell.parent.resources.remove(top);
                // RULES: LIGHT GREY has a 1/5 chance of teleporting a BLUE to a random location.
                if (top.draw_rand.getNext() < 0.20)
                {
                    int newx = (int)(top.draw_rand.getNext() * world.x);
                    int newy = (int)(top.draw_rand.getNext() * world.y);
                    cell.x = newx;
                    cell.y = newy;
                    Cell newParent = world.getCellAt(newx, newy);
                    cell.parent.resources.remove(cell);
                    cell.parent = newParent;
                    newParent.addResource(cell);
                }
            }
            else
            {
                if (null == cell.held)
                {
                    // A BLUE or GREEN may only be picked up when inactive.
                    if (((Color.BLUE != top.color) && (Color.GREEN != top.color)) || (false == top.active))
                    {
                        cell.held = top;
                        cell.parent.resources.remove(top);
                    }
                    else
                    {
                        cost = 0;
                    }
                }
            }
        }
        else
        {
            throw new CommandFailed("Tried to grab with nothing to grab.");
        }
        handleCost(cell, cost);
        return true;
    }

}
