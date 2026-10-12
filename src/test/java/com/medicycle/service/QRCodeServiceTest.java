package com.medicycle.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeServiceTest {
    @Test
    void createsPngDataUri() {
        String qr = new QRCodeService().generateBase64QR("MC-TEST-1234");
        assertNotNull(qr);
        assertTrue(qr.startsWith("data:image/png;base64,"));
        assertTrue(qr.length() > 100);
    }
}
