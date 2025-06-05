package org.example.facialidentification.service;

import org.example.facialidentification.model.FaceData;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service interface for facial identification operations.
 */
public interface FacialIdentificationService {
    
    /**
     * Detects faces in the provided image.
     *
     * @param imageFile the image file to analyze
     * @return a list of detected faces with their coordinates
     * @throws IOException if there's an error processing the image
     */
    List<FaceData> detectFaces(MultipartFile imageFile) throws IOException;
    
    /**
     * Extracts facial features from a detected face for recognition.
     *
     * @param imageFile the image file containing a face
     * @return the face data with extracted features
     * @throws IOException if there's an error processing the image
     */
    FaceData extractFacialFeatures(MultipartFile imageFile) throws IOException;
    
    /**
     * Compares two faces and returns a similarity score.
     *
     * @param face1 the first face data
     * @param face2 the second face data
     * @return a similarity score between 0 and 1, where 1 means identical
     */
    double compareFaces(FaceData face1, FaceData face2);
    
    /**
     * Identifies a face against a collection of known faces.
     *
     * @param faceToIdentify the face to identify
     * @param knownFaces the collection of known faces
     * @param threshold the minimum similarity threshold (0-1)
     * @return the matching face data or null if no match above threshold
     */
    FaceData identifyFace(FaceData faceToIdentify, List<FaceData> knownFaces, double threshold);
}