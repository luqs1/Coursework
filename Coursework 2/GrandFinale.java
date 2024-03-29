import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static java.lang.Math.abs;

/*
Grand Finale - Luqmaan Ahmed

The approach I took to this exercise is slightly different from the Route A and Route B outlined in the guide. I wanted
to be able to conveniently overwrite the assumed optimal direction for a junction, as I saw it was useful for a loopy maze.

Therefore I made use of a (Hash)Map to store the heading for each junction. The important detail about this data structure
was that I could conveniently keep overwriting the heading for any junction, whereas the stack approach was a bit convoluted,
 as I noted from when I used it in Ex2 and Ex3.

But aside from discussing those fine points, another thing I introduced is a heuristicSelect instead of randomlySelect.
This means I chose a shorter path for loopy mazes than I would otherwise. The heuristic is based on the manhattan dist
to the target.

The robot deals with loopy mazes very well, uses heuristic to great effect. Changed reset so that it works on a new maze.
Works on repeat runs of the same maze, but doesn't further optimise, as if it could it should have done so from the start.

I chose not to explore the entire Maze in the first run. This has no effect on a Prim maze as there's only one path for those,
but could make a loopy maze have a very short path. For me, the manhattan heuristic does a very good job of finding a very
short route anyway, and any loops are trimmed in the second run.

The other approach I would have considered, is one where I implement the A* algorithm, as that would reliably give me the
shortest path, but I think my approach takes some of the advantages of the A* algorithm without doing as much computation.

I also tested against the blank maze and the hill maze, and the heuristic had a positive effect on those two as well. On
the first run of a hillMaze it possibly failed miserably to get a nice path but learned properly for subsequent runs. It
would solve the blank maze optimally from the start.
 */

public class GrandFinale {
    // private final String[] headings = {"North", "East", "South", "West"}; // For printing headings in tests.
    private final int[][] offsets = {{0,1,0,-1},{-1,0,1,0}}; // /Looks at the x and y offsets from the curr location depending on heading.
    // See manhattan for more.

    private Point target; // Stores target location once per maze
    private Point start; // Likewise.

    private RobotData robotData; // As before, except for altJunctions.
    private final Surroundings surroundings = new Surroundings(); // A class I made for working one which directions were passages and nonWalls.
    // made before knowledge of robotData.


    private int pollRun = 0; // Counts which "tick".
    private boolean explorerMode; // true for explorer, false for backtracking
    private boolean seerMode; // A mode for when the maze has been explored once at least.

    public void controlRobot(IRobot robot) {
        if ((robot.getRuns() == 0) && (pollRun == 0)) { // All the setup for a new maze.
            robotData = new RobotData();
            explorerMode = true;
            seerMode = false;
            start = robot.getLocation();
            target = robot.getTargetLocation();
            //robotData.altJunctions.put(robot.getLocation(), robot.getHeading()); // I think it becomes redundant.
        }

        else if ((robot.getRuns() > 0) & !seerMode) { // Switches into seerMode.
            seerMode = true;
            System.out.println("Going for the 2nd time or more.");
        }

        pollRun++;

        surroundings.refresh(robot);  //Works out the surroundings again for this tick.
        if (explorerMode) // in seerMode this is always true. The optimal route contains no deadEnds to kick into backtrack.
            exploreControl(robot); // so there's no point in adding ... || seerMode) to the if.
        else
            backtrackControl(robot);
        if (start.x == robot.getLocation().x && start.y == robot.getLocation().y) {
            // The start is a very special case as it is can be a "junction" when it has 2 nonWalls.
            if (seerMode) // Overrides to get the start direction from Map.
                robot.setHeading(robotData.altJunctions.get(surroundings.location));
            else // Updates the start cell as if it were a junction.
                robotData.altJunctions.put(surroundings.location, robot.getHeading()); // The new heading.
        }

    }

    private void exploreControl(IRobot robot){  //DEBUGGING: WORKS FINE just exploring.
        switch(surroundings.nonWall.numberOf){
            case 1:
                //System.out.println("Dead End");
                robot.face(deadEnd());
                explorerMode = false;  // Should start backtracking here.
                break;
            case 2:
                //System.out.println("Corridor/ Corner");
                robot.face(corridor()); //Corridor would also work with the crossroads method I've defined, but is more efficient this way.
                break;
            case 3:
            case 4:
                if (seerMode) { // Important, will just get best heading from the Map.
                    robot.setHeading(robotData.altJunctions.get(surroundings.location));
                }
                else if (surroundings.passage.numberOf == 0) { // Backtracks when there's a fully discovered junction ahead.
                    robot.face(IRobot.BEHIND);
                    explorerMode = false;
                }
                else {
                    //System.out.println("Crossroads or Junction");
                    robotData.recordJunction(robot); // Records Junction in RobotData.
                    robot.face(junction()); // Both Crossroads and Junctions are equivalent.
                }
                robotData.altJunctions.put(surroundings.location, robot.getHeading()); // The new heading.
                break;
        }
        //System.out.println(surroundings.nonWall.numberOf);
        //System.out.println(surroundings.passage.numberOf);
    }

    private void backtrackControl(IRobot robot){ // Backtracking mode, activates after a junction.
        switch (surroundings.nonWall.numberOf){
            case 1:
                robot.face(deadEnd());
                break;
            case 2:
                robot.face(corridor()); // 1 and 2 are equivalent to exploreControl bar the explorerMode change.
                break;
            case 3:
            case 4:
                if (surroundings.passage.numberOf > 0){  // This is the case when explorable passages are available.
                    explorerMode = true;  //Going down unexplored path.
                    robot.face(junction());
                    robotData.altJunctions.put(surroundings.location, robot.getHeading()); // The new heading.
                    /*
                    The line above is the reason I'm using a Map: I can overwrite the heading for a square once the robot
                    comes back to it. This would be much more contrived in a stack or array with Points.
                     */
                }
                else {
                    int arrivalHeading = robotData.searchJunction();
                    int headingShift = (arrivalHeading - IRobot.NORTH) -2;
                    headingShift = headingShift < 0? headingShift + 4: headingShift;
                    robot.setHeading(IRobot.NORTH + headingShift);
                    // Altogether does the equivalent of using a circular array for the headings.
                }
        }
    }

    public void reset() {
        robotData.resetJunctionCounter();
        pollRun = 0; // Important; allows the robot to run multiple mazes in a single execute.
    }

    private int randomlySelect(boolean[] array){  // This returns a random valid direction from an array of direction validity.
        ArrayList<Integer> chosen = new ArrayList<>();
        for (int i=0; i < 4; i++){
            if (array[i] && i != 2) // Doesn't check or include BEHIND.
                chosen.add(i);
        }
        int randNo = (int) Math.floor(Math.random()*chosen.size());
        //System.out.println(chosen.get(randNo));
        return (IRobot.AHEAD + chosen.get(randNo));
    }

    private int heuristicSelect(boolean[] array){ //This returns the best heuristic direction from an array of directions.
        /*
        This is a very important optimisation for a loopy maze as it prioritises routes that go right towards the target.
        It does however make the robots first run on Prims longer sometimes. As it had a net positive effect on the second
        run altogether, I made use of it.
        The heuristicSelect is only important for junctions, so randomlySelect is still used for corridors.
         */
        int best = Integer.MAX_VALUE; // Is definitely bigger than the biggest manhattan distance possible in the maze.
        int index = 0;

        for (int i = 0; i < 4; i++) { // Chooses the direction with the smallest manhattan dist to the target.
            int dist = manhattan(i);
            if (array[i] && i != 2 && dist < best) {
                best = dist;
                index = i;
            }
        }
        return (IRobot.AHEAD + index);
    }

    private int manhattan(int direction) { // This is why i need abs, to get an estimation for how good a cell is.
        /*
        This is also why I need the offsets[][], it allows me to obtain the coordinates for the cell the robot is looking at
        by getting the heading from the direction and then manipulating x and y coords based on that.
         */
        int heading = (surroundings.heading - IRobot.NORTH + direction) % 4; // Used modulus rather than ternary as before.
        int xDif = abs(surroundings.location.x + offsets[0][heading] - target.x);
        int yDif = abs(surroundings.location.y + offsets[1][heading] - target.y);
        return  xDif + yDif;
    }

    private int deadEnd() { // Looks through the Surroundings to find the only nonWallExit and returns it.
        for (int i=0; i < 4; i++){
            if (surroundings.nonWall.isType[i])
                return (IRobot.AHEAD + i);
        }
        return -1; // Error code
    }

    private int corridor(){ // Doesn't check for optimal passage as there is only direction to choose anyway.
        return randomlySelect(surroundings.nonWall.isType);
    }

    private int junction(){ // Does try to go for a passage if possible.
        if (surroundings.passage.numberOf == 0)
            return heuristicSelect(surroundings.nonWall.isType);
        return heuristicSelect(surroundings.passage.isType);
    }

    private static class RobotData {  //This is a modification of the original RobotData to remove wasteful storage.

        ArrayList<Integer> junctions = new ArrayList<>(); // Uses a Stack data structure instead of a Map to navigate the tree.
        public Map<Point, Integer> altJunctions = new HashMap<>();

        // My previous implementation had a Junction class which I removed, as it would only store heading, to save more space.

        public int junctionCounter() {  // No longer needs to be independently stored. Is now equivalent to the size of the list.
            return junctions.size();
        }

        public void resetJunctionCounter(){ // When maze resets.
            junctions = new ArrayList<>();
            //System.out.println(altJunctions);
        }

        public void recordJunction(IRobot robot){ // Pushes junction to the junctions stack.
            int heading = robot.getHeading();
            junctions.add(heading);
            //System.out.println(surroundings.location);
        }

        public int searchJunction(){ // Pops junction from stack.
            return junctions.remove(junctionCounter()-1);
        }

    }

    private static class Surroundings { /* A class detailing the passages, nonWalls and number of each after each move in the maze. It reduces code redundancy vastly.
    Implemented before reading about RobotData.*/
        public Point location;
        public int heading;
        public ExitType nonWall;
        public ExitType passage;
        public ExitType beenBefore;

        public static class ExitType { // A class that lets you have a counter and array for each type of exit.
            public int numberOf;  // The number of such exits.
            public boolean[] isType; // true if the exit is of ExitType starting with Forward going clockwise.

            public ExitType(int num, boolean[] array) {
                numberOf = num;
                isType = array;
            }
        }

        public void refresh(IRobot robot) { // runs every tick of controlRobot to get the surroundings again.
            nonWall = exitsCreate(robot, IRobot.WALL, true);
            passage = exitsCreate(robot, IRobot.PASSAGE, false);
            beenBefore = exitsCreate(robot, IRobot.BEENBEFORE, false);
            location = robot.getLocation();
            heading = robot.getHeading();
        }

        private ExitType exitsCreate(IRobot robot, int object, boolean invert) { // creates an ExitType
            /* The object is what is being checked for: like WALL, PASSAGE and BEENBEFORE.
             * invert will allow you to get a count and array of the squares that aren't of object.
             * */
            int counter=0;
            boolean[] array = new boolean[4];
            for (int i=0; i<4; i++) {
                if (invert != (robot.look(IRobot.AHEAD + i) == object)) {
                    counter++;
                    array[i] = true;
                }
            }
            return new ExitType(counter, array);
        }
    }
}