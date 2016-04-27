  * To determine who wins at the showdown, the cards of the players must be compared, and a winner determined.
  * There are 7 cards to consider, 2 hole cards and 5 community cards.<br> You have to select the best 5 cards out of those 7, and compare them to your opponents' best 5.</li></ul>

This leads us to the first problem, how to compare two sets of 5 cards:<br>
<ul><li>The order of cards is not significant, only the set of cards.<br>
</li><li>If we look at all possible 5 card combinations (52 choose 5 = 2,598,960), and sort them by how strong they are, we get 7,462 distinct strength levels.  One way to evaluate a 5 card set is by assigning it an index of strength, it would be in the range [0,  7,462).<br> <<Need example for lowest and highest hand strength, gotta actually check it>><br>
</li><li>This project does not (yet) contain a <i>direct</i> solution for that problem, although I did see one once on a message board.  The Eval5 class solves this problem indirectly <b>by cheating</b> and actually looking up values in a pre-computed table using a perfect-hashing scheme.<br>
</li><li>You have to check out: <a href='http://www.suffecool.net/poker/evaluator.html'>http://www.suffecool.net/poker/evaluator.html</a>. <br> I got most of the the 5 card evaluation ideas from there, Eval5 is pretty much a direct re-implementation.  It uses a very clever scheme to achieve extremely high performance and low memory requirements.<br>
</li><li><code>Eval5.valueOf(a: Card, b: Card, c: Card, d: Card, e: Card): short [0, 7,462)</code> evaluates the given 5 card combination and returns the hand strength.<br>
</li><li>The lookup tables for Eval5 are provided with the application.  This is the only lookup table that is part of the source distribution, all other ones can be rebuilt, often times with the help of this one.<br>
</li><li><code>HandRank.fromValue(handStrength: short): HandRank</code> can be used to assign the hand a human-friendly name: high card, pair, two pair, trips, straight, flush, full house, quad, straight flush.</li></ul>

The second problem is how to use the 5 card evaluator to evaluate a 7 card hand:<br>
<ul><li><code>EvalSlow.valueOf(Card[5 .. 7]): short</code> simply tries all of the possible 5 card combinations from the 7 cards, there are 7 choose 5 = 21 in total.<br>
</li><li>Eval7Faster uses a technique from:<br> <a href='http://forumserver.twoplustwo.com/showflat.php?Cat=0&Number=8513906&page=0&fpart=all&vc=1'>http://forumserver.twoplustwo.com/showflat.php?Cat=0&amp;Number=8513906&amp;page=0&amp;fpart=all&amp;vc=1</a>
</li><li>Their algorithm is absolutely ingenious, and allows for evaluation a 7 card hand with very few instructions.  The key is to use each card sequence as an index into the next card sequence, and do this in a way that removes redundancies that are introduced due to Suit isomorphisms (more about this in section about canonical card sequence indexing).<br>
</li><li>Eval7Fast is another precomputed table solution, but it doesn't work as well as Eval7Faster.  It is based on a perfect hashing scheme, which is a great general purpose solution.