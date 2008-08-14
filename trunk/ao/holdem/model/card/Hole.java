package ao.holdem.model.card;

import ao.bucket.index.iso_cards.IsoHole;
import ao.bucket.index.iso_cards.Ordering;
import ao.bucket.index.iso_cards.WildCard;
import ao.bucket.index.iso_case.HoleCase;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;


/**
 * Hole cards.
 *
 * Both cards are defined (not null).
 */
public class Hole
{
    //--------------------------------------------------------------------
    private static final Hole[][] VALUES = new Hole[52][52];
    static
    {
        Card cards[] = Card.VALUES;
        for (int i = 0; i < 52; i++)
        {
            for (int j = 0; j < 52; j++)
            {
                if (i == j) continue;
                VALUES[i][j] = new Hole(cards[i], cards[j]);
            }
        }
    }


    //--------------------------------------------------------------------
    public static Hole newInstance(Card a, Card b)
    {
        return VALUES[ a.ordinal() ][ b.ordinal() ];
    }


    //--------------------------------------------------------------------
    private Card A;
    private Card B;


    //--------------------------------------------------------------------
    private Hole(Card a, Card b)
    {
        assert a != null && b != null;
        assert a != b;

        A = a;
        B = b;
    }

    
    //--------------------------------------------------------------------
    public boolean ranks(Rank rankA, Rank rankB)
    {
        return A.rank() == rankA && B.rank() == rankB ||
               A.rank() == rankB && B.rank() == rankA;
    }

    public boolean ranks(Rank rank)
    {
        return A.rank() == rank || B.rank() == rank;
    }

    public boolean suited()
    {
        return A.suit() == B.suit();
    }

    public boolean paired()
    {
        return A.rank() == B.rank();
    }

    public boolean hasXcard()
    {
        return A.rank().ordinal() < Rank.JACK.ordinal() ||
                B.rank().ordinal() < Rank.JACK.ordinal();
    }

    public boolean contains(Card card)
    {
        return A == card || B == card;
    }


    //--------------------------------------------------------------------
    public Card a()
    {
        return A;
    }
    public Card b()
    {
        return B;
    }


    //--------------------------------------------------------------------
    public Card hi()
    {
        assert !paired();
        return (A.rank().compareTo( B.rank() ) > 0
                ? A : B);
    }
    public Card lo()
    {
        assert !paired();
        return (A.rank().compareTo( B.rank() ) < 0
                ? A : B);
    }

    public Card[] asArray()
    {
        return new Card[]{A, B};
    }


    //--------------------------------------------------------------------
    public IsoHole isomorphism()
    {
        Card     a     = paired() ? A : hi();
        Card     b     = paired() ? B : lo();
        Ordering order = ordering();

        return new IsoHole(HoleCase.newInstance(this),
                           new WildCard(a.rank(), order.asWild(a.suit())),
                           new WildCard(b.rank(), order.asWild(b.suit())));
    }

    public Ordering ordering()
    {
        return paired()
                ? Ordering.pair(A.suit(), B.suit())
                : suited()
                  ? Ordering.suited  (A.suit())
                  : Ordering.unsuited(hi().suit(), lo().suit());
    }


    //--------------------------------------------------------------------
    public String toString()
    {
        return "[" + A + ", " + B + "]";
    }

    public boolean equals(Object o)
    {
        //if (this == o) return true;
        if (o == null ||
            getClass() != o.getClass()) return false;

        Hole hole = (Hole) o;
        return A == hole.A && B == hole.B ||
               A == hole.B && B == hole.A;
    }

    public int hashCode()
    {
        int result;
        result  = 31 * A.hashCode();
        result += 31 * B.hashCode();
        return result;
    }


    //--------------------------------------------------------------------
    public static final Binding BINDING = new Binding();
    public static class Binding extends TupleBinding
    {
        public Hole entryToObject(TupleInput input)
        {
            byte cardA = input.readByte();
            byte cardB = input.readByte();
            return Hole.VALUES[ cardA ][ cardB ];
        }

        public void objectToEntry(Object object, TupleOutput output)
        {
            Hole hole = (Hole) object;

            if (hole == null)
            {
                output.writeByte( (byte) 0                );
                output.writeByte( (byte) 0                );
            }
            else
            {
                output.writeByte( (byte) hole.A.ordinal() );
                output.writeByte( (byte) hole.B.ordinal() );
            }
        }
    }
}