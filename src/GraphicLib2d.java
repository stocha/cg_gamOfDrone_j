/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
    
    public static <T extends WithCoord> List<T> clothestElements(List<T> them){
        List<T> res=new ArrayList<>(them.size());

        double minDist=Double.MAX_VALUE;
        T A=null;
        T B=null;
        for(T w : them) for(T w2 : them){
            if(w==w2) continue;
            double dist= w2.cord().distanceSq(w.cord());
            if(dist < minDist){
                minDist=dist;
                A=w;B=w2;
            }
        }        
        res.add(A);
        res.add(B);
        
        return res;
    }   
    
    public static <T extends WithCoord> List<T> farthestElements(List<T> them){
        List<T> res=new ArrayList<>(them.size());

        double minDist=Double.MAX_VALUE;
        T A=null;
        T B=null;
        double maxDist=0;
        for(T w : them) for(T w2 : them){
            if(w==w2) continue;
            double dist= w2.cord().distanceSq(w.cord());
            if(dist > maxDist){
                maxDist=dist;
                A=w;B=w2;
            }
        }                 
        res.add(A);
        res.add(B);
        
        return res;
    }     
    
    public static <T extends WithCoord> Point SegABatDistFromA(T A, T B, double dist){
            double dd=A.cord().distance(B.cord());
            if(dd==0) return A.cord();
            double rat=dist/dd;
            Point res=new Point(B.cord().x-A.cord().x, B.cord().y-A.cord().y);
            res.setLocation(res.x*rat,res.y*rat);
            res.setLocation(res.x+A.cord().x,res.y+A.cord().y);
            
            return res;
    }
    
    public static <T extends WithCoord> Point baryCenter(List<T> them){
        Point res=new Point(0,0);
        them.stream().forEach((w) -> {
            res.setLocation(res.x+w.cord().x,res.y+w.cord().y);
        });                 
        res.setLocation(res.x/them.size(),res.y/them.size());
        
        return res;
    }     
    
}
