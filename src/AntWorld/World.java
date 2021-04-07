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

package AntWorld;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import AntUtil.NonSplittableRandom;
import AntUtil.Rand48;
import JSON.JSONObject;
import JSON.JSONString;
import JSON.JSONValue;
import StateEngine.Environment;
import StateEngine.State;
import StateEngine.CtrlCCtrlV.CallWrapper;
import StateEngine.CtrlCCtrlV.CallingContext;
import StateEngine.CtrlCCtrlV.DebuggerHook;
import StateEngine.CtrlCCtrlV.Executor;
import StateEngine.CtrlCCtrlV.ExecutorBuilder;
import StateEngine.CtrlCCtrlV.FunctionDefinitions;
import StateEngine.CtrlCCtrlV.StateFrame;
import esl2.engine.ConstantsSingleton;
import esl2.input.Lexeme;
import esl2.input.Lexer;
import esl2.input.StringInput;
import esl2.parser.FunctionPairs;
import esl2.parser.GlobalGetterSetter;
import esl2.parser.Parser;
import esl2.parser.ParserLogger;
import esl2.parser.SymbolTable;
import esl2.types.FatalException;
import esl2.types.TypedOperationException;

public final class World
{

    // The bounds of the world.
    public final int x;
    public final int y;

    // The actual data.
    private Cell w [][];

    private final ArrayList<Cell> blues;
    private final ArrayList<Cell> greens;

    public int whites;
    public int blacks;
    private int first_active;
    private int noMoveTurns;

    private boolean everythingIsFucked;
    public final int ENERGY;
    public final int LOOK;
    private final double DENSITY;

    public long numUpdates;

    public World (int seed, int x, int y, int energy, int look, double density)
    {
        ENERGY = energy;
        LOOK = look;
        DENSITY = density;

        this.x = x;
        this.y = y;
        w = new Cell[y][x];

        whites = 0;
        blacks = 0;

        blues = new ArrayList<Cell>();
        greens = new ArrayList<Cell>();

        for (int i = 0; i < w.length; ++i)
        {
            for (int j = 0; j < w[0].length; ++j)
            {
                w[i][j] = buildCell(seed, j, i);
            }
        }

        everythingIsFucked = true;

        first_active = greens.size();
        final NonSplittableRandom rand = new NonSplittableRandom(jenkins_hash(seed, 0, 0));
        // Dump five results for good measure.
        rand.getNext(); rand.getNext(); rand.getNext(); rand.getNext(); rand.getNext();
        final Rand48 give = new Rand48(rand.getNext());
        while (first_active == greens.size())
        {
            int fx = (int)(give.getNext() * x);
            int fy = (int)(give.getNext() * y);
            Cell parent = getCellAt(fx, fy);
            if (true == parent.resources.isEmpty())
            {
                Cell temp = new Cell(this, new Rand48(rand.getNext()), fx, fy);
                temp.color = Color.GREEN;
                temp.prior = 1;
                temp.parent = parent;
                greens.add(temp);
                parent.addResource(temp);
                temp = new Cell(this, new Rand48(rand.getNext()), fx, fy);
                temp.color = Color.BLUE;
                temp.prior = 2;
                temp.parent = parent;
                blues.add(temp);
                parent.addResource(temp);
            }
        }

        noMoveTurns = 0;
        sessionResult = RESULT.NONE;

        numUpdates = 0;
    }

    public static enum RESULT
    {
       WIN,
       LOSE,
       BROKEN,
       NONE
    }

    RESULT sessionResult;

    public RESULT update()
    {
        if (true == everythingIsFucked)
        {
            return RESULT.NONE;
        }
        ++numUpdates;
        boolean someUpdate = false;
        for (Cell cell : blues)
        {
            if (false == cell.active)
            {
                continue;
            }
            try
            {
                cell.update(context);
                if (null != cell.machine.next)
                {
                    someUpdate = cell.machine.next.act(cell, this);
                }
            }
            catch (TypedOperationException | FatalException e)
            {
                everythingIsFucked = true;
                context.fileOut.message("Game has ended with error: " + e.getLocalizedMessage());
                return RESULT.BROKEN;
            }
        }
        for (Cell cell : greens)
        {
            if (false == cell.active)
            {
                continue;
            }
            try
            {
                // YES : blues cannot act until the next turn after being activated by a green.
                cell.update(context);
                if (null != cell.machine.next)
                {
                    cell.machine.next.act(cell, this);
                }
            }
            catch (TypedOperationException | FatalException e)
            {
                everythingIsFucked = true;
                context.fileOut.message("Game has ended with error: " + e.getLocalizedMessage());
                return RESULT.BROKEN;
            }
        }
        if (RESULT.NONE == sessionResult)
        {
            if (((x * y) == whites) || ((x * y) == blacks))
            {
                sessionResult = RESULT.WIN;
                return sessionResult;
            }
            if (false == someUpdate)
            {
                ++noMoveTurns;
                if (100 == noMoveTurns)
                {
                    sessionResult = RESULT.LOSE;
                    return sessionResult;
                }
            }
            else
            {
                noMoveTurns = 0;
            }
        }
        return RESULT.NONE;
    }

    public Color getAt(int ix, int iy)
    {
        if ((ix < 0) || (ix >= x) || (iy < 0) || (iy >= y))
        {
            return Color.PINK;
        }
        Cell color = w[iy][ix].resources.peek();
        if (null != color)
        {
            return color.color;
        }
        return w[iy][ix].color;
    }

    public Cell getCellAt(int ix, int iy)
    {
        if ((ix < 0) || (ix >= x) || (iy < 0) || (iy >= y))
        {
            return null;
        }
        return w[iy][ix];
    }

    private Cell buildCell(int seed, int ix, int iy)
    {
        final NonSplittableRandom rand = new NonSplittableRandom(jenkins_hash(seed, ix, iy));
        final Rand48 give = new Rand48(rand.getNext());
        Cell result = new Cell(this, give, ix, iy);
        result.color = Color.ORANGE;
        switch ((int)(give.getNext() * 3))
        {
        case 0:
            result.color = Color.WHITE;
            ++whites;
            break;
        case 1:
            result.color = Color.BLACK;
            ++blacks;
            break;
        case 2:
            result.color = Color.GRAY;
            break;
        default:
            break;
        }
        result.prior = 0;
        if (give.getNext() < DENSITY)
        {
            Cell resource = buildResource(rand, ix, iy);
            resource.parent = result;
            result.resources.add(resource); // This list should be empty, so this is good.
        }
        return result;
    }

    private Cell buildResource(NonSplittableRandom rand, int ix, int iy)
    {
        final Rand48 give = new Rand48(rand.getNext());
        Cell result = new Cell(this, give, ix, iy);
        result.color = Color.ORANGE;
        double next = give.getNext();
        if (next < 0.25) // 5 in 20 for blue
        {
            result.color = Color.BLUE;
            result.prior = 2;
            blues.add(result);
        }
        else if (next < 0.45) // 4 in 20 for red
        {
            result.color = Color.RED;
            result.prior = 3;
            result.energy = 4; // RULES: It takes 4 moves to destroy a RED.
        }
        else if (next < 0.6) // 3 in 20 for green
        {
            result.color = Color.GREEN;
            result.prior = 1;
            greens.add(result);
        }
        else if (next < 0.75) // 3 in 20 for cyan
        {
            result.color = Color.CYAN;
            result.prior = 3;
        }
        else if (next < 0.85) // 2 in 20 for yellow
        {
            result.color = Color.YELLOW;
            result.prior = 3;
        }
        else if (next < 0.9) // 1 in 20 for everything else
        {
            result.color = Color.MAGENTA;
            result.prior = 3;
        }
        else if (next < 0.95)
        {
            result.color = Color.LIGHT_GRAY;
            result.prior = 3;
        }
        else
        {
            result.color = Color.DARK_GRAY;
            result.prior = 3;
        }
        return result;
    }

    // This is the inefficient one-at-a-time hash published by Bob Jenkins.
    // This function will largely cover for our low quality PRNG.
    private static int jenkins_hash(int seed, int sx, int sy)
    {
        int hash = 0;

            // So, seed should be 32 bits... so do 4 rounds to use it.
            // sx and sy are only 16 bits, so 2 rounds each. And... unroll the loop.
            hash += seed & 0xFF;
            hash += hash << 10;
            hash ^= hash >>> 6;
            hash += (seed >> 8) & 0xFF;
            hash += hash << 10;
            hash ^= hash >>> 6;
            hash += (seed >> 16) & 0xFF;
            hash += hash << 10;
            hash ^= hash >>> 6;
            hash += (seed >> 24) & 0xFF;
            hash += hash << 10;
            hash ^= hash >>> 6;

            hash += sx & 0xFF;
            hash += hash << 10;
            hash ^= hash >>> 6;
            hash += (sx >> 8) & 0xFF;
            hash += hash << 10;
            hash ^= hash >>> 6;

            hash += sy & 0xFF;
            hash += hash << 10;
            hash ^= hash >>> 6;
            hash += (sy >> 8) & 0xFF;
            hash += hash << 10;
            hash ^= hash >>> 6;

        hash += hash << 3;
        hash ^= hash >>> 11;
        hash += hash << 15;

        return hash;
    }

    private static void assertType(JSONValue value, Class<?> type, String name, String expected) throws FatalException
    {
        if (false == type.isInstance(value))
        {
            throw new FatalException("In file \"" + value.sourceFile + " on line " + value.lineNumber + " at " + value.charNumber + "\n" +
                "\tError: \"" + name + "\" is not of the proper type. A(n) " + expected + " was expected.");
        }
    }

    Environment env;
    CallingContext context;

    public void initializeFrom(JSONValue here, ParserLogger logger) throws FatalException
    {
        env = new Environment();
        env.executor = new Executor();
        assertType(here, JSONObject.class, "file input", "object");
        JSONObject input = (JSONObject)here;
        FunctionDefinitions funDefs = new FunctionDefinitions();
        funDefs.buildDefaultFunctions(env.executor);
        for (Map.Entry<JSONString, JSONValue> entry : input.contents.entrySet())
        {
            switch(entry.getKey().getValue())
            {
            case "Initial State":
                assertType(entry.getValue(), JSONString.class, "Initial State", "string");
                env.initialState = ((JSONString)entry.getValue()).getValue();
                break;
            case "Global Functions":
                // NOTA BENE : We should always hit this before "States" because the Object uses a sorted Map.
                assertType(entry.getValue(), JSONString.class, "Global Functions", "string");

            {
                SymbolTable table = new SymbolTable();
                table.pushContext(); // We need a base context to operate on.
                table.frameInfo = env.executor.debugFrames;
                table.gs = funDefs.gs;
                table.addAll(funDefs.stdLibFunctions);
                ExecutorBuilder.finalizeTable(table);
                table.addedHere = funDefs.sharedFunctions;

                StringInput globalFuns = new StringInput(((JSONString)entry.getValue()).getValue());
                Lexer lexer = new Lexer(globalFuns, entry.getValue().sourceFile + " Global Functions", entry.getValue().lineNumber, entry.getValue().charNumber + 1);
                boolean result = Parser.ParseFunctions(lexer, table, env.executor, logger);

                if (false == result)
                {
                    throw new FatalException("The 'Global Functions' contained more than functions.");
                }
            }

                break;
            case "States":
                assertType(entry.getValue(), JSONObject.class, "States", "object");

                for (Map.Entry<JSONString, JSONValue> state : ((JSONObject)entry.getValue()).contents.entrySet())
                {
                    assertType(state.getValue(), JSONObject.class, state.getKey().getValue(), "object");
                    State staat = new State();
                    staat.name = state.getKey().getValue();
                    env.stateArchitypes.put(staat.name, staat);

                    GlobalGetterSetter vars = null;
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
                            Lexer lexer = new Lexer(stateData, staat.name + " Data", piece.getValue().lineNumber, piece.getValue().charNumber);

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

                                if (null != funDefs.sharedFunctions.funs.get(varName))
                                {
                                    throw new FatalException("Variable named " + varName + " cannot have the name of an intrinsic function or a global function.");
                                }

                                nameSet.add(varName);
                                varNames.add(varName);
                            }

                            vars = FunctionDefinitions.buildGetterSetter(varNames, staat.name);
                            StateFrame frame = new StateFrame();
                            frame.frameName = staat.name;
                            for (int i = 0; i < varNames.size(); ++i)
                            {
                                Integer var = Integer.valueOf(i);
                                frame.vars.put(varNames.get(i), var);
                                frame.varNames.put(var, varNames.get(i));
                            }
                            env.executor.stateDebugData.put(staat.name, frame);
                            staat.data.addAll(Collections.nCopies(varNames.size(), ConstantsSingleton.getInstance().DOUBLE_ZERO));
                        }

                            break;
                        case "Functions":
                            assertType(piece.getValue(), JSONString.class, "Functions", "string");

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

                            StringInput funs = new StringInput(((JSONString)piece.getValue()).getValue());
                            Lexer lexer = new Lexer(funs, staat.name + " Functions", piece.getValue().lineNumber, piece.getValue().charNumber + 1);
                            boolean result = Parser.ParseFunctions(lexer, table, env.executor, logger);
                           
                            if (false == result)
                            {
                                throw new FatalException("The State '" + staat.name + "' Functions contained more than functions.");
                            }

                            staat.update = getFunction("Update", 1, staat.name, table, env.executor);
                        }

                            break;
                        default:
                            logger.message("On line " + piece.getKey().lineNumber + " at " + piece.getKey().charNumber + ":\n" +
                                "\tIgnoring unknown thing \"" + piece.getKey().getValue() + "\" in state \"" + staat.name + "\".");
                            // Ignore me.
                            break;
                        }
                    }
                }

                if (false == env.stateArchitypes.containsKey(env.initialState))
                {
                    throw new FatalException("The Initial State '" + env.initialState + "' was never defined.");
                }

                break;
            default:
                logger.message("On line " + entry.getKey().lineNumber + " at " + entry.getKey().charNumber + ":\n" +
                    "\tIgnoring unknown thing \"" + entry.getKey().getValue() + "\" in base object.");
                // Ignore me.
                break;
            }
        }

        context = new CallingContext();
        context.environment = env;
        context.executor = env.executor;
        context.debugger = new DebuggerHook();
        context.debugging = true;
        context.fileOut = logger;
        context.world = this;

        greens.get(first_active).active = true;
        greens.get(first_active).machine.states.add(new LinkedList<State>());
        greens.get(first_active).machine.states.getFirst().add(new State(env.stateArchitypes.get(env.initialState)));

        everythingIsFucked = false;
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

}
