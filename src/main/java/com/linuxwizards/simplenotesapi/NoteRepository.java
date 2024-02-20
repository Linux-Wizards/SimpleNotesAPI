package com.linuxwizards.simplenotesapi;

import org.springframework.data.repository.CrudRepository;

interface NoteRepository extends CrudRepository<Note, Long> {
}
