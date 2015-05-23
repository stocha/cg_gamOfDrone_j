
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
    
    static final int expectedMissionMax=L1_BaseBotLib.supposedMaxTurn*L1_BaseBotLib.supposedMaxZone;
    
    static final boolean debug_transfertList=true;
    
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
            canceled
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
        
        public double distanceSqToFirstDrone;
        Drone closestFreeDrone=null;
        
        public void createConquestInit(Zone target,Bot context){
            missionTarget=target;
            turnCreation=context._turn_Number;
            status=MissionStatus.created;
            type=MissionType.conquestInit;

            Drone close=L0_GraphicLib2d.closestFrom((L0_GraphicLib2d.WithCoord)target, context.freeDrone);
            distanceSqToFirstDrone=missionTarget.cord.distanceSq(close.cord);
            closestFreeDrone=close;
        }

        @Override
        public String toString() {
            return "Mission{" + "type=" + type + ", turnCreation=" + turnCreation + ", turnExpectedEnd=" + turnExpectedEnd + ", turnCanceled=" + turnCanceled + ", status=" + status + ", assignedResource=" + assignedResource + ", distanceSqToFirstDrone=" + distanceSqToFirstDrone + '}';
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
        
        private void examineAndPlanMissionInitConquestProposed(){
            if(missionInitConquestProposed.isEmpty()) return;
            
           final Comparator<Mission> distance;
           distance = (e1, e2) -> (int)(e1.distanceSqToFirstDrone-e2.distanceSqToFirstDrone);
           Collections.sort( missionInitConquestProposed,distance );
           
           
           for(Mission mi : missionInitConquestProposed){
               if(droneTransfert.contains(mi.closestFreeDrone)) continue;
               droneTransfert.add(mi.closestFreeDrone);
               missionTransfert.add(mi);
               mi.assignedResource.add(mi.closestFreeDrone);
           }
           
           missionInitConquestProposed.removeAll(missionTransfert);
           missionActives.addAll(missionTransfert);
           missionTransfert.clear();
            
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
