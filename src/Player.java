/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.Point;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 *
 * @author Jahan
 */
class Player {
    
    static class Zone{
        public int owner; // -1 no controlled
        Point co=new Point(0,0);
        
        Zone(int X,int Y){
            owner=-1;
            co.setLocation(X, Y);
        }
    }
    
    static class WorldBase{
        
        final Scanner in;
        
        final int ID;
        final int D;
        final int P;
        final int Z;
        
        final List<Zone> z=new ArrayList<>(20);        
        final List<List<Point>> playDrone=new ArrayList<>(4);        
        final List<Point> orders=new ArrayList<>(20);
        
        WorldBase(InputStream inst){
            this.in = new Scanner(inst);
            P = in.nextInt(); // number of players in the game (2 to 4 players)
            ID = in.nextInt(); // ID of your player (0, 1, 2, or 3)
            D = in.nextInt(); // number of drones in each team (3 to 11)
            Z = in.nextInt(); // number of zones on the map (4 to 8)          
            
            for (int i = 0; i < Z; i++) {
                int X = in.nextInt(); // corresponds to the position of the center of a zone. A zone is a circle with a radius of 100 units.
                int Y = in.nextInt();
                z.add(new Zone(X,Y));
            }            
            
            for(int i=0;i<P;i++){
                playDrone.add(new ArrayList<>(20));                
                for(int j=0;j<D;j++){
                    playDrone.get(i).add(new Point(0,0));
                }
            }
            for(int j=0;j<D;j++){
                orders.add(new Point(0,0));
            }
        }
        
        private Point dco(int p,int j){
            return playDrone.get(p).get(j);
        }
        
        public void readFromLoop(){
            for (int i = 0; i < Z; i++) {
                z.get(i).owner=in.nextInt();
            }
            for (int i = 0; i < P; i++) {
                for(int j=0;j<D;j++){
                    dco(i,j).setLocation(in.nextInt(),in.nextInt());
                }
                
            }            
        }
        
        public void writeOrders(PrintStream out){
            for(int i=0;i<D;i++){
                out.println(""+orders.get(i).x+" "+orders.get(i).y);
            }        
        }
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WorldBase wb=new WorldBase(System.in);
        
        
        while(true){
            wb.readFromLoop();
            
            wb.writeOrders(System.out);
        }
        
    }
    
}
