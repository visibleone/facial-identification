# Facial Identification API

This project implements a basic facial identification system using JavaCV, a Java wrapper for OpenCV. It provides REST endpoints for face detection, registration, identification, and comparison.

## Features

- **Face Detection**: Detect faces in images and get their coordinates
- **Face Registration**: Register faces with labels for future identification
- **Face Identification**: Identify faces against a collection of known faces
- **Face Comparison**: Compare two faces and get a similarity score

## Technologies Used

- Spring Boot 3.5.0
- JavaCV 1.5.9 (Java interface to OpenCV)
- Java 21
- Maven

## API Endpoints

### Detect Faces

```
POST /api/faces/detect
```

Detects all faces in an uploaded image.

**Request Parameters:**
- `image`: The image file containing faces

**Response:**
```json
[
  {
    "x": 100,
    "y": 150,
    "width": 200,
    "height": 200
  },
  {
    "x": 500,
    "y": 150,
    "width": 200,
    "height": 200
  }
]
```

### Register Face

```
POST /api/faces/register
```

Registers a face with a label for future identification.

**Request Parameters:**
- `image`: The image file containing a face
- `label`: The label/name to associate with the face

**Response:**
```json
{
  "x": 100,
  "y": 150,
  "width": 200,
  "height": 200,
  "label": "John Doe"
}
```

### Identify Face

```
POST /api/faces/identify
```

Identifies a face against registered faces.

**Request Parameters:**
- `image`: The image file containing a face to identify
- `threshold` (optional): Minimum similarity threshold (0-1, default: 0.7)

**Response (match found):**
```json
{
  "identified": true,
  "label": "John Doe",
  "x": 100,
  "y": 150,
  "width": 200,
  "height": 200
}
```

**Response (no match):**
```json
{
  "identified": false,
  "message": "No match found"
}
```

### Compare Faces

```
POST /api/faces/compare
```

Compares two faces and returns a similarity score.

**Request Parameters:**
- `image1`: The first image file containing a face
- `image2`: The second image file containing a face

**Response:**
```json
{
  "similarity": 0.85
}
```

### List Known Faces

```
GET /api/faces/known
```

Lists all registered face labels.

**Response:**
```json
[
  "John Doe",
  "Jane Smith"
]
```

## Implementation Details

The facial identification system uses the following components:

1. **Haar Cascade Classifier**: For face detection
2. **Feature Extraction**: Extracts facial features for recognition
3. **Euclidean Distance**: Measures similarity between face features

In a production environment, you would want to:
- Use a more sophisticated face recognition algorithm (e.g., deep learning-based)
- Store known faces in a database
- Add authentication and authorization
- Optimize for performance and scalability

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on port 8080 by default.

### Testing the API

You can use tools like Postman or curl to test the API endpoints. For example:

```bash
curl -X POST -F "image=@/path/to/image.jpg" http://localhost:8080/api/faces/detect
```

## License

This project is licensed under the MIT License.