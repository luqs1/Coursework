import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;

/*
Luqmaan Ahmed Exercise 3

NOTE
My exercise 2 and 3 are almost the same. Thus I will contrast Ex2/3 with Ex1.

For the record, I think that If i didn't record every junction as i did in Ex2, but recorded junctions selectively based
on the number of BeenBefore cells, there would be a possibility of that program failing on loopy mazes.

The only addition from Ex2 to Ex3 is an optimisation: Specifically lines 71-80 were altered so that the robot would start
backtracking if it came across a completely discovered junction. This was verified to be a positive change that reduced
the mean and the variance of steps taken. (Observed statistically)

END NOTE

My robot in Exercise 1 could only store 1 heading per cell on the maze, this meant that if the robot followed a loop and
started backtracking, it could only backtrack with either the more recent heading, or the older heading.

My exercise 2/3 program used a stack and recorded every junction it came across. As it was naive to where it was
on the maze, it was able to store multiple headings for each cell. This meant that when backtracking it would always
be able to backtrack the "long way" out which guarantees that it finds all the passages that are possible if the maze was
solvable.

I think a good analogy to understand this is one explorer marking which way they came on each tile of a maze, and another
leaving behind a string (literally string) to follow back. The former would have to choose to mark one heading or the other
if they come back to a junction whilst exploring, whereas the latter would simply have string that crossed on itself,
and would be able to tell which heading was relevant to follow when backtracking by following the string at the top.
(Equivalent to how far back/ up in the stack.)
 */

public class Ex3 {
  private EfficientRobotData robotData;
  private final Surroundings surroundings = new Surroundings(); // A class I made for working one which directions were passages and nonWalls.
  // made before knowledge of robotData.

  private int pollRun = 0; // Counts which "tick".
  private boolean explorerMode; // true for explorer, false for backtracking

  public void controlRobot(IRobot robot) {

    if ((robot.getRuns() == 0) && (pollRun == 0)) {
      robotData = new EfficientRobotData();
      explorerMode = true;
    }
    pollRun++;
    surroundings.refresh(robot);  //Works out the surroundings again for this tick.

    if (explorerMode)
      exploreControl(robot);
    else
      backtrackControl(robot);
  }

  private void exploreControl(IRobot robot){  //DEBUGGING: WORKS FINE just exploring.
    int direction = IRobot.AHEAD;
    switch(surroundings.nonWall.numberOf){
      case 1:
        //System.out.println("Dead End");
        direction = deadEnd();
        explorerMode = false;  // Should start backtracking here.
        break;
      case 2:
        //System.out.println("Corridor/ Corner");
        direction = corridor(); //Corridor would also work with the crossroads method I've defined, but is more efficient this way.
        break;
      case 3:
      case 4:
        if (surroundings.passage.numberOf == 0) {
          robot.face(IRobot.BEHIND);
          System.out.println("activated");
          explorerMode = false;
        }
        else {
          //System.out.println("Crossroads or Junction");
          direction = junction(); // Both Crossroads and Junctions are equivalent.
          robotData.recordJunction(robot); // Records Junction in RobotData.
        }
        break;
    }
    //System.out.println(surroundings.nonWall.numberOf);
    //System.out.println(surroundings.passage.numberOf);
    robot.face(direction);
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
    pollRun = 0;
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

  private static class EfficientRobotData {  //This is a modification of the original RobotData to remove wasteful storage.

    ArrayList<Integer> junctions = new ArrayList<>(); // Uses a Stack data structure instead of a Map to navigate the tree.

    // My previous implementation had a Junction class which I removed, as it would only store heading, to save more space.

    public int junctionCounter() {  // No longer needs to be independently stored. Is now equivalent to the size of the list.
      return junctions.size();
    }

    public void resetJunctionCounter(){ // When maze resets.
      junctions = new ArrayList<>();
    }
    public void recordJunction(IRobot robot){ // Pushes junction to the junctions stack.
      int heading = robot.getHeading();
      junctions.add(heading);
      //System.out.println(robot.getLocation());
    }

    public int searchJunction(){ // Pops junction from stack.
      return junctions.remove(junctionCounter()-1);
    }

  }
  private static class Surroundings { /* A class detailing the passages, nonWalls and number of each after each move in the maze. It reduces code redundancy vastly.
    Implemented before reading about RobotData.*/

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