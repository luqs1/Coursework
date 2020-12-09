import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GrandFinale {
    private final String[] headings = {"North", "East", "South", "West"};
    private RobotData robotData;
    private final Surroundings surroundings = new Surroundings(); // A class I made for working one which directions were passages and nonWalls.
    // made before knowledge of robotData.

    private int pollRun = 0; // Counts which "tick".
    private boolean explorerMode; // true for explorer, false for backtracking
    private boolean seerMode;

    public void controlRobot(IRobot robot) {

        if ((robot.getRuns() == 0) && (pollRun == 0)) {
            robotData = new RobotData();
            explorerMode = true;
            seerMode = false;
            //robotData.altJunctions.put(robot.getLocation(), robot.getHeading()); // I think it becomes redundant.
        }

        else if ((robot.getRuns() > 0) & !seerMode) {
            seerMode = true;
            System.out.println("Going for the 2nd time or more.");
            pollRun = 0;
        }

        pollRun++;
        if (seerMode)
            robot.setHeading(robotData.altJunctions.get(robot.getLocation()));
        else {
            surroundings.refresh(robot);  //Works out the surroundings again for this tick.
            if (explorerMode)
                exploreControl(robot);
            else
                backtrackControl(robot);
        }
        robotData.altJunctions.put(surroundings.location, robot.getHeading());
        System.out.println(robot.getHeading());
    }

    private void exploreControl(IRobot robot){  //DEBUGGING: WORKS FINE just exploring.
        boolean recording = false;
        int direction = IRobot.AHEAD;
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
                if (seerMode) {
                    robot.setHeading(robotData.altJunctions.get(surroundings.location));
                }
                else if (surroundings.passage.numberOf == 0) {
                    robot.face(IRobot.BEHIND);
                    explorerMode = false;
                }
                else {
                    //System.out.println("Crossroads or Junction");
                    robotData.recordJunction(robot); // Records Junction in RobotData.
                    robot.face(junction()); // Both Crossroads and Junctions are equivalent.
                }
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
                    robotData.altJunctions.put(surroundings.location, robot.getHeading());
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
            return randomlySelect(surroundings.nonWall.isType);
        return randomlySelect(surroundings.passage.isType);
    }

    private class RobotData {  //This is a modification of the original RobotData to remove wasteful storage.

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

        public void printJunction(){  // Acts more like printHeading now.
            System.out.println(getJunction());
        }

        public Integer getJunction(){ //For Debugging, doesn't pop.
            return junctions.get(junctionCounter()-1);
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