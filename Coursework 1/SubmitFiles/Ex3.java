/* 
Luqmaan Ahmed - Exercise 3
For the heading controller, I analysed the behaviour specification and broke down the logic into:
cases where you had many ideal directions, cases when you had one ideal direction, and cases when
you had many non ideal direction, and one non ideal direction. I broke down ideal into a boolean
and of non-colliding && target, and non ideal is non-colliding && !target. Therefore I made two
boolean arrays that had the non-colliding and target boolean values for all 4 cardinal directions.
Randomness comes into play only if multiple directions are chosen in the end for efficiency.

The homing robot can not always be expected to find the target. It is all too easy for it to be
lost in an endless loop of hitting a wall and coming back 1 square. This demonstrates the need
for randomness to deal with the randomness of the maze. The improvement I would suggest is to
remove the certainty of direction. Ideally, I'd make a probability distribution of the four
cardinals that is based on equal randomness but is then parameterized such that it favors
directions towards the target in some correlation to the magnitude of the displacement.

*/

import uk.ac.warwick.dcs.maze.logic.IRobot; 
import java.util.ArrayList; // Used the ArrayList for its dynamic size feature.

public class Ex3 { 
    
  public void controlRobot(IRobot robot) {
      int heading = headingController(robot); // Get Heading
      ControlTest.test(heading, robot); // Test Heading
      robot.setHeading(heading); // Set Heading, equivalent to robot.face previously (but for headings).
  }

  private int headingController(IRobot robot) { // Gets a heading (sometimes randomly) based on the specification outlined for Ex3.
      byte[] tDirections = {isTargetEast(robot), isTargetNorth(robot)}; // First determine where the target is.
      boolean[] headingsTable = { // Boolean values for whether to target the heading in order NESW.
          tDirections[1] == 1, // North
          tDirections[0] == 1, // East
          tDirections[1] == -1, // South
          tDirections[0] == -1, // West
          // this accounts for the case when tDirections[i] == 0
      };
      boolean[] collisionsTable = new boolean[4]; // Boolean values for whether there ISNT a wall
      boolean[] finalTable = new boolean[4]; // Combined table, separated for ease of future development.
      ArrayList<Integer> headings = new ArrayList<Integer>();  // ArrrayList for heading offsets from NORTH for chosen direction/s.

      for (int i=0; i<4; i++) {  // Adds Headings to the ArrayList if they are both target directions and non colliding.
          collisionsTable[i] = lookHeading(robot, IRobot.NORTH+i) != IRobot.WALL;
          finalTable[i] = headingsTable[i] && collisionsTable[i];
          if (finalTable[i])
            headings.add(i);
      }

      int size = headings.size();  // gets the size
      if (size == 0) {  // if there arent ANY ideal directions, just add from those that dont collide to the same ArrayList.
        for (int i=0; i<4; i++) {
            if (collisionsTable[i])
                headings.add(i);
        }
      }
      if (size == 1) // by placing this after size == 0 and as a distinct if, it allows for the case where only one non ideal direction was added.
          return (IRobot.NORTH + headings.get(0)); // so the chosen direction is returned immediately without processing any randomness.
      
      int randno = (int) Math.floor(Math.random()*headings.size()); // get a random index of the ArrayList
      return (IRobot.NORTH + headings.get(randno)); // returns a random heading from the ArrayList.
  }

  private byte isTargetNorth(IRobot robot) { // comment from guide.
      byte result = 0; // returning 1 for ‘yes’, -1 for ‘no’ and 0 for ‘same latitude’
      int robotY = robot.getLocation().y;
      int targetY = robot.getTargetLocation().y;
      if (robotY > targetY)
        result = 1;
      else if (robotY < targetY)
        result = -1;
      return result;  
  }

  private byte isTargetEast(IRobot robot) { // comment adapted from guide.
    byte result = 0; // returning 1 for ‘yes’, -1 for ‘no’ and 0 for ‘same longitude’
    int robotX = robot.getLocation().x;
    int targetX = robot.getTargetLocation().x;
    if (robotX < targetX)
      result = 1;
    else if (robotX > targetX)
      result = -1;
    return result;
  }
  
  private int lookHeading(IRobot robot, int heading) { // equivalent to robot.look but takes a heading.
      int currentHeading = robot.getHeading(); // this is the heading that is equiv to AHEAD
      int headingDifference = heading - currentHeading; // this is the difference from AHEAD
      headingDifference = (headingDifference < 0)? headingDifference +4: headingDifference; // adds 4 to negative differences so that I can get their postive equivalents.
      return robot.look(IRobot.AHEAD + headingDifference); // goes AHEAD, RIGHT, BEHIND, LEFT.
  }

  public void reset() {
      ControlTest.printResults();
  }

}