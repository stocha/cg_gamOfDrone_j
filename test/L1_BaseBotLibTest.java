/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.Point;
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
public class L1_BaseBotLibTest {
    
    public L1_BaseBotLibTest() {
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


     @Test
     public void scalaire() {
         {
            L1_BaseBotLib.DroneBase d=new L1_BaseBotLib.DroneBase() {
            };
            L1_BaseBotLib.ZoneBase z=new L1_BaseBotLib.ZoneBase() {
            };         

            z.cord.setLocation(-10,11);
            d.cord.setLocation(1,0);
            L1_BaseBotLib.GamePos v=new L1_BaseBotLib.GamePos();
            v.set(new Point(-1,1));
            d.speeds.add(v);

            Assert.assertEquals("v approache 1","v approache "+z.headingSpeed(d));

            v.set(new Point(71,-71));
            Assert.assertEquals("v approache neg -100","v approache neg "+z.headingSpeed(d));
        }
         {
            L1_BaseBotLib.DroneBase d=new L1_BaseBotLib.DroneBase() {
            };
            L1_BaseBotLib.ZoneBase z=new L1_BaseBotLib.ZoneBase() {
            };         

            z.cord.setLocation(11,-10);
            d.cord.setLocation(1,-10);
            L1_BaseBotLib.GamePos v=new L1_BaseBotLib.GamePos();
            v.set(new Point(5,0));
            d.speeds.add(v);

            Assert.assertEquals("v approache 5","v approache "+z.headingSpeed(d));

            v.set(new Point(0,-3));
            Assert.assertEquals("v approache null 0","v approache null "+z.headingSpeed(d));
        }     
         
         {
            L1_BaseBotLib.DroneBase d=new L1_BaseBotLib.DroneBase() {
            };
            L1_BaseBotLib.ZoneBase z=new L1_BaseBotLib.ZoneBase() {
            };         

            z.cord.setLocation(-71,71);
            d.cord.setLocation(0,0);
            L1_BaseBotLib.GamePos v=new L1_BaseBotLib.GamePos();
            v.set(new Point(-7,7));
            d.speeds.add(v);

            Assert.assertEquals("lvl heading 11","lvl heading "+z.headingLevel(d));

        }         
     }
}
