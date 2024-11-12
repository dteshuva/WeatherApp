package edu.uiuc.cs427app;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import android.content.Context;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "AndroidManifest.xml", sdk = 33)
public class LLMServiceTest {

    @Mock
    private Context mockContext;

    @Mock
    private GenerativeModel mockBaseModel;

    @Mock
    private GenerativeModelFutures mockModel;

    @Mock
    private GenerateContentResponse mockResponse;

    @Mock
    private ErrorService mockErrorService;

    private LLMService llmService;
    private static final String SAMPLE_WEATHER_DATA =
            "City: Chicago, Weather: cloudy, Temperature: 20.5°C, Humidity: 65%, Wind Speed: 5.2 m/s";
    private static final String SAMPLE_QUESTION = "Should I bring an umbrella today?";
    private static final String SAMPLE_RESPONSE = "Based on the cloudy weather, it would be wise to bring an umbrella.";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Mock successful response
        when(mockResponse.getText()).thenReturn(SAMPLE_RESPONSE);
        ListenableFuture<GenerateContentResponse> successFuture = Futures.immediateFuture(mockResponse);
        when(mockModel.generateContent(any(Content.class))).thenReturn(successFuture);

        // Initialize service with mocks
        llmService = LLMService.getInstance(mockContext);
        // Inject mocked dependencies using reflection
        injectMocks(llmService);
    }

    private void injectMocks(LLMService service) throws Exception {
        java.lang.reflect.Field modelField = LLMService.class.getDeclaredField("model");
        modelField.setAccessible(true);
        modelField.set(service, mockModel);

        java.lang.reflect.Field errorServiceField = LLMService.class.getDeclaredField("errorService");
        errorServiceField.setAccessible(true);
        errorServiceField.set(service, mockErrorService);
    }

    @Test
    public void testGenerateQuestions_Success() throws Exception {
        ListenableFuture<GenerateContentResponse> future = llmService.generateQuestions(SAMPLE_WEATHER_DATA);
        GenerateContentResponse response = future.get(5, TimeUnit.SECONDS);

        verify(mockModel).generateContent(any(Content.class));
        assertNotNull("Response should not be null", response);
        assertEquals("Response text should match", SAMPLE_RESPONSE, response.getText());
    }

    @Test
    public void testGenerateInsight_Success() throws Exception {
        ListenableFuture<GenerateContentResponse> future =
                llmService.generateInsight(SAMPLE_WEATHER_DATA, SAMPLE_QUESTION);
        GenerateContentResponse response = future.get(5, TimeUnit.SECONDS);

        verify(mockModel).generateContent(any(Content.class));
        assertNotNull("Response should not be null", response);
        assertEquals("Response text should match", SAMPLE_RESPONSE, response.getText());
    }

    @Test
    public void testGenerateQuestions_Error() throws Exception {
        // Mock error response
        RuntimeException mockException = new RuntimeException("API Error");
        ListenableFuture<GenerateContentResponse> errorFuture = Futures.immediateFailedFuture(mockException);
        when(mockModel.generateContent(any(Content.class))).thenReturn(errorFuture);

        ListenableFuture<GenerateContentResponse> future = llmService.generateQuestions(SAMPLE_WEATHER_DATA);

        try {
            future.get(5, TimeUnit.SECONDS);
            fail("Should have thrown an exception");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("API Error", e.getCause().getMessage());
        }
    }

    @Test
    public void testSingleton() {
        LLMService instance1 = LLMService.getInstance(mockContext);
        LLMService instance2 = LLMService.getInstance(mockContext);
        assertSame("Should return same instance", instance1, instance2);
    }

    @Test(expected = TimeoutException.class)
    public void testGenerateInsight_Timeout() throws Exception {
        // Mock a delayed response
        ListenableFuture<GenerateContentResponse> delayedFuture =
                Futures.submitAsync(() -> {
                    Thread.sleep(2000); // Delay for 2 seconds
                    return Futures.immediateFuture(mockResponse);
                }, Executors.newSingleThreadExecutor());

        when(mockModel.generateContent(any(Content.class))).thenReturn(delayedFuture);

        ListenableFuture<GenerateContentResponse> future =
                llmService.generateInsight(SAMPLE_WEATHER_DATA, SAMPLE_QUESTION);

        // Should timeout after 1 second
        future.get(1, TimeUnit.SECONDS);
    }

    @Test
    public void testShutdown() {
        llmService.shutdown();
        // Verify executor is shutdown - we can't directly verify this without additional mocking,
        // but we can ensure the method completes without exceptions
    }
}