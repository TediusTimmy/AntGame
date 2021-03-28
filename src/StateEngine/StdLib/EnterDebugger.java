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

package StateEngine.StdLib;

import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import StateEngine.CtrlCCtrlV.CallingContext;
import StateEngine.CtrlCCtrlV.DebugStackFrame;
import esl2.engine.ConstantsSingleton;
import esl2.types.ArrayValue;
import esl2.types.DictionaryValue;
import esl2.types.Matrix;
import esl2.types.MatrixValue;
import esl2.types.ProgrammingException;
import esl2.types.Quaternion;
import esl2.types.QuaternionValue;
import esl2.types.DoubleValue;
import esl2.types.StringValue;
import esl2.types.ValueType;
import esl2.types.Vector;
import esl2.types.VectorValue;

public final class EnterDebugger extends StandardConstantFunction
{

    public static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();

    @Override
    public ValueType fun(CallingContext context)
    {
        return function(context);
    }

    public static ValueType function(CallingContext context)
    {
        if (false == context.debugging)
        {
            context.fileOut.message("Cannot enter debugger when not debugging!");
            return ConstantsSingleton.getInstance().DOUBLE_ZERO;
        }
        try
        {
            String line, prevLine = "";
            DebugStackFrame frame = ((DebugStackFrame)context.currentFrame.debug);
            StringBuilder builder = new StringBuilder();
            builder.append("In Function ");
            outputFrame(frame.depth, frame, builder);
            context.fileOut.message(builder.toString());
            line = queue.take();
            while (0 != "quit".compareTo(line))
            {
                // Empty lines repeat the previous command.
                if (true == line.isEmpty())
                {
                    line = prevLine;
                }

                switch (line.substring(0, Math.min(5, line.length())))
                {
                case "up":
                    if (null == frame.frame.prev)
                    {
                        context.fileOut.message("Already in top-most frame.");
                    }
                    else
                    {
                        frame = ((DebugStackFrame)frame.frame.prev.debug);
                        builder = new StringBuilder();
                        builder.append("In Function ");
                        outputFrame(frame.depth, frame, builder);
                        context.fileOut.message(builder.toString());
                    }
                    break;
                case "down":
                    if (frame == context.currentFrame.debug)
                    {
                        context.fileOut.message("Already in bottom-most frame.");
                    }
                    else
                    {
                        frame = ((DebugStackFrame)frame.next.debug);
                        builder = new StringBuilder();
                        builder.append("In Function ");
                        outputFrame(frame.depth, frame, builder);
                        context.fileOut.message(builder.toString());
                    }
                    break;
                case "bt":
                    for (DebugStackFrame i = frame; ; i = ((DebugStackFrame)i.frame.prev.debug))
                    {
                        builder = new StringBuilder();
                        outputFrame(i.depth, i, builder);
                        context.fileOut.message(builder.toString());
                        if (null == i.frame.prev)
                        {
                            break;
                        }
                    }
                    break;
                case "print":
                    if (7 > line.length())
                    {
                        boolean printComma = false;
                        builder = new StringBuilder();
                        builder.append("These are the variables in the current stack frame: ");
                        for (Entry<String, Integer> e : frame.info.args.entrySet())
                        {
                            if (true == printComma)
                            {
                                builder.append(", ");
                            }
                            builder.append(e.getKey());
                            printComma = true;
                        }
                        for (Entry<String, Integer> e : frame.info.locals.entrySet())
                        {
                            if (true == printComma)
                            {
                                builder.append(", ");
                            }
                            builder.append(e.getKey());
                            printComma = true;
                        }
                        if (null != frame.stateFrame)
                        {
                            for (Entry<String, Integer> e : frame.stateFrame.vars.entrySet())
                            {
                                if (true == printComma)
                                {
                                    builder.append(", ");
                                }
                                builder.append(e.getKey());
                                printComma = true;
                            }
                        }
                        context.fileOut.message(builder.toString());
                    }
                    else
                    {
                        String name = line.substring(6, line.length());
                        Integer i = frame.info.args.get(name);
                        if (null != i)
                        {
                            builder = new StringBuilder();
                            printValue(frame.frame.args.get(i.intValue()), context, builder);
                            context.fileOut.message(builder.toString());
                        }
                        else
                        {
                            i = frame.info.locals.get(name);
                            if (null != i)
                            {
                                builder = new StringBuilder();
                                printValue(frame.frame.locals.get(i.intValue()), context, builder);
                                context.fileOut.message(builder.toString());
                            }
                            else
                            {
                                if (null != frame.stateFrame)
                                {
                                    i = frame.stateFrame.vars.get(name);
                                    if (null != i)
                                    {
                                        builder = new StringBuilder();
                                        printValue(frame.state.data.get(i.intValue()), context, builder);
                                        context.fileOut.message(builder.toString());
                                    }
                                    else
                                    {
                                        context.fileOut.message("There is no variable >" + name + "< in scope.");
                                    }
                                }
                                else
                                {
                                    context.fileOut.message("There is no variable >" + name + "< in scope.");
                                }
                            }
                        }
                    }
                    break;
                default:
                    context.fileOut.message("Did not understand command >" + line + "<.");
                    context.fileOut.message("Known commands are:");
                    context.fileOut.message("\tquit - exit the debugger and continue running");
                    context.fileOut.message("\tbt - give a back trace to the current stack frame");
                    context.fileOut.message("\tup - go up one calling stack frame");
                    context.fileOut.message("\tdown - go down one callee stack frame");
                    context.fileOut.message("\tprint - print the variables accessible in this stack frame");
                    context.fileOut.message("\tprint variable_name - print the value in the given variable");
                }

                prevLine = line;
                line = queue.take();
            }
        }
        catch (InterruptedException e)
        {
            throw new ProgrammingException("Someone interrupted me: " + e.getLocalizedMessage());
        }
        return ConstantsSingleton.getInstance().DOUBLE_ZERO;
    }

    public static void outputFrame(int location, esl2.engine.DebugStackFrame frame, StringBuilder build)
    {
        build.append("#" + location + ": " + frame.info.frameName + " from file " +
            frame.callingToken.sourceFile + " on line " + frame.callingToken.lineNumber + " at " + frame.callingToken.lineLocation);
    }

    public static void printVec(Vector vec, StringBuilder build)
    {
        build.append(String.format("[%1$.16e, %2$.16e, %3$.16e]", vec.x, vec.y, vec.z));
    }

    public static void printQuat(Quaternion quat, StringBuilder build)
    {
        build.append(String.format("[%1$.16e, %2$.16e, %3$.16e, %4$.16e]", quat.s, quat.i, quat.j, quat.k));
    }

    public static void printMat(Matrix mat, StringBuilder build)
    {
        build.append(String.format("[%1$.16e, %2$.16e, %3$.16e; %4$.16e, %5$.16e, %6$.16e; %7$.16e, %8$.16e, %9$.16e]",
            mat.a11, mat.a12, mat.a13, mat.a21, mat.a22, mat.a23, mat.a31, mat.a32, mat.a33));
    }

    public static void printValue(ValueType val, CallingContext context, StringBuilder build)
    {
        if (null != val)
        {
            if (val instanceof DoubleValue)
            {
                build.append(String.format("%1$.16e", ((DoubleValue)val).value));
            }
            else if (val instanceof VectorValue)
            {
                printVec(((VectorValue)val).value, build);
            }
            else if (val instanceof QuaternionValue)
            {
                printQuat(((QuaternionValue)val).value, build);
            }
            else if (val instanceof MatrixValue)
            {
                printMat(((MatrixValue)val).value, build);
            }
            else if (val instanceof StringValue)
            {
                build.append("\"" + ((StringValue)val).value + "\"");
            }
            else if (val instanceof ArrayValue)
            {
                build.append("{ ");
                boolean printComma = false;
                for (ValueType v : ((ArrayValue)val).value)
                {
                    if (true == printComma)
                    {
                        build.append(", ");
                    }
                    else
                    {
                        printComma = true;
                    }
                    printValue(v, context, build);
                }
                build.append(" }");
            }
            else if (val instanceof DictionaryValue)
            {
                build.append("{ ");
                boolean printComma = false;
                for (Entry<ValueType, ValueType> e : ((DictionaryValue)val).value.entrySet())
                {
                    if (true == printComma)
                    {
                        build.append(", ");
                    }
                    else
                    {
                        printComma = true;
                    }
                    printValue(e.getKey(), context, build);
                    build.append(":");
                    printValue(e.getValue(), context, build);
                }
                build.append(" }");
            }
            else
            {
                build.append("Type not understood (Bug in AntToy, not your code).");
            }
        }
        else
        {
            build.append("Variable is undefined (Normal), or a collection contains a NULL (Bug in AntToy, not your code).");
        }
    }

}
