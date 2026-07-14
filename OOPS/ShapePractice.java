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

        public abstract double area();

        public abstract double perimeter();
    }

    // ---- Circle ----
    static class Circle extends Shape {
        private double radius;

        public Circle(double radius) {
            this.radius = radius;
        }

        @Override
        public double area() {
            return Math.PI * radius * radius;
        }

        @Override
        public double perimeter() {
            return 2 * Math.PI * radius;
        }

        @Override
        public String toString() {
            return "Circle [Radius = " + radius +
                    ", Area = " + String.format("%.2f", area()) +
                    ", Perimeter = " + String.format("%.2f", perimeter()) + "]";
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
            return width * height;
        }

        @Override
        public double perimeter() {
            return 2 * (width + height);
        }

        @Override
        public String toString() {
            return "Rectangle [Width = " + width +
                    ", Height = " + height +
                    ", Area = " + area() +
                    ", Perimeter = " + perimeter() + "]";
        }
    }

    // ---- Triangle ----
    static class Triangle extends Shape {
        private double a, b, c;

        public Triangle(double a, double b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public double area() {
            double s = perimeter() / 2;
            return Math.sqrt(s * (s - a) * (s - b) * (s - c));
        }

        @Override
        public double perimeter() {
            return a + b + c;
        }

        @Override
        public String toString() {
            return "Triangle [Sides = " + a + ", " + b + ", " + c +
                    ", Area = " + String.format("%.2f", area()) +
                    ", Perimeter = " + perimeter() + "]";
        }
    }

    public static void main(String[] args) {

        Shape[] shapes = {
                new Circle(5),
                new Rectangle(4, 6),
                new Triangle(3, 4, 5)
        };

        double totalArea = 0;

        for (Shape s : shapes) {
            System.out.println(s);
            totalArea += s.area();
        }

        System.out.println("--------------------------------");
        System.out.printf("Total Area = %.2f%n", totalArea);
    }
}
