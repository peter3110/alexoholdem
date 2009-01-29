package ao.bucket.index.detail.example;

import ao.bucket.index.detail.flop.FlopDetailFlyweight.CanonFlopDetail;
import ao.bucket.index.detail.flop.FlopDetails;
import ao.bucket.index.detail.preflop.HoleDetails;
import ao.bucket.index.enumeration.HandEnum;
import ao.bucket.index.enumeration.PermisiveFilter;
import ao.bucket.index.enumeration.UniqueFilter;
import ao.bucket.index.flop.Flop;
import ao.bucket.index.flop.FlopLookup;
import ao.bucket.index.hole.CanonHole;
import ao.bucket.index.hole.HoleLookup;
import ao.bucket.index.turn.Turn;
import ao.bucket.index.turn.TurnLookup;
import ao.holdem.model.card.Card;
import ao.holdem.model.card.Community;
import ao.holdem.model.card.Hole;
import ao.odds.agglom.Odds;
import ao.odds.agglom.impl.PreciseHeadsUpOdds;
import ao.util.io.Dir;
import ao.util.misc.Traverser;
import ao.util.persist.PersistentBytes;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;

/**
 * Date: Jan 27, 2009
 * Time: 10:24:53 PM
 */
public class ExampleLookup
{
    //--------------------------------------------------------------------
    private ExampleLookup() {}

    private static final Logger LOG =
            Logger.getLogger(ExampleLookup.class);


    //--------------------------------------------------------------------
    public static void main(String[] args)
    {
        testHoleFlopConcistency();
    }

    private static void testHoleFlopConcistency()
    {
        HandEnum.turns(
                new PermisiveFilter<CanonHole>("%1$s"),
                new PermisiveFilter<Flop>(),
                new UniqueFilter<Turn>(),
                new Traverser<Turn>() {
            public void traverse(Turn turn) {
                Odds givenOdds = PreciseHeadsUpOdds.INSTANCE.compute(
                        turn.flop().hole().reify(),
                        turn.flop().community());

//                CanonTurnDetail turnDet =
//                        TurnDetails.lookup( turn.canonIndex() );
                CanonFlopDetail flopDet =
                        FlopDetails.containing(
                                turn.canonIndex() );
                CanonHole       hole = flopDet.hole();

                Odds predictedOdds = PreciseHeadsUpOdds.INSTANCE.compute(
                        hole.reify(),
                        new Community(flopDet.a(),
                                      flopDet.b(),
                                      flopDet.c()));

                assert givenOdds.equals( predictedOdds );
            }});
    }

    private static void testHoleConcistency()
    {
        HandEnum.flops(
                new PermisiveFilter<CanonHole>("%1$s"),
                new PermisiveFilter<Flop>(),
                new Traverser<Flop>() {
            public void traverse(Flop flop) {

                Odds givenOdds = PreciseHeadsUpOdds.INSTANCE.compute(
                        flop.hole().reify(), Community.PREFLOP);

                Odds predictedOdds = PreciseHeadsUpOdds.INSTANCE.compute(
                        HoleDetails.lookup(
                                FlopDetails.lookup(
                                        flop.canonIndex()).hole().canonIndex()
                        ).example(),
                        Community.PREFLOP);

                assert givenOdds.equals( predictedOdds );
            }});
    }



    //--------------------------------------------------------------------
    private static final File DIR =
            Dir.get("lookup/canon/detail/example/");

    private static final File F_HOLE_A = new File(DIR, "holeA.byte");
    private static final File F_HOLE_B = new File(DIR, "holeB.byte");
    private static final File F_FLOP_A = new File(DIR, "flopA.byte");
    private static final File F_FLOP_B = new File(DIR, "flopB.byte");
    private static final File F_FLOP_C = new File(DIR, "flopC.byte");
    private static final File F_TURN   = new File(DIR, "turn.byte");


    //--------------------------------------------------------------------
    private static final byte[] HOLE_A;
    private static final byte[] HOLE_B;
    private static final byte[] FLOP_A;
    private static final byte[] FLOP_B;
    private static final byte[] FLOP_C;
    private static final byte[] TURN;


    //--------------------------------------------------------------------
    static
    {
        LOG.debug("retrieve or compute");

        byte[] holeA = PersistentBytes.retrieve(F_HOLE_A);
        if (holeA != null)
        {
            HOLE_A = holeA;
            HOLE_B = PersistentBytes.retrieve(F_HOLE_B);
            FLOP_A = PersistentBytes.retrieve(F_FLOP_A);
            FLOP_B = PersistentBytes.retrieve(F_FLOP_B);
            FLOP_C = PersistentBytes.retrieve(F_FLOP_C);
            TURN   = PersistentBytes.retrieve(F_TURN  );

            LOG.debug("retrieved");
        }
        else
        {
            HOLE_A = ordinalArray( HoleLookup.CANONS );
            HOLE_B = ordinalArray( HoleLookup.CANONS );
            FLOP_A = ordinalArray( FlopLookup.CANONS );
            FLOP_B = ordinalArray( FlopLookup.CANONS );
            FLOP_C = ordinalArray( FlopLookup.CANONS );
            TURN   = ordinalArray( TurnLookup.CANONS );

            computeExamples();

            PersistentBytes.persist(HOLE_A, F_HOLE_A);
            PersistentBytes.persist(HOLE_B, F_HOLE_B);
            PersistentBytes.persist(FLOP_A, F_FLOP_A);
            PersistentBytes.persist(FLOP_B, F_FLOP_B);
            PersistentBytes.persist(FLOP_C, F_FLOP_C);
            PersistentBytes.persist(TURN  , F_TURN  );

            LOG.debug("computed");
        }
    }

    private static byte[] ordinalArray(int length)
    {
        byte[] arr = new byte[ length ];
        Arrays.fill(arr, (byte) -1);
        return arr;
    }


    //--------------------------------------------------------------------
    private static void computeExamples()
    {
        HandEnum.uniqueTurns(new Traverser<Turn>() {
            public void traverse(Turn turn) {

                Flop      flop = turn.flop();
                CanonHole hole = flop.hole();
                Community comm = turn.community();

                TURN[ turn.canonIndex() ] = (byte) comm.turn().ordinal();

                if (FLOP_A[ flop.canonIndex() ] == -1)
                {
                    FLOP_A[ flop.canonIndex() ] =
                            (byte) comm.flopA().ordinal();
                    FLOP_B[ flop.canonIndex() ] =
                            (byte) comm.flopB().ordinal();
                    FLOP_C[ flop.canonIndex() ] =
                            (byte) comm.flopC().ordinal();
                }

                if (HOLE_A[ hole.canonIndex() ] == -1)
                {
                    HOLE_A[ hole.canonIndex() ] =
                            (byte) hole.a().ordinal();
                    HOLE_B[ hole.canonIndex() ] =
                            (byte) hole.b().ordinal();
                }
            }
        });
    }


    //--------------------------------------------------------------------
    public static Card holeA(int canonHole)
    {
        return Card.VALUES[ HOLE_A[canonHole] ];
    }
    public static Card holeB(int canonHole)
    {
        return Card.VALUES[ HOLE_B[canonHole] ];
    }
    public static Hole hole(int canonHole)
    {
        return Hole.valueOf(holeA(canonHole),
                            holeB(canonHole));
    }


    //--------------------------------------------------------------------
    public static Card flopA(int canonFlop)
    {
        return Card.VALUES[ FLOP_A[canonFlop] ];
    }
    public static Card flopB(int canonFlop)
    {
        return Card.VALUES[ FLOP_B[canonFlop] ];
    }
    public static Card flopC(int canonFlop)
    {
        return Card.VALUES[ FLOP_C[canonFlop] ];
    }


    //--------------------------------------------------------------------
    public static Card turn(int canonTurn)
    {
        return Card.VALUES[ TURN[canonTurn] ];
    }
}
