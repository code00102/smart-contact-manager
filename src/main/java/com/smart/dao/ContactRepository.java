package com.smart.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	
	//pagination
	
	//we have interface called Page to achieve pagination. 
	//Page is the sublist of the list of the object. It allows to gain information about the position of it in the containing entire list...
	//List ko kunai position ma bhayeko information lai gain garna sakincha with the help of Page. Not talking about the whole list.
	
	//Pageable
	//Yo interface ma chai pagination ko information store huncha....
	
//	@Query("from Contact as c where c.user.user_id=:id")
//	public List<Contact> getAllContactsFromAUser(@Param("id") int id);
	
	@Query("from Contact as c where c.user.user_id=:id")
	public Page<Contact> getAllContactsFromAUser(@Param("id") int id, Pageable pageable);


}
