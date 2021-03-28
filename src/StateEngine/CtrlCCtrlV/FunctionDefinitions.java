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

import esl2.parser.FunctionPairs;
import esl2.parser.GetterSetter;
import esl2.parser.GlobalGetterSetter;

public final class FunctionDefinitions
{

    public final FunctionPairs stdLibFunctions;
    public final FunctionPairs sharedFunctions;

    public final GetterSetter gs;
    public GlobalGetterSetter mgs;

    public FunctionDefinitions()
    {
        stdLibFunctions = new FunctionPairs();
        sharedFunctions = new FunctionPairs();
        gs = new GetterSetter();
        mgs = null;
    }

    public void buildDefaultFunctions(Executor executor)
    {
        ExecutorBuilder.createDefaultFunctions(executor, stdLibFunctions, executor.debugFrames);
    }

    public static GlobalGetterSetter buildGetterSetter(ArrayList<String> variables, String name)
    {
        GlobalGetterSetter result = new GlobalGetterSetter();
        for (int i = 0; i < variables.size(); ++i)
        {
            result.getters.put(variables.get(i), new VariableGetter(i, name));
            result.setters.put(variables.get(i), new VariableSetter(i, name));
        }

        return result;
    }

    private FunctionDefinitions(FunctionDefinitions src)
    {
        stdLibFunctions = src.stdLibFunctions;
        sharedFunctions = new FunctionPairs();
        gs = new GetterSetter();
        mgs = src.mgs;
    }

    public FunctionDefinitions duplicate_base()
    {
        FunctionDefinitions result = new FunctionDefinitions(this);
        return result;
    }

}
