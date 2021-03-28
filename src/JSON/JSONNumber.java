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

public final class JSONNumber extends JSONValue
{

    private String value;

    public JSONNumber()
    {
        value = "0";
    }

    public JSONNumber(String value, String sourceFile, int lineNumber, int charNumber)
    {
        super(sourceFile, lineNumber, charNumber);
        this.value = value;
    }

    public JSONNumber(JSONNumber src)
    {
        super(src);
        value = src.value;
    }

    public String getStringValue()
    {
        return value;
    }

    public double getDoubleValue()
    {
        return Double.parseDouble(value);
    }

    public int getIntegerValue()
    {
        return Integer.parseInt(value);
    }

    public boolean setStringValue(String value)
    {
        if (true == Lexer.representsJSONNumber(value))
        {
            this.value = value;
            return true;
        }
        return false;
    }

    public boolean setDoubleValue(double value)
    {
        if ((false == Double.isInfinite(value)) && (false == Double.isNaN(value)))
        {
            this.value = Double.toString(value);
            return true;
        }
        return false;
    }

    public void setIntegerValue(int value)
    {
        this.value = Integer.toString(value);
    }

    @Override
    public String serialize(int i, int s)
    {
        return value;
    }

    @Override
    public JSONNumber duplicate()
    {
        return new JSONNumber(this);
    }

}
