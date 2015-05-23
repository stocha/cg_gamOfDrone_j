
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
public class BarePlayerImpl {

    public static class Bot extends BaseBotLib.BotBase<BaseBotLib.DroneBase,BaseBotLib.ZoneBase,BaseBotLib.PlayerBase>{

        public Bot(InputStream inst) {
            super(inst);
        }

        @Override
        void doPrepareOrder() {
            
        }

        @Override
        BaseBotLib.DroneBase newdrone() {
            return new BaseBotLib.DroneBase(){};
        }

        @Override
        BaseBotLib.ZoneBase newzone() {
            return new BaseBotLib.ZoneBase(){};
        }

        @Override
        BaseBotLib.PlayerBase newplayer() {
            return new BaseBotLib.PlayerBase(){};
        }
    }
    
    public static void main(String[] args) {
        BaseBotLib.BotBase theBot=new Bot(System.in);
        
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
