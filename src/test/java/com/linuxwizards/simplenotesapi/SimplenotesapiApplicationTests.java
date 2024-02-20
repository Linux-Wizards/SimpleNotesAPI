package com.linuxwizards.simplenotesapi;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimplenotesapiApplicationTests {
	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void shouldReturnANoteWhenDataIsSaved() {
		ResponseEntity<String> response = restTemplate.getForEntity("/notes/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());

		Number id = documentContext.read("$.id");
		assertThat(id).isEqualTo(99);

		String title = documentContext.read("$.title");
		assertThat(title).isEqualTo("This is a title");

		String content = documentContext.read("$.content");
		assertThat(content).isEqualTo("This is a note");
	}

	@Test
	void shouldNotReturnANoteWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate.getForEntity("/notes/1000", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}

	@Test
	@DirtiesContext
	void shouldCreateANewNote() {
		Note newNote = new Note(null, "This is created note", "This is created content", "sarah1");
		ResponseEntity<Void> createResponse = restTemplate.postForEntity("/notes", newNote, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewNote = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewNote, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		String title = documentContext.read("$.title");
		String content = documentContext.read("$.content");

		assertThat(id).isNotNull();
		assertThat(title).isEqualTo("This is created note");
		assertThat(content).isEqualTo("This is created content");
	}

	@Test
	void shouldReturnAllNotesWhenListIsRequested() {
		// Should return all notes even with paging - default page size is 20

		ResponseEntity<String> response = restTemplate.getForEntity("/notes", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int noteCount = documentContext.read("$.length()");
		assertThat(noteCount).isEqualTo(3);

		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

		JSONArray titles = documentContext.read("$..title");
		assertThat(titles).containsExactlyInAnyOrder("This is a title", "Second title", "Another title");

		JSONArray contents = documentContext.read("$..content");
		assertThat(contents).containsExactlyInAnyOrder("This is a note", "Second note", "Another note");
	}

	@Test
	void shouldReturnAPageOfNotes() {
		ResponseEntity<String> response = restTemplate.getForEntity("/notes?page=0&size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);
	}

	@Test
	void shouldReturnASortedPageOfNotes() {
		ResponseEntity<String> response = restTemplate.getForEntity("/notes?page=0&size=1&sort=id,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray read = documentContext.read("$[*]");
		assertThat(read.size()).isEqualTo(1);

		String title = documentContext.read("$[0].title");
		assertThat(title).isEqualTo("Another title");

		String content = documentContext.read("$[0].content");
		assertThat(content).isEqualTo("Another note");
	}

	@Test
	void shouldReturnASortedPageOfNotesWithNoParametersAndUseDefaultValues() {
		/*
		Defaults:
		Sorting order: id, descending
		Page size: 20 (Spring default)
		Page: 0 (Spring default)
		 */

		ResponseEntity<String> response = restTemplate.getForEntity("/notes", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(3);

		JSONArray titles = documentContext.read("$..title");
		assertThat(titles).containsExactly("Another title", "Second title", "This is a title");

		JSONArray contents = documentContext.read("$..content");
		assertThat(contents).containsExactly("Another note", "Second note", "This is a note");
	}
}
