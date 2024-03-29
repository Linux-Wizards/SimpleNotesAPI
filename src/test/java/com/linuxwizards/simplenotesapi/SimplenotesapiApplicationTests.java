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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimplenotesapiApplicationTests {
	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void shouldReturnANoteWhenDataIsSaved() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/notes/99", String.class);

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
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/notes/1000", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}

	@Test
	@DirtiesContext
	void shouldCreateANewNote() {
		Note newNote = new Note(null, "This is created note", "This is created content", null);
		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.postForEntity("/notes", newNote, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewNote = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity(locationOfNewNote, String.class);
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

		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/notes", String.class);
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
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/notes?page=0&size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);
	}

	@Test
	void shouldReturnASortedPageOfNotes() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/notes?page=0&size=1&sort=id,desc", String.class);
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

		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/notes", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(3);

		JSONArray titles = documentContext.read("$..title");
		assertThat(titles).containsExactly("Another title", "Second title", "This is a title");

		JSONArray contents = documentContext.read("$..content");
		assertThat(contents).containsExactly("Another note", "Second note", "This is a note");
	}

	@Test
	void shouldNotReturnANoteWhenUsingBadCredentials() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("BAD_USER", "BAD-PASSWORD")
				.getForEntity("/notes/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		 response = restTemplate
				.withBasicAuth("BAD-USER", "abc123")
				.getForEntity("/notes/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		response = restTemplate
				.withBasicAuth("sarah1", "BAD-PASSWORD")
				.getForEntity("/notes/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldRejectUsersWhoAreNotNotepadUsers() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("hank-cant-note", "qrs456")
				.getForEntity("/notes/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldNotAllowAccessToNotesTheyDoNotOwn() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/notes/102", String.class); // kumar2's note
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldUpdateAnExistingNote() {
		Note noteUpdate = new Note(null, "Updated title", "Updated content", null);
		HttpEntity<Note> request = new HttpEntity<>(noteUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/notes/99", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/notes/99", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		String title = documentContext.read("$.title");
		String content = documentContext.read("$.content");

		assertThat(id).isEqualTo(99);
		assertThat(title).isEqualTo("Updated title");
		assertThat(content).isEqualTo("Updated content");
	}

	@Test
	void shouldNotUpdateANoteThatDoesNotExist() {
		Note unknownNote = new Note(null, "Random title", "Random content", null);
		HttpEntity<Note> request = new HttpEntity<>(unknownNote);

		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/notes/99999", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotUpdateANoteThatIsOwnedBySomeoneElse() {
		Note kumarsNote = new Note(null, "New title", "New content", null);
		HttpEntity<Note> request = new HttpEntity<>(kumarsNote);

		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/notes/102", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldDeleteAnExistingNote() {
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/notes/99", HttpMethod.DELETE, null, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/notes/99", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotDeleteANoteThatDoesNotExist() {
		ResponseEntity<Void> deleteResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/notes/99999", HttpMethod.DELETE, null, Void.class);
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotAllowDeletionOfNotesTheyDoNotOwn() {
		ResponseEntity<Void> deleteResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/notes/102", HttpMethod.DELETE, null, Void.class);
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("kumar2", "xyz789")
				.getForEntity("/notes/102", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void loginEndpointShouldReturnOkForAuthenticatedUser() {
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/login", Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		response = restTemplate
				.withBasicAuth("kumar2", "xyz789")
				.getForEntity("/login", Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void loginEndpointShouldReturnUnauthorizedForBadCredentials() {
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "BAD-PASSWORD")
				.getForEntity("/login", Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		response = restTemplate
				.withBasicAuth("BAD-USER", "xyz789")
				.getForEntity("/login", Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		response = restTemplate
				.withBasicAuth("BAD-USER", "BAD-PASSWORD")
				.getForEntity("/login", Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
}
