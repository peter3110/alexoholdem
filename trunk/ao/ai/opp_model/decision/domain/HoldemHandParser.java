package ao.ai.opp_model.decision.domain;

/**
 *
 */
public class HoldemHandParser
{
//    //--------------------------------------------------------------------
////    private AttributePool    pool       = new AttributePool();
//    private CommunityMeasure thermostat = new CommunityMeasure();
////    private HandParser       parser     = new HandParser();
//
//
//    //--------------------------------------------------------------------
//    public Map<PlayerHandle, PlayerExampleSet>
//            examples(HandHistory inHand)
//    {
//        Map<PlayerHandle, PlayerExampleSet> examples =
//                new HashMap<PlayerHandle, PlayerExampleSet>();
//
//
//
//        return examples;
//    }
//
//    //--------------------------------------------------------------------
////    public HoldemContext nextToActContext(
////            HandHistory hand, PlayerHandle player)
////    {
////        GenericContext ctx = parser.genericNextToActContext(hand, player);
////        return fromGeneric(ctx);
////    }
//
////    private HoldemContext fromGeneric(GenericContext ctx)
////    {
////        if (ctx.round() == BettingRound.PREFLOP)
////        {
////            if (ctx.isHistAware())
////            {
////                return new PreFlopContext(pool, ctx);
////            }
////            else
////            {
////                return new FirstActContext(pool, ctx);
////            }
////        }
////        else
////        {
////            return new PostFlopContext(pool, ctx);
////        }
////    }
//
//
//    //--------------------------------------------------------------------
//    public PlayerExampleSet examples(
//            HandHistory inHand, PlayerHandle forPlayer)
//    {
//        PlayerExampleSet examples = new PlayerExampleSet();
//        for (GenericContext ctx :
//                parser.genericCasesFor(inHand, forPlayer))
//        {
//            examples.add(fromGeneric(ctx),
//                         pool.fromEnum( ctx.currAct() ));
//        }
//        return examples;
//    }
//
//
//    //--------------------------------------------------------------------
//    public List<Example<SimpleAction>> postflopExamples(
//            HandHistory hand,
//            PlayerHandle player)
//    {
//        List<Example<SimpleAction>> examples =
//                new ArrayList<Example<SimpleAction>>();
//
//        HandParser parser = new HandParser();
//        for (GenericContext ctx :
//                parser.genericCasesFor(hand, player))
//        {
//            if (! ctx.isHistAware()) continue;
//            if (ctx.round() == BettingRound.PREFLOP) continue;
//
//            ContextImpl decisionContext = asDecisionContext(ctx);
//            Example<SimpleAction> decisionExample =
//                    decisionContext.withTarget(
//                            pool.fromEnum( ctx.currAct() ));
//            examples.add( decisionExample );
//        }
//
//        return examples;
//    }
//
//    private ContextImpl asDecisionContext(GenericContext ctx)
//    {
//        Collection<Attribute> attrs = new ArrayList<Attribute>();
//
//        attrs.add(pool.fromUntyped(
//                "Committed This Round",
//                ctx.committedThisRound()));
//
//        attrs.add(pool.fromEnum(
//                BetsToCall.fromBets(ctx.betsToCall())));
//
//        attrs.add(pool.fromEnum( ctx.round() ));
//
//        attrs.add(pool.fromUntyped(
//                "Last Bets Called > 0",
//                ctx.lastBetsToCall() > 0));
////        attrs.add(pool.fromUntyped(
////                "Last Bets Called",
////                ctx.lastBetsToCall()));
//
////        attrs.add(pool.fromUntyped(
////                "Last Action", ctx.lastAct()));
//        attrs.add(pool.fromUntyped(
//                "Last Act: Bet/Raise",
//                ctx.lastAct() == SimpleAction.RAISE));
//
//        attrs.add(pool.fromEnum(
//                ActivePosition.fromPosition(
//                        ctx.numActiveOpps(), ctx.activePosition())));
//
//        attrs.add(pool.fromEnum(
//                ActiveOpponents.fromActiveOpps(
//                        ctx.numActiveOpps())));
//
//        attrs.add(pool.fromEnum(
//                BetRatio.fromBetRatio(
//                        ctx.betRatio())));
//        attrs.add(pool.fromEnum(
//                PotOdds.fromPotOdds(
//                        ctx.immedatePotOdds())));
//        attrs.add(pool.fromEnum(
//                PotRatio.fromPotRatio(
//                        ctx.potRatio())));
//
//        attrs.add(pool.fromEnum(
//                Heat.fromHeat(
//                        thermostat.heat(ctx.community()))));
//
//        return new ContextImpl(attrs);
//    }
}