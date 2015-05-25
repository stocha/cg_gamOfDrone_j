
import java.awt.Point;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    static boolean debugPlanner_calcMenace = false;

    public static class Drone extends L1_BaseBotLib.DroneBase implements L0_GraphicLib2d.WithCoord {

        @Override
        public String toString() {
            return "{" +this.owner+"/"+this.id+ '}';
        }
        
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

                SimpleMissions(L0_GraphicLib2d.WithCoord cible, int life) {
                    this.life = life;
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

            public void greedySuite() {
                if (freeDrone.isEmpty()) {
                    return;
                }

                final List<L0_GraphicLib2d.Tuple<Drone, Zone>> dist = L0_GraphicLib2d.lowestCoupleDist(playerDrones.get(ID), zones);
                List<Drone> planed = new ArrayList<>(freeDrone.size());

                int zoneCount[] = new int[Z];

                double maxDist = 0;
                for (L0_GraphicLib2d.Tuple<Drone, Zone> s : dist) {
                    if (planed.contains(s.a)) {
                        continue;
                    }
                    planed.add(s.a);
                    zoneCount[s.b.id]++;

                    if (s.distSq > maxDist) {
                        maxDist = s.distSq;
                    }
                }

                if (!(maxDist <= L1_BaseBotLib.lvl0Dist)) {
                    return;
                }

                List<Drone> atMostPopulated = new ArrayList<>();
                int maxPop = 0;
                int locMax = 0;

                for (int i = 0; i < Z; i++) {
                    if (locMax < zoneCount[i]) {
                        locMax = zoneCount[i];
                        maxPop = i;
                    }
                }
                Zone maxWorld = zones.get(maxPop);
                System.err.println("Max populated = " + maxWorld);

                for (L0_GraphicLib2d.Tuple<Drone, Zone> s : dist) {
                    if (s.distSq >= 50) {
                        break;
                    }

                    if (s.b.id == maxWorld.id) {
                        atMostPopulated.add(s.a);
                    }
                }
                // --------------- Selected drones at MaxPop
                System.err.println("Drones to expand " + atMostPopulated);

                List<Zone> otherZones = new ArrayList<>();
                otherZones.addAll(zones);
                otherZones.remove(maxWorld);

                double maxDistZ = 0;
                for (Zone ot : otherZones) {
                    double dd = ot.cord.distance(maxWorld.cord);
                    if (dd > maxDistZ) {
                        maxDistZ = dd;
                    }
                }
                int TimeToLive = (int) maxDistZ / L1_BaseBotLib.lvl0Dist + 1;

                for (Zone ot : otherZones) {
                    Drone d = atMostPopulated.get(0);

                    L1_BaseBotLib.GamePos gp = new L1_BaseBotLib.GamePos();
                    gp.set(new Point(2000, 20));

                    SimpleMissions mi = new SimpleMissions(ot, TimeToLive);
                    mission.add(mi);
                    mi.addDrone(d);
                    atMostPopulated.remove(d);
                    if (atMostPopulated.isEmpty()) {
                        break;
                    }
                }

                atMostPopulated.clear();

                atMostPopulated.addAll(freeDrone);
                for (Drone ot : atMostPopulated) {
                    SimpleMissions mi = new SimpleMissions(ot, TimeToLive);
                    mission.add(mi);
                    mi.addDrone(ot);
                }

            }

            public class SectorHyp {

                final List<List<Drone>> en = new ArrayList<>();
                final List<List<Drone>> fr = new ArrayList<>();

                final int nbTurnsPlan = 47;

                final Zone core;

                @Override
                public String toString() {
                    return ""+core + "\n en=" + en + "\n fr=" + fr + ", nbTurnsPlan=" + nbTurnsPlan + '}';
                }
                
                

                SectorHyp(Zone core, List<L0_GraphicLib2d.Tuple<Drone, Zone>> frienCl, List<L0_GraphicLib2d.Tuple<Drone, Zone>> eneCl) {
                    for (int i = 0; i < nbTurnsPlan; i++) {
                        en.add(new ArrayList<>(D));
                        fr.add(new ArrayList<>(D));
                    }

                    List<L0_GraphicLib2d.Tuple<Drone, Zone>> filfriend
                            = frienCl.stream().filter(w -> w.b == core).collect(Collectors.toList());

                    List<L0_GraphicLib2d.Tuple<Drone, Zone>> filEne
                            = eneCl.stream().filter(w -> w.b == core).collect(Collectors.toList());

                    this.core = core;

                    for (L0_GraphicLib2d.Tuple<Drone, Zone> t : filEne) {
                        double tA = (Math.sqrt(t.distSq) - L1_BaseBotLib.lvl0Dist) / L1_BaseBotLib.lvl0Dist;
                        int ta = (int)Math.max(0, tA);
                        for(int i=ta;i<nbTurnsPlan;i++){
                            en.get(i).add(t.a);
                        }
                    }
                    
                    for (L0_GraphicLib2d.Tuple<Drone, Zone> t : filfriend) {
                        double tA = (Math.sqrt(t.distSq) + 3) / L1_BaseBotLib.lvl0Dist;
                        int ta = (int)Math.max(0, tA);
                        for(int i=ta;i<nbTurnsPlan;i++){
                            fr.get(i).add(t.a);
                        }
                    }                    
                }

            }

            public void attackDefPlaning() {
                final List<L0_GraphicLib2d.Tuple<Drone, Zone>> friendDist = L0_GraphicLib2d.lowestCoupleDist(playerDrones.get(ID), zones);
                final List<L0_GraphicLib2d.Tuple<Drone, Zone>> enDist = L0_GraphicLib2d.lowestCoupleDist(playerDrones.get(ID), zones);

                Drone[] hypDef = new Drone[200];
                Drone[] hypAtt = new Drone[200];
                
                SectorHyp sh[]=new SectorHyp[Z];
                for(int i=0;i<Z;i++){
                    sh[i]=new SectorHyp(zones.get(i), friendDist, enDist);
                    System.err.println(""+sh[i]);
                }

            }

            public void mirrorPlaning() {
                final List<L0_GraphicLib2d.Tuple<Drone, Drone>> dist = L0_GraphicLib2d.lowestCoupleDist(playerDrones.get(ID), playerDrones.get(ID ^ 1));
                List<Drone> planed = new ArrayList<>(D);
                List<Drone> marked = new ArrayList<>(D);

                List<Zone> friendly = new ArrayList<>(D);
                friendly.addAll(zones);

                for (L0_GraphicLib2d.Tuple<Drone, Drone> s : dist) {
                    if (!freeDrone.contains(s.a)) {
                        continue;
                    }
                    if (planed.contains(s.a)) {
                        continue;
                    }
                    if (marked.contains(s.b)) {
                        continue;
                    }

                    L1_BaseBotLib.GamePos prio = null;

                    if (friendly.size() > 0) {
                        Zone closestToT = L0_GraphicLib2d.closestFrom(s.b, friendly);
                        if (closestToT.cord.distance(s.b.cord) < (L1_BaseBotLib.lvl0Dist - 5) * 2) {
                            Point p = L0_GraphicLib2d.SegABatDistFromA(closestToT, s.b, L1_BaseBotLib.lvl0Dist - 5);
                            prio = new L1_BaseBotLib.GamePos();
                            prio.set(p);
                        }
                    }

                    if (prio != null) {
                        // NearPlanet Friend
                        SimpleMissions mi = new SimpleMissions(prio, 1);
                        mission.add(mi);
                        mi.addDrone(s.a);
                        planed.add(s.a);
                        marked.add(s.b);
                    } else {
                        SimpleMissions mi = new SimpleMissions(s.b, 1);
                        mission.add(mi);
                        mi.addDrone(s.a);
                        planed.add(s.a);
                        marked.add(s.b);
                    }
                }
            }

            boolean once = true;

            public void greedyPlaning() {
                if (!once) {
                    return;
                }
                once = false;

                final List<L0_GraphicLib2d.Tuple<Drone, Zone>> dist = L0_GraphicLib2d.lowestCoupleDist(playerDrones.get(ID), zones);
                List<Drone> planed = new ArrayList<>(freeDrone.size());

                double maxDist = 0;
                for (L0_GraphicLib2d.Tuple<Drone, Zone> s : dist) {
                    if (planed.contains(s.a)) {
                        continue;
                    }
                    planed.add(s.a);

                    if (s.distSq > maxDist) {
                        maxDist = s.distSq;
                    }
                }

                if (maxDist <= L1_BaseBotLib.lvl0Dist) {
                    once = false;
                }

                planed.clear();
                for (L0_GraphicLib2d.Tuple<Drone, Zone> s : dist) {
                    if (planed.contains(s.a)) {
                        continue;
                    }

                    SimpleMissions mi = new SimpleMissions(s.b, (int) (Math.sqrt(s.distSq) / (L1_BaseBotLib.lvl0Dist - 1)) + 1);
                    mission.add(mi);
                    mi.addDrone(s.a);
                    planed.add(s.a);
                }

            }

            public void plan() {
                pre_plan();

                List<Zone> mine = new ArrayList<>(Z);
                List<Zone> ene = new ArrayList<>(Z);

                for (Zone zr : zones) {
                    if (zr.owner == ID || (zr.owner == -1 && P == 2)) {
                        mine.add(zr);
                    } else {
                        ene.add(zr);
                    }
                }

                greedyPlaning();
                //greedySuite();
                attackDefPlaning();
                mirrorPlaning();

                if (debugPlanner_calcMenace) {
                    System.err.println("Mission " + mission);
                }
                post_plan();

            }
        }

        boolean doneInitConquest = false;
        AttackDefPlanner adp = null;

        List<Drone> freeDrone = new ArrayList<>(L1_BaseBotLib.maxDrones);

        public Bot(InputStream inst) {
            super(inst);
        }

        @Override
        void doPrepareOrder() {
            if (this._turn_Number == 0) {
                freeDrone.addAll(this.playerDrones.get(ID));
            }
            if (adp == null) {
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
