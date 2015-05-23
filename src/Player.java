
import java.awt.Point;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
class Player {

    public static class L0_GraphicLib2d {
    
    public interface WithCoord{
        Point cord();
    }

    
    public static < T extends WithCoord> T farthestFrom(WithCoord cc,List<T> them){
        double maxDist=0;
        T max=null;
        for(T w : them){
            double dist= cc.cord().distanceSq(w.cord());
            if(dist >= maxDist){
                maxDist=dist;
                max=w;
            }
        }        
        return max;
    }
    
    public static < T extends WithCoord> T closestFrom(WithCoord cc,List<T > them){
        double minDist=Double.MAX_VALUE;
        T min=null;
        for(T w : them){
            double dist= cc.cord().distanceSq(w.cord());
            if(dist <= minDist){
                minDist=dist;
                min=w;
            }
        }        
        return min;
    }   
    
    public static <T extends WithCoord> List<T> sortFarthestFrom(WithCoord cc, List<T> them){
        List<T> res=new ArrayList<>(them.size());
        res.addAll(them);
        final Comparator<WithCoord> distance;
        distance = (e1, e2) -> (int)(e2.cord().distanceSq(cc.cord())-e1.cord().distanceSq(cc.cord()));
        Collections.sort(res,distance );
        return res;
    }
    
    public static <T extends WithCoord> List<T> sortClothestFrom(WithCoord cc, List<T> them){
        List<T> res=new ArrayList<>(them.size());
        res.addAll(them);
        final Comparator<WithCoord> distance;
        distance = (e1, e2) -> (int)(e1.cord().distanceSq(cc.cord())-e2.cord().distanceSq(cc.cord()));
        Collections.sort(res,distance );
        return res;
    }    
    
    public static <T extends WithCoord> List<T> clothestElements(List<T> them){
        List<T> res=new ArrayList<>(them.size());

        double minDist=Double.MAX_VALUE;
        T A=null;
        T B=null;
        for(T w : them) for(T w2 : them){
            if(w==w2) continue;
            double dist= w2.cord().distanceSq(w.cord());
            if(dist < minDist){
                minDist=dist;
                A=w;B=w2;
            }
        }        
        res.add(A);
        res.add(B);
        
        return res;
    }   
    
    public static <T extends WithCoord> List<T> farthestElements(List<T> them){
        List<T> res=new ArrayList<>(them.size());

        double minDist=Double.MAX_VALUE;
        T A=null;
        T B=null;
        double maxDist=0;
        for(T w : them) for(T w2 : them){
            if(w==w2) continue;
            double dist= w2.cord().distanceSq(w.cord());
            if(dist >= maxDist){
                maxDist=dist;
                A=w;B=w2;
            }
        }                 
        res.add(A);
        res.add(B);
        
        return res;
    }     
    
    public static <T extends WithCoord> Point SegABatDistFromA(T A, T B, double dist){
            double dd=A.cord().distance(B.cord());
            if(dd==0) return A.cord();
            double rat=dist/dd;
            Point res=new Point(B.cord().x-A.cord().x, B.cord().y-A.cord().y);
            res.setLocation(res.x*rat,res.y*rat);
            res.setLocation(res.x+A.cord().x,res.y+A.cord().y);
            
            return res;
    }
    
    public static <T extends WithCoord> Point baryCenter(List<T> them){
        Point res=new Point(0,0);
        them.stream().forEach((w) -> {
            res.setLocation(res.x+w.cord().x,res.y+w.cord().y);
        });                 
        res.setLocation(res.x/them.size(),res.y/them.size());
        
        return res;
    }     
    
}



public static class L1_BaseBotLib {
    
    private static boolean debug_base=true;
    private static boolean debug_players=false;    
    private static boolean debug_drones=false;
    private static boolean debug_zones=false;
    
    private static boolean debug_droneHistory=false;

    static final int supposedMaxZone = 20;
    static final int maxDrones = 13;
    static final int supposedMaxTurn=700;
    
    static final int lvl0Dist=100;
    static final int maxSpeed=100;

    interface Factory<E> {

        E create();
    }

    public static class GamePos implements L0_GraphicLib2d.WithCoord {

        final Point cord = new Point(0, 0);

        @Override
        public Point cord() {
            return cord;
        }

        public void set(Point c) {
            cord.setLocation(c);
        }
        
        public double distToOrigin(){
            return Math.sqrt(cord.x*cord.x+cord.y*cord.y);
        }

        @Override
        public String toString() {
            return "(" + cord.x +"|"+cord.y+"["+(int)distToOrigin()+"]" +")";
        }
        
        
    }

    public abstract static class DroneBase extends GamePos {

        final ArrayDeque<L1_BaseBotLib.GamePos> coords = new ArrayDeque<>(supposedMaxTurn);
        final ArrayDeque<L1_BaseBotLib.GamePos> speeds = new ArrayDeque<>(supposedMaxTurn);

        int id;
        int owner;

        @Override
        public String toString() {
            String res="";
            res+="id=" + id + '}' + coords.getFirst()+" "+speeds.getFirst();
            if(debug_droneHistory)
                res+= "DroneBase{" + "coords=" + coords + ", speeds=" + speeds + ", id=" + id + '}' ;
            
            return res;
        }
        
        

    }

    public abstract static class ZoneBase extends GamePos {

        int id;
        int owner;
        int turnowned;

        @Override
        public String toString() {
            return "ZoneBase{" + "id=" + id + ", owner=" + owner + ", turnowned=" + turnowned + '}';
        }
        
        public int coucheLevel(L0_GraphicLib2d.WithCoord cc){
            double dd = this.cord.distance(cc.cord());
            return (int)dd/lvl0Dist;
        }
        
        public int headingSpeed(DroneBase cc){
            Point vecpos=new Point(cord.x-cc.cord.x,cord.y-cc.cord.y);
            Point vitMob=new Point(0,0);
            if(!cc.speeds.isEmpty()){
                vitMob.setLocation(cc.speeds.getFirst().cord);
            }
            double norm=vecpos.x*vecpos.x+vecpos.y*vecpos.y;
            norm=Math.sqrt(norm);
                    
            double res=vecpos.x*vitMob.x+vecpos.y*vitMob.y;
            if(norm==0) res=0; else res=res/norm;
            return (int)res;
        
        }
        
        public int headingLevel(DroneBase cc){
            int res=headingSpeed(cc);
            
           Point vecpos=new Point(cord.x-cc.cord.x,cord.y-cc.cord.y);
           double norm=vecpos.x*vecpos.x+vecpos.y*vecpos.y;
           norm=Math.sqrt(norm);
            
           if(res==0) return 40;
            if(res<0) return 80;
            return (int)norm/res;
        
        }

    }

    public abstract static class PlayerBase {

        int id;
        int nbControlled;
        final ArrayDeque<List<Integer>> controlHistorique = new ArrayDeque<>(supposedMaxTurn);

        @Override
        public String toString() {
            String res=" "+ "id=" + id + ", nbControlled=" + nbControlled;
            
            if(debug_droneHistory)
                res+="PlayerBase{" + "id=" + id + ", nbControlled=" + nbControlled + ", controlHistorique=" + controlHistorique + '}';
            
            return res;
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
        final double avg_dronePerLegitimateZone;
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
            avg_dronePerLegitimateZone = (double) D / (double) avg_zoneForPlayer;

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
                    n.owner=p;
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
                it.controlHistorique.addFirst(owned);  
                it.nbControlled=owned.size();
            }              
            
            // Les drones
            for (int p = 0; p < P; p++) {
                for (int d = 0; d < D; d++) {
                    Dt it=playerDrones.get(p).get(d);
                    it.cord.setLocation(_playerDronesCords[p][d]);
                    L1_BaseBotLib.GamePos cc=new L1_BaseBotLib.GamePos();
                    cc.set(_playerDronesCords[p][d]);

                    L1_BaseBotLib.GamePos vc=new L1_BaseBotLib.GamePos();
                    
                    if(!it.coords.isEmpty()){
                        L0_GraphicLib2d.WithCoord prev=it.coords.getFirst();
                        vc.cord.setLocation(cc.cord.x-prev.cord().x,cc.cord.y-prev.cord().y);
                    }
                    
                    it.speeds.addFirst(vc);       
                    it.coords.addFirst(cc);
                    
                }                
                
            }
            
            // Debug
            if(debug_base){
                if(debug_players)
                    System.err.println(""+players);
                if(debug_zones)
                    System.err.println(""+zones);
                if(debug_drones)
                    System.err.println(""+playerDrones);
                System.err.println("Turn "+_turn_Number);
                System.err.println("Control ");
                for(int p=0;p<P;p++){
                    System.err.print("|"+_turn_scoreControl[p]);
                }
                System.err.println("drone/zone zone/player drone/legit "+this.avg_dronePerZone+" "+this.avg_zoneForPlayer+" "+this.avg_dronePerLegitimateZone);
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

public static class L3_FirstBot {
    
    static final boolean debugPlanner=false;
    
    static final int expectedMissionMax=L1_BaseBotLib.supposedMaxTurn*L1_BaseBotLib.supposedMaxZone;
    
    static final boolean debug_transfertList=false;
    
    public static class Drone extends L1_BaseBotLib.DroneBase implements L0_GraphicLib2d.WithCoord{
        List<Mission> mission=new ArrayList<>(expectedMissionMax);
    }
    
    public static class Zone extends L1_BaseBotLib.ZoneBase{
    }    
    
    public static class PlayerAnalysis extends L1_BaseBotLib.PlayerBase{
    }        
    
    public static class Mission extends L1_BaseBotLib.GamePos{
        public static enum MissionType{
            conquestInit,
        }
        public static enum MissionStatus{
            created,
            success,
            running,
            canceled,
            suspended,
        }  
        public static enum AbortReason{
            takenByEnemy,
            noFreeDrone,
            threatedToBeTaken,
            priorityCancelation,
        }          
        MissionType type=null;
        int turnCreation=-1;
        int turnExpectedEnd=-1;
        int turnCanceled=-1;
        int exepectedReward=-1;
        MissionStatus status=null;
        
        List<Drone> assignedResource=new ArrayList<>(L1_BaseBotLib.maxDrones);
        
        Zone missionTarget=null;
        Bot context=null;
        
        public double distanceSqToFirstDrone;
        Drone closestFreeDrone=null;
        
        public void createConquestInit(Zone target,Bot context){
            this.context=context;
            missionTarget=target;
            turnCreation=context._turn_Number;
            status=MissionStatus.created;
            type=MissionType.conquestInit;
        }
        
        public void findNextClosest(){
            Drone close=L0_GraphicLib2d.closestFrom((L0_GraphicLib2d.WithCoord)missionTarget, context.freeDrone);
            distanceSqToFirstDrone=missionTarget.cord.distanceSq(close.cord);
            closestFreeDrone=close;            
        }

        @Override
        public String toString() {
            return "Mission{" + "type=" + type + ", turnCreation=" + turnCreation + ", turnExpectedEnd=" + turnExpectedEnd + ", turnCanceled=" + turnCanceled + ", status=" + status + ", assignedResource=" + assignedResource + ", distanceSqToFirstDrone=" + distanceSqToFirstDrone + '}';
        }
        
        public boolean enCours(){
            if( missionTarget.owner!=context.ID){
                boolean allThere=true;
                for(Drone d : assignedResource){
                    if (missionTarget.coucheLevel(d)>0){
                        allThere&=false;
                    }
                }
                return !allThere;            
            }
            
            return false;
        }
        
        
        public void applyResourcesOrders(Point[] ordersOut){
            for(Drone d : this.assignedResource){
                ordersOut[d.id].setLocation(missionTarget.cord);
            
            }
        }
        
        
        
    }
    
    public static class AttackDefPlanner{
        
        public class MissionAttack{

            @Override
            public String toString() {
                return "MissionAttack{" + "assignedResource=" + assignedResource.size() + ", missionTarget=" + missionTarget.id + ", nbTurns=" + nbTurns + ", done=" + done + '}';
            }
            
            
            
            
            List<Drone> assignedResource=new ArrayList<>(L1_BaseBotLib.maxDrones);
            Zone missionTarget=null;    
            
            int nbTurns=9;
            boolean done=false;
            int desiredTeam=0;
            
            MissionAttack(Zone cible,int desiredteam){
                missionTarget=cible;
                this.desiredTeam=desiredteam;
            }
            
            void captureRessource(){
                if(done) return;
                
                int souhai=desiredTeam;
                if(assignedResource.size()>=souhai) return;
                for(int i=0;i<souhai-assignedResource.size();i++){
                    if(context.freeDrone.isEmpty()) break;
                    
                    Drone close=L0_GraphicLib2d.closestFrom(missionTarget, context.freeDrone);
                    assignedResource.add(close);
                    context.freeDrone.removeAll(assignedResource);
                }
                
                if(assignedResource.isEmpty())done=true;
            }
            
            void sendDrones(){
                for(Drone d : assignedResource){
                    context._orders[d.id].setLocation(missionTarget.cord);
                }
            }
            
            void releaseRessource(){
                if(done || assignedResource.isEmpty()) return;
                
                Drone d=L0_GraphicLib2d.farthestFrom(missionTarget,assignedResource);
                
                if(d.cord.distance(missionTarget.cord)<L1_BaseBotLib.lvl0Dist){
                    nbTurns--;
                }
                
                if(nbTurns<=0){
                    context.freeDrone.addAll(assignedResource);
                    assignedResource.clear();
                    done=true;
                }
            }
            
            boolean isDone(){
                return done;
            }
            
        }
        
        final static int maxEtaCalc=60;
        
        Bot context=null;
        private final List<List<Drone>> sectorMenac;
        private final List<List<Integer>> etamenace;
        private final List<Drone> sectorRessource;
        private final List<Integer> etaressourceEta;
        
        private static AttackDefPlanner inst=null;
        private static AttackDefPlanner inst(Bot con){
            if(inst==null){
                inst=new AttackDefPlanner(con);
            }
            return inst;
        }
        
        private final List<MissionAttack> mission=new ArrayList<>(40);

        public AttackDefPlanner(Bot context) {
            this.context = context;
            
            sectorMenac=new ArrayList<>(context.Z);
            etamenace=new ArrayList<>(context.Z);
            for(int z=0;z<context.Z;z++){
                sectorMenac.add(new ArrayList<>(context.D));
                etamenace.add(new ArrayList<>(context.D));
            }
            sectorRessource=new ArrayList<>(context.D);
            etaressourceEta=new ArrayList<>(context.D);
        }
        
        public void calcNamedMenaces(){
            
            // Clear
            for(int i=0;i<sectorMenac.size();i++){
                sectorMenac.get(i).clear();
                etamenace.get(i).clear();
            }
           sectorRessource.clear();
           etaressourceEta.clear();
           
           // parcours bots et sector           
           for(int z=0;z<context.Z;z++){
               Zone zo=context.zones.get(z);
               
               for(int p=0;p<context.P;p++){
                   for(int d=0;d<context.D;d++){
                       Drone dr =context.playerDrones.get(p).get(d);
                       
                       int etapes=zo.coucheLevel(dr);
                       int etan=zo.headingLevel(dr);
                       if(etapes <=2) etan=0;
                       
                       if(etan <maxEtaCalc){
                           if(p==context.ID){
                               sectorRessource.add(dr);
                               etaressourceEta.add(etapes);
                           }else{
                               sectorMenac.get(z).add(dr);
                               etamenace.get(z).add(etan);
                           }
                       }
                   }
               
               }
               
           }
        }// CalcMenace
        
        public int etaForMenaceLevel(Zone zr,int nbDroneEnemy){
            int[][] nbThreatPerEta=new int[context.P][maxEtaCalc];
            
            List<Drone> menDr=sectorMenac.get(zr.id);
            for(int i=0;i<menDr.size();i++){
                Drone dr=menDr.get(i);
                nbThreatPerEta[dr.owner][etamenace.get(zr.id).get(i)]++;             
            }
            
            int[] etaPerPlayer=new int[context.P];
            for(int p=0;p<context.P;p++){
                int count=0;
                for(int t=0;t<maxEtaCalc && count < nbDroneEnemy;t++){
                    count+=nbThreatPerEta[p][t];
                    etaPerPlayer[p]=t;
                }
                
            }
            
            int min=Integer.MAX_VALUE;
            for(int i=0;i<context.P;i++){
                if(i==context.ID) continue;
                if(debugPlanner)
                    System.err.println("z "+zr.id +" p"+i+" etaConst "+etaPerPlayer[i]);
                if(min>etaPerPlayer[i]){
                    min=etaPerPlayer[i];
                }
            }
            return min;
        }
        
        int calcMenace(Zone zr){
            int countP[] = new int[context.P];
            
            {
            int i=0;
                for(Drone d : sectorMenac.get(zr.id)){
                    if(etamenace.get(zr.id).get(i)<10){
                        countP[d.owner]++;
                    }
                    i++;
                }
            }
            
            int max=-1;
            for(int i=0;i<countP.length;i++){
                if(max>countP[i]) max=countP[i];
            }
            
            return max;
        }
        
        public void plan(){
            List<Zone> mine=new ArrayList<>();
            List<Zone> ene=new ArrayList<>();
            
            if(debugPlanner) {System.err.println(""+etamenace);}
            
            for(Zone zr : context.zones){
                if(zr.owner!=context.ID) continue;
                    int szMen=calcMenace(zr);                
                    int count=sectorRessource.size();

                
                
                mission.add(new MissionAttack(zr,szMen+1 - count));
            }
            
            for(Zone zr : context.zones){                
                if(zr.owner==context.ID) mine.add(zr);
                else if(zr.owner==-1) ene.add(zr);
                
                int eta=etaForMenaceLevel(zr, (int)context.avg_dronePerZone);
                
                if(debugPlanner){
                    System.err.println("Zone "+zr.id+" eta "+eta+" for menace "+(context.avg_dronePerLegitimateZone));
                }
                
                if(eta>5 && mission.size() < 3 && zr.owner!=context.ID){
                    mission.add(new MissionAttack(zr,(int)context.avg_dronePerLegitimateZone+1));
                }
            }
            
            List<MissionAttack> rm=new ArrayList<>();
            for(MissionAttack a : mission){
                a.captureRessource();
                a.releaseRessource();
                if(a.isDone()) rm.add(a);
                a.sendDrones();
            }
                       
            
            mission.removeAll(rm);
            
            Point center=null;
            if(!mine.isEmpty()){
                center=L0_GraphicLib2d.baryCenter(mine);
            }
            if(center==null) center=new Point(2000,800);
            for(Drone d: context.freeDrone){
                context._orders[d.id].setLocation(center);
            }
            
        }
    }
    

    public static class Bot extends L1_BaseBotLib.BotBase<Drone,Zone,PlayerAnalysis>{      
            
    boolean doneInitConquest=false;
        
        
        ArrayDeque<Mission> missionActives=new ArrayDeque<>(expectedMissionMax);
        ArrayDeque<Mission> missionCancelled=new ArrayDeque<>(expectedMissionMax);
        List<Mission> missionInitConquestProposed=new ArrayList<>(expectedMissionMax);
        List<Mission> missionTransfert=new ArrayList<>(expectedMissionMax);
        List<Drone> freeDrone=new ArrayList<>(L1_BaseBotLib.maxDrones);
        List<Drone> droneTransfert=new ArrayList<>(L1_BaseBotLib.maxDrones);

        public Bot(InputStream inst) {
            super(inst);
        }
        
        private void decomissionMissionInitConquest(){
            
            missionTransfert.clear();
            droneTransfert.clear();
            for(Mission mi : missionActives){
                if(!mi.enCours()){
                    missionTransfert.add(mi);
                }                
            }
            for(Mission mi : missionTransfert){
                freeDrone.addAll(mi.assignedResource);
                mi.assignedResource.clear();
            }            
            
            missionActives.removeAll(missionTransfert);
            missionTransfert.clear();
            
        }
        
        private void examineAndPlanMissionInitConquestProposed(){
            if(missionInitConquestProposed.isEmpty()) return;
            
            
           while(!freeDrone.isEmpty()){
               
                for(Mission m : missionInitConquestProposed){
                    m.findNextClosest();

                }

                final Comparator<Mission> distance;
                distance = (e1, e2) -> (int)(e1.distanceSqToFirstDrone-e2.distanceSqToFirstDrone);
                Collections.sort( missionInitConquestProposed,distance );


                for(Mission mi : missionInitConquestProposed){
                    if(droneTransfert.contains(mi.closestFreeDrone)) continue;
                    droneTransfert.add(mi.closestFreeDrone);
                    mi.assignedResource.add(mi.closestFreeDrone);
                }

                freeDrone.removeAll(droneTransfert);
                droneTransfert.clear();
           }
                for(Mission mi : missionInitConquestProposed){
                    if(!mi.assignedResource.isEmpty())
                        missionTransfert.add(mi);
                }           

                missionInitConquestProposed.clear();
                missionActives.addAll(missionTransfert);
                missionTransfert.clear();           
            
        }
        
        private void applyMissionPlanning(){
           for(Mission mi : missionActives){
               mi.applyResourcesOrders(this._orders);
           }            
            
        }
        
        private void markFreeDrone(){
            //for(Drone d : freeDrone){
             //   _orders[d.id].setLocation(new Point(0,0));
            //}
            AttackDefPlanner adp=AttackDefPlanner.inst(this);
            adp.calcNamedMenaces();
            adp.plan();
        }

        @Override
        void doPrepareOrder() {
            
            if(!this.doneInitConquest){
                freeDrone.addAll(this.playerDrones.get(ID));
                
                doneInitConquest=true;
                for(Zone z : zones){
                    if(z.owner==-1){
                        Mission conq=new Mission();
                        System.err.println("Creating mission for "+z);
                        conq.createConquestInit(z, this);
                        missionInitConquestProposed.add(conq);
                    }
                }                
            }else{
                decomissionMissionInitConquest();
                markFreeDrone();
            
            }

            
            examineAndPlanMissionInitConquestProposed();
            
            
            // debug
            if(debug_transfertList){
                System.err.println("missionActives"+missionActives);
                System.err.println("missionActives"+missionInitConquestProposed);
                System.err.println("missionTransfert"+missionTransfert);
                System.err.println("droneTransfert"+droneTransfert);
                System.err.println("freeDrone"+freeDrone);
            
            }
            
            applyMissionPlanning();
            
            
        }

        @Override
        Drone newdrone() {
            return new Drone(){};
        }

        @Override
        Zone newzone() {
            return new Zone(){};
        }

        @Override
        PlayerAnalysis newplayer() {
            return new PlayerAnalysis(){};
        }
    }    
    
}


















    
    
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
        L1_BaseBotLib.BotBase theBot=new L3_FirstBot.Bot(System.in);
        
        
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
