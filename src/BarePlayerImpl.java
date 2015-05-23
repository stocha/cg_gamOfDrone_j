
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

    public static class Bot extends PlayerLib.BotBase<PlayerLib.DroneBase,PlayerLib.ZoneBase,PlayerLib.PlayerBase>{

        public Bot(InputStream inst) {
            super(inst);
        }

        @Override
        void doPrepareOrder() {
            
        }

        @Override
        PlayerLib.DroneBase newdrone() {
            return new PlayerLib.DroneBase(){};
        }

        @Override
        PlayerLib.ZoneBase newzone() {
            return new PlayerLib.ZoneBase(){};
        }

        @Override
        PlayerLib.PlayerBase newplayer() {
            return new PlayerLib.PlayerBase(){};
        }
    }
    
    public static void main(String[] args) {
        PlayerLib.BotBase theBot=new Bot(System.in);
        
        double maxT=0;

        while (true) {
            long t0 = System.currentTimeMillis();
            theBot.readTurn();
            theBot.doPrepareOrder();
            theBot.writeOrders(System.out);

            long t1 = System.currentTimeMillis();
            double t = t1 - t0;
            if(maxT <t) maxT=t;
            System.err.println("temps mili " + t+" maxT "+maxT);
            System.gc();
        }

    }        
}
