package com.redshark.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author weswu
 *
 */
public class CommonUtil {
	
	 private static AtomicInteger uidGener = new AtomicInteger(1000);
	 /**
     * 产生sha256
     * @param base
     * @return
     */
    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
           throw new RuntimeException(ex);
        }
    }
    
    public static void copyFrom (Object target, Object source) throws Exception{

        Method[] gettersAndSetters = source.getClass().getMethods();
        for (int i = 0; i < gettersAndSetters.length; i++) {
                String methodName = gettersAndSetters[i].getName();
                try{
                	if(methodName.startsWith("get")){
                		target.getClass().getMethod(methodName.replaceFirst("get", "set") , gettersAndSetters[i].getReturnType() ).invoke(target, gettersAndSetters[i].invoke(source, null));
	                }else if(methodName.startsWith("is") ){
	                	target.getClass().getMethod(methodName.replaceFirst("is", "set") ,  gettersAndSetters[i].getReturnType()  ).invoke(target, gettersAndSetters[i].invoke(source, null));
	                }

                }catch (NoSuchMethodException e) {
                    // TODO: handle exception
                }catch (IllegalArgumentException e) {
                    // TODO: handle exception
                }

        }
    }
    /**
     * 判断是否是有效的Jason字符串
     * @param jsonInString
     * @return
     */
    public static boolean isJSONValid(String jsonInString ) {
        try {
           final ObjectMapper mapper = new ObjectMapper();
           mapper.readTree(jsonInString);
           return true;
        } catch (IOException e) {
           return false;
        }
    }
    
    /**
	 * 按值排序一个map
	 * 
	 * @param oriMap
	 * @return
	 */
	public static Map<Integer, Long> sortMapByValue(Map<Integer, Long> oriMap) {
		Map<Integer, Long> sortedMap = new LinkedHashMap<Integer, Long>();
		if (oriMap != null && !oriMap.isEmpty()) {
			List<Map.Entry<Integer, Long>> entryList = new ArrayList<Map.Entry<Integer, Long>>(oriMap.entrySet());
			Collections.sort(entryList, new Comparator<Map.Entry<Integer, Long>>() {
				public int compare(Entry<Integer, Long> entry1, Entry<Integer, Long> entry2) {
					Long value1 = 0l, value2 = 0l;
					value1 = entry1.getValue();
					value2 = entry2.getValue();
					return value1.compareTo(value2);
				}
			});
			Iterator<Map.Entry<Integer, Long>> iter = entryList.iterator();
			Map.Entry<Integer, Long> tmpEntry = null;
			while (iter.hasNext()) {
				tmpEntry = iter.next();
				sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
			}
		}
		return sortedMap;
	}


	/**
	 * 求Map<String, Integer>中Value(值)的最大值
	 * 
	 * @param map
	 * @return
	 */
	public static int getMaxValue(Map<String, Integer> map) {
		if (null == map || map.size() == 0)
			return 0;
		Collection<Integer> c = map.values();
		Object[] obj = c.toArray();
		Arrays.sort(obj);
		return (int) obj[obj.length - 1];
	}
}
