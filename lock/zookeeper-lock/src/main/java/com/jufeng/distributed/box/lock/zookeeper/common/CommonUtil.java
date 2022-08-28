package com.jufeng.distributed.box.lock.zookeeper.common;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-24 22:40
 **/
public class CommonUtil {
    public static int getMin(List<String> pathList) {

       return pathList.stream()
                .map(x->{
                    final String lock_ = x.replace("lock_", "");
                    return Integer.valueOf(lock_);
                    }
                ).min(Comparator.comparing(Integer::intValue)).get();
    }

    public static int getNumber(String path){
        final String[] s = path.split("_");
        return Integer.valueOf(s[1]).intValue();
    }

    public static String getYoungerBrother(String source,List<String> pathList,String currentPath){
        pathList.sort(Comparator.comparing(String::new));
        int size = pathList.size();
        for (int i = 0; i< size; i++){
            String p = "/"+source + "/"+pathList.get(i);
            if (p.equals(currentPath)){
                if (i>0){
                    return  "/"+source + "/"+pathList.get(i-1);
                }
            }
        }
        return null;
    }
}
