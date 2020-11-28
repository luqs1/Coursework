import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;

public class Explorer {

  private final Surroundings surroundings = new Surroundings(); // A class i made for working one which directions were passages and nonWalls.
  
  public void controlRobot(IRobot robot) {
    surroundings.refresh(robot);  //Works out the surroundings again for this tick.
    int direction = IRobot.AHEAD;
    switch(surroundings.nonWall.numberOf){
      case 1:
        //System.out.println("Dead End");
	      direction = deadEnd();
	      break;
      case 2:
        //System.out.println("Corridor/ Corner");
	      direction = corridor(); //Corridor would also work with the crossroads method I've defined, but is more efficient this way.
	      break;
      case 3:
      case 4:
        //System.out.println("Crossroads or Junction");
	      direction = junction(); // Both Crossroads and Junctions are equivalent.
	      break;
    }
    //System.out.println(surroundings.nonwallExits);
    //System.out.println(surroundings.passageExits);
    robot.face(direction);
  }
  
  private int randomlySelect(boolean[] array){  // This returns a random valid direction from an array of direction validity.
    ArrayList<Integer> chosen = new ArrayList<Integer>();
    for (int i=0; i < 4; i++){ 
      if (array[i] && i != 2) // Doesn't check or include BEHIND.
	      chosen.add(i);
    }
    int randno = (int) Math.floor(Math.random()*chosen.size());
    //System.out.println(chosen.get(randno));
    return (IRobot.AHEAD + chosen.get(randno));
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

  public class RobotData {
    public int junctionCounter;
    ArrayList<Junction> junctions = new ArrayList<Junction>();

    public class Junction {
      int x;
      int y;
      int arrivalHeading;
  }

  }
  public class Surroundings { /* A class detailing the passages, nonWalls and number of each after each move in the maze. It reduces code redundancy vastly.
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