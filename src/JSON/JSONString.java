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

public final class JSONString extends JSONValue implements Comparable<JSONString>
{

    private String value;

    public JSONString()
    {
        value = "";
    }

    public JSONString(String value, String sourceFile, int lineNumber, int charNumber)
    {
        super(sourceFile, lineNumber, charNumber);
        this.value = value;
    }

    public JSONString(JSONString src)
    {
        super(src);
        value = src.value;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public String serialize(int i, int s)
    {
        return "\"" + value + "\"";
    }

    @Override
    public JSONString duplicate()
    {
        return new JSONString(this);
    }

    @Override
    public int compareTo(JSONString arg)
    {
        return value.compareTo(arg.value);
    }
    
    @Override
    public int hashCode()
    {
        return value.hashCode();
    }

}
