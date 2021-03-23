package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.messages.Message;



@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title", "Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/signup")
	public String signUp(Model model) {
		
		model.addAttribute("title", "Sign Up-Smart Contact Manager");
		model.addAttribute("user",  new User());
		return "signup";
		
	}
	
	@PostMapping("/handleform")
	public String handleForm(@Valid @ModelAttribute("user") User user, BindingResult results, @RequestParam(value = "isChecked", defaultValue = "false") boolean agreement, Model m , HttpSession session) {
		
//		if(!agreement) {
//			return "signup";
//		}
//		
//		else {
//			System.out.println(user.toString());
//			this.userRepository.save(user);
//			return "home";
//		}
		
		
		try {
			
		if(!agreement) {
		
		System.out.println(agreement);
		
		
		throw new Exception("Agreement not checked");
		
		
		}
		
		if(results.hasErrors()) {
			m.addAttribute("user", user);
			return "signup";
		}
		
		
		
		user.setRole("ROLE_USER");
		user.setEnabled(true);
		user.setImageUrl("default.png");
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		
		System.out.println(user);
		
		 userRepository.save(user);
		m.addAttribute("user", new User());
		session.setAttribute("message",   new Message("Successfully Registered", "alert-success") );
		return "signup";
		
		}
		catch (Exception e) {
			
			m.addAttribute("user",user);
			session.setAttribute("message",  new  Message("Please fill your form properly", "alert-danger"));
			
			return "signup";
		
		}
		
	}
	
	@RequestMapping("/signin")
	public String signinPage(Model model) {
		
		model.addAttribute("title", "Sign In- Smart Contact Manager");
		return "signin"; 
		
	}
}
