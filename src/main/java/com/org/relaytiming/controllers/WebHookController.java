package com.org.relaytiming.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class WebHookController {
    @RequestMapping("/")
    public String index() {
        return "index.html";
    }
    
    @GetMapping("/api/items")
    @ResponseBody
    public String getAll() {
        return "[{\"id\":1,\"name\":\"Item 1\"},{\"id\":2,\"name\":\"Item 2\"}]";
    }
    
    @GetMapping("/api/items/{id}")
    @ResponseBody
    public String getById(@PathVariable Long id) {
        return "{\"id\":" + id + ",\"name\":\"Item " + id + "\"}";
    }
    
    @PostMapping("/api/items")
    @ResponseBody
    public String create(@RequestBody String item) {
        return "{\"id\":3,\"name\":\"New Item\",\"status\":\"created\"}";
    }
    
    @PutMapping("/api/items/{id}")
    @ResponseBody
    public String update(@PathVariable Long id, @RequestBody String item) {
        return "{\"id\":" + id + ",\"name\":\"Updated Item\",\"status\":\"updated\"}";
    }
    
    @DeleteMapping("/api/items/{id}")
    @ResponseBody
    public String delete(@PathVariable Long id) {
        return "{\"id\":" + id + ",\"status\":\"deleted\"}";
    }
}
