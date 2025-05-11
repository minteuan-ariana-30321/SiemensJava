package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;


@Service
public class ItemService {
    //autowired repo to interact with the database
    @Autowired
    private ItemRepository itemRepository;

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }
    /*
     Asynchronously processes all items, updates status to Processed
     Ensures thread safety proper completion, and accurate results.
     */

    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();
        //list to hold all completableFuture tasks
        List<CompletableFuture<Item>> futures = new ArrayList<>();

        for (Long id : itemIds) {

            CompletableFuture<Item> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(100);//simulate  delay
                    return itemRepository.findById(id)
                        .map(item -> {
                        item.setStatus("PROCESSED");
                        return itemRepository.save(item);
                    }).orElse(null);
                } catch (Exception e) {
                    throw new RuntimeException("error processing item ID: " + id, e);
                }
            });
            futures.add(future);
        }
        //once all tasks are completed, the results are gathered into a list
        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<Item> processedItems = new ArrayList<>();
                    for(CompletableFuture<Item> future : futures){
                        try{
                            Item item = future.join();
                            if(item != null){
                                processedItems.add(item);
                            }
                        }catch(Exception e){
                            //log any errors encountered during processing
                            System.err.println("processing failed: " + e.getMessage());
                        }
                    }
                    return processedItems;
                });

    }
}

