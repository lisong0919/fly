package com.wealth.fly.core.strategy.criteria;


import java.util.HashSet;
import java.util.Set;

public class CriteriaParser {


    public static Set<Sector> parseSectorType(Criteria criteria){
        Set<Sector> result=new HashSet<>();
        if(criteria instanceof SimpleCriteria){
            SimpleCriteria simpleCriteria=(SimpleCriteria)criteria;
            result.add(simpleCriteria.getSource());
            result.add(simpleCriteria.getTarget());
        }else if(criteria instanceof CompoundCriteria){
            CompoundCriteria compoundCriteria=(CompoundCriteria) criteria;
            for(Criteria c:compoundCriteria.getCriteriaList()){
                result.addAll(parseSectorType(c));
            }
        }else if(criteria instanceof LastNKlineCriteria){
            LastNKlineCriteria lastNKlineCriteria=(LastNKlineCriteria) criteria;
            result.addAll(parseSectorType(lastNKlineCriteria.getMatcher()));
        }
        return result;
    }

   public static int getLastKlineMaxNum(Criteria criteria){
        int max=0;
        if(criteria instanceof CompoundCriteria){
            CompoundCriteria compoundCriteria=(CompoundCriteria) criteria;
            for(Criteria c:compoundCriteria.getCriteriaList()){
                int v=getLastKlineMaxNum(c);
                if(v>max){
                    max=v;
                }
            }
        }else if(criteria instanceof LastNKlineCriteria){
            LastNKlineCriteria lastNKlineCriteria=(LastNKlineCriteria) criteria;
            if(lastNKlineCriteria.getN()>max){
                max=lastNKlineCriteria.getN();
            }
        }
        return max;
   }

}
