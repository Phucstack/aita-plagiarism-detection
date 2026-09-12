package com.aita.plagiarism.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Quét Mã Nguồn Hàng Loạt (BatchScannerServlet)")
public class BatchScannerServletTest {

    private BatchScannerServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new BatchScannerServlet();
    }

    @Test
    @DisplayName("GET: Chuyển tiếp tới giao diện batch-scanner.jsp")
    void testDoGet() throws ServletException, IOException {
        when(request.getRequestDispatcher("/batch-scanner.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(response).setContentType("text/html;charset=UTF-8");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST: Kích hoạt quét hàng loạt và chuyển hướng về Dashboard với scanSuccess=true")
    void testDoPost() throws ServletException, IOException {
        when(request.getContextPath()).thenReturn("/aita");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/aita/dashboard?scanSuccess=true");
    }
}
