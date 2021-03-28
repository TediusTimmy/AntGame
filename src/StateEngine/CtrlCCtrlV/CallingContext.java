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

package StateEngine.CtrlCCtrlV;

import java.util.ArrayList;

import AntWorld.Cell;
import AntWorld.World;
import StateEngine.Environment;
import StateEngine.State;
import esl2.input.Token;
import esl2.parser.ParserLogger;

public class CallingContext extends esl2.engine.CallingContext
{

    private final ArrayList<State> stateStack;

    public CallingContext()
    {
       stateStack = new ArrayList<State>();
    }

    public ParserLogger fileOut; // DebugPrint writes to here.
    public Environment environment;
    public World world;
    public Cell cell;

    public State top()
    {
        return stateStack.get(stateStack.size() - 1);
    }

    public void pushState(State state)
    {
        stateStack.add(state);
    }

    public void popState()
    {
        stateStack.remove(stateStack.size() - 1);
    }

    @Override
    public DebugStackFrame getFrame(int location, Token callingToken)
    {
        return new DebugStackFrame(currentFrame, callingToken, executor.debugFrames.get(location),
            top(), ((Executor)executor).stateDebugData.get(top().name));
    }

    public void copyFromHere(CallingContext src)
    {
        // Given that this only copies public members, it is a method of convenience alone.
        // ESL2::Engine::CallingContext
        // Ignore currentFrame, it should be null.
        executor = src.executor;
        debugger = src.debugger;
        // Engine::CallingContext
        fileOut = src.fileOut;
        environment = src.environment;
        // Don't touch the state stack. It should be empty.
    }

}
