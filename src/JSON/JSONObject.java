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

import java.util.Set;
import java.util.TreeMap;

public final class JSONObject extends JSONValue
{

    public TreeMap<JSONString, JSONValue> contents;

    public JSONObject()
    {
        contents = new TreeMap<JSONString, JSONValue>();
    }

    public JSONObject(TreeMap<JSONString, JSONValue> contents, String sourceFile, int lineNumber, int charNumber)
    {
        super(sourceFile, lineNumber, charNumber);
        deepCopy(contents);
    }

    public JSONObject(JSONObject src)
    {
        super(src);
        deepCopy(src.contents);
    }

    @Override
    public JSONObject duplicate()
    {
        return new JSONObject(this);
    }

    private void deepCopy(TreeMap<JSONString, JSONValue> contents)
    {
        this.contents = new TreeMap<JSONString, JSONValue>();
        
        for (JSONString s : contents.keySet())
        {
            this.contents.put(s.duplicate(), contents.get(s).duplicate());
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
        str.append('{');
        ++i;
        Set<JSONString> keys = contents.keySet();
        for (JSONString k : keys)
        {
            if (k != keys.iterator().next())
            {
                str.append(',');
            }
            if (0 != s)
            {
                str.append('\n');
                addSpaces(str, i * s);
            }
            str.append("\"" + k.getValue() + "\":");
            str.append(contents.get(k).serialize(i, s));
        }
        --i;
        if (0 != s)
        {
            str.append('\n');
            addSpaces(str, i * s);
        }
        str.append('}');
        return str.toString();
    }

}
