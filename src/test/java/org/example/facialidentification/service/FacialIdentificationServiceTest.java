package org.example.facialidentification.service;

import org.example.facialidentification.model.FaceData;
import org.example.facialidentification.service.impl.FacialIdentificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the FacialIdentificationService implementation.
 * Note: These tests are simplified and may not run without actual image files.
 * In a real application, you would use test resources or mock the service.
 */
public class FacialIdentificationServiceTest {

    private FacialIdentificationService facialIdentificationService;

    @BeforeEach
    public void setUp() throws IOException {
        facialIdentificationService = new FacialIdentificationServiceImpl();
        ((FacialIdentificationServiceImpl) facialIdentificationService).init();
    }

    /**
     * Test for comparing two identical face feature vectors.
     * This test doesn't require actual images.
     */
    @Test
    public void testCompareFacesIdentical() {
        // Create two identical face data objects
        float[] features = new float[100 * 100];
        for (int i = 0; i < features.length; i++) {
            features[i] = (float) Math.random() * 255;
        }
        
        FaceData face1 = FaceData.builder()
                .x(10)
                .y(10)
                .width(100)
                .height(100)
                .features(features.clone())
                .build();
        
        FaceData face2 = FaceData.builder()
                .x(20)
                .y(20)
                .width(100)
                .height(100)
                .features(features.clone())
                .build();
        
        // Compare the faces
        double similarity = facialIdentificationService.compareFaces(face1, face2);
        
        // They should be identical (similarity = 1.0)
        assertEquals(1.0, similarity, 0.0001);
    }
    
    /**
     * Test for comparing two different face feature vectors.
     * This test doesn't require actual images.
     */
    @Test
    public void testCompareFacesDifferent() {
        // Create two different face data objects
        float[] features1 = new float[100 * 100];
        float[] features2 = new float[100 * 100];
        
        for (int i = 0; i < features1.length; i++) {
            features1[i] = (float) Math.random() * 255;
            features2[i] = (float) Math.random() * 255;
        }
        
        FaceData face1 = FaceData.builder()
                .x(10)
                .y(10)
                .width(100)
                .height(100)
                .features(features1)
                .build();
        
        FaceData face2 = FaceData.builder()
                .x(20)
                .y(20)
                .width(100)
                .height(100)
                .features(features2)
                .build();
        
        // Compare the faces
        double similarity = facialIdentificationService.compareFaces(face1, face2);
        
        // They should be different (similarity < 1.0)
        assertTrue(similarity < 1.0);
    }
    
    /**
     * Test for identifying a face among known faces.
     * This test doesn't require actual images.
     */
    @Test
    public void testIdentifyFace() {
        // Create a face to identify
        float[] features = new float[100 * 100];
        for (int i = 0; i < features.length; i++) {
            features[i] = (float) Math.random() * 255;
        }
        
        FaceData faceToIdentify = FaceData.builder()
                .x(10)
                .y(10)
                .width(100)
                .height(100)
                .features(features.clone())
                .build();
        
        // Create a list of known faces
        FaceData knownFace1 = FaceData.builder()
                .x(20)
                .y(20)
                .width(100)
                .height(100)
                .features(new float[100 * 100]) // Different features
                .label("Person 1")
                .build();
        
        FaceData knownFace2 = FaceData.builder()
                .x(30)
                .y(30)
                .width(100)
                .height(100)
                .features(features.clone()) // Same features as faceToIdentify
                .label("Person 2")
                .build();
        
        List<FaceData> knownFaces = List.of(knownFace1, knownFace2);
        
        // Identify the face
        FaceData match = facialIdentificationService.identifyFace(faceToIdentify, knownFaces, 0.9);
        
        // It should match knownFace2
        assertNotNull(match);
        assertEquals("Person 2", match.getLabel());
    }
}