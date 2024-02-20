package com.linuxwizards.simplenotesapi;

import org.springframework.data.annotation.Id;

record Note(@Id Long id, String title, String content) {
}
