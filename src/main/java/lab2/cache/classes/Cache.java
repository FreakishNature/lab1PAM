package lab2.cache.classes;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Cache {
    ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();

    public List<String> getList(String key) throws Exception {
        if (!data.containsKey(key)) {
            data.put(key, new ArrayList<String>());
        }
        if (data.get(key) instanceof List) {
            return (List<String>) data.get(key);
        } else {
            throw new Exception("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public void setnx(String key, Object value) {
        if (!data.containsKey(key) && data.get(key) == null) {
            data.put(key, value);
        }
    }

    public List<Object> mget(List<String> keys) {
        return keys.stream().map(key -> data.getOrDefault(key, null))
                .collect(Collectors.toList());
    }

    public Integer del(List<String> keys) {
        AtomicInteger count = new AtomicInteger();
        keys.forEach(key -> {
            if (data.containsKey(key)) {
                data.remove(key);
                count.getAndIncrement();
            }
        });
        return count.get();
    }

    public Integer incr(String key) {
        if (!data.containsKey(key)) data.put(key, "0");
        Integer num = (Integer) data.get(key);
        data.put(key, num);
        return num;
    }

    public Integer llen(String key) throws Exception {
        return getList(key).size();
    }

    public List<String> lrange(String key, int start, int finish) throws Exception {
        return getList(key).subList(start, finish);
    }

    public Integer lrem(String key, int count, String item) throws Exception {
        List<String> list = getList(key);
        Integer deletedCount = 0;

        for (int i = 0; i < count; i++) {
            int index = list.indexOf(item);
            if (index == -1) break;
            deletedCount++;
            list.remove(index);
        }
        data.put(key, list);
        return deletedCount;
    }

    public Integer lpush(String key, String value) throws Exception {
        List<String> list = getList(key);
        list.add(0, value);
        data.put(key, list);
        return list.size();
    }

    public String rpoplpush(String key1, String key2) throws Exception {
        List<String> list1 = getList(key1);
        List<String> list2 = getList(key2);

        String item = list1.remove(list1.size() - 1);
        list2.add(0, item);

        data.put(key1, list1);
        data.put(key2, list2);
        return item;
    }
}
