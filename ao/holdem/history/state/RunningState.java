package ao.holdem.history.state;

import ao.holdem.def.state.env.RealAction;
import ao.holdem.def.model.cards.Hand;
import ao.holdem.def.model.Money;
import ao.holdem.history.Event;
import ao.holdem.history.HandHistory;
import ao.holdem.history.PlayerHandle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class RunningState
{
    //--------------------------------------------------------------------
    private HoldemState endState;
    private List<Event> events = new ArrayList<Event>();
    private CardSource  cards  = new DeckCardSource();


    //--------------------------------------------------------------------
    private RunningState() {}

    public RunningState(
            List<PlayerHandle> clockwiseDealerLast)
    {
        endState = new HoldemState(clockwiseDealerLast);
    }
    public RunningState(
            List<PlayerHandle> clockwiseDealerLast,
            PlayerHandle       smallBlind,
            PlayerHandle       bigBlind)
    {
        endState = new HoldemState(clockwiseDealerLast,
                                   smallBlind, bigBlind);
    }


    //--------------------------------------------------------------------
    public CardSource cards()
    {
        return cards;
    }


    //--------------------------------------------------------------------
    public void advance(RealAction act)
    {
        Event event = new Event(nextToAct(), head().round(), act);

        HoldemState nextState = endState.advance(act);
        process(act, nextState);
        endState = nextState;

        events.add( event );
    }

    private void process(RealAction act, HoldemState newState)
    {

    }


    //--------------------------------------------------------------------
    public HandHistory toHistory()
    {
        HandHistory hist = new HandHistory();

        for (PlayerState player : head().players())
        {
            PlayerHandle handle = player.handle();
            hist.addPlayer( handle                        );
            hist.addHole(   handle, cards.holeFor(handle) );
        }

        for (Event event : events)
        {
            hist.addEvent( event );
        }

        assignDeltas(hist);
        return hist;
    }

    private void assignDeltas(HandHistory hist)
    {
        List<PlayerState> winners = winners();

        Money totalLost = new Money();
        for (PlayerState player : head().players())
        {
            if (! winners.contains(player))
            {
                Money commit = player.commitment();
                hist.setDelta(player.handle(),
                              Money.ZERO.minus(commit));
                totalLost = totalLost.plus(commit);
            }
        }
        
        Money winnings  = totalLost.split(     winners.size() );
        Money remainder = totalLost.remainder( winners.size() );
        for (int i = 0; i < winners.size(); i++)
        {
            PlayerHandle winner = winners.get(i).handle();
            Money total = (i == 0)
                          ? winnings
                          : winnings.plus( remainder );
            hist.setDelta(winner, total);
        }
    }


    //--------------------------------------------------------------------
    public HoldemState head()
    {
        return endState;
    }

    public PlayerHandle nextToAct()
    {
        return head().nextToAct().handle();
    }

    public boolean winnersKnown()
    {
        return head().atEndOfHand();
    }

    public List<PlayerState> winners()
    {
        List<PlayerState>       winners   = new ArrayList<PlayerState>();
        Collection<PlayerState> finalists = head().finalists();

        if (finalists.size() == 1)
        {
            winners.addAll( finalists );
        }
        else if (finalists.size() > 1)
        {
            short topHandRank = -1;
            for (PlayerState player : finalists)
            {
                short handRank =
                    new Hand(cards.holeFor(player.handle()),
                             cards.community()).value();

                if (handRank > topHandRank)
                {
                    winners.clear();
                    topHandRank = handRank;
                }
                if (handRank == topHandRank) winners.add( player );
            }
        }
        return winners;
    }


    //--------------------------------------------------------------------
    public RunningState continueFrom()
    {
        RunningState proto = new RunningState();
        proto.endState     = endState;
        proto.cards        = cards.prototype();
        return proto;
    }
}
