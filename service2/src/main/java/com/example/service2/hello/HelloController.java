package com.example.service2.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HelloController {
    private ArrayList<String> arrayList;
    private HashMap<Integer, String> hashMap;

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "World") String name) {
        return "Hello, " + name + "!";
    }

    @GetMapping("/update-array")
    public String updateArrayList(@RequestParam String s) {
        if (arrayList == null) {
            arrayList = new ArrayList<>();
        }
        arrayList.add(s);
        return "Added to ArrayList: " + s;
    }

    @GetMapping("/show-array")
    public List<String> showArrayList() {
        return arrayList == null ? List.of() : arrayList;
    }

    @GetMapping("/update-map")
    public String updateHashMap(@RequestParam String s) {
        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        hashMap.put(hashMap.size() + 1, s);
        return "Added to HashMap: " + s;
    }

    @GetMapping("/show-map")
    public Map<Integer, String> showHashMap() {
        return hashMap == null ? Map.of() : hashMap;
    }

    @GetMapping("/show-all-length")
    public String showAllLength() {
        int arrayLength = arrayList == null ? 0 : arrayList.size();
        int mapLength = hashMap == null ? 0 : hashMap.size();
        return "ArrayList: " + arrayLength + ", HashMap: " + mapLength;
    }
}
