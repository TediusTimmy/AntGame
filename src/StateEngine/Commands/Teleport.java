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

public final class Teleport extends Command
{

    Cell to;

    public Teleport(Cell to)
    {
        this.to = to;
    }

    @Override
    public boolean act(Cell cell, World world) throws CommandFailed
    {
        cell.parent.resources.remove(cell);
        cell.parent = to;
        to.addResource(cell);
        cell.x = to.x;
        cell.y = to.y;
        handleCost(cell, 1);
        return true;
    }

}
