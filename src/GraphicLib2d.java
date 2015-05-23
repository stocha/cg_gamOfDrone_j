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
import java.util.Comparator;

/**
 *
 * @author denis
 */
public class GraphicLib2d {
    
    public interface WithCoord{
        Point cord();
    }

    
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
        final Comparator<WithCoord> distance;
        distance = (e1, e2) -> (int)(e2.cord().distanceSq(cc.cord())-e1.cord().distanceSq(cc.cord()));
        Collections.sort(res,distance );
        return res;
    }
    
    public static <T extends WithCoord> List<T> sortClothestFrom(WithCoord cc, List<T> them){
        List<T> res=new ArrayList<>(them.size());
        res.addAll(them);
        final Comparator<WithCoord> distance;
        distance = (e1, e2) -> (int)(e1.cord().distanceSq(cc.cord())-e2.cord().distanceSq(cc.cord()));
        Collections.sort(res,distance );
        return res;
    }    
    
}
