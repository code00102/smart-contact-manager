package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.messages.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String name = principal.getName();
		System.out.println(name);

		// get the user using username aka email.
		User userByUserName = this.userRepository.getUserByUserName(name);
		model.addAttribute("user", userByUserName);
		model.addAttribute("title", userByUserName.getUsername());

	}

	@RequestMapping("/index")
	public String userDash(Model model, Principal principal) {

		return "normal/user-dashboard";
	}

	@RequestMapping("/add-contacts")
	public String openAddContactForm(Model m) {

		m.addAttribute("title", "Add New Contact");
		return "normal/add-contact";
	}

	// processing add contact form

	@PostMapping("/process-contact")
	public String processContact(Model m, @ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {

		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		contact.setUser(user);
		try {

			if (file.isEmpty()) {
				contact.setPhoto("contact.png");
			}

			else {

				contact.setPhoto(file.getOriginalFilename());

				// images saving in folder and adding the name in database
				File myFile = new ClassPathResource("/static/img/").getFile();
				Path path = Paths.get(myFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("File uploaded successfully");
			}
			// sending message
			session.setAttribute("message", new Message("Contact successfully registered.", "success"));
			user.getContacts().add(contact);
			this.userRepository.save(user);

			System.out.println("Added successfully");

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong! Please try again.", "danger"));
		}

		return "normal/add-contact";
	}

	// handler for view contacts
	// n = no.of contacts per pages
	// page = current page = 0(initially aka first page)
	@GetMapping("/view-contacts/{page}")
	public String handleViewContacts(@PathVariable("page") int page, Model m, Principal principal) {

		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);

		// pageable will have n and page.
		Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> allContactsFromAUser = this.contactRepository.getAllContactsFromAUser(user.getUser_id(),
				pageable);
		m.addAttribute("contacts", allContactsFromAUser);
		m.addAttribute("currentPage", page);
		m.addAttribute("total_pages", allContactsFromAUser.getTotalPages());

		return "normal/view_contacts";
	}

	// show single contact information
	@GetMapping("/vc/{id}")
	public String showSingleContactInfo(@PathVariable("id") Integer id, Model model, Principal principal) {

		Optional<Contact> singleContact = this.contactRepository.findById(id);
		Contact contact = singleContact.get();
		int user_id = contact.getUser().getUser_id();

		User user = this.userRepository.getUserByUserName(principal.getName());

		if (user_id == user.getUser_id()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", user.getUsername());
		}

		return "normal/show_single_contact";
	}

	// deleting contact
	@GetMapping("/delete/{id}")
	public String deleteSingleContact(@PathVariable("id") int id, Principal principal, HttpSession session) {

		Optional<Contact> opt = this.contactRepository.findById(id);
		Contact contact = opt.get();

		int user_id = contact.getUser().getUser_id();

		User user = this.userRepository.getUserByUserName(principal.getName());

		if (user_id == user.getUser_id()) {
			
			//deleting the image
			
			File delFile;
			try {
				delFile = new ClassPathResource("/static/img").getFile();
				File f = new File(delFile, contact.getPhoto());
				f.delete();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
			

			contact.setUser(null);
			this.contactRepository.delete(contact);
			session.setAttribute("message", new Message("Contact successfully deleted", "alert-success"));

		}

		return "redirect:/user/view-contacts/0";
	}

	@RequestMapping("/update/{id}")
	public String updateSingleContact(@PathVariable("id") int id, Model m, Principal principal, HttpSession session) {

		Optional<Contact> opt = this.contactRepository.findById(id);
		Contact contact = opt.get();
		int user_id = contact.getUser().getUser_id();

		String name = principal.getName();
		User userByUserName = this.userRepository.getUserByUserName(name);
		if (userByUserName.getUser_id() == user_id) {

		
			m.addAttribute("contact", contact);
			return "normal/update_contact";
		}

		else {

			return "redirect:/user/view-contacts/0";

		}
	} 
	
	
	@PostMapping("/on_update_submit")
	public String onUpdateSubmit(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, 
			Model model, HttpSession session,
			Principal principal) {
		
		try {
			
			//old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getContact_id()).get();
			
			
			if(!file.isEmpty()) {
			
				//rewrite the file
				//to rewrite --> delete the old photo and upload a new one. 
				
				//delete the old photo
				
				File delFile = new ClassPathResource("/static/img").getFile();
				File f = new File(delFile, oldContactDetail.getPhoto());
				f.delete();
				
				
				
				
				//upload a new photo
				contact.setPhoto(file.getOriginalFilename());
				File imgfile = new ClassPathResource("/static/img").getFile();
				Path path = Paths.get(imgfile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
				
			}
			else {
				contact.setPhoto(oldContactDetail.getPhoto());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);

			this.contactRepository.save(contact);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return "redirect:/user/vc/"+contact.getContact_id()+"/";
	}
	
	

}
