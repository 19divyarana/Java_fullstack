/*
 * EXERCISE 1: Shape Area Calculator
 * Concepts: Abstraction, Inheritance, Polymorphism
 *
 * TASK:
 * 1. Complete the abstract class Shape.
 * 2. Implement Circle, Rectangle, and Triangle so they extend Shape.
 * 3. Override area() and perimeter() correctly in each subclass.
 * 4. Override toString() in each subclass to print something like:
 *    "Circle[radius=5.0] -> area=78.54, perimeter=31.42"
 * 5. In main(), create a Shape[] array containing different shapes,
 *    loop through it, and print each one polymorphically.
 *
 * BONUS:
 * - Make Shape implement Comparable<Shape> so shapes can be sorted by area.
 * - Add a static method Shape.totalArea(Shape[] shapes) that sums all areas.
 *
 * Compile:  javac ShapePractice.java
 * Run:      java ShapePractice
 */

public class ShapePractice {

    // ---- Abstract base class ----
    static abstract class Shape {
        // TODO: declare any common fields (e.g., a name) if you want

        public abstract double area();

        public abstract double perimeter();

        // TODO: override toString() here if you want a default format
    }

    // ---- Circle ----
    static class Circle extends Shape {
        private double radius;

        public Circle(double radius) {
            this.radius = radius;
        }

        @Override
        public double area() {
            // TODO: implement
            return 0;
        }

        @Override
        public double perimeter() {
            // TODO: implement
            return 0;
        }

        @Override
        public String toString() {
            // TODO: implement
            return "";
        }
    }

    // ---- Rectangle ----
    static class Rectangle extends Shape {
        private double width;
        private double height;

        public Rectangle(double width, double height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public double area() {
            // TODO: implement
            return 0;
        }

        @Override
        public double perimeter() {
            // TODO: implement
            return 0;
        }

        @Override
        public String toString() {
            // TODO: implement
            return "";
        }
    }

    // ---- Triangle (assume you're given 3 sides, use Heron's formula) ----
    static class Triangle extends Shape {
        private double a, b, c;

        public Triangle(double a, double b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public double area() {
            // TODO: implement using Heron's formula
            return 0;
        }

        @Override
        public double perimeter() {
            // TODO: implement
            return 0;
        }

        @Override
        public String toString() {
            // TODO: implement
            return "";
        }
    }

    public static void main(String[] args) {
        Shape[] shapes = {
            new Circle(5),
            new Rectangle(4, 6),
            new Triangle(3, 4, 5)
        };

        for (Shape s : shapes) {
            System.out.println(s);
        }

        // TODO: print total area of all shapes using a loop or a static helper
    }
}
