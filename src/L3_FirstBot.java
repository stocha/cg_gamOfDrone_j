
import java.awt.Point;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jahan
 */
public class L3_FirstBot {
    
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
            List<Drone> assignedResource=new ArrayList<>(L1_BaseBotLib.maxDrones);
            Zone missionTarget=null;    
            
            int nbTurns=5;
            boolean done=false;
            
            MissionAttack(Zone cible){
                missionTarget=cible;
            }
            
            void captureRessource(){
                if(done) return;
                
                if(assignedResource.size()>=(int)context.avg_dronePerLegitimateZone) return;
                for(int i=0;i<4;i++){
                    if(context.freeDrone.isEmpty()) break;
                    
                    assignedResource.add(context.freeDrone.get(0));
                    context.freeDrone.removeAll(assignedResource);
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
        
        public void plan(){
            List<Zone> mine=new ArrayList<>();
            List<Zone> ene=new ArrayList<>();
            
            if(debugPlanner) {System.err.println(""+etamenace);}
            
            for(Zone zr : context.zones){                
                if(zr.owner==context.ID) mine.add(zr);
                else if(zr.owner==-1) ene.add(zr);
                
                int eta=etaForMenaceLevel(zr, (int)context.avg_dronePerLegitimateZone+1);
                
                if(debugPlanner){
                    System.err.println("Zone "+zr.id+" eta "+eta+" for menace "+(context.avg_dronePerLegitimateZone+1));
                }
                
                if(eta>5){
                    mission.add(new MissionAttack(zr));
                }
            }
            
            List<MissionAttack> rm=new ArrayList<>();
            for(MissionAttack a : mission){
                a.captureRessource();
                a.releaseRessource();
                if(a.isDone()) rm.add(a);
            }
            
            mission.removeAll(rm);
            
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
