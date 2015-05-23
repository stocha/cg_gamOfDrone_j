
import java.awt.Point;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
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
    
    private static boolean debug_base=true;

    static final int supposedMaxZone = 20;
    static final int maxDrones = 13;
    static final int supposedMaxTurn=700;

    interface Factory<E> {

        E create();
    }

    public static class GamePos implements GraphicLib2d.WithCoord {

        final Point cord = new Point(0, 0);

        @Override
        public Point cord() {
            return cord;
        }

        public void set(Point c) {
            cord.setLocation(c);
        }
    }

    public abstract static class DroneBase extends Player.PlayerLib.GamePos {

        final ArrayDeque<Player.PlayerLib.GamePos> coords = new ArrayDeque<>(supposedMaxTurn);
        final ArrayDeque<Player.PlayerLib.GamePos> speeds = new ArrayDeque<>(supposedMaxTurn);

        int id;

        @Override
        public String toString() {
            return "DroneBase{" + "coords=" + coords + ", speeds=" + speeds + ", id=" + id + '}' ;
        }
        
        

    }

    public abstract static class ZoneBase extends Player.PlayerLib.GamePos {

        int id;
        int owner;
        int turnowned;

        @Override
        public String toString() {
            return "ZoneBase{" + "id=" + id + ", owner=" + owner + ", turnowned=" + turnowned + '}';
        }
        
        

    }

    public abstract static class PlayerBase {

        int id;
        int nbControlled;
        final ArrayDeque<List<Integer>> controlHistorique = new ArrayDeque<>(supposedMaxTurn);

        @Override
        public String toString() {
            return "PlayerBase{" + "id=" + id + ", nbControlled=" + nbControlled + ", controlHistorique=" + controlHistorique + '}';
        }
        
        
    }

    public static abstract class BotBase<Dt extends DroneBase, Zt extends ZoneBase, Pt extends PlayerBase> {

        final Scanner in;

        final int ID;
        final int D;
        final int P;
        final int Z;

        final double avg_zoneForPlayer;
        final double avg_dronePerZone;
        private final Point[] _worldZoneCoord;        

        int _turn_Number;
        final int[] _turn_scoreControl;

        final int[] _owner;
        private final Point[][] _playerDronesCords;
        final Point[] _orders;        

        final List<List<Dt>> playerDrones;
        final List<Zt> zones;
        final List<Pt> players;

        public BotBase(InputStream inst) {
            this.in = new Scanner(inst);
            P = in.nextInt(); // number of players in the game (2 to 4 players)
            ID = in.nextInt(); // ID of your player (0, 1, 2, or 3)
            D = in.nextInt(); // number of drones in each team (3 to 11)
            Z = in.nextInt(); // number of zones on the map (4 to 8)       
            
            _worldZoneCoord=new Point[Z];
            for (int i = 0; i < Z; i++) {
                int X = in.nextInt(); // corresponds to the position of the center of a zone. A zone is a circle with a radius of 100 units.
                int Y = in.nextInt();
                _worldZoneCoord[i]=new Point(X, Y);
            }            

            _owner = new int[Z];
            _playerDronesCords = new Point[P][];
            for (int p = 0; p < P; p++) {
                _playerDronesCords[p] = new Point[D];
                for (int d = 0; d < D; d++) {
                    _playerDronesCords[p][d] = new Point(0, 0);
                }
            }

            _orders = new Point[D];
            for (int d = 0; d < D; d++) {
                _orders[d] = new Point(20, 20);
            }

            _turn_scoreControl = new int[P];

            avg_zoneForPlayer = (double) Z / (double) P;
            avg_dronePerZone = (double) D / (double) Z;

            players = new ArrayList<>(P);
            zones = new ArrayList<>(Z);
            playerDrones = new ArrayList<>(P);
            for (int p = 0; p < P; p++) {
                playerDrones.add(new ArrayList<>(D));
            }

        }

        abstract void doPrepareOrder();

        abstract Dt newdrone();

        abstract Zt newzone();

        abstract Pt newplayer();

        private boolean init = false;

        private void alloc() {
            init = true;
            for (int p = 0; p < P; p++) {
                Pt n = newplayer();
                n.id = p;
                players.add(n);
            }

            for (int z = 0; z < Z; z++) {
                Zt n = newzone();
                n.id = z;
                zones.add(n);
            }
            for (int p = 0; p < P; p++) {
                for (int d = 0; d < D; d++) {
                    Dt n = newdrone();
                    n.id = d;
                    playerDrones.get(p).add(n);
                }
            }
        }

        public void readTurn() {
            if (!init) {
                alloc();
            }

            for (int i = 0; i < Z; i++) {
                int own = in.nextInt();
                _owner[i] = own;
            }

            for (int i = 0; i < P; i++) {
                for (int j = 0; j < D; j++) {
                    _playerDronesCords[i][j].setLocation(in.nextInt(), in.nextInt());
                }
                _turn_scoreControl[i] = 0;
            }

            for (int i = 0; i < Z; i++) {
                int n = _owner[i];
                if (n == -1) {
                    continue;
                }
                _turn_scoreControl[n]++;
            }

            for (int d = 0; d < D; d++) {
                _orders[d].setLocation(20, 20);
            }
            
            
            // Ventile vers user
            
            // Les zones
            for (int i = 0; i < Z; i++) {
                int n = _owner[i];
                Zt it=zones.get(i);
                if(it.owner==n){
                    it.turnowned++;
                }else{
                    it.turnowned=0;
                }
                it.owner=n;
                
                it.cord.setLocation(this._worldZoneCoord[i]);                
            }      
            
            // Les players
            for (int p = 0; p < P; p++) {
                Pt it=players.get(p);
                
                
                List<Integer> owned=new ArrayList<>(Z);
                for(int z=0;z<Z;z++){
                    if(_owner[z]==it.id) owned.add(z);
                }
                it.controlHistorique.add(owned);             
            }              
            
            if(debug_base){
                System.err.println(""+players);
                System.err.println(""+zones);
                System.err.println(""+playerDrones);
                
            }
        }

        public void writeOrders(PrintStream out) {
            doPrepareOrder();

            for (int d = 0; d < D; d++) {
                out.println("" + _orders[d].x + " " + _orders[d].y);
            }

            _turn_Number++;
        }

    }
}
