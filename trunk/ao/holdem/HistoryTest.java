package ao.holdem;

import ao.holdem.def.history_bot.BotHandle;
import ao.holdem.def.history_bot.HistoryBot;
import ao.holdem.def.state.action.Action;
import ao.holdem.history.HandHistory;
import ao.holdem.history.Snapshot;
import ao.holdem.history.persist.PlayerHandleLookup;
import ao.holdem.history_game.Dealer;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.Arrays;

/**
 *
 */
public class HistoryTest
{
    //--------------------------------------------------------------------
    @Inject Provider<Dealer>   dealerProvider;
    @Inject PlayerHandleLookup players;


    //--------------------------------------------------------------------
    public void historyTest()
    {        
        Dealer dealer = dealerProvider.get();
        dealer.configure(Arrays.<BotHandle>asList(
                new BotHandle(players.lookup("a"), new DummyBot()),
                new BotHandle(players.lookup("b"), new DummyBot())));

        dealer.play();
    }


    //--------------------------------------------------------------------
    private static class DummyBot implements HistoryBot
    {
        public Action act(HandHistory hand, Snapshot env)
        {
            return Action.CHECK_OR_CALL;
        }
    }


    //--------------------------------------------------------------------
//    public static void main(String args[]) throws Exception
//    {
//        try
//        {
//            new HistoryTest().historyTest();
//        }
//        catch (Exception e)
//        {
//            if (e.getCause() instanceof BatchUpdateException)
//            {
//                ((BatchUpdateException) e.getCause())
//                        .getNextException()
//                        .printStackTrace();
//            }
//            else
//            {
//                e.printStackTrace();
//            }
//        }
//    }
}