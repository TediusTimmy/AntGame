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

import esl2.input.GenericInput;
import esl2.input.Lexer.BufferedGenericInput;
import esl2.types.FatalException;

public final class Lexer
{

    private final BufferedGenericInput bgi;
    private final String sourceName;
    private Token nextToken;

    public Lexer(GenericInput input, String sourceName, int startLine, int startChar) throws FatalException
    {
        bgi = new BufferedGenericInput(input);
        this.sourceName = sourceName;
        currentLine = startLine;
        currentCharacter = startChar;
        composeNextToken();
    }

    private int currentLine;
    private int currentCharacter;

    private void consume() throws FatalException
    {
        int next = bgi.consume();
        if ('\n' == next)
        {
            currentCharacter = 1;
            ++currentLine;
        }
        else if (GenericInput.ENDOFFILE != next)
        {
            ++currentCharacter;
        }
    }

    private void consumeToEndOfLine() throws FatalException
    {
        while ((GenericInput.ENDOFFILE != bgi.peek()) && ('\n' != bgi.peek()))
        {
            consume();
        }
    }

    private void consumeWhiteSpace() throws FatalException
    {
        for(;;)
        {
            switch(bgi.peek())
            {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                consume();
                break;
            case '#':
                // Shell-style to-end-of-line comment.
                consumeToEndOfLine();
                break;
            case '/':
                if ('/' == bgi.peek(1))
                {
                    // C++-style to-end-of-line comment.
                    consumeToEndOfLine();
                }
                else if ('*' == bgi.peek(1))
                {
                    int lineNo = currentLine;
                    int charNo = currentCharacter;
                    // C-style comment.
                    consume(); // Consume the '/' and '*'
                    consume(); // so that "/*/" is not a valid comment.
                    while ((GenericInput.ENDOFFILE != bgi.peek()) && (('*' != bgi.peek()) || ('/' != bgi.peek(1))))
                    {
                        consume();
                    }
                    if (GenericInput.ENDOFFILE == bgi.peek())
                    {
                        throw new FatalException("End of Input reached before comment terminated. Comment starting on line " + Integer.toString(lineNo) + " at position " + Integer.toString(charNo) + ".");
                    }
                    consume(); // Finish off the comment.
                    consume();
                }
                else
                {
                    return;
                }
                break;
            default:
                return;
            }
        }
    }

    // Return the next token of input without removing it.
    public Token peekNextToken()
    {
        return nextToken;
    }

    // Remove and return the next token of input.
    public Token getNextToken() throws FatalException
    {
        Token result = nextToken;
        composeNextToken();
        return result;
    }

    // Create next token of input.
    private void composeNextToken() throws FatalException
    {
        consumeWhiteSpace();

        int lineNo = currentLine;
        int charNo = currentCharacter;

        StringBuilder buildText = null;
        String text = null;

        Lexeme tokenType = Lexeme.INVALID;

        switch(bgi.peek())
        {
        case GenericInput.ENDOFFILE:
            tokenType = Lexeme.END_OF_FILE;
            break;
        case '{':
            tokenType = Lexeme.OPEN_BRACE;
            text = "{";
            consume();
            break;
        case '}':
            tokenType = Lexeme.CLOSE_BRACE;
            text = "}";
            consume();
            break;
        case '[':
            tokenType = Lexeme.OPEN_BRACKET;
            text = "[";
            consume();
            break;
        case ']':
            tokenType = Lexeme.CLOSE_BRACKET;
            text = "]";
            consume();
            break;
        case ':':
            tokenType = Lexeme.COLON;
            text = ":";
            consume();
            break;
        case ',':
            tokenType = Lexeme.COMMA;
            text = ",";
            consume();
            break;
        case '"':
            tokenType = Lexeme.STRING;
            buildText = new StringBuilder();
            consume();
            while ((GenericInput.ENDOFFILE != bgi.peek()) && ('"' != bgi.peek()))
            {
                buildText.append((char) bgi.peek());
                consume();
            }
            if (GenericInput.ENDOFFILE == bgi.peek())
            {
                tokenType = Lexeme.INVALID;
            }
            consume();
            text = buildText.toString();
            break;
        default:
            // tokenType remains INVALID
            buildText = new StringBuilder();

            // Consume until the next token is something that we understand.
            do
            {
                buildText.append((char) bgi.peek());
                consume();
            }
            while ((GenericInput.ENDOFFILE != bgi.peek()) && (-1 == "\",:][}{ \t\r\n/#".indexOf(bgi.peek())));

            text = buildText.toString();
            // Was it one of the things we actually do understand?
            if (text.equals("null"))
            {
                tokenType = Lexeme.JSON_NULL;
            }
            else if (text.equals("false"))
            {
                tokenType = Lexeme.JSON_FALSE;
            }
            else if (text.equals("true"))
            {
                tokenType = Lexeme.JSON_TRUE;
            }
            else if (representsJSONNumber(text))
            {
                tokenType = Lexeme.NUMBER;
            }
        }

        nextToken = new Token(tokenType, text, sourceName, lineNo, charNo);
    }

    public static boolean representsJSONNumber(String test)
    {
        // An empty string is not a valid number.
        if (true == test.isEmpty())
        {
            return false;
        }

        int currentChar = 0;
        int length = test.length();

        // Consume the optional negative sign.
        if ('-' == test.charAt(currentChar))
        {
            ++currentChar;
        }
        // A sign alone is invalid.
        if (length == currentChar)
        {
            return false;
        }

        // Zero is only allowed to be alone.
        if ('0' == test.charAt(currentChar))
        {
            ++currentChar;
        }
        else if (('1' <= test.charAt(currentChar)) || ('9' >= test.charAt(currentChar)))
        {
            ++currentChar;
            while ((length != currentChar) && (('0' <= test.charAt(currentChar)) && ('9' >= test.charAt(currentChar))))
            {
                ++currentChar;
            }
        }


        if (length == currentChar)
        {
            return true;
        }
        if ('.' == test.charAt(currentChar))
        {
            ++currentChar;

            // We can't have a trailing decimal place.
            if (length == currentChar)
            {
                return false;
            }
            // It MUST be followed by at least one digit.
            if (('0' >= test.charAt(currentChar)) && ('9' <= test.charAt(currentChar)))
            {
                return false;
            }

            while ((length != currentChar) && (('0' <= test.charAt(currentChar)) && ('9' >= test.charAt(currentChar))))
            {
                ++currentChar;
            }
        }

        if (length == currentChar)
        {
            return true;
        }
        if (('e' == test.charAt(currentChar)) || ('E' == test.charAt(currentChar)))
        {
            ++currentChar;

            // It must be followed by at least one digit.
            if (length == currentChar)
            {
                return false;
            }

            if (('-' == test.charAt(currentChar)) || ('+' == test.charAt(currentChar)))
            {
                ++currentChar;

                // At least one digit.
                // We don't need to check that what's next is a digit,
                // because only digits will be consumed after this.
                if (length == currentChar)
                {
                    return false;
                }
            }

            while ((length != currentChar) && (('0' <= test.charAt(currentChar)) && ('9' >= test.charAt(currentChar))))
            {
                ++currentChar;
            }

        }

        return currentChar == length;
    }

}
