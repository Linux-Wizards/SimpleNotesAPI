package com.linuxwizards.simplenotesapi;

import org.springframework.data.annotation.Id;
import jakarta.validation.constraints.NotNull;

record Note(
        @Id Long id,
        @NotNull String title,
        @NotNull String content,
        @NotNull String owner
) {}
