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

public abstract class Move extends Command
{

    private Cell startParent, endParent;
    private boolean succeededMove;
    
    public Move()
    {
        startParent = null;
        endParent = null;
        succeededMove = false;
    }

    protected void setStartParent(Cell cell)
    {
        startParent = cell.parent;
        endParent = cell.parent;
    }

    protected void attemptMove(Cell cell, World world, int i, int j)
    {
        Cell newParent = world.getCellAt(i, j);
        if ((true == newParent.resources.isEmpty()) || (Color.MAGENTA != newParent.resources.getFirst().color))
        {
            endParent = newParent;
            succeededMove = true;
        }
        // RULES: Moving onto a MAGENTA while carrying a CYAN destroys the MAGENTA and CYAN.
        else if ((null != cell.held) && (Color.CYAN == cell.held.color))
        {
            endParent = newParent;
            succeededMove = true;
            cell.held = null; // It dead!
            newParent.resources.clear(); // It dead, too!
        }
    }

    protected boolean finalizeMove(Cell cell, World world)
    {
        int cost = computeCost();

        if (true == succeededMove)
        {
            cell.x = endParent.x;
            cell.y = endParent.y;
            cell.parent.resources.remove(cell);
            cell.parent = endParent;
            endParent.addResource(cell);
        }

        handleCost(cell, world, cost);

        return succeededMove;
    }

    private int computeCost()
    {
        int cost = 1;
        // RULES: It costs 4 to move onto or off of a RED obstruction.
        for (Cell cell : startParent.resources)
        {
            if (Color.RED == cell.color)
            {
                cost = 4;
            }
        }
        for (Cell cell : endParent.resources)
        {
            if (Color.RED == cell.color)
            {
                cost = 4;
            }
        }
        return cost;
    }

}
