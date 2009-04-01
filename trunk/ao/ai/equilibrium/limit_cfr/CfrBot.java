package ao.ai.equilibrium.limit_cfr;

import ao.ai.AbstractPlayer;
import ao.bucket.abstraction.HoldemAbstraction;
import ao.bucket.index.flop.Flop;
import ao.bucket.index.hole.CanonHole;
import ao.bucket.index.river.River;
import ao.bucket.index.turn.Turn;
import ao.holdem.engine.analysis.Analysis;
import ao.holdem.engine.state.State;
import ao.holdem.engine.state.tree.StateTree;
import ao.holdem.engine.state.tree.StateTree.Node;
import ao.holdem.model.Avatar;
import ao.holdem.model.Chips;
import ao.holdem.model.Round;
import ao.holdem.model.act.AbstractAction;
import ao.holdem.model.act.Action;
import ao.holdem.model.card.sequence.CardSequence;
import ao.regret.holdem.InfoBranch;
import ao.regret.holdem.InfoTree;

import java.util.Map;

/**
 * User: alex
 * Date: 1-Apr-2009
 * Time: 9:17:18 AM
 */
public class CfrBot extends AbstractPlayer
{
    //--------------------------------------------------------------------
    private final HoldemAbstraction ABS;


    //--------------------------------------------------------------------
    public CfrBot(HoldemAbstraction precomputedAbstraction)
    {
        ABS = precomputedAbstraction;
    }


    //--------------------------------------------------------------------
    public void handEnded(Map<Avatar, Chips> deltas) {}


    //--------------------------------------------------------------------
    public Action act(State        state,
                      CardSequence cards,
                      Analysis     analysis)
    {
        assert state.seats().length == 2
                : "Only works in heads-up mode";

        Node       gamePath = StateTree.fromState(state);
        InfoTree   info     = ABS.info();
        InfoBranch branch   =
                info.info(gamePath.pathToFlop(), gamePath.round());

        char      roundBucket;
        CanonHole canonHole  = cards.hole().asCanon();
        byte      holeBucket = ABS.tree().getHole(
                                canonHole.canonIndex());

        if (state.round().equals( Round.PREFLOP )) {
            roundBucket = (char) holeBucket;
        } else {
            Flop flop       = canonHole.addFlop(cards.community());
            byte flopBucket = ABS.tree().getFlop( flop.canonIndex() );

            if (state.round().equals( Round.FLOP )) {
                roundBucket = ABS.decoder().decode(
                        holeBucket, flopBucket);
            } else {
                Turn turn       = flop.addTurn(cards.community().turn());
                byte turnBucket = ABS.tree().getTurn( turn.canonIndex() );

                if (state.round().equals( Round.TURN )) {
                    roundBucket = ABS.decoder().decode(
                            holeBucket, flopBucket, turnBucket);
                } else {
                    River river       =
                            turn.addRiver( cards.community().river() );
                    byte  riverBucket =
                            ABS.tree().getRiver( river.canonIndex() );

                    roundBucket = ABS.decoder().decode(
                            holeBucket, flopBucket, turnBucket,
                            riverBucket);
                }
            }
        }

        InfoBranch.InfoSet infoSet = branch.get(
                roundBucket, gamePath.roundPathId());

        AbstractAction act = infoSet.nextProbableAction(state.canRaise());
        return state.reify( act.toFallbackAction() );
    }
}
