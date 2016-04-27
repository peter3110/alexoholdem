  * There are Suit isomorphisms, where for example "A5 Suited" could mean one of 4 equally good hands.
  * The scheme used in is project is based on the following paper:<br> <a href='http://www.cs.cmu.edu/~sandholm/gs3.aaai07.pdf'>http://www.cs.cmu.edu/~sandholm/gs3.aaai07.pdf</a> section "Exploiting suit isomorphism".<br>
<ul><li>The main idea is to use the co-lexicographic index function within each set of suit-strength rankingings.<br> This part of the coding was tedious and was possibly the hardest part of the project to get right (not that we're sure everything works right :D).<br>
</li><li>There is a friendly public API that hides the complexity.<br>
<ul><li><code>CanonHole</code> is like a <code>Hole</code> (you can get it from <code>Hole.asCanon()</code>) but it takes into account suit isomorphisms when computing equality.  Each CanonHole has a canonical index in the range [0, 169).<br>
</li><li><code>Flop</code> work in a similar way, but with 2 + 3 = 5 cards.  Flops also have canon indexes.<br> For any two CanonHole indexes <code>i, j st. i &lt; j</code> all of the Flops in between i and j will has consecutive canonical indexes.<br>
<ul><li>You can get a <code>Flop</code> directly using the constructor or by using <code>CanonHole.addFlop(...)</code>
</li></ul></li><li><code>Turn</code>, see: <code>CanonHole</code>, <code>Flop</code><br> <code>Flop.addTurn(...): Turn</code>
</li><li><code>River</code>, see: <code>CanonHole</code>, <code>Flop</code>, <code>Turn</code><br> <code>Turn.addRiver(...): River</code>