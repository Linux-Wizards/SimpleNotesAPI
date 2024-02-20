package com.linuxwizards.simplenotesapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

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
}
