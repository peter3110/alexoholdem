package ao.odds.agglom;

import ao.holdem.persist.GenericBinding;
import ao.odds.eval.eval5.Eval5;
import ao.util.data.Arr;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Date: Sep 29, 2008
 * Time: 5:26:34 PM
 *
 * Histogram of possible strengths of hands.
 */
public class StrengthHist implements Comparable<StrengthHist>
{
    //--------------------------------------------------------------------
    private final int    HIST[];
    private       double mean = Double.NaN;


    //--------------------------------------------------------------------
    public StrengthHist()
    {
        this( new int[Eval5.VALUE_COUNT] );
    }
    private StrengthHist(int hist[])
    {
        HIST = hist;
    }


    //--------------------------------------------------------------------
    public void count(short value)
    {
        HIST[ value ]++;
        mean = Double.NaN;
    }


    //--------------------------------------------------------------------
    public int get(short value)
    {
        return HIST[ value ];
    }

    public int maxCount()
    {
        int maxCount = 0;
        for (int count : HIST)
        {
            if (maxCount < count)
            {
                maxCount = count;
            }
        }
        return maxCount;
    }

    public long totalCount()
    {
        long total = 0;
        for (int count : HIST) total += count;
        return total;
    }


    //--------------------------------------------------------------------
    public long secureHashCode()
    {
        try
        {
            return computeSecureHashCode();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new Error( e );
        }
    }
    private long computeSecureHashCode()
            throws NoSuchAlgorithmException
    {
        MessageDigest m = MessageDigest.getInstance("MD5");

        for (int count : HIST)
        {
            m.update((byte) (count >>> 24));
            m.update((byte) (count >>> 16));
            m.update((byte) (count >>>  8));
            m.update((byte)  count        );
        }

        return new BigInteger(1, m.digest()).longValue();
    }


    //--------------------------------------------------------------------
    public double mean()
    {
//        if (Double.isNaN(mean))
//        {
            mean = calculateMean();
//        }
        return mean;
    }
    private double calculateMean()
    {
        long sum   = 0;
        long count = 0;

        for (int i = 0; i < HIST.length; i++)
        {
            long histCount = HIST[i];

            sum   += histCount * (i + 1);
            count += histCount;
        }

        return (double) sum / count;
    }


    //--------------------------------------------------------------------
    public double nonLossProb(StrengthHist that)
    {
        return -1;
    }


    //--------------------------------------------------------------------
    public int compareTo(StrengthHist o)
    {
        return Double.compare(mean(), o.mean());
    }


    //--------------------------------------------------------------------
    @Override public String toString()
    {
        return Arr.join(HIST, "\t");
    }

    @Override public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StrengthHist strengthHist = (StrengthHist) o;
        return Arrays.equals(HIST, strengthHist.HIST);
    }

    @Override public int hashCode()
    {
        return Arrays.hashCode(HIST);
    }


    //--------------------------------------------------------------------
    public static final int     BINDING_SIZE = Eval5.VALUE_COUNT * 4;
    public static final Binding BINDING      = new Binding();
    public static class Binding extends GenericBinding<StrengthHist>
    {
        public StrengthHist read(TupleInput input)
        {
            int hist[] = new int[ Eval5.VALUE_COUNT ];
            for (int i = 0; i < hist.length; i++) {
                hist[i] = input.readInt();
            }
            return new StrengthHist(hist);
        }

        public void write(StrengthHist o, TupleOutput to)
        {
            for (int i = 0; i < o.HIST.length; i++) {
                to.writeInt( o.HIST[i] );
            }
        }
    }


    //--------------------------------------------------------------------
    public static void main(String[] args)
    {
        StrengthHist h = new StrengthHist();
        System.out.println(h.secureHashCode());

        h.count((short) 0);
        System.out.println(h.secureHashCode());

        h.count((short) 1);
        System.out.println(h.secureHashCode());

        h.count((short) 2);
        System.out.println(h.secureHashCode());
    }
}
