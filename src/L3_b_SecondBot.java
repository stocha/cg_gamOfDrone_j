
import java.awt.Point;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.

2 Players
fayr 18 : greedy start.


 */

/**
 *
 * @author Jahan
 */
public class L3_b_SecondBot {
    
    static boolean debugPlanner_calcMenace=false;


    public static class Drone extends L1_BaseBotLib.DroneBase implements L0_GraphicLib2d.WithCoord {

    }

    public static class Zone extends L1_BaseBotLib.ZoneBase {
    }

    public static class PlayerAnalysis extends L1_BaseBotLib.PlayerBase {
    }



    public static class Bot extends L1_BaseBotLib.BotBase<Drone, Zone, PlayerAnalysis> {

    public class AttackDefPlanner {

        public class Menace implements Comparable<Menace> {

            final Drone d;
            final int eta;

            public Menace(Drone d, int eta) {
                this.d = d;
                this.eta = eta;
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 67 * hash + Objects.hashCode(this.d);
                hash = 67 * hash + this.eta;
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final Menace other = (Menace) obj;
                if (!Objects.equals(this.d, other.d)) {
                    return false;
                }
                if (this.eta != other.eta) {
                    return false;
                }
                return true;
            }

            @Override
            public String toString() {
                return "{" + "d=" + d.id + ", eta=" + eta + '}';
            }

            @Override
            public int compareTo(Menace t) {
                return -t.eta + this.eta;
            }

        }

        public class SimpleMissions {

            @Override
            public String toString() {
                return "MissionAttack{" + "assignedResource=" + assignedResource + ", missionTarget=" + missionTarget + ", done=" + done + '}';
            }

            List<Drone> assignedResource = new ArrayList<>(L1_BaseBotLib.maxDrones);
            L0_GraphicLib2d.WithCoord missionTarget = null;

            boolean done = false;
            int lived = 0;
            int life = 1;

            SimpleMissions(L0_GraphicLib2d.WithCoord cible,int life) {
                this.life=life;
                missionTarget = cible;
            }            
            SimpleMissions(L0_GraphicLib2d.WithCoord cible) {
                missionTarget = cible;
            }

            void addDrone(Drone d) {
                assignedResource.add(d);
                freeDrone.removeAll(assignedResource);
            }

            void sendDrones() {
                for (Drone d : assignedResource) {
                    _orders[d.id].setLocation(missionTarget.cord());
                }
                lived++;
            }

            boolean releaseRessource() {
                if (lived >= life) {
                    done = true;
                } else {
                    return false;
                }

                if (assignedResource.isEmpty()) {
                    return true;
                }

                freeDrone.addAll(assignedResource);
                assignedResource.clear();
                done = true;

                return done;
            }

        }

        private final List<List<List<Menace>>> sectorMenace; // player // zone // drone
        private final List<List<Menace>> sectorResource; // player // zone // drone

        private final List<SimpleMissions> mission = new ArrayList<>(40);

        public AttackDefPlanner() {

            sectorMenace = new ArrayList<>(P);
            for (int p = 0; p < P; p++) {
                sectorMenace.add(new ArrayList<>(Z));
                for (int z = 0; z < Z; z++) {
                    sectorMenace.get(p).add(new ArrayList<>(D));
                }
            }

            sectorResource = new ArrayList<>(Z);
            for (int z = 0; z < Z; z++) {
                sectorResource.add(new ArrayList<>(D));
            }

        }

        public void calcNamedMenaces() {
            // Clear
            for (int p = 0; p < P; p++) {
                for (int i = 0; i < Z; i++) {
                    sectorMenace.get(p).get(i).clear();
                }
            }
            for (int i = 0; i < Z; i++) {
                sectorResource.get(i).clear();
            }

            // parcours bots et sector           
            for (int z = 0; z < Z; z++) {
                Zone zo = zones.get(z);

                for (int p = 0; p < P; p++) {
                    for (int d = 0; d < D; d++) {
                        Drone dr = playerDrones.get(p).get(d);
                        int etapes = zo.coucheLevel(dr);
                        sectorMenace.get(p).get(z).add(new Menace(dr, etapes));
                        sectorResource.get(z).add(new Menace(dr, etapes));

                    }

                }

            }
            for (int p = 0; p < P; p++) {
                for (int i = 0; i < Z; i++) {
                    Collections.sort(sectorMenace.get(p).get(i));
                    if (debugPlanner_calcMenace) {
                        System.err.println("p " + p + " Zone " + i + " eta " + sectorMenace.get(p).get(i));
                    }
                }
            }
            for (int i = 0; i < Z; i++) {
                Collections.sort(sectorResource.get(i));
                if (debugPlanner_calcMenace) {
                    System.err.println("Ressource " + " Zone " + i + " eta " + sectorResource.get(i));
                }
            }

        }// CalcMenace

        private int menaceMaxByPAtZforTime(int p, int z, int t) {
            List<Menace> lm = sectorMenace.get(p).get(z);
            if (lm.isEmpty()) {
                return 0;
            }
            int count = 0;

            for (Menace m : lm) {
                if (m.eta <= t) {
                    count++;
                }
            }

            return count;
        }

        private int maxMenaceAtZForTime(int z, int t) {
            int[] men = new int[P];
            for (int p = 0; p < P; p++) {
                men[p] = menaceMaxByPAtZforTime(p, z, t);
            }

            int max = 0;
            for (int p = 0; p < P; p++) {
                if (men[p] > max) {
                    max = men[p];
                }
            }

            return max;
        }

        private int supportAtZForT(int z, int t) {
            List<Menace> lm = sectorResource.get(z);
            if (lm.isEmpty()) {
                return 0;
            }
            int count = 0;

            for (Menace m : lm) {
                if (m.eta <= t) {
                    count++;
                }
            }

            return count;
        }

        private void pre_plan() {
            List<SimpleMissions> rm = new ArrayList<>();
            for (SimpleMissions a : mission) {
                if (a.releaseRessource()) {
                    rm.add(a);
                }
            }
            mission.removeAll(rm);
        }

        private void post_plan() {
            for (SimpleMissions a : mission) {
                a.sendDrones();
            }
        }

        public void plan() {
            pre_plan();

            List<Zone> mine = new ArrayList<>(Z);
            List<Zone> ene = new ArrayList<>(Z);

            for (Zone zr : zones) {
                if (zr.owner == ID ||(zr.owner==-1 && P==2)) {
                    mine.add(zr);
                } else {
                    ene.add(zr);
                }
            }

            List<Zone> closestZ=L0_GraphicLib2d.clothestElements(zones);
            L1_BaseBotLib.GamePos EmCenter=new L1_BaseBotLib.GamePos();
            EmCenter.cord.setLocation(L0_GraphicLib2d.baryCenter(closestZ));
            
            List<Drone> drone=new ArrayList<>(40);
            drone.addAll(freeDrone);
            
            int perm=0;     
            while(freeDrone.size()>Math.max(0,D-4) && mission.size()<4){
                Zone cz=closestZ.get(perm);
                Drone d=L0_GraphicLib2d.closestFrom(cz, freeDrone);
                SimpleMissions mi=new SimpleMissions(cz, 10000);
                mi.addDrone(d);
                mission.add(mi);
                
                perm^=1;
            }
            
            if(freeDrone.size()>0){
                L1_BaseBotLib.GamePos DCenter=new L1_BaseBotLib.GamePos();
                DCenter.cord.setLocation(L0_GraphicLib2d.baryCenter(freeDrone));             
                
                if(!ene.isEmpty()){
                    Zone z = L0_GraphicLib2d.closestFrom(DCenter, ene);
                    drone.addAll(freeDrone);
                    SimpleMissions mi=new SimpleMissions(z, 1);
                    mission.add(mi);   
                    for(Drone d :  drone){
                        mi.addDrone(d);
                    }                    
                    
                }else{
                    drone.addAll(freeDrone);
                    SimpleMissions mi=new SimpleMissions(DCenter, 1);
                    mission.add(mi);   
                    for(Drone d :  drone){

                        mi.addDrone(d);
  
                    }
                                                
                }
                
            }

            if(debugPlanner_calcMenace)
                System.err.println("Mission "+mission);
            post_plan();

        }
    }        
        

        boolean doneInitConquest = false;
        AttackDefPlanner adp=null;

        List<Drone> freeDrone = new ArrayList<>(L1_BaseBotLib.maxDrones);

        public Bot(InputStream inst) {
            super(inst);       
        }


        @Override
        void doPrepareOrder() {
            if(this._turn_Number==0){
                        freeDrone.addAll(this.playerDrones.get(ID));     
            }
            if(adp==null){
                adp = new AttackDefPlanner();
            }

            adp.calcNamedMenaces();
            adp.plan();

        }

        @Override
        Drone newdrone() {
            return new Drone() {
            };
        }

        @Override
        Zone newzone() {
            return new Zone() {
            };
        }

        @Override
        PlayerAnalysis newplayer() {
            return new PlayerAnalysis() {
            };
        }
    }

}
