package com.tawana.controllers;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tawana.exception.CustomException;
import com.tawana.models.authentication.AuthRequest;
import com.tawana.models.authentication.ChangePassword;
import com.tawana.models.authentication.ForgotPassword;
import com.tawana.models.authentication.ProfileUpdate;
import com.tawana.models.authentication.RegisterDevice;
import com.tawana.models.authentication.User;
import com.tawana.services.UserService;

@RestController
@CrossOrigin
@RequestMapping("/authenticate")
public class AuthenticationController {
	
	private final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

	@Autowired
	private UserService userService;

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody AuthRequest loginUser) {
		return userService.login(loginUser);
	}

	@PostMapping("/register")
	public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
		log.info("User register:"+user);
		return userService.createUser(user);
	}
	
	@PostMapping("/register-device")
	public ResponseEntity<?> registerDevice(@Valid @RequestBody RegisterDevice reg_device) {
		log.info("Devices register:"+reg_device);
		return userService.registerDevice(reg_device);
	}

	@PostMapping("/forgot_password")
	public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPassword forgotPassword) {
		return userService.forgotPassword(forgotPassword);
	}

	
	@PostMapping("/change_password")
	public ResponseEntity<?> chOangePassword(@Valid @RequestBody ChangePassword changePassword) {
		return userService.changePassword(changePassword);
	}
	
	 @PostMapping("/profile_update")
	 public ResponseEntity<?> profileUpdates(@Valid @RequestBody ProfileUpdate profile_update){	    		
           return userService.profileUpdate(profile_update);
	    }


    @PostMapping("/profile_image_update")
    public ResponseEntity<?> profileImageUpdate( @RequestParam("user_avatar") MultipartFile user_avatar){
    	log.info("User user_avatar:"+user_avatar);
    	try {
             return userService.profileImageUpdate(user_avatar);
    	}catch(Exception e) {
    		log.info("ProfileImageUpdate  user_avatar: "+e.getMessage());
    		e.getMessage();
    		 throw new CustomException("Failed to update empty file");
    	}
    }
    
    @GetMapping("/image-profile/{path}")
    @ResponseBody
    public ResponseEntity<?> getPhoto(@PathVariable("path") String path) {
        return userService.getPhoto(path);
    }
    
    
    @GetMapping("/vender_verify")
    public ResponseEntity<?> venderVerify() {
        return userService.venderVerify();
    }

    
    @GetMapping("/vender_register/{category_id}/{category_name}")
    public ResponseEntity<?> venderSubmitVerify(@PathVariable("category_id") String category_id,@PathVariable("category_name")String category_name) {
        return userService.venderRegister(category_id,category_name);
    }
    
    
    @GetMapping("/notifications")
    public ResponseEntity<?> notification() {
        return userService.notification();
    }

}
