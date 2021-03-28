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

import java.util.ArrayList;
import java.util.TreeMap;

import esl2.parser.ParserException;
import esl2.parser.ParserLogger;
import esl2.types.FatalException;

public final class Parser
{

    private static String errorDetail(String sourceFile, int lineNumber, int columnNumber, String message)
    {
        return "In file " + sourceFile + " on line " + lineNumber + " at position " + columnNumber + " : " + message;
    }

    public static JSONValue parse(Lexer lexer, ParserLogger logger) throws ParserException, FatalException
    {
        JSONValue result = parseValue(lexer, logger);
        if (Lexeme.END_OF_FILE != lexer.peekNextToken().tokenType)
        {
            throw new ParserException(errorDetail(lexer.peekNextToken().sourceFile, lexer.peekNextToken().lineNumber,
                lexer.peekNextToken().lineLocation, "Parse did not consume all of input."));
        }
        return result;
    }

    private static JSONValue parseValue(Lexer lexer, ParserLogger logger) throws ParserException, FatalException
    {
        JSONValue result = null;

        Token token = lexer.getNextToken();
        switch (token.tokenType)
        {
        case OPEN_BRACE:
            TreeMap<JSONString, JSONValue> attributes = new TreeMap<JSONString, JSONValue>();
            JSONString name = null;

            // Allow for an empty object.
            if (Lexeme.CLOSE_BRACE != lexer.peekNextToken().tokenType)
            {
                if (Lexeme.STRING != lexer.peekNextToken().tokenType)
                {
                    throw new ParserException(errorDetail(lexer.peekNextToken().sourceFile, lexer.peekNextToken().lineNumber,
                        lexer.peekNextToken().lineLocation, "Expected attribute name, none found."));
                }
                name = new JSONString(lexer.peekNextToken().text, lexer.peekNextToken().sourceFile, lexer.peekNextToken().lineNumber, lexer.peekNextToken().lineLocation);
                lexer.getNextToken();
                if (Lexeme.COLON == lexer.peekNextToken().tokenType)
                {
                    lexer.getNextToken();
                }
                else
                {
                    logger.message(errorDetail(lexer.peekNextToken().sourceFile, lexer.peekNextToken().lineNumber,
                        lexer.peekNextToken().lineLocation, "Expected colon between attribute and value, none found. Assuming that the colon was omitted."));
                }
                attributes.put(name, parseValue(lexer, logger));
            }

            while (Lexeme.CLOSE_BRACE != lexer.peekNextToken().tokenType)
            {
                if (Lexeme.COMMA == lexer.peekNextToken().tokenType)
                {
                    lexer.getNextToken();
                }
                else
                {
                    logger.message(errorDetail(lexer.peekNextToken().sourceFile, lexer.peekNextToken().lineNumber,
                        lexer.peekNextToken().lineLocation, "Expected comma in object, none found. Assuming that the comma was omitted."));
                }

                if (Lexeme.CLOSE_BRACE == lexer.peekNextToken().tokenType)
                {
                    logger.message(errorDetail(lexer.peekNextToken().sourceFile, lexer.peekNextToken().lineNumber,
                        lexer.peekNextToken().lineLocation, "Ignoring erroneous comma at end of object contents."));
                    break;
                }

                if (Lexeme.STRING != lexer.peekNextToken().tokenType)
                {
                    throw new ParserException(errorDetail(lexer.peekNextToken().sourceFile, lexer.peekNextToken().lineNumber,
                        lexer.peekNextToken().lineLocation, "Expected attribute name, none found."));
                }
                name = new JSONString(lexer.peekNextToken().text, lexer.peekNextToken().sourceFile, lexer.peekNextToken().lineNumber, lexer.peekNextToken().lineLocation);
                lexer.getNextToken();
                if (Lexeme.COLON == lexer.peekNextToken().tokenType)
                {
                    lexer.getNextToken();
                }
                else
                {
                    logger.message(errorDetail(lexer.peekNextToken().sourceFile, lexer.peekNextToken().lineNumber,
                        lexer.peekNextToken().lineLocation, "Expected colon between attribute and value, none found. Assuming that the colon was omitted."));
                }

                if (attributes.containsKey(name))
                {
                    logger.message(errorDetail(name.sourceFile, name.lineNumber, name.charNumber,
                        "An attribute with name \"" + name.getValue() + "\" already exists in this object. Ignoring redefinition."));

                    // Parse and ignore.
                    parseValue(lexer, logger);
                }
                else
                {
                    attributes.put(name, parseValue(lexer, logger));
                }
            }

            if (Lexeme.CLOSE_BRACE == lexer.peekNextToken().tokenType)
            {
                lexer.getNextToken();
            }
            else
            {
                logger.message(errorDetail(lexer.peekNextToken().sourceFile, lexer.peekNextToken().lineNumber,
                    lexer.peekNextToken().lineLocation, "Expected closing brace to object, none found. Assuming that the closing brace was omitted."));
            }
            result = new JSONObject(attributes, token.sourceFile, token.lineNumber, token.lineLocation);
            break;
        case OPEN_BRACKET:
            ArrayList<JSONValue> array = new ArrayList<JSONValue>();
            // Allow for an empty array.
            if (Lexeme.CLOSE_BRACKET != lexer.peekNextToken().tokenType)
            {
                array.add(parseValue(lexer, logger));
            }
            while (Lexeme.CLOSE_BRACKET != lexer.peekNextToken().tokenType)
            {
                if (Lexeme.COMMA == lexer.peekNextToken().tokenType)
                {
                    lexer.getNextToken();
                }
                else
                {
                    logger.message(errorDetail(lexer.peekNextToken().sourceFile, lexer.peekNextToken().lineNumber,
                        lexer.peekNextToken().lineLocation, "Expected comma in array, none found. Assuming that the comma was omitted."));
                }
                if (Lexeme.CLOSE_BRACKET != lexer.peekNextToken().tokenType)
                {
                    array.add(parseValue(lexer, logger));
                }
                else
                {
                    logger.message(errorDetail(lexer.peekNextToken().sourceFile, lexer.peekNextToken().lineNumber,
                        lexer.peekNextToken().lineLocation, "Ignoring erroneous comma at end of array contents."));
                }
            }
            if (Lexeme.CLOSE_BRACKET == lexer.peekNextToken().tokenType)
            {
                lexer.getNextToken();
            }
            else
            {
                logger.message(errorDetail(lexer.peekNextToken().sourceFile, lexer.peekNextToken().lineNumber,
                    lexer.peekNextToken().lineLocation, "Expected closing bracket to array, none found. Assuming that the closing bracket was omitted."));
            }
            result = new JSONArray(array, token.sourceFile, token.lineNumber, token.lineLocation);
            break;
        case STRING:
            result = new JSONString(token.text, token.sourceFile, token.lineNumber, token.lineLocation);
            break;
        case NUMBER:
            result = new JSONNumber(token.text, token.sourceFile, token.lineNumber, token.lineLocation);
            break;
        case JSON_TRUE:
            result = new JSONTrue(token.sourceFile, token.lineNumber, token.lineLocation);
            break;
        case JSON_FALSE:
            result = new JSONFalse(token.sourceFile, token.lineNumber, token.lineLocation);
            break;
        case JSON_NULL:
            result = new JSONNull(token.sourceFile, token.lineNumber, token.lineLocation);
            break;
        default:
            throw new ParserException(errorDetail(token.sourceFile, token.lineNumber, token.lineLocation, "Unexpected data in file."));
        }

        return result;
    }

}
