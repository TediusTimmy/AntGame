 /*
   Copyright (C) 2017 Thomas DiModica <ricinwich@yahoo.com>

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

public final class Token
{

    public final Lexeme tokenType;
    public final String text;

    public final String sourceFile;
    public final int lineNumber;
    public final int lineLocation;

    public Token(Lexeme tokenType, String text, String sourceFile, int lineNumber, int lineLocation)
    {
        this.tokenType = tokenType;
        this.text = text;

        this.sourceFile = sourceFile;
        this.lineNumber = lineNumber;
        this.lineLocation = lineLocation;
    }

}
