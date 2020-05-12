package com.tawana.services;


import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.tawana.models.authentication.AuthRequest;
import com.tawana.models.authentication.ChangePassword;
import com.tawana.models.authentication.ForgotPassword;
import com.tawana.models.authentication.ProfileUpdate;
import com.tawana.models.authentication.RegisterDevice;
import com.tawana.models.authentication.User;


public interface UserService {
	
    ResponseEntity<?> createUser(User user);
    
    ResponseEntity<?> registerDevice(RegisterDevice reg_device);
   
    ResponseEntity<?> login(AuthRequest user);

    ResponseEntity<?> forgotPassword(ForgotPassword forgotPassword);

    ResponseEntity<?> changePassword(ChangePassword changePassword);
    
    ResponseEntity<?> profileImageUpdate(MultipartFile user_avatar);

    ResponseEntity<?> profileUpdate(ProfileUpdate profile_update);

    ResponseEntity<?> getPhoto(String path);
    
    ResponseEntity<?> venderVerify();
    
    ResponseEntity<?> venderRegister(String category_id,String category_name);
    
    ResponseEntity<?> notification();
    
    
    
    
    
    
    
   
    
}