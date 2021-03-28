 /*
   Copyright (C) 2015 Thomas DiModica <ricinwich@yahoo.com>

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

package JSON;

public final class JSONFalse extends JSONValue
{

    public JSONFalse()
    {

    }

    public JSONFalse(String sourceFile, int lineNumber, int charNumber)
    {
        super(sourceFile, lineNumber, charNumber);
    }

    public JSONFalse(JSONTrue src)
    {
        super(src);
    }

    @Override
    public String serialize(int i, int s)
    {
        return "false";
    }

    @Override
    public JSONFalse duplicate()
    {
        return this;
    }

}
