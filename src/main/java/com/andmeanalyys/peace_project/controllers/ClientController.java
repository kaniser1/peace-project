package com.andmeanalyys.peace_project.controllers;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientController {

    @GetMapping("/")
    public String redirectToPeaceProject() {
        return "redirect:/login";
    }

    @GetMapping("/peace-project")
    public String servePeaceProject() {
        return "forward:/peace-project/index.html";
    }

    @GetMapping("/x-clone")
    public String serveXClone() {
        return "forward:/x-clone/index.html";
    }

    @GetMapping("/login")
    public String serveLoginPage() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "forward:/login/index.html";
        }
        return "forward:/peace-project/index.html";
    }
}