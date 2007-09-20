package ao.ai.simple;

import ao.holdem.def.bot.AbstractBot;
import ao.holdem.model.act.Action;
import ao.strategy.Sklansky;

/**
 * http://www.onlinepokercenter.com/articles/poker_strategy/sklanskys_hand_rankings.php
 * Assumes loose/passive play.
 */
public class LooseSklanskyBot extends AbstractBot
{
    public Action act(Environment env)
    {
        int group = Sklansky.groupOf( env.hole() );

        if (group <= 5)
        {
            return Action.RAISE_OR_CALL;
        }

        if (group == 6)
        {
            if (env.realPosition() >= 0.5)
            {
                return Action.RAISE_OR_CALL;
            }
            else
            {
                return Action.CHECK_OR_FOLD;
            }
        }

        if (group == 7)
        {
            if (env.realPosition() >= 0.67)
            {
                return Action.RAISE_OR_CALL;
            }
            else
            {
                return Action.CHECK_OR_FOLD;
            }
        }

        return Action.CHECK_OR_FOLD;
    }
}