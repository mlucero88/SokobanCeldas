import java.util.Random;

import core.ArcadeMachine;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TestSinglePlayerCeldas
{

    public static void main(String[] args)
    {
    	String grupo04Controller = "fiubaceldas.grupo04.Agent";

        String game = "examples/gridphysics/sokoban.txt";
        String level = "examples/gridphysics/sokoban_lvl0.txt";
        
        boolean visuals = true;
        int seed = new Random().nextInt();
        String recordActionsFile = "actions_sp_sokoban_lvl0" + "_" + seed + ".txt";

        // 1. This starts a game, in a level, played by a human.
        ArcadeMachine.playOneGame(game, level, recordActionsFile, seed);
        
        // 2. This plays a game in a level by the controller.
//        ArcadeMachine.runOneGame(game, level, visuals, grupo04Controller, recordActionsFile, seed, 0);

        // 3. This replays a game from an action file previously recorded
        //String readActionsFile = recordActionsFile;
        //ArcadeMachine.replayGame(game, level, visuals, readActionsFile);
    }
}
