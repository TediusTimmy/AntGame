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

package StateEngine;

import java.util.TreeMap;

import StateEngine.CtrlCCtrlV.Executor;

public final class Environment
{

    public String initialState;
    public final TreeMap<String, State> stateArchitypes;
    public Executor executor;

    public Environment()
    {
        stateArchitypes = new TreeMap<String, State>();
    }

}
