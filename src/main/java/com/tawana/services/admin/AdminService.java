package com.tawana.services.admin;

import org.springframework.http.ResponseEntity;

import com.tawana.models.authentication.VerifyModel;

public interface AdminService {
	
    ResponseEntity<?> isVerify(VerifyModel venderModel);

    ResponseEntity<?> adminService();
}