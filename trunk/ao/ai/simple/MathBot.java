package ao.ai.simple;

import ao.ai.AbstractPlayer;
import ao.holdem.model.act.EasyAction;
import ao.holdem.model.card.Community;
import ao.holdem.model.card.Hole;
import ao.odds.agglom.impl.ApproximateOddFinder;
import ao.odds.agglom.OddFinder;
import ao.odds.agglom.Odds;
import ao.holdem.engine.persist.HandHistory;
import ao.holdem.engine.state.HandState;
import ao.holdem.engine.state.StateManager;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Designed for heads up play.
 */
public class MathBot extends AbstractPlayer
{
    //--------------------------------------------------------------------
    public void handEnded(HandHistory history) {}

    
    //--------------------------------------------------------------------
    protected EasyAction act(StateManager env, HandState state, Hole hole)
    {
        Community community = env.cards().community();

        OddFinder oddFinder = new ApproximateOddFinder();
        Odds odds = oddFinder.compute(
                        hole, community, state.numActivePlayers()-1);

        double toCall = state.remainingBetsInRound() * state.betsToCall();
        double potOdds =
                (toCall) /
                (toCall + state.pot().smallBets());

//        if (odds.winPercent() <= potOdds)
//        {
//            System.out.println(
//                "odds: " + odds +
//                " with " + hole +
//                " on " + community +
//                " vs [" + Math.round(potOdds * 100) + "]");
//        }

        return (odds.winPercent() > potOdds)
                ? EasyAction.RAISE_OR_CALL
                : EasyAction.CHECK_OR_CALL;
    }


    public static void main(String args[]) throws IOException
    {
        for (int i = 0; i < 10000; i++)
        {
            System.out.println();
            System.out.println("=========================");
            System.out.println("=========================");

            System.out.println("to call:");
            char toCallBytes[] = new char[20];
            new InputStreamReader(System.in).read(toCallBytes);
            String toCallStr  = new String(toCallBytes).trim();

            System.out.println("pot size:");
            char potSizeBytes[] = new char[20];
            new InputStreamReader(System.in).read(potSizeBytes);
            String potOddsStr = new String(potSizeBytes).trim();

            double toCall  = Double.valueOf(toCallStr);
            double potSize = Double.valueOf(potOddsStr);
            double potOdds = toCall / (toCall + potSize);

            System.out.println("-------------------");
            System.out.println("potOdds: " + potOdds);
        }
    }
}








