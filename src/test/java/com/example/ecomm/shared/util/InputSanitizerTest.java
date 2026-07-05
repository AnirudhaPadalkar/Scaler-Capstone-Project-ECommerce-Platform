package com.example.ecomm.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InputSanitizerTest {

    private final InputSanitizer sanitizer = new InputSanitizer();

    @Test
    void sanitize_removesHtmlTags() {
        assertThat(sanitizer.sanitize("<b>hello</b>")).isEqualTo("hello");
    }

    @Test
    void sanitize_removesScriptTags() {
        assertThat(sanitizer.sanitize("<script>alert('xss')</script>clean")).isEqualTo("clean");
    }

    @Test
    void sanitize_removesJavascriptProtocol() {
        assertThat(sanitizer.sanitize("javascript:alert(1)")).doesNotContain("javascript:");
    }

    @Test
    void sanitize_plainTextPassesThrough() {
        assertThat(sanitizer.sanitize("John Doe")).isEqualTo("John Doe");
    }

    @Test
    void sanitize_nullReturnsNull() {
        assertThat(sanitizer.sanitize(null)).isNull();
    }
}
