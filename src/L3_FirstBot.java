
import java.awt.Point;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

    static final boolean debugPlanner_calcMenace = true;
    static final boolean debugPlanner = true;

    static final int expectedMissionMax = L1_BaseBotLib.supposedMaxTurn * L1_BaseBotLib.supposedMaxZone;

    static final boolean debug_transfertList = false;

    public static class Drone extends L1_BaseBotLib.DroneBase implements L0_GraphicLib2d.WithCoord {

        List<Mission> mission = new ArrayList<>(expectedMissionMax);
    }

    public static class Zone extends L1_BaseBotLib.ZoneBase {
    }

    public static class PlayerAnalysis extends L1_BaseBotLib.PlayerBase {
    }

    public static class Mission extends L1_BaseBotLib.GamePos {

        public static enum MissionType {

            conquestInit,
        }

        public static enum MissionStatus {

            created,
            success,
            running,
            canceled,
            suspended,
        }

        public static enum AbortReason {

            takenByEnemy,
            noFreeDrone,
            threatedToBeTaken,
            priorityCancelation,
        }
        MissionType type = null;
        int turnCreation = -1;
        int turnExpectedEnd = -1;
        int turnCanceled = -1;
        int exepectedReward = -1;
        MissionStatus status = null;

        List<Drone> assignedResource = new ArrayList<>(L1_BaseBotLib.maxDrones);

        Zone missionTarget = null;
        Bot context = null;

        public double distanceSqToFirstDrone;
        Drone closestFreeDrone = null;

        public void createConquestInit(Zone target, Bot context) {
            this.context = context;
            missionTarget = target;
            turnCreation = context._turn_Number;
            status = MissionStatus.created;
            type = MissionType.conquestInit;
        }

        public void findNextClosest() {
            Drone close = L0_GraphicLib2d.closestFrom((L0_GraphicLib2d.WithCoord) missionTarget, context.freeDrone);
            distanceSqToFirstDrone = missionTarget.cord.distanceSq(close.cord);
            closestFreeDrone = close;
        }

        @Override
        public String toString() {
            return "Mission{" + "type=" + type + ", turnCreation=" + turnCreation + ", turnExpectedEnd=" + turnExpectedEnd + ", turnCanceled=" + turnCanceled + ", status=" + status + ", assignedResource=" + assignedResource + ", distanceSqToFirstDrone=" + distanceSqToFirstDrone + '}';
        }

        public boolean enCours() {
            if (missionTarget.owner != context.ID) {
                boolean allThere = true;
                for (Drone d : assignedResource) {
                    if (missionTarget.coucheLevel(d) > 0) {
                        allThere &= false;
                    }
                }
                return !allThere;
            }

            return false;
        }

        public void applyResourcesOrders(Point[] ordersOut) {
            for (Drone d : this.assignedResource) {
                ordersOut[d.id].setLocation(missionTarget.cord);

            }
        }

    }

    public static class AttackDefPlanner {

        public static class Menace implements Comparable<Menace> {

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
                return "MissionAttack{" + "assignedResource=" + assignedResource.size() + ", missionTarget=" + missionTarget + ", done=" + done + '}';
            }

            List<Drone> assignedResource = new ArrayList<>(L1_BaseBotLib.maxDrones);
            L0_GraphicLib2d.WithCoord missionTarget = null;

            boolean done = false;
            int lived = 0;
            int life = 1;

            SimpleMissions(L0_GraphicLib2d.WithCoord cible) {
                missionTarget = cible;
            }

            void addDrone(Drone d) {
                assignedResource.add(d);
                context.freeDrone.removeAll(assignedResource);
            }

            void sendDrones() {
                for (Drone d : assignedResource) {
                    context._orders[d.id].setLocation(missionTarget.cord());
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

                context.freeDrone.addAll(assignedResource);
                assignedResource.clear();
                done = true;

                return done;
            }

        }

        final static int maxEtaCalc = 60;

        Bot context = null;
        private final List<List<List<Menace>>> sectorMenace; // player // zone // drone
        private final List<List<Menace>> sectorResource; // player // zone // drone

        private static AttackDefPlanner inst = null;

        private static AttackDefPlanner inst(Bot con) {
            if (inst == null) {
                inst = new AttackDefPlanner(con);
            }
            return inst;
        }

        private final List<SimpleMissions> mission = new ArrayList<>(40);

        public AttackDefPlanner(Bot context) {
            this.context = context;

            sectorMenace = new ArrayList<>(context.P);
            for (int p = 0; p < context.P; p++) {
                sectorMenace.add(new ArrayList<>(context.Z));
                for (int z = 0; z < context.Z; z++) {
                    sectorMenace.get(p).add(new ArrayList<>(context.D));
                }
            }

            sectorResource = new ArrayList<>(context.Z);
            for (int z = 0; z < context.Z; z++) {
                sectorResource.add(new ArrayList<>(context.D));
            }

        }

        public void calcNamedMenaces() {
            // Clear
            for (int p = 0; p < context.P; p++) {
                for (int i = 0; i < context.Z; i++) {
                    sectorMenace.get(p).get(i).clear();
                }
            }
            for (int i = 0; i < context.Z; i++) {
                sectorResource.get(i).clear();
            }

            // parcours bots et sector           
            for (int z = 0; z < context.Z; z++) {
                Zone zo = context.zones.get(z);

                for (int p = 0; p < context.P; p++) {
                    for (int d = 0; d < context.D; d++) {
                        Drone dr = context.playerDrones.get(p).get(d);

                        int etapes = zo.coucheLevel(dr);
                        int etan = zo.headingLevel(dr);

                        if (p != context.ID) {
                            etan -= 1;
                            if (etapes <= 4) {
                                etan = etapes - 1;
                            }
                            if (etan < 0) {
                                etan = 0;
                            }

                            if (etan < maxEtaCalc) {
                                sectorMenace.get(p).get(z).add(new Menace(dr, etan));
                            }
                        } else {
                            // Friendly forces projection
                            sectorMenace.get(p).get(z).add(new Menace(dr, etan));
                            sectorResource.get(z).add(new Menace(dr, etapes));
                        }
                    }

                }

            }
            for (int p = 0; p < context.P; p++) {
                for (int i = 0; i < context.Z; i++) {
                    Collections.sort(sectorMenace.get(p).get(i));
                    if (debugPlanner_calcMenace) {
                        System.err.println("p " + p + " Zone " + i + " eta " + sectorMenace.get(p).get(i));
                    }
                }
            }
            for (int i = 0; i < context.Z; i++) {
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
            int[] men = new int[context.P];
            for (int p = 0; p < context.P; p++) {
                men[p] = menaceMaxByPAtZforTime(p, z, t);
            }

            int max = 0;
            for (int p = 0; p < context.P; p++) {
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

            List<Zone> mine = new ArrayList<>(context.Z);
            List<Zone> ene = new ArrayList<>(context.Z);

            for (Zone zr : context.zones) {
                if (zr.owner == context.ID) {
                    mine.add(zr);
                } else {
                    ene.add(zr);
                }
            }

            // Application a nos colonies a nous
            for (Zone zr : mine) {
                List<Drone> candidates = new ArrayList<>();
                for (Menace def : sectorResource.get(zr.id)) {
                    if (def.eta > 0) {
                        break;
                    }
                    candidates.add(def.d);
                }
                int men0 = maxMenaceAtZForTime(zr.id, 1);
                if (men0 <= candidates.size()) {
                    int men10 = maxMenaceAtZForTime(zr.id, 10);
                    int sent = Math.min(candidates.size(), men10);

                    SimpleMissions mi = new SimpleMissions(zr);
                    mission.add(mi);
                    for (int i = 0; i < sent; i++) {
                        mi.addDrone(sectorResource.get(zr.id).get(i).d);
                    }
                }

            }

            List<Zone> toTry = new ArrayList<>(20);
            toTry.addAll(ene);

            if (!context.freeDrone.isEmpty()) {
                Point cc = L0_GraphicLib2d.baryCenter(context.zones);
                L1_BaseBotLib.GamePos gp = new L1_BaseBotLib.GamePos();
                gp.cord.setLocation(cc);
                            
                while (!context.freeDrone.isEmpty() && !toTry.isEmpty()) {
                    if (toTry.size() == 1) {
                        SimpleMissions mi = new SimpleMissions(toTry.get(0));
                        mission.add(mi);
                        List<Drone> sent = new ArrayList<>();
                        sent.addAll(context.freeDrone);
                        for (int i = 0; i < sent.size(); i++) {
                            mi.addDrone(sent.get(i));
                        }
                        break;
                    }
                    
  

                    Zone z = L0_GraphicLib2d.closestFrom(gp, toTry);
                    List<Drone> closeOnes=new ArrayList<>();
                    int szl=this.sectorResource.get(z.id).size();
                    for(int i=0;i<szl && i<(context.D / 2 )+1 ;i++){
                        closeOnes.add(sectorResource.get(z.id).get(i).d);
                    }
                    
                    cc = L0_GraphicLib2d.baryCenter(closeOnes);
                    L1_BaseBotLib.GamePos myCenter = new L1_BaseBotLib.GamePos();
                    myCenter.cord.setLocation(cc);                      
                    
                    double d = z.cord.distance(myCenter.cord);
                    int TT = (int) (d / L1_BaseBotLib.lvl0Dist);
                    TT += 3;

                    int men = maxMenaceAtZForTime(z.id, TT);
                    if (men <= context.D / 2) {
                        SimpleMissions mi = new SimpleMissions(toTry.get(0));
                        mission.add(mi);
                        List<Drone> sent = new ArrayList<>();
                        sent.addAll(context.freeDrone);
                        for (int i = 0; i < sent.size(); i++) {
                            mi.addDrone(sent.get(i));
                        }
                    }
                    toTry.remove(z);
                }
            }

            post_plan();

        }
    }

    public static class Bot extends L1_BaseBotLib.BotBase<Drone, Zone, PlayerAnalysis> {

        boolean doneInitConquest = false;

        ArrayDeque<Mission> missionActives = new ArrayDeque<>(expectedMissionMax);
        ArrayDeque<Mission> missionCancelled = new ArrayDeque<>(expectedMissionMax);
        List<Mission> missionInitConquestProposed = new ArrayList<>(expectedMissionMax);
        List<Mission> missionTransfert = new ArrayList<>(expectedMissionMax);
        List<Drone> freeDrone = new ArrayList<>(L1_BaseBotLib.maxDrones);
        List<Drone> droneTransfert = new ArrayList<>(L1_BaseBotLib.maxDrones);

        public Bot(InputStream inst) {
            super(inst);
        }

        private void decomissionMissionInitConquest() {

            missionTransfert.clear();
            droneTransfert.clear();
            for (Mission mi : missionActives) {
                if (!mi.enCours()) {
                    missionTransfert.add(mi);
                }
            }
            for (Mission mi : missionTransfert) {
                freeDrone.addAll(mi.assignedResource);
                mi.assignedResource.clear();
            }

            missionActives.removeAll(missionTransfert);
            missionTransfert.clear();

        }

        private void examineAndPlanMissionInitConquestProposed() {
            if (missionInitConquestProposed.isEmpty()) {
                return;
            }

            while (!freeDrone.isEmpty()) {

                for (Mission m : missionInitConquestProposed) {
                    m.findNextClosest();

                }

                final Comparator<Mission> distance;
                distance = (e1, e2) -> (int) (e1.distanceSqToFirstDrone - e2.distanceSqToFirstDrone);
                Collections.sort(missionInitConquestProposed, distance);

                for (Mission mi : missionInitConquestProposed) {
                    if (droneTransfert.contains(mi.closestFreeDrone)) {
                        continue;
                    }
                    droneTransfert.add(mi.closestFreeDrone);
                    mi.assignedResource.add(mi.closestFreeDrone);
                }

                freeDrone.removeAll(droneTransfert);
                droneTransfert.clear();
            }
            for (Mission mi : missionInitConquestProposed) {
                if (!mi.assignedResource.isEmpty()) {
                    missionTransfert.add(mi);
                }
            }

            missionInitConquestProposed.clear();
            missionActives.addAll(missionTransfert);
            missionTransfert.clear();

        }

        private void applyMissionPlanning() {
            for (Mission mi : missionActives) {
                mi.applyResourcesOrders(this._orders);
            }

        }

        private void markFreeDrone() {
            //for(Drone d : freeDrone){
            //   _orders[d.id].setLocation(new Point(0,0));
            //}
            AttackDefPlanner adp = AttackDefPlanner.inst(this);
            adp.calcNamedMenaces();
            adp.plan();
        }

        @Override
        void doPrepareOrder() {

            if (!this.doneInitConquest) {
                freeDrone.addAll(this.playerDrones.get(ID));

                doneInitConquest = true;
                for (Zone z : zones) {
                    if (z.owner == -1) {
                        Mission conq = new Mission();
                        System.err.println("Creating mission for " + z);
                        conq.createConquestInit(z, this);
                        missionInitConquestProposed.add(conq);
                    }
                }
            } else {
                decomissionMissionInitConquest();
                markFreeDrone();

            }

            examineAndPlanMissionInitConquestProposed();

            // debug
            if (debug_transfertList) {
                System.err.println("missionActives" + missionActives);
                System.err.println("missionActives" + missionInitConquestProposed);
                System.err.println("missionTransfert" + missionTransfert);
                System.err.println("droneTransfert" + droneTransfert);
                System.err.println("freeDrone" + freeDrone);

            }

            applyMissionPlanning();

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
