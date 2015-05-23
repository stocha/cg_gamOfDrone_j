
import java.awt.Point;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jahan
 */
public class PlayerLib {
    
    public static class GamePos implements GraphicLib2d.WithCoord{
        final Point cord=new Point(0,0);
        
        @Override
        public Point cord() {
            return cord;
        }
        
        public void set(Point c){
            cord.setLocation(c);
        }
    }
        
    public static class DroneBase extends GamePos{

    }
    
    public static class ZoneBase extends GamePos{

    }    

    public static abstract class BotBase{                
        final Scanner in;

        final int ID;
        final int D;
        final int P;
        final int Z;
        
        int _turn_Number;
        final int[] _turn_scoreControl;
        
        final int[] _owner;
        final Point[][] _playerDronesCords;
        final Point[] _orders;
        
        public BotBase(InputStream inst){
            this.in = new Scanner(inst);
            P = in.nextInt(); // number of players in the game (2 to 4 players)
            ID = in.nextInt(); // ID of your player (0, 1, 2, or 3)
            D = in.nextInt(); // number of drones in each team (3 to 11)
            Z = in.nextInt(); // number of zones on the map (4 to 8)       
            
            _owner=new int[Z];
            _playerDronesCords=new Point[P][];
            for(int p=0;p<P;p++){
                _playerDronesCords[p]=new Point[D];
                for(int d=0;d<D;d++){
                    _playerDronesCords[p][d]=new Point(0,0);
                }
            }
            
            _orders=new Point[D];
            for(int d=0;d<D;d++){
                _orders[d]=new Point(20,20);
            }
            
            _turn_scoreControl=new int[P];
            
        }
        
        abstract void doPrepareOrder();
            
        
        public void readTurn(){
            for (int i = 0; i < Z; i++) {
                int own=in.nextInt();
                _owner[i] = own;                
            }            
            
            for (int i = 0; i < P; i++) {
                for (int j = 0; j < D; j++) {
                    _playerDronesCords[i][j].setLocation(in.nextInt(), in.nextInt());
                }
                _turn_scoreControl[i]=0;
            }     
            
            for (int i = 0; i < Z; i++) {
                int n=_owner[i];
                if(n==-1) continue;
                _turn_scoreControl[n]++;
            }
            
            
            for(int d=0;d<D;d++){
                _orders[d].setLocation(20,20);
            }            
        }
        
        public void writeOrders(PrintStream out){
            doPrepareOrder();
            
            for (int d = 0; d < D; d++) {
                out.println("" + _orders[d].x + " " + _orders[d].y);
            }           
            
            _turn_Number++;
        }
    
    }
}
