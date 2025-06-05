package org.example.facialidentification.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.example.facialidentification.model.FaceData;
import org.example.facialidentification.service.FacialIdentificationService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Slf4j
@Service
public class FacialIdentificationServiceImpl implements FacialIdentificationService {

    private CascadeClassifier faceDetector;
    private FaceRecognizer faceRecognizer;
    
    private static final String HAAR_CASCADE_PATH = "haarcascade_frontalface_default.xml";
    private static final double FACE_SIMILARITY_THRESHOLD = 0.7;
    
    @PostConstruct
    public void init() throws IOException {
        // Initialize face detector
        Path tempDir = Files.createTempDirectory("opencv");
        Path cascadePath = tempDir.resolve(HAAR_CASCADE_PATH);
        
        // Extract the Haar cascade file from the classpath to a temp file
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(HAAR_CASCADE_PATH)) {
            if (inputStream == null) {
                throw new IOException("Could not find Haar cascade file: " + HAAR_CASCADE_PATH);
            }
            Files.copy(inputStream, cascadePath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        faceDetector = new CascadeClassifier(cascadePath.toString());
        if (faceDetector.empty()) {
            throw new IOException("Could not load Haar cascade classifier");
        }
        
        // Initialize face recognizer
        faceRecognizer = LBPHFaceRecognizer.create();
        
        log.info("Facial identification service initialized successfully");
    }
    
    @Override
    public List<FaceData> detectFaces(MultipartFile imageFile) throws IOException {
        // Convert MultipartFile to a temporary file
        Path tempFile = Files.createTempFile("upload", imageFile.getOriginalFilename());
        imageFile.transferTo(tempFile.toFile());
        
        // Load the image
        Mat image = imread(tempFile.toString());
        if (image.empty()) {
            throw new IOException("Could not read image file");
        }
        
        // Convert to grayscale for face detection
        Mat grayImage = new Mat();
        cvtColor(image, grayImage, COLOR_BGR2GRAY);
        
        // Detect faces
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(
            grayImage, 
            faces, 
            1.1, 
            3, 
            0, 
            new Size(30, 30), 
            new Size()
        );
        
        // Convert detected faces to FaceData objects
        List<FaceData> faceDataList = new ArrayList<>();
        for (long i = 0; i < faces.size(); i++) {
            Rect face = faces.get(i);
            FaceData faceData = FaceData.builder()
                .x(face.x())
                .y(face.y())
                .width(face.width())
                .height(face.height())
                .build();
            faceDataList.add(faceData);
        }
        
        // Clean up
        Files.delete(tempFile);
        
        return faceDataList;
    }
    
    @Override
    public FaceData extractFacialFeatures(MultipartFile imageFile) throws IOException {
        // First detect faces
        List<FaceData> faces = detectFaces(imageFile);
        if (faces.isEmpty()) {
            return null;
        }
        
        // Use the first detected face
        FaceData faceData = faces.get(0);
        
        // Convert MultipartFile to a temporary file
        Path tempFile = Files.createTempFile("upload", imageFile.getOriginalFilename());
        imageFile.transferTo(tempFile.toFile());
        
        // Load the image
        Mat image = imread(tempFile.toString());
        Mat grayImage = new Mat();
        cvtColor(image, grayImage, COLOR_BGR2GRAY);
        
        // Extract the face region
        Rect faceRect = new Rect(faceData.getX(), faceData.getY(), faceData.getWidth(), faceData.getHeight());
        Mat faceROI = new Mat(grayImage, faceRect);
        
        // Resize to a standard size for feature extraction
        Mat resizedFace = new Mat();
        resize(faceROI, resizedFace, new Size(100, 100));
        
        // Extract features (using pixel values as simple features)
        float[] features = new float[100 * 100];
        FloatIndexer indexer = resizedFace.createIndexer();
        for (int y = 0; y < resizedFace.rows(); y++) {
            for (int x = 0; x < resizedFace.cols(); x++) {
                features[y * resizedFace.cols() + x] = indexer.get(y, x);
            }
        }
        
        faceData.setFeatures(features);
        
        // Clean up
        Files.delete(tempFile);
        
        return faceData;
    }
    
    @Override
    public double compareFaces(FaceData face1, FaceData face2) {
        if (face1 == null || face2 == null || 
            face1.getFeatures() == null || face2.getFeatures() == null) {
            return 0.0;
        }
        
        // Calculate Euclidean distance between feature vectors
        float[] features1 = face1.getFeatures();
        float[] features2 = face2.getFeatures();
        
        double sumSquaredDiff = 0.0;
        for (int i = 0; i < features1.length && i < features2.length; i++) {
            double diff = features1[i] - features2[i];
            sumSquaredDiff += diff * diff;
        }
        
        double distance = Math.sqrt(sumSquaredDiff);
        
        // Convert distance to similarity score (0-1)
        // Normalize by maximum possible distance (assuming pixel values 0-255)
        double maxDistance = Math.sqrt(features1.length * 255 * 255);
        double similarity = 1.0 - (distance / maxDistance);
        
        return similarity;
    }
    
    @Override
    public FaceData identifyFace(FaceData faceToIdentify, List<FaceData> knownFaces, double threshold) {
        if (faceToIdentify == null || knownFaces == null || knownFaces.isEmpty()) {
            return null;
        }
        
        FaceData bestMatch = null;
        double bestSimilarity = threshold;
        
        for (FaceData knownFace : knownFaces) {
            double similarity = compareFaces(faceToIdentify, knownFace);
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestMatch = knownFace;
            }
        }
        
        return bestMatch;
    }
}