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
    
    // Gather attack : essentiel pour ameliorer l'existant. Une attaque se fait depuis une base, et non dans le vide
    
    static final boolean debug_attack_plan=true;
    static final boolean debug_defense_plan=true;
    static final boolean debug_scores=false;

    final static int DISTCONT = 100;

    /**
     * Forces presentes ou future
     */
    static class Forces {

        public static final int nbTurns = 70;

        final int Z;
        int v[][];

        Forces(int Z) {
            this.Z = Z;
            v = new int[Z][];
            for (int i = 0; i < Z; i++) {
                v[i] = new int[nbTurns];
            }
        }

        public void calc(List<Zone> zones, List<Point> drones) {
            int D = drones.size();

            for (int t = 0; t < nbTurns; t++) {
                for (int z = 0; z < Z; z++) {
                    v[z][t] = 0;
                    for (int d = 0; d < D; d++) {
                        double dist = zones.get(z).co.distanceSq(drones.get(d));
                        double dfut = (t+1) * DISTCONT;
                        dfut *= dfut;

                        if (dist <= dfut) {
                            v[z][t]++;
                        }
                    }
                }
            }

        }

        public void set0() {
            for (int t = 0; t < nbTurns; t++) {
                for (int z = 0; z < Z; z++) {
                    v[z][t] = 0;
                }
            }
        }

        public void maxFrom(Forces f) {
            for (int t = 0; t < nbTurns; t++) {
                for (int z = 0; z < Z; z++) {
                    v[z][t] = Math.max(f.v[z][t], v[z][t]);
                }
            }
        }

    }

    static class Zone {

        public int owner; // -1 no controlled
        Point co = new Point(0, 0);

        Zone(int X, int Y) {
            owner = -1;
            co.setLocation(X, Y);
        }
    }

    static class Orders {

        final int D;
        final Point orders[];
        final boolean done[];
        
        int nbCurrDone=0;
        
        List<Integer> filterDroneSelect=new ArrayList<>(20);

        Orders(int D) {
            this.D = D;
            done = new boolean[D];

            orders = new Point[D];
            for (int i = 0; i < D; i++) {
                orders[i] = new Point(2000, 100 + i * 100);
            }
        }

        public void resetTurn() {
            for (int i = 0; i < D; i++) {
                done[i] = false;
            }
            
            nbCurrDone=0;
        }

        public Point get(int i) {
            return orders[i];
        }
        
        public boolean filterSelectClosestTo(Point dest, List<Point> pos, int nb){
            filterDroneSelect.clear();
            if(nbCurrDone+nb > D){
                return false;
            }
            
            for (int sed = 0; sed < nb; sed++) {
                int it=sendClosestTo(dest, pos);
                filterDroneSelect.add(it);
            }
            
            return true;
        }
        
        public int testMinMutualDist(List<Integer> them,List<Point> pos){
            double r=0;
            for(int i=0;i<them.size();i++){
                for(int j=0;j<them.size();j++){
                    int a=them.get(i);
                    int b=them.get(j);
                    
                    double di=pos.get(a).distanceSq(pos.get(b));
                    if(di>r) r=di;
                }                
            }
            return (int)r;
        }
        
         public void sendPacket(Point dest,List<Integer> them) {
             for(Integer i : them){
                 orders[i].setLocation(dest);
             }
         }

        public boolean sendPacketClosestTo(Point dest, List<Point> pos, int nb) {

            boolean suc = true;
            if(nbCurrDone+nb > D){
                return false;
            }
            
            for (int sed = 0; sed < nb; sed++) {
                suc &= (sendClosestTo(dest, pos)!=-1);
                if(!suc) throw new RuntimeException("Impossible path");
            }

            return suc;
        }

        public int sendClosestTo(Point dest, List<Point> pos) {

            double minDist = Integer.MAX_VALUE;
            int found = -1;
            for (int i = 0; i < D; i++) {
                if (done[i]) {
                    continue;
                }

                double dist = dest.distanceSq(pos.get(i));
                if (dist < minDist) {
                    minDist = dist;
                    found = i;
                }
            }
            if (found == -1) {
                return found;
            }
            
            nbCurrDone++;

            done[found] = true;
            orders[found].setLocation(dest);

            return found;

        }

    }

    static class WorldBase {

        final Scanner in;

        final int ID;
        final int D;
        final int P;
        final int Z;

        final List<Zone> z = new ArrayList<>(20);
        final List<List<Point>> playDrone = new ArrayList<>(4);

        final List<Forces> fo = new ArrayList<>(20);

        final Forces maxMe;
        final Forces maxOther;
        
        final List<Zone> filterZone= new ArrayList<>(20);
        
        final Integer scores[];
        final int controlled[];

        Orders orders;
        
        int numTurn=0;

        WorldBase(InputStream inst) {
            this.in = new Scanner(inst);
            P = in.nextInt(); // number of players in the game (2 to 4 players)
            ID = in.nextInt(); // ID of your player (0, 1, 2, or 3)
            D = in.nextInt(); // number of drones in each team (3 to 11)
            Z = in.nextInt(); // number of zones on the map (4 to 8)          
            
            scores=new Integer[P];
            for(int i=0;i<scores.length;i++){
                scores[i]=0;
            }
            controlled=new int[P];

            for (int i = 0; i < Z; i++) {
                int X = in.nextInt(); // corresponds to the position of the center of a zone. A zone is a circle with a radius of 100 units.
                int Y = in.nextInt();
                z.add(new Zone(X, Y));
            }

            for (int i = 0; i < P; i++) {
                playDrone.add(new ArrayList<>(20));
                for (int j = 0; j < D; j++) {
                    playDrone.get(i).add(new Point(0, 0));
                }

                fo.add(new Forces(Z));
            }

            maxMe = new Forces(Z);
            maxOther = new Forces(Z);

            orders = new Orders(D);
        }
        
        private int leadScore(){
            int mine=scores[ID];
            
            int diff=Integer.MAX_VALUE;
            for(int i=0;i<P;i++){
                if(i==ID) continue;
                int diloc=mine-scores[i];
                if(diloc < diff) diff=diloc;
            
            }
            return diff;
        }
        
        private int leadControl(){
            int mine=controlled[ID];
            
            int diff=Integer.MAX_VALUE;
            for(int i=0;i<P;i++){
                if(i==ID) continue;
                int diloc=mine-controlled[i];
                if(diloc < diff) diff=diloc;
            
            }
            return diff;            
        }

        private Point dco(int p, int j) {
            return playDrone.get(p).get(j);
        }

        public void readFromLoop() {
            numTurn++;
            
            for (int i = 0; i < Z; i++) {
                int own=in.nextInt();
                z.get(i).owner = own;
                
                if(own!=-1){
                    controlled[z.get(i).owner]++;
                    scores[own]+=1;
                }
                
                if(debug_scores)
                System.err.println("scores "+java.util.Arrays.asList(scores));
            }
            for (int i = 0; i < P; i++) {
                for (int j = 0; j < D; j++) {
                    dco(i, j).setLocation(in.nextInt(), in.nextInt());
                }

            }
        }
        
        List<Zone> filterZoneOwner(){
            filterZone.clear();
            
            for(int i=0;i<Z;i++){
                if(z.get(i).owner==ID || z.get(i).owner==-1){
                    filterZone.add(z.get(i));
                }
            
            }
            
            return filterZone;
        }
        
        Zone closestTo(Point dst,List<Zone> them){
            double minDist=Integer.MAX_VALUE;
            int r=-1;
            
            for(int i=0;i<them.size();i++){
                double d=them.get(i).co.distanceSq(dst);
                if(d<minDist){
                    minDist=d;
                    r=i;
                }
            }
            return them.get(r);
        }

        public void writeOrders(PrintStream out) {
            for (int i = 0; i < D; i++) {
                out.println("" + orders.get(i).x + " " + orders.get(i).y);
            }
        }

        public void turnCalc() {
            for (int i = 0; i < P; i++) {
                fo.get(i).calc(z, playDrone.get(i));
            }

            maxMe.set0();
            maxOther.set0();

            for (int i = 0; i < P; i++) {
                if (i == ID) {
                    maxMe.maxFrom(fo.get(i));
                } else {
                    maxOther.maxFrom(fo.get(i));
                }
            }

        }

        public void turnPlanning() {

            int drLeft = D;            
            orders.resetTurn();
            
            boolean targPlaned[]=new boolean[Z];
            for(int i=0;i<Z;i++){
                targPlaned[i]=false;
            }
            
            int leadControlled=leadControl();
            int leadScores=leadScore();
            
                boolean attackEnabled=(leadControlled < 0 || (leadControlled==0 && leadScores <= 0));
                if(debug_attack_plan)
                    System.err.println("Attack is ON "+attackEnabled+"  leadC "+leadControlled+" leadS "+leadScores);            

            for (int t = 0; t < Forces.nbTurns; t++) {
                
                if(leadScores>-10 && leadScores> -2)
                for (int i = 0; i < Z; i++) {

                    int mother = maxOther.v[i][t];
                    int mme = maxMe.v[i][t];
                    
                    // Defense
                    if (mme >= mother && mother>=1 && (z.get(i).owner == ID || z.get(i).owner==-1 )&& !targPlaned[i]) {
                        boolean success = true;
                        success &= orders.sendPacketClosestTo(z.get(i).co, playDrone.get(ID), mother);                        
                        if (!success) {
                            break;
                        }
                        targPlaned[i]=true;
                        
                        if(debug_defense_plan)
                        System.err.println("DEFENSE OF "+i+" WITH "+mother+" crew"+" t="+t);
                    }                    
                }
                
                if(attackEnabled)
                for (int i = 0; i < Z; i++) {
                    int mother = maxOther.v[i][t];
                    int mme = maxMe.v[i][t];                                      
                    
                    // Attack
                    if (mme > mother && z.get(i).owner != ID && !targPlaned[i]) {
                        boolean success = orders.filterSelectClosestTo(z.get(i).co, playDrone.get(ID), mother + 1);
                        //success &= orders.sendPacketClosestTo(z.get(i).co, playDrone.get(ID), mother + 1);                        
                        if (!success) {
                            break;
                        }
                        targPlaned[i]=true;
                        
                        boolean gathered=orders.testMinMutualDist(orders.filterDroneSelect, playDrone.get(ID)) < 50*50;
                        
                        List<Zone> owned=filterZoneOwner();
                        
                        if(gathered || owned.size()==0 || numTurn<Integer.MAX_VALUE){
                            orders.sendPacket(z.get(i).co, orders.filterDroneSelect);
                        }else{
                            
                            orders.sendPacket(this.closestTo(z.get(i).co, owned).co, orders.filterDroneSelect);
                            
                        }
                        
                        if(debug_attack_plan)
                        System.err.println("ATTACK OF "+i+" WITH "+(mother +1)+" crew"+" t="+t);
                    }
                }                
            }
            final Point center=new Point(2000,800);
            List<Zone> owned=filterZoneOwner();
            
            orders.sendPacketClosestTo(this.closestTo(center, z).co, playDrone.get(ID),D-orders.nbCurrDone);

        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WorldBase wb = new WorldBase(System.in);
        
        int numTurn=0;

        while (true) {
            long t0 = System.currentTimeMillis();

            wb.readFromLoop();
            wb.turnCalc();

            wb.turnPlanning();

            wb.writeOrders(System.out);

            long t1 = System.currentTimeMillis();

            double t = t1 - t0;
            System.err.println("temps mili " + t);
            numTurn++;
        }

    }

}
