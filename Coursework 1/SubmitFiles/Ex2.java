/*
Luqmaan Ahmed - Exercise 2
The first difference with Ex1 is the different approach for getting random values that result in equal probabilities
for all four directions. I chose to use a distribution [0,3] in N so that i could add the value to IRobot.AHEAD and 
obtain the other directions instead of a set of cumbersome if statements. The old distribution rounded the continuous
[0,3) in R. But whereas [0.5,1.5) rounds to 1 and [1.5,2.5) rounds to 2, there's half the range for 0 and 3: [0,0.5)
and [2.5,3) respectively. My method uses the floor function on [0,4) in R so all of 0, 1, 2 and 3 have a range of 1.

As before, there is a wall check is in the while loop but this time we check for colliding properly rather than assuming
it is true, effectively meaning we keep going ahead if we can. All of that doesn't run if newRandom, the boolean for the
1/8 probability of choosing a direction randomly again, is true, ensuring no unnecessary calculations take place.

The program did work initially, but i notice that it frequently goes back and forth along a corridor which could be
optimised.
*/
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex2
{

	public void controlRobot(IRobot robot) {

        int direction = IRobot.AHEAD;  // initialise direction variable, needs to exist within the whole method scope.
        String[] directionWords = {"forwards", "right", "backwards", "left"}; // All the associated directionWords for each direction in order.
        String directionWord = "forwards"; // initialise directionWord, needs the same scope as direction.
        boolean newRandom = Math.random() < 0.125; // A boolean with probability 1/8 of being true, as required.

        boolean colliding = robot.look(direction) == IRobot.WALL; // Find out if the robot is going to collide.
        while (colliding && !newRandom) {
            int randno = (int) Math.floor(Math.random()*4);  // get random number from 0-3 inclusive.
            // Convert this to a direction
            direction = IRobot.AHEAD + randno; // use AHEAD + 1 = RIGHT etc... to get direction.
            directionWord = directionWords[randno]; // use the randno as the index to directionWords which works as directionWords is in order.
            if (robot.look(direction) != IRobot.WALL)  // check for collisions:
                colliding = false;  // breaks the loop if not colliding.
        }

		if (newRandom) { 
			int randno = (int) Math.floor(Math.random()*4); // same logic as in collding while loop
			direction = IRobot.AHEAD + randno;
            directionWord = directionWords[randno];
		}

        String locationType = getLocationType(robot); // calls getLocationType method.

        System.out.println("I'm going " + directionWord + " " + locationType);
        robot.face(direction);
        }

        public String getLocationType(IRobot robot) {  // This method finds out if the robot is in a junction, corridor, or dead end, and assigns the appropriate sentence ending.
            String locationType = "";
            int wallsNo = 0;
            int start_direction = IRobot.AHEAD;

            for (int i = 0; i < 4; i++) { // uses IRobot.AHEAD + 1 = IRobot.RIGHT etc... to iterate through directions.
                if (robot.look(start_direction + i) == IRobot.WALL) {
                    wallsNo ++; // adds 1 to wallsNo if there was a wall in the direction looked.
                }
            }
            if (wallsNo == 1)  // assigns appropriate sentence ending based on no. of walls. Could be done with a list and index too.
                locationType = "at a junction";
            else if (wallsNo == 2)
                locationType = "down a corridor";
            else if (wallsNo == 3)
                locationType = "at a dead end";
            return locationType;
        }

}
