package ao.holdem.model.act;

import ao.holdem.model.Money;
import ao.holdem.model.act.RealAction;

/**
 *
 */
public enum SimpleAction
{
    //--------------------------------------------------------------------
    FOLD,
    CALL,
    RAISE;


    //--------------------------------------------------------------------
    public RealAction toRealAction(
            Money   toCall,
            boolean betMadeThisRound)
    {
        switch (this)
        {
            case FOLD:
                return RealAction.FOLD;

            case CALL:
                return toCall.equals( Money.ZERO )
                        ? RealAction.CHECK
                        : RealAction.CALL;

            case RAISE:
                return betMadeThisRound
                        ? RealAction.RAISE
                        : RealAction.BET;
        }
        throw new Error("should never be here");
    }
}