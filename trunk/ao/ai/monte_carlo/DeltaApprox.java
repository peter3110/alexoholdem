package ao.ai.monte_carlo;

import ao.ai.opp_model.classifier.raw.Classifier;
import ao.ai.opp_model.classifier.raw.DomainedClassifier;
import ao.ai.opp_model.decision.classification.ConfusionMatrix;
import ao.ai.opp_model.decision.classification.RealHistogram;
import ao.ai.opp_model.decision.classification.raw.Prediction;
import ao.ai.opp_model.decision.input.raw.example.Context;
import ao.ai.opp_model.decision.input.raw.example.ContextImpl;
import ao.ai.opp_model.decision.input.raw.example.Datum;
import ao.ai.opp_model.decision.random.RandomLearner;
import ao.ai.opp_model.input.ContextPlayer;
import ao.ai.opp_model.model.domain.HandStrength;
import ao.holdem.engine.Dealer;
import ao.holdem.engine.LiteralCardSource;
import ao.holdem.model.BettingRound;
import ao.holdem.model.Money;
import ao.holdem.model.act.SimpleAction;
import ao.persist.Event;
import ao.persist.HandHistory;
import ao.persist.PlayerHandle;
import ao.state.StateManager;
import ao.util.rand.Rand;

import java.util.*;
import java.io.Serializable;

/**
 * Approximates the difference between the average
 *  expected hand strength, and the hand strength
 *  of a particular player.
 */
public class DeltaApprox
{
    //--------------------------------------------------------------------
    private static final String[][] dataNameCache = new String[4][4];
    static
    {
        for (BettingRound r : BettingRound.values())
        {
            for (int i = 0; i < 4; i++)
            {
                dataNameCache[ r.ordinal() ][ i ] =
                        r.toString() + " " + i;
            }
        }
    }

    private static final int MEMORY = 32;


    //--------------------------------------------------------------------
    private Classifier global = newClassifier();
    private LinkedHashMap<Serializable, Classifier> individual =
            new LinkedHashMap<Serializable, Classifier>(
                    MEMORY, 0.75f, true);

    private HoldemPredictor<SimpleAction> actPredictor;
    private ConfusionMatrix<HandStrength> othersErrors =
            new ConfusionMatrix<HandStrength>();
    private Map<Serializable, ConfusionMatrix<HandStrength>> errors =
            new HashMap<Serializable, ConfusionMatrix<HandStrength>>();

    private int size;


    //--------------------------------------------------------------------
    public DeltaApprox(HoldemPredictor<SimpleAction> actPredictor)
    {
        this.actPredictor = actPredictor;
    }


    //--------------------------------------------------------------------
    public void examine(HandHistory history)
    {
        RealHistogram<PlayerHandle> approx = approximateShowdown(history);
        if (approx == null) return;

        Money win = Money.ZERO;
        for (Map.Entry<PlayerHandle, Money> delta :
                history.getDeltas().entrySet())
        {
            int cmp = delta.getValue().compareTo( win );
            if (cmp > 0) win = delta.getValue();
        }
        PlayerHandle winner = null;
        for (Map.Entry<PlayerHandle, Money> delta :
                history.getDeltas().entrySet())
        {
            int cmp = delta.getValue().compareTo( win );
            if (cmp == 0)
            {
                if (winner != null) return;
                winner = delta.getKey();
            }
        }
        assert winner != null;
        
        PlayerHandle probable = approx.mostProbable();
        boolean      isWin    = winner.equals(probable);
        System.out.println((isWin ? 1 : 0)     + "\t" +
                           winner              + "\t" +
                           probable            + "\t" +
                           approx.sampleSize() + "\t" +
                           approx);
    }


    //--------------------------------------------------------------------
    public RealHistogram<PlayerHandle> approximateShowdown(
            HandHistory history)
    {
        return approximate(extractShowdownChoices(history, null));
    }

    public RealHistogram<PlayerHandle>
            approximate( Map<PlayerHandle, List<Choice>> choices )
    {
        if (choices.size() < 2) return null;

        int sample = Integer.MAX_VALUE;
        Map<PlayerHandle, RealHistogram<HandStrength>> hands =
                new HashMap<PlayerHandle, RealHistogram<HandStrength>>();
        for (Map.Entry<PlayerHandle, List<Choice>> c :
                choices.entrySet())
        {
            RealHistogram<HandStrength> handStrength =
                    approximate(c.getKey(), c.getValue());
//            if (handStrength == null   ||
//                handStrength.isEmpty() ||
//                handStrength.total() == 0) return null;
            
            hands.put(c.getKey(), handStrength);

            sample = Math.min(sample, handStrength.sampleSize());
        }

        RealHistogram<PlayerHandle> approx = doApproximate( hands );
        approx.setSampleSize(sample);
        return approx;
    }

    private RealHistogram<PlayerHandle> doApproximate(
                Map<PlayerHandle, RealHistogram<HandStrength>> hands)
    {
        return (hands.size() == 2)
                ? approxTwo(hands)
                : approxN(hands);
    }
    private RealHistogram<PlayerHandle> approxTwo(
                Map<PlayerHandle, RealHistogram<HandStrength>> hands)
    {
        Iterator<PlayerHandle>      players = hands.keySet().iterator();
        PlayerHandle                playerA = players.next();
        RealHistogram<HandStrength> handA   = hands.get( playerA );
        PlayerHandle                playerB = players.next();
        RealHistogram<HandStrength> handB   = hands.get( playerB );

        double aWin = winProbability(handA, handB);
        
        RealHistogram<PlayerHandle> approx =
                new RealHistogram<PlayerHandle>();
        approx.add(playerA, aWin);
        approx.add(playerB, 1.0 - aWin);
        return approx;
    }
    private RealHistogram<PlayerHandle> approxN(
                Map<PlayerHandle, RealHistogram<HandStrength>> hands)
    {
        RealHistogram<PlayerHandle> approx  =
                new RealHistogram<PlayerHandle>();
        List<PlayerHandle>          inOrder =
                new ArrayList<PlayerHandle>( hands.keySet() );
        for (int i = 0; i < inOrder.size(); i++)
        {
            double                      win     = 1.0;
            PlayerHandle                playerA = inOrder.get(i);
            RealHistogram<HandStrength> handA   = hands.get( playerA );

            for (int j = 0; j < inOrder.size(); j++)
            {
                if (i == j) continue;

                PlayerHandle playerB = inOrder.get( j );
                win = Math.min(win,
                               winProbability(
                                       handA, hands.get( playerB )));
            }
            approx.add(playerA, win);
        }
        return approx;
    }

    private double winProbability(
            RealHistogram<HandStrength> handA,
            RealHistogram<HandStrength> vsHandB)
    {
//        boolean aPriori = handA.isEmpty();
//        if (vsHandB.isEmpty())
//        {
//            return aPriori
//                   ? 0.5
//                   : (1.0 - notLosingProbability(vsHandB, handA));
//        }

        double tieProb = 0;
        double winProb = 0;
        for (int i = HandStrength.values().length - 1; i >=0; i--)
        {
            HandStrength hs    = HandStrength.values()[ i ];
            double       probA = handA.probabilityOf(hs);

            tieProb += probA * vsHandB.probabilityOf(hs);

            double probBltA = 0; // P(B lessThan A)
            for (int j = 0; j < i; j++)
            {
                probBltA +=
                    vsHandB.probabilityOf(HandStrength.values()[ j ]);
            }
            winProb += (probA * probBltA);
        }

        return winProb + tieProb/2;
    }


    //--------------------------------------------------------------------
    public void learn(HandHistory history)
    {
        learn(history, null);
    }
    public void learn(HandHistory history, PlayerHandle onlyFor)
    {
        for (Map.Entry<PlayerHandle, List<Choice>> choice :
                extractChoices(history, onlyFor, true).entrySet())
        {
            List<Choice> choiceList = choice.getValue();
            Choice       lastChoice =
                            choiceList.get( choiceList.size()-1 );

            HandStrength strength = HandStrength.fromState(
                    lastChoice.state(),
                    history.getCommunity(),
                    history.getHoles().get( choice.getKey() ));
            learn( choice.getKey(), choiceList, strength );
        }
    }

    private void learn(
            PlayerHandle player,
            List<Choice> surprises,
            HandStrength actual)
    {
        doLearn(getClassifier(player),
                getConfusion(player),
                surprises, actual);

        size++;
        if (size < 1024 ||
            Rand.nextBoolean(Math.sqrt(size) / size))
        {
            doLearn(global, othersErrors, surprises, actual);
        }
    }
    private void doLearn(
            Classifier                    classifier,
            ConfusionMatrix<HandStrength> confusion,
            List<Choice>                  surprises,
            HandStrength                  actual)
    {
        //individual
        Context      ctx       = contextFor(surprises);
        Prediction   p         = classifier.classify(ctx);
        HandStrength predicted =
                (HandStrength) p.toRealHistogram().mostProbable();
//        System.out.println((actual.equals(predicted) ? 1 : 0)
//                                          + "\t" +
//                           actual         + "\t" +
//                           predicted      + "\t" +
//                           p.sampleSize() + "\t" +
//                           p);
        confusion.add(actual, predicted);

        classifier.add( ctx.withTarget(new Datum(actual)) );
    }

    //history.holesVisible( choice. )
    public Map<PlayerHandle, List<Choice>>
            extractShowdownChoices(
                HandHistory history, PlayerHandle onlyFor)
    {
        return extractChoices(history, onlyFor, true);
    }
    public Map<PlayerHandle, List<Choice>>
            extractChoices(HandHistory  history,
                           PlayerHandle onlyFor,
                           boolean      atShowdown)
    {
        StateManager start =
                new StateManager(history.getPlayers(),
                                 new LiteralCardSource(history));

        Map<PlayerHandle, ContextPlayer> brains =
                new HashMap<PlayerHandle, ContextPlayer>();
        for (PlayerHandle player : history.getPlayers())
        {
            brains.put(player,
                       new ContextPlayer(history, player));
        }

        new Dealer(start, brains).playOutHand( false );

        Map<PlayerHandle, List<Choice>> choices =
                new HashMap<PlayerHandle, List<Choice>>();
        for (Map.Entry<PlayerHandle, ContextPlayer> playerEntry :
                brains.entrySet())
        {
            PlayerHandle  player    = playerEntry.getKey();
            ContextPlayer ctxPlayer = playerEntry.getValue();
            if ( !ctxPlayer.finishedUnfolded()                  ||
                 (atShowdown && !history.holesVisible( player ) ||
                 (onlyFor != null && !player.equals( onlyFor ))))
            {
                continue;
            }

            List<Choice>   s   = new ArrayList<Choice>();
            int            i   = 0;
            List<Context>  ctx = ctxPlayer.contexts();
            for (Event e : history.getEvents( player ))
            {
                if (e.getAction().isBlind()) continue;

                s.add(new Choice(
                            actPredictor.predict(
                                        player, ctx.get(i)),
                            e.getAction().toSimpleAction(),
                            ctxPlayer.states().get( i )));
                i++;
            }

            choices.put(player, s);
        }
        return choices;
    }


    //--------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public RealHistogram<HandStrength>
            approximate(PlayerHandle player,
                        List<Choice> choices)
    {
        Context    ctx      = contextFor(choices);
        Classifier personal = getClassifier(player);
        Prediction p        = personal.classify( ctx );

        Prediction mostConfident = p;
        if (p.sampleSize() < 5)
        {
            Prediction globalP = global.classify( ctx );
            if (globalP.sampleSize() > p.sampleSize())
            {
                mostConfident = globalP;
            }
        }
        return (RealHistogram<HandStrength>)
                mostConfident.toRealHistogram();
    }


    //--------------------------------------------------------------------
    private Context contextFor(List<Choice> choices)
    {
        double       maxRoundSurprise = Long.MIN_VALUE;
        BettingRound prevRound        = null;

        double roundMaxes[] = new double[ 4 ];

        Context ctx = new ContextImpl();
        for (Choice s : choices)
        {
            BettingRound round = s.round();
            if (prevRound != round)
            {
                maxRoundSurprise = Long.MIN_VALUE;
            }

            double surprise = s.surprise();
            if (surprise > maxRoundSurprise)
            {
                maxRoundSurprise              = surprise;
                roundMaxes[ round.ordinal() ] = surprise;
            }
            prevRound = s.round();
        }
        for (int i = 0; i < 4; i++)
        {
            ctx.add(new Datum(BettingRound.values()[ i ].toString(),
                              roundMaxes[ i ]) );
        }

//        for (Choice s : choices)
//        {
//            BettingRound round = s.round();
//            if (prevRound != round)
//            {
//                count = 0;
//            }
//
//            ctx.add(new Datum(dataNameCache[ round.ordinal() ][ count ],
//                              s.surprise()) );
//
//            prevRound = s.round();
//            count++;
//        }
        return ctx;
    }


    //--------------------------------------------------------------------
    private ConfusionMatrix<HandStrength>
                getConfusion(PlayerHandle player)
    {
        ConfusionMatrix<HandStrength> confusion =
                errors.get( player.getId() );
        if (confusion == null)
        {
            confusion = new ConfusionMatrix<HandStrength>();
            errors.put( player.getId(), confusion );
        }
        return confusion;
    }

    private Classifier getClassifier(PlayerHandle player)
    {
        Classifier personal = individual.get( player.getId() );
        if (personal == null)
        {
            personal = newClassifier();
            individual.put( player.getId(), personal );

            while (individual.size() > MEMORY)
            {
                Serializable forgottenId =
                        individual.keySet().iterator().next();
                individual.remove( forgottenId );
                othersErrors.addAll( errors.remove(forgottenId) );
            }
        }
        return personal;
    }

    private Classifier newClassifier()
    {
        return new DomainedClassifier(new RandomLearner.Factory());
    }


    //--------------------------------------------------------------------
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<Serializable, ConfusionMatrix<HandStrength>> err :
                errors.entrySet())
        {
            str.append("\nConfusion for ")
               .append(err.getKey())
               .append("\n")
               .append(err.getValue().toString());
        }

        str.append("\nConfusion for others\n")
               .append(othersErrors.toString());
        return str.toString();
    }
}

