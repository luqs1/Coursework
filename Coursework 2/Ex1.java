import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
Luqmaan Ahmed Exercise 1

"Question. Discuss your
handling of passageExits, nonWallExits and the four controller methods. How did you
ensure your program is efficient?"

I realised early on that nonWallExits and then passageExits (and finally beenBefore) were repeatedly used in different
parts of the program. Therefore I created a surroundings object that computed all of these values whilst also remembering
what each exit was. This streamlined a lot of my now exploreControl code.
My abstraction resulted in the ExitType class which has organised my approach to the exits, and the exitsCreate which
removes repeated code.

I have made similar abstractions within exploreControl with the randomlySelect function. The function chooses from a set
of valid directions made available by the ExitType.isType array I created. So I can try to run in on passages, and then
run it on nonWalls.

"Question. Discuss your chosen implementation of the
RobotData class and how it is utilised by the backtracking. Discuss your reasoning for
how you’ve designed your explorer and backTracker."

The RobotData class contains a Map from Point objects to Junctions. I realise that really, I could just store Headings
instead of the Coordinates again in the Junction, but this is really just a preference between doing additional
calculations to recreate a Junction everytime one is needed, or whether to just store the junctions.

The backtracking requests the Junctions heading using a Point, and then uses a modular arithmetic method to reverse the
heading. It then uses the convenient setHeading function to trace backwards. One thing I did was to reuse
exploreControl within backtrackControl selectively. I did this because the backtrackControl is a headingController and
exploreControl uses relative directions. explorerMode remained false which was the important distinction.

"Question.  Is there repeated code anywhere?
Could some functionality have been combined in a better way?"

I think my use of the surroundings object reduced repetition massively. The fact that I reused exploreControl is similar.
I think possibly robotData and surroundings could have been combined in a more interesting way, but they serve their
purpose adequately well.

"Question. Worst case analysis?"

In a non-loopy l*h solvable maze, this robot will always reduce the number of unexplored squares. An upper bound to the
number of steps would be l*h*n, where n is the number of junctions. P.S. This is not the lowest upper bound.
 */

public class Ex1 {
  private RobotData robotData;
  private final Surroundings surroundings = new Surroundings(); // A class I made for working one which directions were passages and nonWalls.
  // made before knowledge of robotData.

  private int pollRun = 0;
  private boolean explorerMode; // true for explorer, false for backtracking

  public void controlRobot(IRobot robot) {

    if ((robot.getRuns() == 0) && (pollRun == 0)) {
      robotData = new RobotData();
      explorerMode = true;
    }
    pollRun++;
    surroundings.refresh(robot);  //Works out the surroundings again for this tick.

    if (explorerMode)
      exploreControl(robot);
    else
      backtrackControl(robot);
  }

  private void exploreControl(IRobot robot){
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
        //System.out.println("Crossroads or Junction");
        direction = junction(); // Both Crossroads and Junctions are equivalent.
        robotData.recordJunction(robot.getLocation(), robot); // Records Junction in RobotData.
        robotData.printJunction(robotData.getJunction(robot.getLocation()));
        break;
    }
    //System.out.println(surroundings.nonWall.numberOf);
    //System.out.println(surroundings.passage.numberOf);
    robot.face(direction);
  }

  private void backtrackControl(IRobot robot){
    switch (surroundings.nonWall.numberOf){
      case 1:
        robot.face(deadEnd());
        break;
      case 2:
        robot.face(corridor()); // 1 and 2 are equivalent to exploreControl bar the explorerMode change.
        break;
      case 3:
      case 4:
        if (surroundings.passage.numberOf > 0){
          explorerMode = true;  //Going down unexplored path.
          robot.face(junction());
        }
        else {
          int arrivalHeading = robotData.searchJunction(robot.getLocation());
          int headingShift = (arrivalHeading - IRobot.NORTH) -2;
          headingShift = headingShift < 0? headingShift + 4: headingShift;
          robot.setHeading(IRobot.NORTH + headingShift);
          // Altogether does the equivalent of using a circular array.
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

  private class RobotData {
    public int junctionCounter;

    public class Junction extends Point {
      public int arrivalHeading;

      public Junction(int x, int y, int heading) {
        super(x, y);
        arrivalHeading = heading;
      }

      public String toString() {
        return "Junction{" +
                "arrivalHeading=" + arrivalHeading +
                super.toString() + '}';
      }
    }

    private Map<Point, Junction> junctions = new HashMap<>(); // Is private to enforce use of recordJunction and getJunction.
    /* For any Point in the maze, returns the junction. Could have reduced memory usage with <Point, Integer>, with the
    heading as an Integer, and then returned new Junction(point.x, point.y, heading), but it's not scalable if we add more than a heading.
     */

    public void resetJunctionCounter() {
      junctionCounter = 0; // Tne behaviour of this function will need to be redesigned for when we try to remember Maze Solutions.
      junctions = new HashMap<>();
    }

    public void recordJunction(Point point, IRobot robot) {
      int heading = robot.getHeading();
      if (junctions.get(point) == null) { // This makes sure junctions aren't overwritten.
        junctions.put(point, new Junction(point.x, point.y, heading));
        junctionCounter++;
      }
    }

    public Junction getJunction(Point point) {
      return junctions.get(point);
    }

    public int searchJunction(Point point) {
      Junction junction  = junctions.get(point);
      if (junction != null)
        return junction.arrivalHeading; // returns the heading.
      return -1; // This is an error
    }

    public void printJunction(Junction junction) {
      System.out.println(junction.toString());
    }
  }

  private class Surroundings { /* A class detailing the passages, nonWalls and number of each after each move in the maze. It reduces code redundancy vastly.
    Implemented before reading about RobotData.*/

    public ExitType nonWall;
    public ExitType passage;
    public ExitType beenBefore;

    public class ExitType { // A class that lets you have a counter and array for each type of exit.
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