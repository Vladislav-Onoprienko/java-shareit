package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class ShareItTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethodTest() {
        assertDoesNotThrow(() -> {
            String originalProperty = System.getProperty("spring.main.web-application-type");

            try {
                System.setProperty("spring.main.web-application-type", "none");
                ShareItApp.main(new String[]{});
            } finally {
                if (originalProperty != null) {
                    System.setProperty("spring.main.web-application-type", originalProperty);
                } else {
                    System.clearProperty("spring.main.web-application-type");
                }
            }
        });
    }
}
