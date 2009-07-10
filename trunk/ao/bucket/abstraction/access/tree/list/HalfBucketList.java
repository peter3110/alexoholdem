package ao.bucket.abstraction.access.tree.list;

import ao.bucket.abstraction.access.tree.BucketList;
import ao.bucket.abstraction.access.tree.PersistentBucketList;
import ao.util.io.Dir;
import ao.util.persist.PersistentBytes;

import java.io.File;

/**
 * Date: Jan 26, 2009
 * Time: 2:51:42 PM
 */
public class HalfBucketList implements PersistentBucketList
{
    //--------------------------------------------------------------------
    private static final File DIR = Dir.get("test");


    //--------------------------------------------------------------------
    public static void main(String[] args)
    {
        int        size = 100;
        BucketList bl   = new HalfBucketList(
                                new File(DIR, "hb.test3.byte"), size);

        for (int i = 0; i < size; i++)
        {
            bl.set(i, (byte) (i % 15));

            if (bl.get(i) != (i % 15)) {
                System.out.println("ERORR at " + i);
            }
        }
    }


    //--------------------------------------------------------------------
    private static final byte LO_MASK     =        0x0f;
    private static final byte HI_MASK     = (byte) 0xf0;
    private static final int  HI_INT_MASK =  0x000000f0;


    //--------------------------------------------------------------------
    private final File   FILE;
    private final byte[] LIST;


    //--------------------------------------------------------------------
    public HalfBucketList(File file, long size)
    {
        FILE = file;
        LIST = retrieveOrCreate( size );
    }

    private byte[] retrieveOrCreate(long size)
    {
        byte[] list = (FILE == null)
                      ? null
                      : PersistentBytes.retrieve(FILE);
        if (list != null) return list;

        list = new byte[ halfIndex(size) +
                         (isLow(size) ? 0 : 1) ];
        return list;
    }


    //--------------------------------------------------------------------
    public byte maxBuckets() {
        return LO_MASK;
    }


    //--------------------------------------------------------------------
    public void set(long index, byte bucket)
    {
        assert bucket < LO_MASK;

        byte current = LIST[ halfIndex(index) ];
        LIST[ halfIndex(index) ] =
                (byte)(isLow(index)
                       ? current & HI_MASK | bucket
                       : current & LO_MASK | bucket << 4);
    }

    public byte get(long index)
    {
        byte pair = LIST[ halfIndex(index) ];
        return (byte)(isLow(index)
                      ?  pair & LO_MASK
                      : (pair & HI_INT_MASK) >>> 4);
    }


    //--------------------------------------------------------------------
    public void flush()
    {
        PersistentBytes.persist(LIST, FILE);
    }


    //--------------------------------------------------------------------
    public static int halfIndex(long index)
    {
        return (int)(index >> 1);
    }
    public static boolean isLow(long index)
    {
        return index % 2 == 0;
    }
}
