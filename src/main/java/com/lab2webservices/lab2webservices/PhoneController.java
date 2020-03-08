package com.lab2webservices.lab2webservices;



import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;



@RequestMapping("/api/v1/phones")
@RestController
public class PhoneController {

    private final PhoneDataModelAssembler phoneDataModelAssembler;
    private PhoneRepository repository;

    PhoneController(PhoneRepository repository, PhoneDataModelAssembler phoneDataModelAssembler) {
        this.repository = repository;
        this.phoneDataModelAssembler = phoneDataModelAssembler;
    }


    @GetMapping
    public CollectionModel<EntityModel<Phone>> all() {
        return phoneDataModelAssembler.toCollectionModel(repository.findAll());
    }

    @GetMapping(value = "/{id:[\\d]+}")
    public ResponseEntity<EntityModel<Phone>> one(@PathVariable Long id) {
        return repository.findById(id)
                .map(phoneDataModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/{phoneName:[\\D]+[\\d]*}")
    public ResponseEntity<EntityModel<Phone>> oneOrMany(@PathVariable String phoneName) {
        return repository.findByPhoneName(phoneName)
                .map(phoneDataModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    @GetMapping(value = "/brand/{brandId}")
    public Optional<Phone> one(@PathVariable int brandId) {
        return repository.findById((long) brandId);
    }

    @PostMapping
    ResponseEntity<Phone> newPhone(@RequestBody Phone phone) {
        if (repository.existsPhoneByPhoneName(phone.getPhoneName()))
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        repository.save(phone);
        var entityModelResponseEntity = repository.findById(phone.getId())
                .map(phoneDataModelAssembler::toModel);
        return new ResponseEntity(entityModelResponseEntity, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> deletePhone(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{id}")
    ResponseEntity<EntityModel<Phone>> replacePhone(@RequestBody Phone phoneIn, @PathVariable Long id) {

        if(repository.findById(id).isPresent()){
            var p = repository.findById(id)
                    .map(existingPhone -> {
                        existingPhone.setPhoneName(phoneIn.getPhoneName());
                        existingPhone.setBrandId(phoneIn.getBrandId());
                        repository.save(existingPhone);
                        return existingPhone;})
                    .get();
            var entityModel = phoneDataModelAssembler.toModel(p);
            return new ResponseEntity<>(entityModel, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}")
    ResponseEntity<EntityModel<Phone>> modifyPhone(@RequestBody Phone updatedPhone, @PathVariable Long id){
        if(repository.findById(id).isPresent()){
            var p = repository.findById(id)
                    .map(newPhone -> {
                        if(updatedPhone.getPhoneName() != null)
                            newPhone.setPhoneName(updatedPhone.getPhoneName());
                        if(updatedPhone.getBrandId() != 0)
                            newPhone.setBrandId(updatedPhone.getBrandId());
                        repository.save(newPhone);
                        return newPhone;}).get();
            var entityModel = phoneDataModelAssembler.toModel(p);
            return new ResponseEntity<>(entityModel, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
