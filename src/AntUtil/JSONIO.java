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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeSet;

import JSON.JSONObject;
import JSON.JSONString;
import JSON.JSONValue;
import StateEngine.Environment;
import StateEngine.State;
import StateEngine.CtrlCCtrlV.CallWrapper;
import StateEngine.CtrlCCtrlV.Executor;
import StateEngine.CtrlCCtrlV.ExecutorBuilder;
import StateEngine.CtrlCCtrlV.FunctionDefinitions;
import StateEngine.CtrlCCtrlV.StateFrame;
import esl2.engine.ConstantsSingleton;
import esl2.input.FileInput;
import esl2.input.Lexeme;
import esl2.input.Lexer;
import esl2.input.StringInput;
import esl2.parser.FunctionPairs;
import esl2.parser.GlobalGetterSetter;
import esl2.parser.Parser;
import esl2.parser.ParserException;
import esl2.parser.ParserLogger;
import esl2.parser.SymbolTable;
import esl2.types.FatalException;

public final class JSONIO
{

    private static void assertType(JSONValue value, Class<?> type, String name, String expected) throws FatalException
    {
        if (false == type.isInstance(value))
        {
            throw new FatalException("In file \"" + value.sourceFile + " on line " + value.lineNumber + " at " + value.charNumber + "\n" +
                "\tError: \"" + name + "\" is not of the proper type. A(n) " + expected + " was expected.");
        }
    }

    public static JSONValue readFile(String fileName, ParserLogger logger) throws FatalException
    {
        try
        {
            FileInput file = new FileInput(fileName);
            JSON.Lexer lexer = new JSON.Lexer(file, fileName, 1, 1);
            return JSON.Parser.parse(lexer, logger);
        }
        catch (ParserException e)
        {
            throw new FatalException(e.getLocalizedMessage());
        }
    }

    public static PreEnvironment initialize(JSONValue here, ParserLogger logger) throws FatalException
    {
        PreEnvironment env = new PreEnvironment();

        assertType(here, JSONObject.class, "file input", "object");
        JSONObject input = (JSONObject)here;
        String initialState = null;
        for (Map.Entry<JSONString, JSONValue> entry : input.contents.entrySet())
        {
            switch(entry.getKey().getValue())
            {
            case "Initial State":
                assertType(entry.getValue(), JSONString.class, "Initial State", "string");
                initialState = ((JSONString)entry.getValue()).getValue();
                break;
            case "Global Functions":
                // NOTA BENE : We should always hit this before "States" because the Object uses a sorted Map.
                assertType(entry.getValue(), JSONString.class, "Global Functions", "string");
                env.functions = (JSONString)entry.getValue();

                break;
            case "States":
                assertType(entry.getValue(), JSONObject.class, "States", "object");

                for (Map.Entry<JSONString, JSONValue> state : ((JSONObject)entry.getValue()).contents.entrySet())
                {
                    assertType(state.getValue(), JSONObject.class, state.getKey().getValue(), "object");
                    PreState staat = new PreState();
                    env.states.put(state.getKey().getValue(), staat);

                    for (Map.Entry<JSONString, JSONValue> piece : ((JSONObject)state.getValue()).contents.entrySet())
                    {
                        switch(piece.getKey().getValue())
                        {
                        case "Data":
                            assertType(piece.getValue(), JSONString.class, "Data", "string");

                        {
                            ArrayList<String> varNames = new ArrayList<String>();
                            TreeSet<String> nameSet = new TreeSet<String>();
                            StringInput stateData = new StringInput(((JSONString)piece.getValue()).getValue());
                            Lexer lexer = new Lexer(stateData, state.getKey().getValue() + " Data", piece.getValue().lineNumber, piece.getValue().charNumber);

                            while (Lexeme.END_OF_FILE != lexer.peekNextToken().tokenType)
                            {
                                String varName = lexer.peekNextToken().text;

                                if (Lexeme.IDENTIFIER != lexer.getNextToken().tokenType)
                                {
                                    throw new FatalException("Variable named >" + varName + "< is not a valid identifier.");
                                }

                                if (true == nameSet.contains(varName))
                                {
                                    throw new FatalException("Variable named " + varName + " is not unique.");
                                }

                                nameSet.add(varName);
                                varNames.add(varName);
                            }

                            staat.data.addAll(varNames);
                        }

                            break;
                        case "Functions":
                            assertType(piece.getValue(), JSONString.class, "Functions", "string");
                            staat.functions = (JSONString)piece.getValue();

                            break;
                        default:
                            logger.message("On line " + piece.getKey().lineNumber + " at " + piece.getKey().charNumber + ":\n" +
                                "\tIgnoring unknown thing \"" + piece.getKey().getValue() + "\" in state \"" + state.getKey().getValue() + "\".");
                            // Ignore me.
                            break;
                        }
                    }
                }

                if ((null != initialState) && (false == initialState.isEmpty()))
                {
                    if (false == env.states.containsKey(initialState))
                    {
                        throw new FatalException("The Initial State '" + initialState + "' was never defined.");
                    }
                    else
                    {
                        env.states.get(initialState).isInitialState = true;
                    }
                }

                break;
            default:
                logger.message("On line " + entry.getKey().lineNumber + " at " + entry.getKey().charNumber + ":\n" +
                    "\tIgnoring unknown thing \"" + entry.getKey().getValue() + "\" in base object.");
                // Ignore me.
                break;
            }
        }

        return env;
    }

    public static Environment transform(PreEnvironment here, ParserLogger logger) throws FatalException
    {
        Environment env = new Environment();
        env.executor = new Executor();

        FunctionDefinitions funDefs = new FunctionDefinitions();
        funDefs.buildDefaultFunctions(env.executor);

        {
            SymbolTable table = new SymbolTable();
            table.pushContext(); // We need a base context to operate on.
            table.frameInfo = env.executor.debugFrames;
            table.gs = funDefs.gs;
            table.addAll(funDefs.stdLibFunctions);
            ExecutorBuilder.finalizeTable(table);
            table.addedHere = funDefs.sharedFunctions;

            StringInput globalFuns = new StringInput(here.functions.getValue());
            Lexer lexer = new Lexer(globalFuns, here.functions.sourceFile + " Global Functions", here.functions.lineNumber, here.functions.charNumber + 1);
            boolean result = Parser.ParseFunctions(lexer, table, env.executor, logger);

            if (false == result)
            {
                throw new FatalException("The 'Global Functions' contained more than functions.");
            }
        }

        boolean initialState = false;
        for (Map.Entry<String, PreState> entry : here.states.entrySet())
        {
            if (true == entry.getValue().isInitialState)
            {
                env.initialState = entry.getKey();
                initialState = true;
            }

            for (String name : entry.getValue().data)
            {
                if (null != funDefs.sharedFunctions.funs.get(name))
                {
                    throw new FatalException("Variable named " + name + " cannot have the name of an intrinsic function or a global function.");
                }
            }

            State staat = new State();
            staat.name = entry.getKey();
            env.stateArchitypes.put(staat.name, staat);

            GlobalGetterSetter vars = null;

            vars = FunctionDefinitions.buildGetterSetter(entry.getValue().data, staat.name);
            StateFrame frame = new StateFrame();
            frame.frameName = staat.name;
            for (int i = 0; i < entry.getValue().data.size(); ++i)
            {
                Integer var = Integer.valueOf(i);
                frame.vars.put(entry.getValue().data.get(i), var);
                frame.varNames.put(var, entry.getValue().data.get(i));
            }
            env.executor.stateDebugData.put(staat.name, frame);
            staat.data.addAll(Collections.nCopies(entry.getValue().data.size(), ConstantsSingleton.getInstance().DOUBLE_ZERO));

            {
                SymbolTable table = new SymbolTable();
                table.pushContext(); // We need a base context to operate on.
                table.frameInfo = env.executor.debugFrames;
                table.gs = funDefs.gs;
                table.addAll(funDefs.stdLibFunctions);
                table.addAll(funDefs.sharedFunctions);
                table.globalGS = vars; // Because "Data" should have been processed first.
                ExecutorBuilder.finalizeTable(table);
                table.addedHere = new FunctionPairs();

                StringInput funs = new StringInput(entry.getValue().functions.getValue());
                Lexer lexer = new Lexer(funs, staat.name + " Functions", entry.getValue().functions.lineNumber, entry.getValue().functions.charNumber + 1);
                boolean result = Parser.ParseFunctions(lexer, table, env.executor, logger);

                if (false == result)
                {
                    throw new FatalException("The State '" + staat.name + "' Functions contained more than functions.");
                }

                staat.update = getFunction("Update", 1, staat.name, table, env.executor);
            }
        }

        if (false == initialState)
        {
            throw new FatalException("The Initial State was never defined.");
        }

        return env;
    }

    private static CallWrapper getFunction(String name, int nargs, String stateName, SymbolTable table, Executor executor) throws FatalException
    {
        if (null != table.addedHere.funs.get(name))
        {
            int outputFun = table.addedHere.funs.get(name).intValue();
            if (nargs != executor.args.get(outputFun).intValue())
            {
                throw new FatalException("The \"" + name + "\" function of the State '" + stateName + "' doesn't take " + Integer.toString(nargs) + " argument(s).");
            }
            return new StateEngine.CtrlCCtrlV.CallWrapper(outputFun, executor.functions.get(outputFun).token);
        }
        else
        {
            throw new FatalException("The State '" + stateName + "' doesn't have a function called \"" + name + "\".");
        }
    }

    public static boolean writeFile(PreEnvironment env, String filename)
    {
        boolean success = true;
        JSONObject toWrite = new JSONObject();
        for (Map.Entry<String, PreState> entry : env.states.entrySet())
        {
            if (true == entry.getValue().isInitialState)
            {
                toWrite.contents.put(new JSONString("Initial State", "", 0, 0), new JSONString(entry.getKey(), "", 0, 0));
            }
        }
        toWrite.contents.put(new JSONString("Global Functions", "", 0, 0), env.functions);
        JSONObject states = new JSONObject();
        toWrite.contents.put(new JSONString("States", "", 0, 0), states);
        for (Map.Entry<String, PreState> entry : env.states.entrySet())
        {
            JSONObject state = new JSONObject();
            states.contents.put(new JSONString(entry.getKey(), "" , 0, 0), state);
            state.contents.put(new JSONString("Functions", "", 0, 0), entry.getValue().functions);
            String result = "";
            for (String name : entry.getValue().data)
            {
                result += name + " ";
            }
            state.contents.put(new JSONString("Data", "", 0, 0), new JSONString(result, "", 0, 0));
        }
        try (PrintWriter writer = new PrintWriter(filename, "UTF-8"))
        {
            writer.println(toWrite.serialize(0, 4));
        }
        catch (FileNotFoundException | UnsupportedEncodingException e)
        {
            success = false;
        }
        return success;
    }

}
