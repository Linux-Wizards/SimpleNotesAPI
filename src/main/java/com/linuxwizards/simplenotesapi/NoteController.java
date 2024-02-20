package com.linuxwizards.simplenotesapi;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.security.Principal;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/notes")
class NoteController {
    private final NoteRepository noteRepository;

    private NoteController(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }


    @GetMapping("/{requestedId}")
    private ResponseEntity<Note> findById(@PathVariable Long requestedId, Principal principal) {
        Optional<Note> noteOptional = Optional.ofNullable(noteRepository.findByIdAndOwner(requestedId, principal.getName()));

        if (noteOptional.isPresent()) {
            return ResponseEntity.ok(noteOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    private ResponseEntity<List<Note>> findAll(Pageable pageable, Principal principal) {
        Page<Note> page = noteRepository.findByOwner(principal.getName(),
                PageRequest.of(
                  pageable.getPageNumber(),
                  pageable.getPageSize(),
                  pageable.getSortOr(Sort.by(Sort.Direction.DESC, "id"))
                ));

        return ResponseEntity.ok(page.getContent());
    }

    @PostMapping
    private ResponseEntity<Void> createNote(@RequestBody Note newNoteRequest, UriComponentsBuilder ucb, Principal principal) {
        Note noteWithOwner = new Note(null, newNoteRequest.title(), newNoteRequest.content(), principal.getName());
        Note savedNote = noteRepository.save(noteWithOwner);

        URI locationOfNewNote = ucb
                .path("notes/{id}")
                .buildAndExpand(savedNote.id())
                .toUri();

        return ResponseEntity.created(locationOfNewNote).build();
    }
}
