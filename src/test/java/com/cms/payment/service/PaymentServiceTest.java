package com.cms.payment.service;

import com.cms.payment.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.web.client.RestTemplate;

import static org.mockito.MockitoAnnotations.openMocks;

class PaymentServiceTest {

    private static final String STUDENT_BASE_URL = "http://localhost:8104/";
    private static final String LOCATION_BASE_URL = "http://localhost:8105/";
    private static final String GET_STUDENT_BY_ID_URL = "api/v1/student/##STUDENT-ID##";
    private static final String GET_ALL_LOCATION_URL = "api/v1/tuition/";
    private static final String GET_ALL_STUDENT_URL = "api/v1/student";

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RestTemplate restTemplate;
    private PaymentService paymentService;


    @BeforeEach
    void setUp() {
        openMocks(this);
        paymentService = new PaymentService(paymentRepository, restTemplate, STUDENT_BASE_URL, LOCATION_BASE_URL,
                GET_STUDENT_BY_ID_URL, GET_ALL_LOCATION_URL, GET_ALL_STUDENT_URL);
    }


    @AfterEach
    void tearDown() {
    }
}