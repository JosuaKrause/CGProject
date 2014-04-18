package cgp.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cgp.algos.TriangleStorage;
import cgp.data.AffineTransform4;
import cgp.data.Triangle;
import cgp.data.Vec4;

/**
 * @author Timothy Chu
 */
public class OBJReader implements MeshLoader {
  private String objectFile;
  private final ArrayList<Vec4> vertices = new ArrayList<Vec4>();
  private final ArrayList<Vec4> vertexNormals = new ArrayList<Vec4>();
  private final ArrayList<int[]> faces = new ArrayList<int[]>();

  public OBJReader(final String filename) {
    objectFile = filename;
  }

  public void setObjectFile(final String filename) {
    objectFile = filename;
  }

  @Override
  public void loadMesh(final TriangleStorage ts, final AffineTransform4 aff) {
    FileInputStream objFile;
    try {
      boolean shouldReset = false;
      objFile = new FileInputStream(objectFile);
      final BufferedReader in = new BufferedReader(new InputStreamReader(objFile));
      vertices.clear();
      vertexNormals.clear();
      faces.clear();
      String line;
      while((line = in.readLine()) != null) {
        final String[] coords = line.split("\\s+");
        // Add vertices
        if(coords[0].equals("v")) {
          vertices.add(new Vec4(Double.parseDouble(coords[1]),
              Double.parseDouble(coords[2]),
              Double.parseDouble(coords[3]), true));
        }
        // Add vertex normals
        else if(coords[0].equals("vn")) {
          vertexNormals.add(new Vec4(Double.parseDouble(coords[1]),
              Double.parseDouble(coords[2]),
              Double.parseDouble(coords[3]), false));
        }
        // Add faces
        else if(coords[0].equals("f")) {
          // Triangle face
          if(coords.length == 4) {
            final int[] face = { Integer.parseInt(coords[1]),
                Integer.parseInt(coords[2]),
                Integer.parseInt(coords[3])};
            faces.add(face);
          }
          // Quadrilateral face
          if(coords.length == 5) {
            final int[] face = { Integer.parseInt(coords[1]),
                Integer.parseInt(coords[2]),
                Integer.parseInt(coords[3]), Integer.parseInt(coords[4])};
            faces.add(face);
          }
          // If OBJ file keeps faces in relation to only 3 or 4 vertices, keep
          // resetting the arrayLists.
          if(vertices.size() <= 4) {
            shouldReset = true;
            constructTriangles(ts, aff);
            vertices.clear();
            vertexNormals.clear();
            faces.clear();
          }
        }
      }
      objFile.close();
      if(!shouldReset) {
        constructTriangles(ts, aff);
        vertices.clear();
        vertexNormals.clear();
        faces.clear();
      }
    } catch(final IOException e) {
      System.err.println(e);
    }
  }

  private void constructTriangles(final TriangleStorage ts, final AffineTransform4 aff) {
    for(final int[] face : faces) {
      if(face.length == 3) {
        // No normals
        if(vertexNormals.isEmpty()) {
          ts.addTriangle(aff.transform(new Triangle(vertices.get(face[0] - 1),
              vertices.get(face[1] - 1), vertices.get(face[2] - 1))));
        }
        // Has normals
        else {
          ts.addTriangle(aff.transform(new Triangle(vertices.get(face[0] - 1),
              vertices.get(face[1] - 1), vertices.get(face[2] - 1),
              vertexNormals.get(face[0] - 1),
              vertexNormals.get(face[1] - 1), vertexNormals.get(face[2] - 1))));
        }
      }
      else {
        // No normals
        if(vertexNormals.isEmpty()) {
          ts.addTriangle(aff.transform(new Triangle(vertices.get(face[0] - 1),
              vertices.get(face[1] - 1), vertices.get(face[2] - 1))));
          ts.addTriangle(aff.transform(new Triangle(vertices.get(face[2] - 1),
              vertices.get(face[3] - 1), vertices.get(face[0] - 1))));
        }
        // Has normals
        else {
          ts.addTriangle(aff.transform(new Triangle(vertices.get(face[0] - 1),
              vertices.get(face[1] - 1), vertices.get(face[2] - 1),
              vertexNormals.get(face[0] - 1),
              vertexNormals.get(face[1] - 1), vertexNormals.get(face[2] - 1))));
          ts.addTriangle(aff.transform(new Triangle(vertices.get(face[2] - 1),
              vertices.get(face[3] - 1), vertices.get(face[0] - 1),
              vertexNormals.get(face[2] - 1),
              vertexNormals.get(face[3] - 1), vertexNormals.get(face[0] - 1))));
        }
      }
    }
  }
}
