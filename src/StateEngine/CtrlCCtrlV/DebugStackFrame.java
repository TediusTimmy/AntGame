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

import StateEngine.State;
import esl2.engine.StackFrame;
import esl2.input.Token;
import esl2.parser.FrameDebugInfo;

public class DebugStackFrame extends esl2.engine.DebugStackFrame
{

    public final State state;
    public final StateFrame stateFrame;

    public DebugStackFrame(StackFrame frame, Token callingToken, FrameDebugInfo info, State state, StateFrame stateFrame)
    {
        super(frame, callingToken, info);
        this.state = state;
        this.stateFrame = stateFrame;
    }

}
