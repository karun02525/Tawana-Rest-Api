package com.tawana.services.category;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.tawana.exception.CustomException;
import com.tawana.models.Category;
import com.tawana.models.common.ResponseArrayModel;
import com.tawana.models.common.ResponseModel;
import com.tawana.models.common.ResponseObjectModel;
import com.tawana.utils.StorageProperties;
import com.tawana.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private final Path categoryPath = StorageProperties.getInstance().getCategoryPath();
    private Query query;

    @Override
    public ResponseEntity<?> categorySave(String category_name,int category_postion, MultipartFile avatar_small) {

        String avatarSmallest = "category_" + UUID.randomUUID() + ".png";
      
        try {
            try (InputStream inputStream = avatar_small.getInputStream()) {
                Files.copy(inputStream, this.categoryPath.resolve(avatarSmallest), StandardCopyOption.REPLACE_EXISTING);
            }
          
            try {
                Category cate = new Category();
                cate.setCategory_id(RandomStringUtils.randomNumeric(5));
                cate.setCategory_name(category_name);
                cate.setCategory_postion(category_postion);
                cate.setCategory_avatar(avatarSmallest);
                mongoTemplate.save(cate);
                return new ResponseEntity<>(new ResponseObjectModel(true, "Category created successfully", cate), HttpStatus.CREATED);
            } catch (Exception e) {
                throw new CustomException("Failed to save empty file");
            }
        } catch (IOException e) {
            throw new CustomException("Failed to store empty file");
        }
    }

    /* Find all category*/
    @Override
    public ResponseEntity<?> findAllCategory() {
        List<Category> cat = mongoTemplate.findAll(Category.class);
        if (!cat.isEmpty()) {
            return new ResponseEntity<>(new ResponseArrayModel(true, "list all", cat), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ResponseArrayModel(false, "no data available"), HttpStatus.NOT_FOUND);
    }


    /*Get Photo by path*/
    @Override
    public ResponseEntity<?> getPhoto(String path) {
        Path imaPath = Paths.get(categoryPath + "\\" + path);
        if(Files.exists(imaPath)){
            return Utils.getImageLoad(imaPath);
        }else {
            return new ResponseEntity<>(new ResponseModel(false, "image path not exists"), HttpStatus.NOT_FOUND);
        }
    }

    /*Find Category by category id*/
    @Override
    public ResponseEntity<?> findByCategory(String category_id) {
        query = new Query();
        query.addCriteria(Criteria.where("_id").is(category_id));
        Category cat = mongoTemplate.findOne(query, Category.class);
        if (cat == null)
            return new ResponseEntity<>(new ResponseArrayModel(false, "no data found"), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(new ResponseObjectModel(true, "show one category", cat), HttpStatus.OK);
    }

    /*Remove Image by avatar key*/

    @Override
    public ResponseEntity<?> removeCategoryAvatar(String avatar_key) {
        Path imaPath = Paths.get(categoryPath + "\\" + avatar_key);
        query = new Query();
        Update update=null;
        if (avatar_key.contains("category_")) {
            query.addCriteria(Criteria.where("category_avatar").is(avatar_key));
            update = new Update().set("category_avatar", "");
        } 

        if (FileSystemUtils.deleteRecursively(imaPath.toFile())) {
            try {
                mongoTemplate.upsert(query, update, Category.class);
                return new ResponseEntity<>(new ResponseModel(true, "category avatar has been deleted"), HttpStatus.OK);
            } catch (Exception e) {
                throw new CustomException("Failed to not update");
            }
        } else {
            return new ResponseEntity<>(new ResponseModel(true, "this avatar already has been deleted."), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> updateCategory(String category_id, String category_name,int category_postion,MultipartFile category_avatar) {
        query = new Query();
        Query query1 = new Query();
        query1.addCriteria(Criteria.where("_id").is(category_id));
        Category model=mongoTemplate.findOne(query1,Category.class);

        Path imaPath = Paths.get(categoryPath + "\\" + model.getCategory_avatar());
        FileSystemUtils.deleteRecursively(imaPath.toFile());

  

        String avatarSmallest = "category_" + UUID.randomUUID() + ".png";

        try {
            try (InputStream inputStream = category_avatar.getInputStream()) {
                Files.copy(inputStream, this.categoryPath.resolve(avatarSmallest), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                query.addCriteria(Criteria.where("_id").is(category_id));
                Update update = new Update().set("category_name", category_name).set("category_avatar", avatarSmallest);
                mongoTemplate.upsert(query, update, Category.class);
                Map<String, String> map = new HashMap<>();
                map.put("category_id", category_id);
                map.put("category_name", category_name);
                map.put("category_avatar", avatarSmallest);
                return new ResponseEntity<>(new ResponseObjectModel(true, "Category has been updated successfully", map), HttpStatus.OK);
            } catch (Exception e) {
                throw new CustomException("Failed to save empty file");
            }
        } catch (IOException e) {
            throw new CustomException("Failed to store empty file");
        }
    }
}
