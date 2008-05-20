package ao.regret.node;

import ao.simple.KuhnAction;
import ao.simple.rules.KuhnBucket;
import ao.simple.rules.KuhnRules;
import ao.simple.state.StateFlow;
import ao.util.rand.Rand;
import ao.util.text.Txt;

import java.util.EnumMap;
import java.util.Map;

/**
 *
 */
public class ProponentNode implements PlayerNode
{
    //--------------------------------------------------------------------
    private Map<KuhnAction, double[]> regret;
    private Map<KuhnAction, InfoNode> actions;
    private Map<KuhnAction, double[]> prob;


    //--------------------------------------------------------------------
    public ProponentNode(KuhnRules rules, KuhnBucket bucket)
    {
        prob    = new EnumMap<KuhnAction, double[]>(KuhnAction.class);
        regret  = new EnumMap<KuhnAction, double[]>(KuhnAction.class);
        actions = new EnumMap<KuhnAction, InfoNode>(KuhnAction.class);

        for (Map.Entry<KuhnAction, KuhnRules> transition :
                rules.transitions().entrySet())
        {
            KuhnRules nextRules = transition.getValue();
            StateFlow nextState = nextRules.state();

            if (nextState.endOfHand())
            {
                actions.put(transition.getKey(),
                            new TerminalNode(
                                    bucket, nextState.outcome()));
            }
            else
            {
                actions.put(transition.getKey(),
                            new OpponentNode(nextRules, bucket));
            }
        }

        for (KuhnAction act : KuhnAction.VALUES)
        {
            prob.put(act, new double[]{
                             1.0 / KuhnAction.VALUES.length});
            regret.put(act, new double[]{0});
        }
    }


    //--------------------------------------------------------------------
    public KuhnAction nextAction()
    {
        KuhnAction bestAction       = null;
        double     bestActionWeight = Long.MIN_VALUE;

        for (Map.Entry<KuhnAction, double[]> p : prob.entrySet())
        {
            double weight = Rand.nextDouble(p.getValue()[0]);
            if (bestActionWeight < weight)
            {
                bestAction       = p.getKey();
                bestActionWeight = weight;
            }
        }

        return bestAction;
    }


    //--------------------------------------------------------------------
    public double probabilityOf(KuhnAction action)
    {
        return prob.get( action )[ 0 ];
    }


    //--------------------------------------------------------------------
    public void add(Map<KuhnAction, Double> counterfactualRegret)
    {
        for (Map.Entry<KuhnAction, Double> r :
                counterfactualRegret.entrySet())
        {
            regret.get( r.getKey() )[0] += r.getValue();
        }
    }


    //--------------------------------------------------------------------
    public void updateActionPabilities()
    {
        double cumRegret = positiveCumulativeCounterfactualRegret();

        if (cumRegret <= 0)
        {
            for (double[] p : prob.values())
            {
                p[0] = 1.0 / KuhnAction.VALUES.length;
            }
        }
        else
        {
            for (Map.Entry<KuhnAction, double[]> p : prob.entrySet())
            {
                double cRegret = regret.get( p.getKey() )[0];

                p.getValue()[0] =
                        (cRegret < 0)
                        ? 0
                        : cRegret / cumRegret;
            }
        }
    }

    private double positiveCumulativeCounterfactualRegret()
    {
        double positiveCumulation = 0;
        for (double[] pointRegret : regret.values())
        {
            if (pointRegret[0] > 0)
            {
                positiveCumulation += pointRegret[0];
            }
        }
        return positiveCumulation;
    }


    //--------------------------------------------------------------------
    public InfoNode child(KuhnAction forAction)
    {
        return actions.get( forAction );
    }
//    public double expectedValue(KuhnAction   forAction,
//                                OpponentNode opponent)
//    {
//        InfoNode afterAct = actions.get( forAction );
//        //regret
//        //opponent.
//
//
//        return 0;
//    }


    //--------------------------------------------------------------------
    public String toString()
    {
        return toString(0);
    }

    public String toString(int depth)
    {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<KuhnAction, InfoNode> action : actions.entrySet())
        {
            str.append( Txt.nTimes("\t", depth) )
               .append( action.getKey() )
               .append( " :: " )
               .append( prob.get(action.getKey())[0] )
               .append( " :: " )
               .append( regret.get(action.getKey())[0] )
               .append( "\n" )
               .append( action.getValue().toString(depth + 1) )
               .append( "\n" );
        }
        return str.substring(0, str.length()-1);
    }
}