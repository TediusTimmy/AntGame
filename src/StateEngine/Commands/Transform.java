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

public final class Transform extends Command
{

    @Override
    public boolean act(Cell cell, World world)
    {
        Cell top = cell.parent.getFirstNot(cell);
        if (null != top)
        {
            if (Color.RED == top.color)
            {
                --top.energy;
                if (0 == top.energy)
                {
                    cell.parent.resources.remove(top);
                }
            }
            else if (Color.DARK_GRAY == top.color)
            {
                cell.parent.resources.remove(top);
                // DARK GREY has a 3/4 chance of destroying a BLUE that tries to transform it.
                if (top.draw_rand.getNext() < 0.75)
                {
                    cell.active = false;
                    cell.energy = -world.ENERGY;
                }
            }
            else if (Color.LIGHT_GRAY == top.color)
            {
                cell.parent.resources.remove(top);
                // LIGHT GREY has a 4/5 chance of teleporting a BLUE to a random location if it tries to transform it.
                if (top.draw_rand.getNext() < 0.80)
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
        }
        else
        {
            if (Color.BLACK == cell.parent.color)
            {
                cell.parent.color = Color.GRAY;
                --world.blacks;
            }
            else if (Color.WHITE == cell.parent.color)
            {
                cell.parent.color = Color.GRAY;
                --world.whites;
            }
            else
            {
                if (cell.parent.draw_rand.getNext() < 0.5)
                {
                    cell.parent.color = Color.BLACK;
                    ++world.blacks;
                }
                else
                {
                    cell.parent.color = Color.WHITE;
                    ++world.whites;
                }
            }
        }
        handleCost(cell, world, 1);
        return true;
    }

}
