package ao.ai.opp_model.decision2.classification;

import ao.ai.opp_model.decision2.data.Datum;
import ao.util.stats.Info;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Histogram extends Classification
{
    //--------------------------------------------------------------------
    private Map<Datum, Integer> hist;
    private int                 total = 0;


    //--------------------------------------------------------------------
    public Histogram()
    {
        hist = new HashMap<Datum, Integer>();
    }


    //--------------------------------------------------------------------
    public void add(Datum datum)
    {
        Integer count = hist.get(datum);
        hist.put(datum,
                 (count == null ? 1 : ++count));
        total++;
    }


    //--------------------------------------------------------------------
    public double probabilityOf(Datum attribute)
    {
        return (attribute == null)
                ? 0
                : (double) countOf(attribute)
                            / total;
    }

    private int countOf(Datum attribute)
    {
        Integer count = hist.get(attribute);
        return (count == null) ? 0 : count;
    }


    //--------------------------------------------------------------------
    public Datum mostProbable()
    {
        Datum mostFrequent = null;
        int   count        = -1;

        for (Map.Entry<Datum, Integer> entry : hist.entrySet())
        {
            if (entry.getValue() > count)
            {
                count        = entry.getValue();
                mostFrequent = entry.getKey();
            }
        }
        return mostFrequent;
    }


    //--------------------------------------------------------------------
    //If there are M classes,
    //and in the first j things of a category, i[m] have had class m,
    // the class of the (j + l)th thing is encoded assigning a probability
    //  q[m] = (i[m] + alpha)/(j + M * alpha)
    public double transmissionCost(double alpha)
    {
        int numClasses = hist.size();

        int    j      = 0;
        double length = 0;
        for (Integer classCount : hist.values())
        {
            for (int i = 0; i < classCount; i++)
            {
                double p = (i + alpha)/(j + numClasses*alpha);
                length  -= Info.log2(p);
                j++;
            }
        }
        return length;
    }


    //--------------------------------------------------------------------
    @Override
    public String toString()
    {
        return hist.toString() + "\t" +
               Info.cost(probabilityOf(mostProbable()));
    }
}
