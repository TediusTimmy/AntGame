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

package JSON.test;

import JSON.JSONValue;
import JSON.Lexer;
import JSON.Parser;
import esl2.input.FileInput;
import esl2.input.StringInput;
import esl2.parser.ParserException;
import esl2.test.DummyLogger;
import esl2.types.FatalException;

public final class JSONParserTest
{

    public static void main(String[] args)
    {
        try
        {
            FileInput file = new FileInput("SampleFile.txt");
            Lexer lexer = new Lexer(file, "SampleFile.txt", 1, 1);

            DummyLogger logger = new DummyLogger();
            JSONValue value = Parser.parse(lexer, logger);

            String string1 = value.serialize(0, 4);
            StringInput string = new StringInput(string1);
            Lexer lexer2 = new Lexer(string, "round trip", 1, 1);

            JSONValue value2 = Parser.parse(lexer2, logger);

            String string2 = value2.serialize(0, 4);
            System.out.print("This should be true: " + string1.equals(string2));
        }
        catch(FatalException | ParserException e)
        {
            System.err.println("Unexpected exception thrown: " + e.getLocalizedMessage());
        }
    }

}
