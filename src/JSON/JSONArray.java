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

import java.util.ArrayList;

public final class JSONArray extends JSONValue
{

    public ArrayList<JSONValue> contents;

    public JSONArray()
    {
        contents = new ArrayList<JSONValue>();
    }

    public JSONArray(ArrayList<JSONValue> contents, String sourceFile, int lineNumber, int charNumber)
    {
        super(sourceFile, lineNumber, charNumber);
        deepCopy(contents);
    }

    public JSONArray(JSONArray src)
    {
        super(src);
        deepCopy(src.contents);
    }

    @Override
    public JSONArray duplicate()
    {
        return new JSONArray(this);
    }

    private void deepCopy(ArrayList<JSONValue> contents)
    {
        this.contents = new ArrayList<JSONValue>();
        for (JSONValue v : contents)
        {
            this.contents.add(v.duplicate());
        }
    }

    private void addSpaces(StringBuilder sink, int number)
    {
        for (int i = 0; i < number; ++i)
        {
            sink.append(" ");
        }
    }

    @Override
    public String serialize(int i, int s)
    {
        StringBuilder str = new StringBuilder();
        if ((0 != i) && (0 != s))
        {
            str.append('\n');
            addSpaces(str, i * s);
        }
        str.append('[');
        ++i;
        if (0 != s)
        {
            str.append('\n');
            addSpaces(str, i * s);
        }
        for (int n = 0; n < contents.size(); ++n)
        {
            if (0 != n)
            {
                str.append(',');
            }
            str.append(contents.get(n).serialize(i, s));
        }
        --i;
        if ((0 != i) && (0 != s))
        {
            str.append('\n');
            addSpaces(str, i * s);
        }
        str.append(']');
        return str.toString();
    }

}
