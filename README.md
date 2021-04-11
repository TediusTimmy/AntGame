AntToy
======

Ant Toy - A toy that looks like ants.

What IS this? Ant Toy is a "game" or "toy" about writing AI.  
So, I was watching an Economics Explained YouTube video about the economy of Eve Online. In the second episode, they go over how botting exasperates inflation in the Eve economy. In Eve, money is created when players perform mercenary missions, and some players have AI bots fly mission after mission in order to increase their wealth, which increases the money supply and causes inflation. Of course, CCP Games tries to limit botting. So, I had this idea: what if I made a game that was Eve, but with botting? What if the idea of the game was botting? Of course, at that point, it would be more like Elite with botting, but I digress. So, you make a game whose point is to play itself, like a Clicker game. So, Eve Clicker was born. I started working on that, and have the origins of a procedurally-generated universe on my hard drive. Making the game meter-scale, however, reminded me of my own insignificance in the universe and how big and empty it all is and how insignificant I am. So, I decided to cut down the scope and produce something that would be smaller and prove out the AI mechanics of what I envisioned for Eve Clicker. That became Ant Game, which I immediately started calling Ant Toy, because it's more of a toy than a game. So this is a toy, to program an AI to collect resources and expand its knowledge about the universe. It's a game about programming that plays itself, so not a game at all.

# Rules
The idea behind Eve Clicker started with the mechanics of a Clicker game: the player starts manually performing the tasks of resource acquisition and advancement, and advancement produced tools to automate further advancement. Ant Toy cuts out that beginning portion: the player starts with automated tools of advancement and can never escape them. The player's only input is at the beginning, in deciding what automated program to proceed with.

The game starts with the player controlling a GREEN and having an unTasked BLUE. What a GREEN is will be described in more detail later, but think of it like a star base or space station. As for a BLUE, it is like a drone space ship, it is an agent to go out and actually do things in the world. The player starts with a resource to organize exploring the world, and a resource to actually explore the world with. The world is a field of WHITE, GRAY, and BLACK locations, with some resources scattered on them.

The AI is limited by what it can "see". In order to make plans about places it cannot see, it has to go there. By default, the AI can "see" everything within a Manhattan distance of twenty, and a BLUE can perform one hundred actions, like moving, before having to return to a GREEN to recharge. In addition, the likelihood of any given location to have a resource is one in a hundred. These three values can be changed in the settings, however.

## Resources
* GREEN - This is a "base". It recharges BLUEs when they stop here. All BLUEs have to be Tasked, initially, from a GREEN. So, in order to have more agents, a BLUE has to go out, find another BLUE, and carry it back to the GREEN. GREENs can be picked up and moved, but the BLUE won't recharge while in transit. A GREEN that is unTasked may be Tasked by a BLUE: the BLUE has relayed orders to the GREEN. About 3 in 20 resources will be GREEN.
* BLUE - This is a "drone". It goes out and does stuff. It can be picked up and moved, but not when it has Tasking of its own. About 1 in 4 resources will be BLUE.
* RED - This is a nuisance. REDs cost four actions to move onto and off of, and require four 'Action' Commands to remove. About 1 in 5 resources will be RED.
* MAGENTA - This is an obstruction. MAGENTAs cannot be moved onto, unless the BLUE is carrying a CYAN. Then, the MAGENTA and CYAN destroy one another. About 1 in 20 resources will be MAGENTA.
* CYAN - This is a useless resource. It's needed to clear MAGENTA, but ought to be more numerous than MAGENTA, so the rest are clutter. About 3 in 20 resources will be CYAN. One ought to always be able to remove all MAGENTAs.
* YELLOW - This is a portal or teleporter. It allows for instant travel across the world. It can be picked up and moved, but it can't teleport while in transit. About 1 in 10 resources will be YELLOW.
* LIGHT GRAY - This is an unpredictable nuisance. This has a one in five chance of teleporting the BLUE to a random location in the world when interacted with ('Action' Command at that location). This may involve that BLUE becoming unusable due to running out of energy. About 1 in 20 resources will be LIGHT GRAY.
* DARK GRAY - This is a dangerous nuisance. This has a one in four chance of destroying the BLUE when interacted with ('Action' Command at that location). About 1 in 20 resources will be DARK GRAY. You should always have more BLUEs than will be destroyed.

## Goal
The overall "goal" of the game is to turn all of the locations to either BLACK or WHITE. All of them BLACK, or all of them WHITE. When `call Transform()` is performed on a location with no resources, it transforms the location. BLACK and WHITE both transform to GRAY. GRAY will transform to BLACK or WHITE, with 50% probability. When all are BLACK or all are WHITE, you win.  
One loses by failing to make a move for a hundred turns. If a BLUE hasn't moved, picked up something, or dropped something in a hundred turns, then the AI that you programmed probably isn't working towards its goal any more, and that is a losing position.

# Superdeterminism
The game is super determined. For a given random seed, a given location on the board will always be the same color. It will always spawn the same extra at that location. Any given BLUE or GREEN will always give the same sequence of random numbers. Each LIGHT GRAY and DARK GRAY will always either be harmless or perform their effect based on their location and the random seed. Randomness is overdetermined at the beginning of the game.

This allows the player to win by skill, not by dumb luck. If the player can learn the outcome of "random events", then the player excels by robustly handling new and unique random events, but not by getting lucky with any single random event. Ant Toy is a toy of skill, not a toy of dumb luck.

# AI implementation
The mechanism for implementing AI in Ant Game is that of a pushdown automaton, though it is powerful enough to represent a full Turing machine, and you may only need the power of a finite state machine, like a Moore or a Mealy machine.  
The machine operates on a stack of queues of states, where the terms stack and queue are used loosely. The idea is that the player can create a list (or queue) of activities, represented by states, that perform some action. If I want to close the door, I am going to stand up, walk to the door, and then close the door. Hopefully, the complexity of each state (stand up, walk to location, close door) is less than one state which performs all three. Managing complexity is a very important concept in modern software engineering. The purpose of the stack then is to respond to events. If people are loud outside my door, I am going to stand up, walk to the door, close the door, walk back to my desk, and sit back down. I am going to push this list of actions onto the stack, run through it, then return to what I am doing.

There's the concept: the player defines a list of activities to perform an action, and sometimes events cause a new list of activities to take precedence. Afterwards, the original action can be returned to.  
As the underlying language that describes what a state will do each turn is Turing-complete, only one state is needed. It would just be an overly complex state. The state mechanism allows the player to manage the complexity of their states, and hopefully allow them to reuse those less complex states. In addition, the looseness of the stack and queue abstractions hopefully don't limit the player's creativity in solving problems.

# Input Format
The input file is a JSON-like file, with some incompatible changes from JSON that make JSON readers reject it.

Firstly, the file can contain shell-style `# comments`, as well as C-family `// comments` and `/* comments */`.  
Secondly, the string format allows characters that normally should be escaped, like a newline.
Thirdly, there is no way to escape characters in a string.

The JSON file describes a JSON object.  
This JSON object has three properties:
* "Initial State" - The name of the initial State for the first GREEN that the player has.
* "Global Functions" - A string of functions that are available to all States to use.
* "States" - A JSON object, where each property is the name of a State that can be utilized by the player. Each State is, in and of itself, also a JSON object.
  * "Data" - A string of space-delimited variable names for variables that will be persisted across calls within that State. All variables are initialized to zero.
  * "Functions" - A string of functions that are used within this State alone. This group of functions must define one function:
    * "Update" - This function is called every step of the simulation to update the internal state of the State, now that it can do something different. This function takes one argument, and that value is used to communicate between States. Most of the time, the argument to this function is what it returned in the previous call. If this State Leaves, then the return value is the argument to the next State's Update function.

# The Language
I just decided to use ESL2, which is also located on this GitHub somewhere. There are some changes and additions, described below.

## Comments
`"#" To end of line`  
`"(*" Comment "*)"`  
I added the ability to use old-style Pascal comments, because it really goes with the rest of the language. The older, shell-style comments have always been around, but I don't think that I documented it anywhere. Now it is documented here. Old-style Pascal comments really round out the language as being valid even when newlines are replaced with spaces.

## Strings
It has bitten me, myself, multiple times that the only valid string delimiters are `'single quotes'`. Double quote marks are read by the JSON reader as the end of a JSON string, and there is no way to escape them.

## Standard Library Additions
* double Abandon () # Pops the current State queue from the stack. Abandons the current stack frame.
* double CurrentEnergy () # Returns the current amount of energy a BLUE has. Is an error for a GREEN to call this.
* double Down () # Returns one. Schedule a Down command.
* double Drop () # Schedule a Drop command. Returns one if something was dropped, zero otherwise.
* double Enqueue (array) # Returns one. Argument should be an array of string. Performs the string version of the function over the array.
* double Enqueue (string) # Returns one. Argument should be a State name. Queues up the named State to run later by placing it at the end of the queue, where it belongs.
* double EnterDebugger () # Returns zero. Function described below.
* array FindAll (string) # Given the color name, return an array of vectors of all visible instances of color, within visible range, relative to the caller.
* array FindNearest (string) # Given the color name, return an array of vectors of the nearest instances of color, within visible range, relative to the caller.
* double Follow (array) # Returns one. Argument should be an array of string. "Queues" up the named States to follow the current State in their array order.
* double Follow (string) # Returns one. "Queues" up the named State to follow the current State.
* double FreeAgents () # Returns the count of BLUEs or GREENs on the current location that can be Tasked. GREENs get a BLUE count and vice-versa.
* array GetInfo () # Returns an array of everything the cell has been Informed about since the last update.
* string GetName() # Returns the name of the State that is currently being processed.
* double Grab () # Schedule a Grab command. Returns one if there is something to grab, throws an error otherwise.
* array Inform (value) # Returns one. Passes a piece of data to every active thing in the current location. Inform is a command.
* double Inject (array) # Returns one. Argument should be an array of string. Insert a new queue as the second on the stack and add these States to it in their array order.
* double Inject (string) # Returns one. Insert a new queue as the second on the stack and add this State to it.
* string Inventory () # Returns the color of the currently held item, or an empty string. GREEN always gets empty string.
* double Leave () # Removes the current State from the State queue. If the queue is now empty, pops the queue from the stack. If the stack is empty, this is an error.
* double Left () # Returns one. Schedule a Left command.
* double Look (vector) # Report the top-most thing that can be "seen" at the given location relative to this. BLUEs and GREENs don't see themselves. The value "ORANGE" is beyond the edge of the universe. The value "PINK" is beyond what is visible. GREEN is always seen first, then BLUE, then everything else in the reverse order they were moved onto the location.
* double Precede (array) # Returns one. Argument should be an array of string. Add the named States to the queue before the current State in their array order.
* double Precede (string) # Returns one. Add the named State to the queue before the current State.
* double Push (array) # Returns one. Argument should be an array of string. Push a new queue to the top of the stack and add these States to it in their array order.
* double Push (string) # Returns one. Push a new queue to the top of the stack and add this State to it.
* double Rand () # Returns the next random number from the cell's personal random number generator.
* double Report () # Report for Duty/Orders. The cell is deactivated and becomes a free agent. This is an error for a BLUE to report not on a GREEN. Report is a command.
* double Rewind (string) # Returns one. Rewind the stack to a stack frame that begins with the named State. It is an error if no such State is found.
* double Right () # Returns one. Schedule a Right command.
* double Skip (string) # Returns one. Pop States from the current queue until the named State is found. It is an error if no such State is found.
* double Task (string, value) # Give a free agent in the current location the starting named State with starting data. Return one if an agent was Tasked and zero if not. Task is a command.
* vector Teleport (vector) # If BLUE is on a YELLOW, jump to the YELLOW nearest to the given location relative to the current one. Return where that location is relative to where you now are. Teleport is a Command.
* double Transform () # Returns one. Schedule a Transform command.
* double Transition (string) # Returns one. Removes the current State from the queue and replaces it with the named State.
* double Up () # Returns one. Schedule an Up command.

A visualization of the AI engine:  
```
  +-+
  |#|
+-+-+-+-+-+-+-+-+
|@|1|2|3|4|5|6|$|
+-+-+-+-+-+-+-+-+
  |7|8|
  +-+-+-+-+
  |9|A|B|C|
  +-+-+-+-+
```
The numbers `1-9` and letters `A-C` represent States that are set to execute eventually, with `1` being the state that is currently being executed. Conceptually, the natural insertion points are marked `#` and `$`. Location `$` represents where something would be added to a queue, and `#` represents where something would be added to a stack. So, Enqueue inserts States at `$`, and Push inserts States at `#`. There are other places that we can insert States, though, from the perspective of the currently executing State. Preceding `1` with a State at `@` is like Pushing it, but if that State calls `Abandon`, then `1` will not be returned to. Injecting a State between `1` and `7` will be like Enqueueing it, but if `Abandon` is called, it will still be run. Finally, a State can Follow `1` by being put between `1` and `2`, which really doesn't fit in with the rest of these. (Humorously, Inject was implemented as an afterthought, due to building this diagram, and me wanting an orthogonal command set.)

### Commands
All commands, except Report, expend energy to perform. It takes four energy to move onto or off of a RED. All other commands take one energy. These are the commands:
* Up - Move up on the screen in the universe. Movement is with respect to what the player sees, not the global coordinates of the universe.
* Down - Move down on the screen in the universe.
* Left - Move left on the screen in the universe.
* Right - Move right on the screen in the universe.
  * MAGENTA cannot be moved onto.
  * Moving onto a MAGENTA while carrying a CYAN destroys the MAGENTA and CYAN. The move succeeds.
  * It costs energy to attempt a move that cannot be performed, whether trying to move onto MAGENTA or out of bounds.
  * A BLUE has to successfully move to reset the lose counter.
* Grab - If colocated with something that can be picked up, pick it up. If not, this is an error.
  * Grabbing a LIGHT GRAY will teleport the BLUE to a random location one in five times. These are unpredictable.
  * Grabbing a DARK GRAY will destroy the BLUE one in four times. These are dangerous.
  * You cannot pick up an active BLUE or GREEN and trying to do so will not do anything and take no energy.
* Transform
  * A BLUE has to transform a RED four times to remove it. It is a nuisance.
  * If on a BLACK or WHITE, it changes to a GRAY.
  * If on a GRAY, it changes to BLACK or WHITE with 50% probability.
  * It is an error to transform with something else obstructing the transformation.
* Drop - If something is held, drop it. If not holding anything, no energy is used and the turn is wasted.
* Report - Report for Duty/Orders. The cell is deactivated and becomes a free agent. This is an error for a BLUE to report not on a GREEN.

### The Debugger
The debugger is a simple translation to the debug console of a console debugger. It is very primitive. One enters commands to the debugger in the input box, and sends them to the debugger with by either pressing enter, or by clicking on the button. At any time, you can type "help" and it will tell you that it doesn't understand what "help" is and then list what it does understand.
* Debugger commands
  * quit - exits the debugger
  * bt - prints out a backtrace of the stack frames that led to the debugger being entered
  * up - moves up in the call stack: moves examination to the stack frame of the function that called the current function
  * down - moves down in the call stack: moves examination to the function that was called
  * print - prints out the names of all variables that are visible in this stack frame
  * print <variable> - prints out the value of the named variable

That's it! This should suffice for most rudimentary debugging of program errors.

## Commands vs Library Functions
What is the point between this strange distinction between Commands and other things that happen in the Standard Library. In general, Commands are things that interface with the external world. The Standard Library changes the internal world of the AI.

In normal AI tasks, the AI commands something (a fin to cant, the engine to gimbal) and then it has to wait, enter a new update cycle, and evaluate the results of what was commanded before. Sometimes there's mechanical failure, or a physical limitation has been reached, or ... everything happened correctly. At this point, though, the AI needs to reevaluate the goal and how it intends to achieve that goal, and make a new command. That's what I'm trying to emulate here. Commands are meant to emulate those things that need real time to actually happen in the real world. And, Report does meet that distinction, even if it seems like a cheap cop-out by me to make sure that Report doesn't occur in tandem with a move.
