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

package AntUtil;

import java.util.ArrayList;

import JSON.JSONString;

public final class PreState
{

    public boolean isInitialState;
    public final ArrayList<String> data;
    public JSONString functions;

    public PreState()
    {
        isInitialState = false;
        data = new ArrayList<String>();
        functions = new JSONString(); // Set this to an empty string for convenience.
    }

}
