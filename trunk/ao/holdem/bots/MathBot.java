package ao.holdem.bots;

import ao.holdem.bots.util.*;
import ao.holdem.def.bot.AbstractBot;
import ao.holdem.def.model.cards.Hole;
import ao.holdem.def.model.cards.community.Community;
import ao.holdem.def.state.action.Action;
import ao.holdem.def.state.env.Environment;

/**
 * Designed for heads up play.
 */
public class MathBot extends AbstractBot
{
    //--------------------------------------------------------------------
    public Action act(Environment env)
    {
//        if (env.opponentCount() != 1)
//        {
//            return Action.CHECK_OR_FOLD;
//        }

        Hole      hole      = env.hole();
        Community community = env.community();

//        HeadsUpOddFinder oddFinder = new HeadsUpOddFinder();
        OddFinder oddFinder = new AproximatingOddFinder();
        Odds odds = oddFinder.compute(
                        hole, community, env.activeOpponents());

        double potOdds =
                ((double) env.toCall()) /
                (env.toCall() + env.pot());

        System.out.println(
                "odds: " + odds +
                " with " + hole +
                " on " + community +
                " vs [" + Math.round(potOdds * 100) + "]");
        return (odds.winPercent() > potOdds)
                ? Action.RAISE_OR_CALL
                : Action.CHECK_OR_FOLD;
    }
}