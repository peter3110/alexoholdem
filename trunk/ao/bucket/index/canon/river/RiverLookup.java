package ao.bucket.index.canon.river;

import ao.bucket.index.canon.turn.TurnLookup;
import ao.util.math.Calc;
import org.apache.log4j.Logger;

/**
 * Date: Sep 16, 2008
 * Time: 3:53:13 PM
 */
public class RiverLookup
{
    //--------------------------------------------------------------------
    private static final Logger LOG =
            Logger.getLogger(RiverLookup.class);

    private static final int  SHRINK    = 3;
    private static final int  CHUNK     = (1 << (SHRINK));
    private static final int  OFFSETS[] = computeOffsets();

    public  static final long CANONS    = 2428287420L;


    //--------------------------------------------------------------------
    public static void main(String[] args)
    {
        offset(55190537);
    }


    //--------------------------------------------------------------------
    public static long offset(int canonTurn)
    {
        int  index = canonTurn / CHUNK;
        long base  = Calc.unsigned(OFFSETS[ index ]);

        int  addend = 0;
        int  delta  = canonTurn % CHUNK;
        for (int i = 0; i < delta; i++)
        {
            addend += RiverRawLookup.caseSet(index * CHUNK + i).size();
        }

        return base + addend;
    }


    //--------------------------------------------------------------------
    private static int[] computeOffsets()
    {
        RiverRawLookup.caseSet(0); // precompute
        LOG.info("computeOffsets");

        int offset    = 0;
        int offsets[] =
                new int[ TurnLookup.CANONS / CHUNK + 1 ];

        int prevIndex = -1;
        for (int i = 0; i < TurnLookup.CANONS; i++)
        {
//            if (i % 1000000 == 1)
//            {
//                System.out.print(".");
//                System.out.flush();
//            }

            int index = i / CHUNK;
            if (prevIndex != index)
            {
                offsets[ index ] = offset;
                prevIndex = index;
            }
            RiverCaseSet caseSet = RiverRawLookup.caseSet(i);
            offset += caseSet.size();
        }

//        System.out.println();
        return offsets;
    }
}
