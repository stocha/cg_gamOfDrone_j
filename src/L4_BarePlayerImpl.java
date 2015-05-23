
import java.io.InputStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jahan
 */
public class L4_BarePlayerImpl {

    public static class Bot extends L1_BaseBotLib.BotBase<L1_BaseBotLib.DroneBase,L1_BaseBotLib.ZoneBase,L1_BaseBotLib.PlayerBase>{

        public Bot(InputStream inst) {
            super(inst);
        }

        @Override
        void doPrepareOrder() {
            
        }

        @Override
        L1_BaseBotLib.DroneBase newdrone() {
            return new L1_BaseBotLib.DroneBase(){};
        }

        @Override
        L1_BaseBotLib.ZoneBase newzone() {
            return new L1_BaseBotLib.ZoneBase(){};
        }

        @Override
        L1_BaseBotLib.PlayerBase newplayer() {
            return new L1_BaseBotLib.PlayerBase(){};
        }
    }
    
    public static void main(String[] args) {
        L1_BaseBotLib.BotBase theBot=new Bot(System.in);
        
        double maxT=0;

        while (true) {
            long t0 = System.currentTimeMillis();
            theBot.readTurn();
            theBot.doPrepareOrder();
            theBot.writeOrders(System.out);

            long t1 = System.currentTimeMillis();
            double t = t1 - t0;
            if(maxT <t) maxT=t;
            System.err.println("------------------------------");
            System.err.println("temps mili " + t+" maxT "+maxT);
            System.gc();
        }

    }        
}
