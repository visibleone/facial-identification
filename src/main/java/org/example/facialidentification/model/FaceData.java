package org.example.facialidentification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class to store face data including the face rectangle coordinates and features.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceData {
    // Rectangle coordinates
    private int x;
    private int y;
    private int width;
    private int height;
    
    // Face features (encoding) for recognition
    private float[] features;
    
    // Optional name/label for the face
    private String label;
}