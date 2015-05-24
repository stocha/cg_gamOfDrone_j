
import com.sun.management.jmx.Trace;
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

        public class MissionAttack {

            @Override
            public String toString() {
                return "MissionAttack{" + "assignedResource=" + assignedResource.size() + ", missionTarget=" + missionTarget.id + ", nbTurns=" + nbTurns + ", done=" + done + '}';
            }

            List<Drone> assignedResource = new ArrayList<>(L1_BaseBotLib.maxDrones);
            Zone missionTarget = null;

            int nbTurns = 3;
            boolean done = false;
            int desiredTeam = 0;

            MissionAttack(Zone cible, int desiredteam) {
                missionTarget = cible;
                this.desiredTeam = desiredteam;
            }

            void captureRessource() {
                if (done) {
                    return;
                }

                int souhai = desiredTeam;
                if (assignedResource.size() >= souhai) {
                    return;
                }
                for (int i = 0; i < souhai - assignedResource.size(); i++) {
                    if (context.freeDrone.isEmpty()) {
                        break;
                    }

                    Drone close = L0_GraphicLib2d.closestFrom(missionTarget, context.freeDrone);
                    assignedResource.add(close);
                    context.freeDrone.removeAll(assignedResource);
                }

                if (assignedResource.isEmpty()) {
                    done = true;
                }
            }

            void sendDrones() {
                for (Drone d : assignedResource) {
                    if(context._orders[d.id].x==20 && context._orders[d.id].y==20)
                        context._orders[d.id].setLocation(missionTarget.cord);
                }
            }

            void releaseRessource() {
                if (done || assignedResource.isEmpty()) {
                    return;
                }

                Drone d = L0_GraphicLib2d.farthestFrom(missionTarget, assignedResource);

                if (d.cord.distance(missionTarget.cord) < L1_BaseBotLib.lvl0Dist) {
                    nbTurns--;
                }

                if (nbTurns <= 0) {
                    context.freeDrone.addAll(assignedResource);
                    assignedResource.clear();
                    done = true;
                }
            }

            boolean isDone() {
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

        private final List<MissionAttack> mission = new ArrayList<>(40);

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

        public int calcMenace(Zone z) {
            return 2;
        }

        public int etaForMenaceLevel(Zone z, int level) {
            return 0;
        }

        public void plan() {
            List<Zone> mine = new ArrayList<>(context.Z);
            List<Zone> ene = new ArrayList<>(context.Z);

            for (Zone zr : context.zones) {
                if (zr.owner == context.ID) {
                    mine.add(zr);
                } else {
                    ene.add(zr);
                }
            }
            


            for (Zone zr : ene) {                
                int force;
                if(zr.owner!=-1)
                    force=sectorMenace.get(zr.owner).get(zr.id).size();else{
                    force=0;
                }
                if (mission.size() < 3) {
                    mission.add(new MissionAttack(zr, force+1));
                }
            }             
            List<MissionAttack> rm = new ArrayList<>();
            for (MissionAttack a : mission) {
                a.captureRessource();
                a.releaseRessource();
                if (a.isDone()) {
                    rm.add(a);
                }
                a.sendDrones();
            }

            mission.removeAll(rm);                       

            for (Zone zr : mine) {

                for (int p = 0; p < context.P; p++) {
                    int currEta = 0;
                    int ef = 0;
                    int ff = 0;

                    int ind = 0;
                    Menace myfor = null;
                    List<Drone> enroute = new ArrayList<>(context.D);

                    if (sectorResource.get(zr.id).size() > ind) {
                        myfor = sectorResource.get(zr.id).get(ind++);
                    }

                    for (Menace m : sectorMenace.get(p).get(zr.id)) {
                        if (m.eta == currEta) {
                            ef++;
                        }
                        if (m.eta > currEta) {
                            currEta = m.eta;
                            ef++;
                        }

                        while (myfor != null && myfor.eta <= currEta) {
                            ff++;

                            if (ff >= ef) {
                                enroute.add(myfor.d);
                            }

                            if (sectorResource.get(zr.id).size() > ind) {
                                myfor = sectorResource.get(zr.id).get(ind++);
                            } else {
                                myfor = null;
                            }
                        }
                    }
                    int menace=ef-enroute.size();
                    for(int i=0;i<menace;i++){
                        for(Menace r : sectorResource.get(zr.id)){
                            if(enroute.contains(r.d)) continue;
                            {
                                context._orders[r.d.id].setLocation(zr.cord);
                            }
                        }
                    }
                }

            }


            Point center = null;

            if (!mine.isEmpty()) {
                center = L0_GraphicLib2d.baryCenter(mine);
            }
            if (center
                    == null) {
                center = new Point(2000, 800);
            }
            for (Drone d
                    : context.freeDrone) {
                context._orders[d.id].setLocation(center);
            }

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
