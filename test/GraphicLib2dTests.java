/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jahan
 */
public class GraphicLib2dTests {
    
    public GraphicLib2dTests() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    public static class P implements L0_GraphicLib2d.WithCoord{
        final Point p=new Point(0,0);
        
        public P(){
        }
        
        public P(int x,int y){
            p.setLocation(x,y);
        }
        

        @Override
        public Point cord() {
            return p;
        }

        @Override
        public String toString() {
            return p.toString();
        }
        
        
        
    }
    
    @Test
    public void testFarthest(){
        List<P> tos=new ArrayList<>();
        tos.add(new P(-1,0));
        tos.add(new P(2,0));
        
        P zer=new P(0,0);
        
        Assert.assertEquals("Farthest java.awt.Point[x=2,y=0]","Farthest "+L0_GraphicLib2d.farthestFrom(zer, tos));
        Assert.assertEquals("Clothest java.awt.Point[x=-1,y=0]","Clothest "+L0_GraphicLib2d.closestFrom(zer, tos));
        
        Assert.assertEquals("Farthest sort [java.awt.Point[x=2,y=0], java.awt.Point[x=-1,y=0]]","Farthest sort "+L0_GraphicLib2d.sortFarthestFrom(zer, tos));
        Assert.assertEquals("Clothest sort [java.awt.Point[x=-1,y=0], java.awt.Point[x=2,y=0]]","Clothest sort "+L0_GraphicLib2d.sortClothestFrom(zer, tos));   
        
        tos.add(new P(-1,1));
        Assert.assertEquals("Closest elem [java.awt.Point[x=-1,y=0], java.awt.Point[x=-1,y=1]]","Closest elem "+L0_GraphicLib2d.clothestElements( tos));
        Assert.assertEquals("Farthest elem [java.awt.Point[x=-1,y=1], java.awt.Point[x=2,y=0]]","Farthest elem "+L0_GraphicLib2d.farthestElements(tos));
        
        //System.out.println();
        
    
    }
    
    @Test
    public void testTupleDistanceCouple(){
        List<P> A=new ArrayList<>();
        List<P> B=new ArrayList<>();
        
        A.add(new P(0,0));
        A.add(new P(1,0));
        A.add(new P(2,0));
        
        B.add(new P(0,1));
        B.add(new P(1,1));
              
        
        List<L0_GraphicLib2d.Tuple<P,P>> res=L0_GraphicLib2d.lowestCoupleDist(A, B);
        System.out.println(""+res);
        
    }
    
    @Test
    public void testOtherFun(){

        
        P zer=new P(1,1);
        P dir=new P(101,1);
        
        
        
        Assert.assertEquals("java.awt.Point[x=2,y=1]",""+L0_GraphicLib2d.SegABatDistFromA(zer, dir, 1));
        Assert.assertEquals("java.awt.Point[x=4,y=1]",""+L0_GraphicLib2d.SegABatDistFromA(zer, dir, 3));
        
        dir.cord().setLocation(-99,-99);
        Assert.assertEquals("java.awt.Point[x=0,y=0]",""+L0_GraphicLib2d.SegABatDistFromA(zer, dir, 1));
        Assert.assertEquals("java.awt.Point[x=-20,y=-20]",""+L0_GraphicLib2d.SegABatDistFromA(zer, dir, 30));        
        
        zer.cord().setLocation(0,0);
        dir.cord().setLocation(1,1);
        Assert.assertEquals("java.awt.Point[x=1,y=1]",""+L0_GraphicLib2d.SegABatDistFromA(zer, dir, 1));
        Assert.assertEquals("java.awt.Point[x=71,y=71]",""+L0_GraphicLib2d.SegABatDistFromA(zer, dir, 100));         
        
        //System.out.println();
        
        List<P> l= Arrays.asList(new P(-30,-10), new P(30,-10),new P(0,10));
        Assert.assertEquals("java.awt.Point[x=0,y=-3]",""+L0_GraphicLib2d.baryCenter(l));
        
        List<P> l2= Arrays.asList(new P(0,0), new P(0,100),new P(0,100));
        Assert.assertEquals("java.awt.Point[x=0,y=66]",""+L0_GraphicLib2d.baryCenter(l2));
    
    }    
}
