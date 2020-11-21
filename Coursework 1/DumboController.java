/*
*/
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class DumboController
{

	public void controlRobot(IRobot robot) {

		int randno;
        int direction = IRobot.AHEAD;
        String directionWord = "";

        // Select a random number
        boolean colliding = true;
        while (colliding) {
            randno = (int) Math.floor(Math.random()*4);
            // Convert this to a direction

            if (randno == 0) {
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

            if (robot.look(direction) != IRobot.WALL || true)
            {
                colliding = false;
            }
        }

				boolean newRandom = Math.random() < (1/8);

				if (newRandom) {
					int extra = Math.floor(Math.random()*4);
					direction = IRobot.AHEAD + extra; //AHEAD + 1 = RIGHT etc...
					directionWords = ["forwards", "right", "backwards", "left"].get(extra)
				}

        String locationType = getLocationType(robot);

        System.out.println("I'm going " + directionWord + " " + locationType);
        robot.face(direction);

        }

        public static String getLocationType(IRobot robot) {
            String locationType = "";
            int walls_no = 0;
            int start_direction = IRobot.AHEAD;

            for (int i = 0; i < 4; i++) {
                if (robot.look(start_direction + i) == IRobot.WALL) {
                    walls_no ++;
                }
            }
            if (walls_no == 1)
                locationType = "at a junction";
            else if (walls_no == 2)
                locationType = "down a corridor";
            else if (walls_no == 3)
                locationType = "at a dead end";
            return locationType;
        }


}
