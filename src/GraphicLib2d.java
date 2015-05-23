/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codinggamedrone;

import java.util.List;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author denis
 */
public class GraphicLib2d {
    
    public interface WithCoord{
        Point cord();
    }
    
    public static Comparator<T extends WithCoord> distance = (e1, e2) -> Integer.compare(
            e1.getEmployeeNumber(), e2.getEmployeeNumber());

    employees.stream().sorted(byEmployeeNumber)
            .forEach(e -> System.out.println(e)); 
    
    public static < T extends WithCoord> T farthestFrom(WithCoord cc,List<T> them){
        double maxDist=0;
        T max=null;
        for(T w : them){
            double dist= cc.cord().distanceSq(w.cord());
            if(dist > maxDist){
                maxDist=dist;
                max=w;
            }
        }        
        return max;
    }
    
    public static < T extends WithCoord> T closestFrom(WithCoord cc,List<T> them){
        double minDist=Double.MAX_VALUE;
        T min=null;
        for(T w : them){
            double dist= cc.cord().distanceSq(w.cord());
            if(dist < minDist){
                minDist=dist;
                min=w;
            }
        }        
        return min;
    }   
    
    public static <T extends WithCoord> List<T> sortFarthestFrom(WithCoord cc, List<T> them){
        List<T> res=new ArrayList<>(them.size());
        res.addAll(them);
        Collections.sort(res, );
    }
    
}
