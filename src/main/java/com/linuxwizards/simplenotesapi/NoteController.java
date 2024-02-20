package com.linuxwizards.simplenotesapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.net.URI;

@RestController
@RequestMapping("/notes")
class NoteController {
    private final NoteRepository noteRepository;

    private NoteController(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }


    @GetMapping("/{requestedId}")
    private ResponseEntity<Note> findById(@PathVariable Long requestedId) {
        Optional<Note> noteOptional = noteRepository.findById(requestedId);

        if (noteOptional.isPresent()) {
            return ResponseEntity.ok(noteOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    private ResponseEntity<Void> createNote(@RequestBody Note newNoteRequest, UriComponentsBuilder ucb) {
        Note savedNote = noteRepository.save(newNoteRequest);
        URI locationOfNewNote = ucb
                .path("notes/{id}")
                .buildAndExpand(savedNote.id())
                .toUri();

        return ResponseEntity.created(locationOfNewNote).build();
    }
}
