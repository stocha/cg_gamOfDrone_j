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

    final static int DISTCONT = 100;

    /**
     * Forces presentes ou future
     */
    static class Forces {

        public static final int nbTurns = 20;

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
                        double dfut = t * DISTCONT;
                        dfut *= dfut;

                        if (dist < dfut) {
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

        Orders(int D) {
            this.D = D;
            done = new boolean[D];

            orders = new Point[D];
            for (int i = 0; i < D; i++) {
                orders[i] = new Point(2000, 100 + i * 100);
            }
        }

        public void restTurn() {
            for (int i = 0; i < D; i++) {
                done[i] = false;
            }
        }

        public Point get(int i) {
            return orders[i];
        }

        public boolean sendPacketClosestTo(Point dest, List<Point> pos, int nb) {

            boolean suc = true;
            for (int sed = 0; sed < nb; sed++) {
                suc &= sendClosestTo(dest, pos);
            }

            return suc;
        }

        public boolean sendClosestTo(Point dest, List<Point> pos) {

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
                return false;
            }

            done[found] = true;
            orders[found].setLocation(dest);

            return true;

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

        Orders orders;

        WorldBase(InputStream inst) {
            this.in = new Scanner(inst);
            P = in.nextInt(); // number of players in the game (2 to 4 players)
            ID = in.nextInt(); // ID of your player (0, 1, 2, or 3)
            D = in.nextInt(); // number of drones in each team (3 to 11)
            Z = in.nextInt(); // number of zones on the map (4 to 8)          

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

        private Point dco(int p, int j) {
            return playDrone.get(p).get(j);
        }

        public void readFromLoop() {
            for (int i = 0; i < Z; i++) {
                z.get(i).owner = in.nextInt();
            }
            for (int i = 0; i < P; i++) {
                for (int j = 0; j < D; j++) {
                    dco(i, j).setLocation(in.nextInt(), in.nextInt());
                }

            }
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

            for (int t = 0; t < Forces.nbTurns; t++) {
                for (int i = 0; i < Z; i++) {

                    int mother = maxOther.v[i][t];
                    int mme = maxMe.v[i][t];
                    if (mme > mother && z.get(i).owner != ID) {

                        boolean success = true;
                        success &= orders.sendPacketClosestTo(z.get(i).co, playDrone.get(ID), mother + 1);

                        if (!success) {
                            return;
                        }
                    }

                }
            }

        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WorldBase wb = new WorldBase(System.in);

        while (true) {
            long t0 = System.currentTimeMillis();

            wb.readFromLoop();
            wb.turnCalc();

            wb.turnPlanning();

            wb.writeOrders(System.out);

            long t1 = System.currentTimeMillis();

            double t = t1 - t0;
            System.err.println("temps mili " + t);
        }

    }

}
