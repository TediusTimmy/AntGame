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

import java.util.List;

import StateEngine.StdLib.Error;
import StateEngine.StdLib.Fatal;
import StateEngine.StdLib.FindAll;
import StateEngine.StdLib.FindNearest;
import StateEngine.StdLib.Follow;
import StateEngine.StdLib.FreeAgents;
import StateEngine.StdLib.Grab;
import StateEngine.StdLib.Info;
import StateEngine.StdLib.Inform;
import StateEngine.StdLib.Inject;
import StateEngine.StdLib.Inventory;
import StateEngine.StdLib.Leave;
import StateEngine.StdLib.Left;
import StateEngine.StdLib.Look;
import StateEngine.StdLib.Precede;
import StateEngine.StdLib.Push;
import StateEngine.StdLib.Abandon;
import StateEngine.StdLib.CurrentEnergy;
import StateEngine.StdLib.Down;
import StateEngine.StdLib.Drop;
import StateEngine.StdLib.Rewind;
import StateEngine.StdLib.Right;
import StateEngine.StdLib.Enqueue;
import StateEngine.StdLib.EnterDebugger;
import StateEngine.StdLib.Rand;
import StateEngine.StdLib.Report;
import StateEngine.StdLib.Skip;
import StateEngine.StdLib.Task;
import StateEngine.StdLib.Teleport;
import StateEngine.StdLib.Transform;
import StateEngine.StdLib.Transition;
import StateEngine.StdLib.Up;
import StateEngine.StdLib.Warn;
import esl2.engine.Executor;
import esl2.parser.FrameDebugInfo;
import esl2.parser.FunctionPairs;

public class ExecutorBuilder extends esl2.parser.ExecutorBuilder
{

    public static void createDefaultFunctions(Executor executor, FunctionPairs funs, List<FrameDebugInfo> frameInfo)
    {
        esl2.parser.ExecutorBuilder.createDefaultFunctions(executor, funs, frameInfo);

            // First, overwrite the default logging functions with specialized ones.
        executor.functions.set(funs.funs.get("Fatal").intValue(), new Fatal());
        executor.functions.set(funs.funs.get("Error").intValue(), new Error());
        executor.functions.set(funs.funs.get("Warn").intValue(), new Warn());
        executor.functions.set(funs.funs.get("Info").intValue(), new Info());

        addFunction("Rand", new Rand(), 0, executor, funs, frameInfo);
        addFunction("CurrentEnergy", new CurrentEnergy(), 0, executor, funs, frameInfo);
        addFunction("FreeAgents", new FreeAgents(), 0, executor, funs, frameInfo);
        addFunction("Leave", new Leave(), 0, executor, funs, frameInfo);
        addFunction("Abandon", new Abandon(), 0, executor, funs, frameInfo);
        addFunction("Inventory", new Inventory(), 0, executor, funs, frameInfo);
        addFunction("EnterDebugger", new EnterDebugger(), 0, executor, funs, frameInfo);
        addFunction("Report", new Report(), 0, executor, funs, frameInfo);
        addFunction("Left", new Left(), 0, executor, funs, frameInfo);
        addFunction("Down", new Down(), 0, executor, funs, frameInfo);
        addFunction("Up", new Up(), 0, executor, funs, frameInfo);
        addFunction("Right", new Right(), 0, executor, funs, frameInfo);
        addFunction("Drop", new Drop(), 0, executor, funs, frameInfo);
        addFunction("Grab", new Grab(), 0, executor, funs, frameInfo);
        addFunction("Transform", new Transform(), 0, executor, funs, frameInfo);

        addFunction("Transition", new Transition(), 1, executor, funs, frameInfo);
        addFunction("Push", new Push(), 1, executor, funs, frameInfo);
        addFunction("Enqueue", new Enqueue(), 1, executor, funs, frameInfo);
        addFunction("Follow", new Follow(), 1, executor, funs, frameInfo);
        addFunction("Precede", new Precede(), 1, executor, funs, frameInfo);
        addFunction("Skip", new Skip(), 1, executor, funs, frameInfo);
        addFunction("Rewind", new Rewind(), 1, executor, funs, frameInfo);
        addFunction("FindNearest", new FindNearest(), 1, executor, funs, frameInfo);
        addFunction("Look", new Look(), 1, executor, funs, frameInfo);
        addFunction("Teleport", new Teleport(), 1, executor, funs, frameInfo);
        addFunction("Inform", new Inform(), 1, executor, funs, frameInfo);
        addFunction("Inject", new Inject(), 1, executor, funs, frameInfo);
        addFunction("FindAll", new FindAll(), 1, executor, funs, frameInfo);

        addFunction("Task", new Task(), 2, executor, funs, frameInfo);
    }

}
