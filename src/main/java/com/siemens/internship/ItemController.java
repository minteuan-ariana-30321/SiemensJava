package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        //return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
        return ResponseEntity.ok(itemService.findAll());
    }

    @PostMapping
    //public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
    public ResponseEntity<?> createItem(@Valid @RequestBody Item item, BindingResult result) {
            if (result.hasErrors()) {
            //return new ResponseEntity<>(null, HttpStatus.CREATED);
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
               // .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                //.orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id,@Valid @RequestBody Item item, BindingResult result) {
       // Optional<Item> existingItem = itemService.findById(id);
        /*if (existingItem.isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }*/
        if(result.hasErrors()){
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        return itemService.findById(id)
                .map(existing->{
                    item.setId(id);
                    return new ResponseEntity<>(itemService.save(item),HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {

        if(itemService.findById(id).isPresent()){
            itemService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/process")
    public CompletableFuture<ResponseEntity<List<Item>>> processItems() {
        return itemService.processItemsAsync()
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex->{
                    //handles exception and returns an appropriate response
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(List.of());
                });
    }
}
