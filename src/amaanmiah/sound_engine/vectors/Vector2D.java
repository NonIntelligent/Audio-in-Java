package amaanmiah.sound_engine.vectors;

import java.io.Serializable;

/**
 * The purpose of this class is to act as a mathematical vector
 * and make vector calculations much simpler.
 * <p>
 * Methods are static so that they can be used without creating
 * an object of Vector2D.
 * @author Amaan Miah
 *
 */

public class Vector2D implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8692936253252396608L;
	public int x;
	public int y;
	
	/**
	 * Create a position vector at x,y
	 * @param x The coordinate on the x-axis
	 * @param y The coordinate on the y-axis
	 */
	public Vector2D(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Adds two vectors together
	 * @param A Vector2D object
	 * @param B Vector2D object
	 * @return A vector2D object that is the sum of A + B
	 */
	
	public static Vector2D Add(Vector2D A, Vector2D B) {
		// vector A + vector B
		int x = 0;
		int y = 0;
		x = A.x + B.x;
		y = A.y + B.y;
		Vector2D C = new Vector2D(x,y);
		return C;
	}
	
	/**
	 * Subtracts two vectors
	 * @param A Vector2D object
	 * @param B Vector2D object
	 * @return A vector2D object that is the sum of A - B
	 */
	
	public static Vector2D Subtract(Vector2D A, Vector2D B) {
		// vector A - vector B
		int x = 0;
		int y = 0;
		x = A.x - B.x;
		y = A.y - B.y;
		Vector2D C = new Vector2D(x,y);
		return C;
	}
	
	/**
	 * The Dot product of two vectors
	 * @param A Vector2D object
	 * @param B Vector2D object
	 * @return The dot product as a Double
	 */
	
	public static double Dot(Vector2D A, Vector2D B) {
		// A dot B
		return (A.x*B.x) + (A.y*B.y);
		
	}
	
	/**
	 * The Determinant of two vectors
	 * @param A Vector2D object
	 * @param B Vector2D object
	 * @return the determinant as a Double
	 */
	
	public static double Det(Vector2D A, Vector2D B) {
		// A det B
		return (A.x*B.y) - (A.y*B.x);
	}
	
	/**
	 * The magnitude of two points
	 * @param x The change in x
	 * @param y The change in y
	 * @return The magnitude of the line as a double
	 */
	
	public static double magnitude(double x, double y) {
		return Math.sqrt(x*x + y*y);
	}
	
}
