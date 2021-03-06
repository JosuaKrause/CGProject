package cgp.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cgp.algos.TriangleStorage;
import cgp.data.Triangle;
import cgp.data.Vec4;

/**
 * @author Timothy Chu
 */
public class OBJReader implements MeshLoader {
  /**
   * Filepath of the object file to be read in
   */
  private final String objectFile;
  /**
   * Stored vertices from the object file
   */
  private final List<Vec4> vertices = new ArrayList<>();
  /**
   * Stored vertex normals from the object file
   */
  private final List<Vec4> vertexNormals = new ArrayList<>();
  /**
   * Indices of the vertices that make up each face
   */
  private final List<int[]> faces = new ArrayList<>();

  /**
   * Constructor
   *
   * @param filename filepath for the object file
   */
  public OBJReader(final String filename) {
    objectFile = filename;
  }

  @Override
  public void loadMesh(final TriangleStorage ts) throws IOException {
    boolean shouldReset = false;
    try (final BufferedReader in = new BufferedReader(new FileReader(objectFile))) {
      vertices.clear();
      vertexNormals.clear();
      faces.clear();
      String line;
      while((line = in.readLine()) != null) {
        final String[] coords = line.split("\\s+");
        switch(coords[0]) {
          case "v":
            // Add vertices
            vertices.add(new Vec4(
                Double.parseDouble(coords[1]), Double.parseDouble(coords[2]),
                Double.parseDouble(coords[3]), true));
            break;
          case "vn":
            // Add vertex normals
            vertexNormals.add(new Vec4(
                Double.parseDouble(coords[1]), Double.parseDouble(coords[2]),
                Double.parseDouble(coords[3]), false));
            break;
          case "f":
            // Add faces
            if(coords.length == 4) {
              // Triangle face
              final int[] face = {
                  Integer.parseInt(coords[1]),
                  Integer.parseInt(coords[2]),
                  Integer.parseInt(coords[3])
              };
              faces.add(face);
            } else if(coords.length == 5) {
              // Quadrilateral face
              final int[] face = {
                  Integer.parseInt(coords[1]),
                  Integer.parseInt(coords[2]),
                  Integer.parseInt(coords[3]),
                  Integer.parseInt(coords[4])
              };
              faces.add(face);
            }
            if(vertices.size() <= 4) {
              // If OBJ file keeps faces in relation to only 3 or 4 vertices,
              // keep resetting the arrayLists.
              shouldReset = true;
              constructTriangles(ts);
              vertices.clear();
              vertexNormals.clear();
              faces.clear();
            }
            break;
          default:
            // ignore other lines
        }
      }
      if(!shouldReset) {
        constructTriangles(ts);
        vertices.clear();
        vertexNormals.clear();
        faces.clear();
      }
    }
  }

  /**
   * @param ts Data structure to store the triangles in
   */
  private void constructTriangles(final TriangleStorage ts) {
    for(final int[] face : faces) {
      if(face.length == 3) {
        // No normals
        if(vertexNormals.isEmpty()) {
          ts.addTriangle(new Triangle(vertices.get(face[0] - 1),
              vertices.get(face[1] - 1), vertices.get(face[2] - 1)));
        } else {
          // Has normals
          ts.addTriangle(new Triangle(
              vertices.get(face[0] - 1), vertices.get(face[1] - 1),
              vertices.get(face[2] - 1), vertexNormals.get(face[0] - 1),
              vertexNormals.get(face[1] - 1), vertexNormals.get(face[2] - 1)));
        }
      } else {
        // No normals
        if(vertexNormals.isEmpty()) {
          ts.addTriangle(new Triangle(vertices.get(face[0] - 1),
              vertices.get(face[1] - 1), vertices.get(face[2] - 1)));
          ts.addTriangle(new Triangle(vertices.get(face[2] - 1),
              vertices.get(face[3] - 1), vertices.get(face[0] - 1)));
        } else {
          // Has normals
          ts.addTriangle(new Triangle(
              vertices.get(face[0] - 1), vertices.get(face[1] - 1),
              vertices.get(face[2] - 1), vertexNormals.get(face[0] - 1),
              vertexNormals.get(face[1] - 1), vertexNormals.get(face[2] - 1)));
          ts.addTriangle(new Triangle(
              vertices.get(face[2] - 1), vertices.get(face[3] - 1),
              vertices.get(face[0] - 1), vertexNormals.get(face[2] - 1),
              vertexNormals.get(face[3] - 1), vertexNormals.get(face[0] - 1)));
        }
      }
    }
  }

}
