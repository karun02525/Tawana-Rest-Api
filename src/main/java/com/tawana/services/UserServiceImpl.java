package com.tawana.services;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import com.tawana.exception.CustomException;
import com.tawana.models.NotificationModel;
import com.tawana.models.authentication.AuthRequest;
import com.tawana.models.authentication.ChangePassword;
import com.tawana.models.authentication.ForgotPassword;
import com.tawana.models.authentication.ProfileUpdate;
import com.tawana.models.authentication.User;
import com.tawana.models.authentication.VenderVerifyModel;
import com.tawana.models.common.ResponseArrayModel;
import com.tawana.models.common.ResponseModel;
import com.tawana.models.common.ResponseObjectModel;
import com.tawana.security.JwtTokenProvider;
import com.tawana.utils.StorageProperties;
import com.tawana.utils.Utils;

@Service
public class UserServiceImpl implements UserService {

	private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private AuthenticationManager authenticationManager;
	
	
	private final Path profilePath = StorageProperties.getInstance().getProfilePath();

	public ResponseEntity<?> login(AuthRequest authRequest) {
		final User userPayload = mongoTemplate
				.findOne(new Query(Criteria.where("mobile").is(authRequest.getMobile().trim())), User.class);

		if (userPayload == null) {
			return new ResponseEntity<>(
					new ResponseModel(false, "Mobile number or password invalid ! please try again"),
					HttpStatus.BAD_REQUEST);
		} else {
			try {
				authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userPayload.getUid(),
						authRequest.getPassword().trim()));
				userPayload.setToken(jwtTokenProvider.createToken(userPayload.getUid()));
				userPayload.setPassword(null);
				return new ResponseEntity<>(new ResponseObjectModel(true, "Your login successfully", userPayload),
						HttpStatus.OK);

			} catch (AuthenticationException e) {
				return new ResponseEntity<>(
						new ResponseModel(false, "Mobile number or password invalid ! please try again"),
						HttpStatus.BAD_REQUEST);
			}
		}
	}

	@Override
	public ResponseEntity<?> createUser(User user) {
		User checkUserMobile;
		checkUserMobile = mongoTemplate.findOne(new Query(Criteria.where("mobile").is(user.getMobile().trim())),
				User.class);
		if (checkUserMobile == null) {
			String uid = Utils.timeStamp();
			user.setUid(uid);
			user.setPassword(passwordEncoder.encode(user.getPassword().trim()));
			mongoTemplate.save(user);
			user.setPassword(null);
			user.setToken(jwtTokenProvider.createToken(uid));
			return new ResponseEntity<>(new ResponseObjectModel(true, "Your registration successfully", user),
					HttpStatus.CREATED);

		} else {
			return new ResponseEntity<>(new ResponseModel(false, "Mobile is already in use"),
					HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public ResponseEntity<?> changePassword(ChangePassword changePassword) {
		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		 User user;
	        user = mongoTemplate.findOne(new Query(Criteria.where("uid").is(uid.trim())), User.class);
	        if (user == null) {
	            return new ResponseEntity<>(new ResponseModel(false, "Your uid invalid ! please try again"), HttpStatus.NOT_FOUND);
	        } else {
	            if (!passwordEncoder.matches(changePassword.getCurrent_password(), user.getPassword())) {
	                return new ResponseEntity<>(new ResponseModel(false, "Your current password invalid ! please try again"), HttpStatus.NOT_FOUND);
	            } else {
	                Query query1 = new Query();
	                query1.addCriteria(Criteria.where("uid").is(uid.trim()));
	                Update update = new Update().set("password", passwordEncoder.encode(changePassword.getNew_password().trim()));
	                mongoTemplate.upsert(query1, update, User.class);
	                return new ResponseEntity<>(new ResponseModel(true, "Your password has been changed successfully"), HttpStatus.OK);
	            }
	        }
	}
	
	@Override
	public ResponseEntity<?> forgotPassword(ForgotPassword forgotPassword) {
		    Criteria criteria = new Criteria();
	        Query query = new Query(criteria);
	        User user;
	        criteria.andOperator(Criteria.where("mobile").is(forgotPassword.getMobile().trim()),Criteria.where("email").is(forgotPassword.getEmail().trim()));
	        user = mongoTemplate.findOne(query, User.class);
	        if (user == null) {
	            return new ResponseEntity<>(new ResponseModel(false, "Your mobile number or email address invalid ! please try again"), HttpStatus.BAD_REQUEST);
	        } else {
	            query.addCriteria(Criteria.where("mobile").is(forgotPassword.getMobile().trim()));
	            final String rendomPassGenerate =""+new Random().nextInt(931403200);
	            Update update = new Update().set("password", passwordEncoder.encode(rendomPassGenerate));
	            mongoTemplate.upsert(query, update, User.class);
	            return new ResponseEntity<>(new ResponseModel(true, "Your password reset successfully and your temporary password "+rendomPassGenerate), HttpStatus.OK);
	        }
	}
	
	
	@Override
	public ResponseEntity<?> profileUpdate(ProfileUpdate profile_update) {
		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		User model = mongoTemplate.findOne(new Query(Criteria.where("uid").is(uid.trim())), User.class);
	        if (model == null) {
	            return new ResponseEntity<>(new ResponseModel(false, "User id invalid! please try again"), HttpStatus.BAD_REQUEST);
	        } else {
	                Query query = new Query();
	                query.addCriteria(Criteria.where("uid").is(uid.trim()));
	                Update update = new Update().set("email", profile_update.getEmail())
	                		                   .set("gender", profile_update.getGender())
	                		                   .set("address", profile_update.getAddress())
	                		                   .set("pincode", profile_update.getPincode());
	                mongoTemplate.upsert(query, update, User.class);
	                
	               return new ResponseEntity<>(new ResponseObjectModel(true, "profile has been updated successfully", profile_update), HttpStatus.OK);
	           
	        }
	    }

	
	@Override
	public ResponseEntity<?> profileImageUpdate(MultipartFile user_avatar) {
	   	String uid = SecurityContextHolder.getContext().getAuthentication().getName();
	   	log.info("uid"+uid);
		 User model = mongoTemplate.findOne(new Query(Criteria.where("uid").is(uid.trim())), User.class);
	        if (model == null) {
	            return new ResponseEntity<>(new ResponseModel(false, "User id invalid! please try again"), HttpStatus.BAD_REQUEST);
	        } else {
	            if (model.getUser_avatar() != null && !model.getUser_avatar().equals("")) {
	                Path imaPath = Paths.get(profilePath + "\\" + model.getUser_avatar());
	                FileSystemUtils.deleteRecursively(imaPath.toFile());
	            }
	            String avatarUser = "profile_" + UUID.randomUUID() + ".png";
	            try {
	                try (InputStream inputStream = user_avatar.getInputStream()) {
	                    Files.copy(inputStream, this.profilePath.resolve(avatarUser), StandardCopyOption.REPLACE_EXISTING);
	                }
	                Query query = new Query();
	                query.addCriteria(Criteria.where("uid").is(uid.trim()));
	                Update update = new Update().set("user_avatar", avatarUser);
	                mongoTemplate.upsert(query, update, User.class);
	                Map<String, String> map = new HashMap<>();
	                map.put("user_avatar", avatarUser);
	                return new ResponseEntity<>(new ResponseObjectModel(true, "Your profile image has been updated successfully", map), HttpStatus.OK);
	            } catch (Exception e) {
	                throw new CustomException("Failed to update empty file");
	            }
	        }
	}
	
	 @Override
	    public ResponseEntity<?> getPhoto(String path) {
	        Path imaPath = Paths.get(profilePath + "\\" + path);
	        if (Files.exists(imaPath)) {
	            return Utils.getImageLoad(imaPath);
	        } else {
	            return new ResponseEntity<>(new ResponseModel(false, "image path not exists"), HttpStatus.BAD_REQUEST);
	        }
	    }

	 
	 @Override
		public ResponseEntity<?> venderVerify() {
			String uid = SecurityContextHolder.getContext().getAuthentication().getName();
			User model = mongoTemplate.findOne(new Query(Criteria.where("uid").is(uid.trim())), User.class);
		        if (model == null) {
		            return new ResponseEntity<>(new ResponseModel(false, "User id invalid! please try again"), HttpStatus.BAD_REQUEST);
		        } else {
		            	
	        	VenderVerifyModel result = mongoTemplate.findOne(new Query(Criteria.where("uid").is(uid.trim())), VenderVerifyModel.class);
		             if (result == null) {
		            	 Map<String,Integer> map =new HashMap<>();
		            	 map.put("is_verify", 0);
		                 return new ResponseEntity<>(new ResponseObjectModel(true, "First time create",map), HttpStatus.OK);
		             } else {
		            	 String message="";
	                    	 if(result.getIs_verify()==1) {
		            		     message="Your Verification Pending";
	            	        }
			                 if(result.getIs_verify()==3) {
		                	      message="Your Shop has been rejected";
			            	 }
		                     return new ResponseEntity<>(new ResponseObjectModel(true,message,result), HttpStatus.OK);                 
		           }
		        }
	       
		} 
	 
	 
	@Transactional 
	@Override
	public ResponseEntity<?> venderRegister(String category_id,String category_name) {
		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		User model = mongoTemplate.findOne(new Query(Criteria.where("uid").is(uid.trim())), User.class);
	        if (model == null) {
	            return new ResponseEntity<>(new ResponseModel(false, "User id invalid! please try again"), HttpStatus.BAD_REQUEST);
	        } else {
	        	VenderVerifyModel verify=new VenderVerifyModel();
	        	verify.setUid(uid);
	        	verify.setName(model.getName());
	        	verify.setMobile(model.getMobile());
	        	verify.setCategory_id(category_id.trim());
	        	verify.setCategory_name(category_name);
	        	verify.setIs_verify(1);//Pending verification (1) means
	        	mongoTemplate.save(verify);  
	        		
	        	NotificationModel noti=new NotificationModel();
	        	noti.setUid(uid);
	        	noti.setVender_id(verify.getVender_id());
	        	noti.setCategory(category_name);
	        	noti.setTitle("Pending");
	        	noti.setType("Vender Register for Shop");
	        	noti.setMessage("Your verification pending.");
	        	mongoTemplate.save(noti);
                return new ResponseEntity<>(new ResponseObjectModel(true, "Your verification pending", verify), HttpStatus.OK);
	        }
	}

	@Override
	public ResponseEntity<?> notification() {
		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		List<NotificationModel> mess = mongoTemplate.find(new Query(Criteria.where("uid").is(uid.trim())), NotificationModel.class);
	        if (mess.isEmpty()) {
	            return new ResponseEntity<>(new ResponseModel(false, "No data available"), HttpStatus.BAD_REQUEST);
	        } else {
                return new ResponseEntity<>(new ResponseArrayModel(true, "All Notifications", mess), HttpStatus.OK);
	        }
	}
}

