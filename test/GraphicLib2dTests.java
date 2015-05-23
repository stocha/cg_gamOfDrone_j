/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import codinggamedrone.GraphicLib2d;
import java.awt.Point;
import java.util.ArrayList;
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
    
    public static class P implements GraphicLib2d.WithCoord{
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
        
        Assert.assertEquals("Farthest java.awt.Point[x=2,y=0]","Farthest "+GraphicLib2d.farthestFrom(zer, tos));
        Assert.assertEquals("Clothest java.awt.Point[x=-1,y=0]","Clothest "+GraphicLib2d.closestFrom(zer, tos));
        
        Assert.assertEquals("Farthest sort [java.awt.Point[x=2,y=0], java.awt.Point[x=-1,y=0]]","Farthest sort "+GraphicLib2d.sortFarthestFrom(zer, tos));
        Assert.assertEquals("Clothest sort [java.awt.Point[x=-1,y=0], java.awt.Point[x=2,y=0]]","Clothest sort "+GraphicLib2d.sortClothestFrom(zer, tos));        
        
    
    }
}
