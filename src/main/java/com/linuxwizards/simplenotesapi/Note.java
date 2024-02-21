package com.linuxwizards.simplenotesapi;

import org.springframework.data.annotation.Id;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record Note(@Id Long id, @NotNull @Size(min=1, max=50) String title, @NotNull @Size(min=1, max=20000) String content, @NotNull @Size(min=1, max=255) String owner) {
}
