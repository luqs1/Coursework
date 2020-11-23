import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;

public class Explorer {

  private Surroundings surroundings = new Surroundings(); // A class i made for working one which directions were passages and nonWalls.
  
  public void controlRobot(IRobot robot) {
    surroundings.refresh(robot);  //Works out the surroundings again for this tick.
    int direction = IRobot.AHEAD;
    switch(surroundings.nonwallExits){
      case 1:
        //System.out.println("Dead End");
	      direction = deadEnd(robot);
	      break;
      case 2:
        //System.out.println("Corridor/ Corner");
	      direction = corridor(robot); //Corridor would also work with the crossroads method I've defined, but is more efficient this way.
	      break;
      case 3:
      case 4:
        //System.out.println("Crossroads or Junction");
	      direction = crossroads(robot); // Both Crossroads and Junctions are equivalent.
	      break;
    }
    //System.out.println(surroundings.nonwallExits);
    //System.out.println(surroundings.passageExits);
    robot.face(direction);
  }
  
  private int randomlySelect(IRobot robot, boolean[] array){  // This returns a random valid direction from an array of direction validity.
    ArrayList<Integer> chosen = new ArrayList<Integer>();
    for (int i=0; i < 4; i++){ 
      if (array[i] && i != 2) // Doesn't check or include BEHIND.
	      chosen.add(i);
    }
    int randno = (int) Math.floor(Math.random()*chosen.size());
    //System.out.println(chosen.get(randno));
    return (IRobot.AHEAD + chosen.get(randno));
  }  
  
  private int deadEnd(IRobot robot) { // Looks through the Surroundings to find the only nonWallExit and returns it.
    for (int i=0; i < 4; i++){
      if (surroundings.isNonWall[i])
        return (IRobot.AHEAD + i);
    }
    return -1; // Error code
  }
  
  private int corridor(IRobot robot){ // Doesn't check for optimal passage as there is only direction to choose anyway.
    return randomlySelect(robot, surroundings.isNonWall);
  }
  
  private int crossroads(IRobot robot){ // Does try to go for a passage if possible.
    if (surroundings.passageExits == 0)
      return randomlySelect(robot,surroundings.isNonWall);
    return randomlySelect(robot, surroundings.isPassage);
  }

  public class RobotData {
    public int junctionCounter;
    ArrayList<Junction> junctions = new ArrayList<Junction>();
  }

  public class Junction {
    int x;
    int y;
    int arrivalHeading;
  }
  public class Surroundings { /* A class detailing the passages, nonWalls and number of each after each move in the maze. It reduces code redundancy vastly.
    Implemented before reading about RobotData.*/
    public boolean[] isPassage = new boolean[4];
    
    public boolean[] isNonWall = new boolean[4];
    
    public int nonwallExits;
    
    public int passageExits;
    
    public void refresh(IRobot robot) { // runs every tick of controlRobot to get the surroundings again.
      this.isPassage = new boolean[4];
      this.isNonWall = new boolean[4];
      nonwallExitsCreate(robot);
      passageExitsCreate(robot);
      }
    
    private void nonwallExitsCreate(IRobot robot) {
      /* This method sets the number of exits that aren't a wall
      and sets the value for each element in the isNonWall array.
      */
      int output=0;
      for (int i=0; i<4; i++){
        if (robot.look(IRobot.AHEAD + i) != IRobot.WALL) {
          output++;
          this.isNonWall[i] = true;
        }
      }
      this.nonwallExits = output;
    }
    
    private void passageExitsCreate(IRobot robot) {
      /* This method sets the number of exits that are a passage
      (not already explored as well as notreturn output wall) and sets the values
      for the isPassage array. */
    
      int output=0;
      for (int i=0; i<4; i++){
        if (robot.look(IRobot.AHEAD + i) == IRobot.PASSAGE) {
          output++;
          this.isPassage[i] = true;
        }
      }
      this.passageExits = output;
    }
  }
}