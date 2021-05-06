package StateEngine.StdLib;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

import AntWorld.Cell;
import AntWorld.World;
import StateEngine.CtrlCCtrlV.CallingContext;
import esl2.types.ArrayValue;
import esl2.types.FatalException;
import esl2.types.StringValue;
import esl2.types.TypedOperationException;
import esl2.types.ValueType;
import esl2.types.Vector;
import esl2.types.VectorValue;

public class Path extends StandardUnaryFunction
{

    public static class Step implements Comparable<Step>
    {
        public int cost;
        public int toHere;
        public int dist;
        public Cell location;
        public StringValue direction;
        public Step parent;

        public Step(int cost, int dist, Cell location, StringValue direction, Step parent)
        {
            this.cost = cost;
            this.dist = dist;
            this.location = location;
            this.direction = direction;
            this.parent = parent;
        }

        @Override
        public int compareTo(Step o)
        {
            return cost - o.cost;
        }
    }

    private static StringValue UP = new StringValue("UP");
    private static StringValue DOWN = new StringValue("DOWN");
    private static StringValue LEFT = new StringValue("LEFT");
    private static StringValue RIGHT = new StringValue("RIGHT");

    private static int getCost(Color destColor, Color srcColor)
    {
        int cost = 1;
        // It costs 4 to move off of a RED.
        if (Color.RED == srcColor)
        {
            cost = 4;
        }
        // This needs to be a large number, but not so large it causes overflow.
        if (Color.MAGENTA == destColor)
        {
            cost = 1000000;
        }
        // It costs 4 to move onto a RED (but not 8 if one is moving OFF a RED and ONTO a different RED).
        else if (Color.RED == destColor)
        {
            cost = 4;
        }
        return cost;
    }

    public static Color localLook(Cell chello)
    {
        Color result = Color.ORANGE;
        if (true == chello.resources.isEmpty())
        {
            result = chello.color;
        }
        else
        {
            result = chello.resources.get(0).color;
        }
        return result;
    }

    private static void addNeighborMaybe(Cell temp, StringValue direction, Step parent, LinkedList<Cell> known, PriorityQueue<Step> frontier, int dx, int dy)
    {
        if (null != temp)
        {
            boolean contains = false;
            for (Cell cell : known)
            {
                if (cell == temp) // These should all be unique, so reference comparison should be right.
                {
                    contains = true;
                    break;
                }
            }
            if (false == contains)
            {
                known.add(temp);
                int stepCost = parent.toHere + getCost(localLook(temp), localLook(parent.location));
                int destCost = FindNearest.ManhattanDistance(temp.x, temp.y, dx, dy);
                Step step = new Step(stepCost + destCost, stepCost, temp, direction, parent);
                frontier.add(step);
            }
        }
    }

    public static void addNeighbors(World world, LinkedList<Cell> known, PriorityQueue<Step> frontier, Step parent, int dx, int dy)
    {
        int x = parent.location.x;
        int y = parent.location.y;
        Cell temp = world.getCellAt(x + 1, y);
        addNeighborMaybe(temp, RIGHT, parent, known, frontier, dx, dy);
        temp = world.getCellAt(x - 1, y);
        addNeighborMaybe(temp, LEFT, parent, known, frontier, dx, dy);
        temp = world.getCellAt(x, y + 1);
        addNeighborMaybe(temp, DOWN, parent, known, frontier, dx, dy);
        temp = world.getCellAt(x, y - 1);
        addNeighborMaybe(temp, UP, parent, known, frontier, dx, dy);
    }

    private static void unroll(Step cur, LinkedList<Step> list)
    {
        if (null != cur.parent)
        {
            unroll(cur.parent, list);
            list.add(cur);
        }
        return;
    }

    /*
     * A* search algorithm for generating a path from one location to the next.
     */
    public static LinkedList<Step> findPath(World world, int sx, int sy, int dx, int dy, int max)
    {
        if ((sx == dx) && (sy == dy))
        {
            return new LinkedList<Step>();
        }

        LinkedList<Cell> known = new LinkedList<Cell>();
        PriorityQueue<Step> frontier = new PriorityQueue<Step>();
        Step root = new Step(0, 0, world.getCellAt(sx, sy), null, null);

        known.add(root.location);
        addNeighbors(world, known, frontier, root, dx, dy);

        Step found = null;
        while (false == frontier.isEmpty())
        {
            Step front = frontier.poll();
            if ((front.location.x == dx) && (front.location.y == dy))
            {
                found = front;
                break;
            }
            int dist = FindNearest.ManhattanDistance(sx, sy, front.location.x, front.location.y);
            if (max > dist)
            {
                addNeighbors(world, known, frontier, front, dx, dy);
            }
        }

        LinkedList<Step> result = new LinkedList<Step>();
        if (null != found)
        {
            unroll(found, result);
        }
        return result;
    }

    @Override
    public ValueType fun(CallingContext context, ValueType arg) throws TypedOperationException, FatalException
    {
        if (arg instanceof VectorValue)
        {
            Vector dest = ((VectorValue)arg).value;
            int destx = (int)(context.cell.x + dest.x);
            int desty = (int)(context.cell.y - dest.y);
            LinkedList<Step> path = new LinkedList<Step>();
            if (FindNearest.ManhattanDistance(context.cell.x, context.cell.y, destx, desty) <= context.world.LOOK)
            {
                path = findPath(context.world, context.cell.x, context.cell.y, destx, desty, context.world.LOOK);
            }
            ArrayList<ValueType> result = new ArrayList<ValueType>();
            for (Step step : path)
            {
                result.add(step.direction);
            }
            return new ArrayValue(result);
        }
        else
        {
            throw new TypedOperationException("Path called without vector location to go to.");
        }
    }

}
