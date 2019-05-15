package it.polito.ai.mmap.esercitazione3.controller;

import it.polito.ai.mmap.esercitazione3.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
public class AdminController {

    @Autowired
    private UserService userService;

        //TODO: check ANGELO
//    @GetMapping(value="/users")
//    public String getUsers(Model model)
//    {
//        model.addAttribute("users", userService.getAllUsers());
//        return "users";
//    }

}
