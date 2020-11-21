/*
Luqmaan Ahmed - Exercise 1

To fix the problem of collisions with the walls, I employed a while loop (non-fixed iteration) to continuosly
generate a random number, from which I used selection to select one of the 4 directions. If the direction was
obstructed by a wall, I would repeat the cycle until I found a direction that wasn't obstructed by a wall.

I seperated the code that found whether the Robot was in a junction, corridor, or deadend into another method
as I felt it was seperate functionality, meaning that my variable wallsNo had a different scope to the rest 
of the program. The method counts the number of immediate cardinal walls and makes a judgement based on that.
*/
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex1
{

	public void controlRobot(IRobot robot) {

	// initialise direction and directionWord as they need a method wide scope.
        int direction = IRobot.AHEAD;
        String directionWord = ""; 

        // Select a random number
        boolean colliding = true; // assume it will collide.
        while (colliding) {
            int randno = (int) Math.round(Math.random()*3); // randno initialised within while loop. Not equally random as explained in Ex2.
            // Convert this to a direction

            if (randno == 0) {  // multiple selections to choose direction and directionWord based on randno.
            direction = IRobot.LEFT;
            directionWord = "left";
            }
            else if (randno == 1) {
            direction = IRobot.RIGHT;
            directionWord = "right";
            }
            else if (randno == 2) {
            direction = IRobot.BEHIND;
            directionWord = "backwards";
            }
            else {
            direction = IRobot.AHEAD;
            directionWord = "forwards";
            }

            if (robot.look(direction) != IRobot.WALL) // breaks loop if the next square in the direction specified is not a wall.
            {
                colliding = false;
            }
        }
        
        String locationType = getLocationType(robot); // created getLocationType method that returns the rest of the String output.
    
        System.out.println("I'm going " + directionWord + " " + locationType);
        robot.face(direction); // robot finally is told to face the chosen direction.

        }

        public String getLocationType(IRobot robot) {  /* Counts the number of walls in the four immediate cardinal blocks
								to return the relevant locationType String */
            String locationType = "";
            int wallsNo = 0;
            int start_direction = IRobot.AHEAD;

            for (int i = 0; i < 4; i++) { // uses the fact that IRobot.AHEAD + 1 = IRobot.RIGHT etc...
                if (robot.look(start_direction + i) == IRobot.WALL) {
                    wallsNo ++;
                }
            }
            if (wallsNo == 1)
                locationType = "at a junction";
            else if (wallsNo == 2)
                locationType = "down a corridor";
            else if (wallsNo == 3)
                locationType = "at a dead end";
            return locationType;
        }
		

}
