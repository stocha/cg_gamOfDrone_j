/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.


// Pour anticipation trajectoire sur ressources attaques
nbZones=4
nbDrones=5
gameSeed=1432573070715

nbZones=4
nbDrones=11
gameSeed=1432573664773

/////////

 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jahan
 */
public class L3_FirstBotTest {
    
    public L3_FirstBotTest() {
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
    
    @Test
    public void testBot(){
        
        String init="";
        init+="2 0 1 2\n"; // P I D Z
        init+="3 3\n"; // Z CENTER X Y
        init+="2 6\n"; // Z CENTER X Y
        
        init+="-1 -1\n"; // Control
        init+="1 1\n"; // Z CENTER X Y
        
        init+="-1 -1\n"; // control turn 1
        init+="1 1\n"; // turn 1 p1 b CENTER X Y         
        
        init+="-1 -1\n"; // control turn 1
        init+="1 1\n"; // turn 1 p1 b CENTER X Y          
        
        init+="-1 -1\n"; // control turn 1
        init+="1 1\n"; // turn 1 p1 b CENTER X Y         
        
        init+="-1 -1\n"; // control turn 1
        init+="1 1\n"; // turn 1 p1 b CENTER X Y           
        
        
        InputStream stream = new ByteArrayInputStream(init.getBytes(StandardCharsets.UTF_8));
        L1_BaseBotLib.BotBase b=new L3_b_SecondBot.Bot(stream);
        
        b.readTurn();
        b.doPrepareOrder();
        b.writeOrders(System.out);
        
        b.readTurn();
        b.doPrepareOrder();
        b.writeOrders(System.out);
        
        b.readTurn();
        b.doPrepareOrder();
        b.writeOrders(System.out);        
        
        
    }
}
