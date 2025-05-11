package com.siemens.internship;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.scheduling.annotation.EnableAsync;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.mockito.junit.jupiter.MockitoExtension;

@EnableAsync
//@SpringBootTest
@ExtendWith(MockitoExtension.class)
class InternshipApplicationTests {
	@Mock
	private ItemRepository itemRepository;
	@InjectMocks
	private ItemService itemService;

	@Test
	void testProcessItemsAsyncSuccess() throws Exception {
		// Setup mock data for the test
		List<Long> itemIds = List.of(1L, 2L, 3L);

		Item item1 = new Item(1L, "Item 1", "Description 1", "NEW", "email1@example.com");
		Item item2 = new Item(2L, "Item 2", "Description 2", "NEW", "email2@example.com");
		Item item3 = new Item(3L, "Item 3", "Description 3", "NEW", "email3@example.com");

		// Mocking the repository calls
		when(itemRepository.findAllIds()).thenReturn(itemIds);
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
		when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
		when(itemRepository.findById(3L)).thenReturn(Optional.of(item3));
		when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Call the method to test
		CompletableFuture<List<Item>> futureResult = itemService.processItemsAsync();

		// Wait for completion (you can also use a timeout here)
		List<Item> processedItems = futureResult.get();

		// Validate the result
		assertNotNull(processedItems);
		assertEquals(3, processedItems.size());
		assertEquals("PROCESSED", processedItems.get(0).getStatus());
		assertEquals("PROCESSED", processedItems.get(1).getStatus());
		assertEquals("PROCESSED", processedItems.get(2).getStatus());

		// Verify that the repository methods were called
		verify(itemRepository, times(1)).findAllIds();
		verify(itemRepository, times(3)).findById(anyLong());
		verify(itemRepository, times(3)).save(any(Item.class));
	}

	@Test
	void contextLoads() throws ExecutionException, InterruptedException {
		List<Long> itemIds = List.of(1L, 2L, 3L);

		Item item1 = new Item(1L, "Item 1", "Description 1", "NEW", "email1@example.com");
		Item item2 = new Item(2L, "Item 2", "Description 2", "NEW", "email2@example.com");
		// Mocking repository methods
		when(itemRepository.findAllIds()).thenReturn(itemIds);
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
		when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
		when(itemRepository.findById(3L)).thenThrow(new RuntimeException("Database error"));

		// Call the method to test
		CompletableFuture<List<Item>> futureResult = itemService.processItemsAsync();

		// Wait for completion (you can also use a timeout here)
		List<Item> processedItems = futureResult.join();

		// Validate the result (expecting only the first two items to be processed)
		assertNotNull(processedItems);
		assertEquals(2, processedItems.size());
		assertEquals("PROCESSED", processedItems.get(0).getStatus());
		assertEquals("PROCESSED", processedItems.get(1).getStatus());

		// Verify the repository interactions
		verify(itemRepository, times(1)).findAllIds();
		verify(itemRepository, times(3)).findById(anyLong());
		verify(itemRepository, times(2)).save(any(Item.class));
	}

}
