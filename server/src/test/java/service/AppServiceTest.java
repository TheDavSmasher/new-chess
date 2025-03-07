package service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class AppServiceTest {

    @Test
    void clearData() {
        Assertions.assertDoesNotThrow(AppService::clearData);
    }
}