package org.example.facialidentification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.facialidentification.model.FaceData;
import org.example.facialidentification.service.FacialIdentificationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for facial identification operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/faces")
@RequiredArgsConstructor
public class FacialIdentificationController {

    private final FacialIdentificationService facialIdentificationService;
    
    // In-memory storage for known faces (in a real app, this would be a database)
    private final Map<String, FaceData> knownFaces = new HashMap<>();
    
    /**
     * Detects faces in an uploaded image.
     *
     * @param image the image file to analyze
     * @return a list of detected faces with their coordinates
     */
    @PostMapping("/detect")
    public ResponseEntity<?> detectFaces(@RequestParam("image") MultipartFile image) {
        try {
            List<FaceData> faces = facialIdentificationService.detectFaces(image);
            return ResponseEntity.ok(faces);
        } catch (IOException e) {
            log.error("Error detecting faces", e);
            return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
        }
    }
    
    /**
     * Registers a face with a label for future identification.
     *
     * @param image the image file containing a face
     * @param label the label/name to associate with the face
     * @return the registered face data
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerFace(
            @RequestParam("image") MultipartFile image,
            @RequestParam("label") String label) {
        try {
            FaceData faceData = facialIdentificationService.extractFacialFeatures(image);
            if (faceData == null) {
                return ResponseEntity.badRequest().body("No face detected in the image");
            }
            
            faceData.setLabel(label);
            knownFaces.put(label, faceData);
            
            // Return face data without the features array (to reduce response size)
            FaceData response = FaceData.builder()
                .x(faceData.getX())
                .y(faceData.getY())
                .width(faceData.getWidth())
                .height(faceData.getHeight())
                .label(faceData.getLabel())
                .build();
                
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error registering face", e);
            return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
        }
    }
    
    /**
     * Identifies a face in an uploaded image against registered faces.
     *
     * @param image the image file containing a face to identify
     * @param threshold optional similarity threshold (default: 0.7)
     * @return the matching face data or null if no match
     */
    @PostMapping("/identify")
    public ResponseEntity<?> identifyFace(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "threshold", defaultValue = "0.7") double threshold) {
        try {
            FaceData faceToIdentify = facialIdentificationService.extractFacialFeatures(image);
            if (faceToIdentify == null) {
                return ResponseEntity.badRequest().body("No face detected in the image");
            }
            
            List<FaceData> knownFacesList = new ArrayList<>(knownFaces.values());
            FaceData match = facialIdentificationService.identifyFace(faceToIdentify, knownFacesList, threshold);
            
            if (match == null) {
                return ResponseEntity.ok(Map.of("identified", false, "message", "No match found"));
            }
            
            // Return match data without the features array
            Map<String, Object> response = Map.of(
                "identified", true,
                "label", match.getLabel(),
                "x", match.getX(),
                "y", match.getY(),
                "width", match.getWidth(),
                "height", match.getHeight()
            );
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error identifying face", e);
            return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
        }
    }
    
    /**
     * Compares two faces and returns a similarity score.
     *
     * @param image1 the first image file containing a face
     * @param image2 the second image file containing a face
     * @return the similarity score between the two faces
     */
    @PostMapping("/compare")
    public ResponseEntity<?> compareFaces(
            @RequestParam("image1") MultipartFile image1,
            @RequestParam("image2") MultipartFile image2) {
        try {
            FaceData face1 = facialIdentificationService.extractFacialFeatures(image1);
            FaceData face2 = facialIdentificationService.extractFacialFeatures(image2);
            
            if (face1 == null || face2 == null) {
                return ResponseEntity.badRequest().body("Face not detected in one or both images");
            }
            
            double similarity = facialIdentificationService.compareFaces(face1, face2);
            
            return ResponseEntity.ok(Map.of("similarity", similarity));
        } catch (IOException e) {
            log.error("Error comparing faces", e);
            return ResponseEntity.badRequest().body("Error processing images: " + e.getMessage());
        }
    }
    
    /**
     * Lists all registered face labels.
     *
     * @return a list of registered face labels
     */
    @GetMapping("/known")
    public ResponseEntity<List<String>> getKnownFaces() {
        return ResponseEntity.ok(new ArrayList<>(knownFaces.keySet()));
    }
}