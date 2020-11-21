import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;

public class Explorer {

  private Surroundings surroundings = new Surroundings();
  
  public void controlRobot(IRobot robot) {
    surroundings.refresh(robot);
    int exits = surroundings.nonwallExits;
    int direction;
    switch(exits){
      case 1:
	direction = deadEnd(robot);
	break;
      case 2:
	direction = corridor(robot);
	break;
      case 3:
	direction = junction(robot);
	break;
      case 4:
	direction = crossroads(robot);// This method returns the number of exits that aren't a wall.
	break;
    }
    robot.face(direction);
  }
  
  private int randomlySelect(IRobot robot, boolean[] array){
    ArrayList<Integer> chosen = new ArrayList<Integer>();
    for (int i; i < 3; i++){ // Doesn't check or include BEHIND.
      if (array[i])
	chosen.add(i);
    }
    int randno = (int) Math.floor(Math.random()*chosen.size());
    return chosen.get(randno);
  }  
  
  private int deadEnd(IRobot robot) { // Looks through the Surroundings to find the only nonWallExit and returns it.
    for (int i; i < 4; i++){
      if (surroundings.isNonWall[i])
	return (IRobot.AHEAD + i);
    }
  }
  
  private int corridor(IRobot robot){ // and 
    if (surroundings.passageExits == 0)
      return randomlySelect(robot,surroundings.isNonWall);
    return randomlySelect(robot, surroundings.isPassage);
  }
  
  private int junction(IRobot robot){
    if (surroundings.passageExits == 0)
      return randomlySelect(robot,surroundings.isNonWall);
    return randomlySelect(robot, surroundings.isPassage);
  }
  
  private int crossroads(Irobot robot){
    if (surroundings.passageExits == 0)
      return randomlySelect(robot,surroundings.isNonWall);
    return randomlySelect(robot, surroundings.isPassage);
  }
}

public class Surroundings {
  public boolean[] isPassage = new boolean[4];
  
  public boolean[] isNonWall = new boolean[4];
  
  public int nonwallExits;
  
  public int passageExits;
  
  public void refresh(IRobot robot) {
    // runs every tick of controlRobot to get the surroundings again.
    this.isPassage = new boolean[4];
    this.isNonWall = new boolean[4];
    this.nonwallExits = nonwallExitsCreate(robot);
    this.passageExits = passageExitsCreate(robot);
    }
  
  private int nonwallExitsCreate(IRobot robot) {
    /* This method sets the number of exits that aren't a wall
     and sets the value for each element in the isNonWall array.
     */
    int output;
    for (int i; i<4; i++){
      if (robot.face(IRobot.AHEAD + i) != IRobot.WALL) {
	output++;
	this.isNonWall[i] = true;
	}
    }
    this.nonwallExits = output;
  }
  
  private int passageExitsCreate(IRobot robot) {
  /* This method sets the number of exits that are a passage
  (not already explored as well as notreturn output wall) and sets the values
  for the isPassage array. */
  
    int output;
    for (int i; i<4; i++){
      if (robot.face(IRobot.AHEAD + i) == IRobot.PASSAGE) {
	output++;
	this.isPassage[i] = true;
      }
    }
  this.passageExits = output;
  }
}