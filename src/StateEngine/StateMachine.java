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

package StateEngine;

import java.util.LinkedList;

import StateEngine.CtrlCCtrlV.CallingContext;
import esl2.engine.ConstantsSingleton;
import esl2.types.FatalException;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;

public final class StateMachine
{

    public final LinkedList<LinkedList<State>> states;
    public ValueType last;
    public String next;

    public StateMachine()
    {
        states = new LinkedList<LinkedList<State>>();
        last = ConstantsSingleton.getInstance().EMPTY_DICTIONARY;
    }

    public boolean update(CallingContext context) throws FatalException, TypedOperationException
    {
        State currentState = null;
        do
        {
            // State to process is the front of the list (queue) of the top of the stack.
            currentState = states.getLast().getFirst();
            last = currentState.update(context, last);
            next = currentState.next;
        }
        // If the current active state has changed, run its update function NOW.
        // The idea here is to support dispatch states: a state which enqueues an activity to run,
        // so that the activity has a stable name, even if its components are not.
        while ((false == states.isEmpty()) && (currentState != states.getLast().getFirst()));
        return false == states.isEmpty();
    }

}
