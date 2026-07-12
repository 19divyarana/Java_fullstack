# Java Basics — Notes

## 1. What is Java?
Java is a **high-level, object-oriented, platform-independent** programming language developed by Sun Microsystems (1995), now owned by Oracle.

- Code is compiled into **bytecode** (`.class` file) by the Java compiler (`javac`)
- Bytecode runs on the **JVM (Java Virtual Machine)** — this is why Java is platform-independent ("Write Once, Run Anywhere")

**JDK vs JRE vs JVM**
| Term | Meaning |
|---|---|
| JVM | Runs the bytecode |
| JRE | JVM + libraries needed to *run* Java programs |
| JDK | JRE + compiler & tools needed to *develop* Java programs |

---

## 2. Features of Java

- **Simple** – syntax close to C/C++ but removes complex features like pointers
- **Object-Oriented** – everything revolves around classes and objects
- **Platform Independent** – bytecode runs on any OS with a JVM
- **Secure** – no explicit pointers, runs in a sandboxed JVM environment
- **Robust** – strong memory management, automatic garbage collection, exception handling
- **Multithreaded** – supports running multiple threads (tasks) at once
- **Architecture Neutral** – no OS/hardware-dependent features in the language
- **Portable** – same bytecode works everywhere
- **High Performance** – uses JIT (Just-In-Time) compiler for faster execution
- **Distributed** – supports networking (RMI, sockets, etc.)
- **Dynamic** – supports dynamic loading of classes at runtime

---

## 3. Variables

A variable is a **named memory location** used to store data.

```java
int age = 21;         // int variable
String name = "Ravi";  // String variable
```

### Types of Variables
| Type | Description |
|---|---|
| Local Variable | Declared inside a method, only accessible there |
| Instance Variable | Declared inside a class, outside methods; each object gets its own copy |
| Static Variable | Declared with `static` keyword; shared across all objects |

```java
class Demo {
    int instanceVar = 10;         // instance variable
    static int staticVar = 20;    // static variable

    void show() {
        int localVar = 30;        // local variable
        System.out.println(instanceVar + staticVar + localVar);
    }
}
```

### Rules for naming variables
- Can contain letters, digits, `_`, `$`
- Cannot start with a digit
- Case-sensitive (`age` ≠ `Age`)
- Cannot use reserved keywords (`int`, `class`, `public`, etc.)

---

## 4. Data Types

Java is **statically typed** — every variable must have a declared type.

### Primitive Data Types (8 total)
| Type | Size | Example |
|---|---|---|
| `byte` | 1 byte | `byte b = 10;` |
| `short` | 2 bytes | `short s = 1000;` |
| `int` | 4 bytes | `int x = 100000;` |
| `long` | 8 bytes | `long l = 100000L;` |
| `float` | 4 bytes | `float f = 3.14f;` |
| `double` | 8 bytes | `double d = 3.1415;` |
| `char` | 2 bytes | `char c = 'A';` |
| `boolean` | 1 bit | `boolean flag = true;` |

```java
public class DataTypesExample {
    public static void main(String[] args) {
        byte b = 10;
        short s = 500;
        int i = 100000;
        long l = 10000000000L;
        float f = 5.75f;
        double d = 19.99;
        char c = 'J';
        boolean flag = true;

        System.out.println(b + " " + s + " " + i + " " + l + " " + f + " " + d + " " + c + " " + flag);
    }
}
```

### Non-Primitive (Reference) Data Types
Examples: `String`, Arrays, Classes, Interfaces
```java
String name = "Java";
int[] numbers = {1, 2, 3, 4};
```

---

## 5. Type Casting

Converting one data type into another.

### a) Widening (Implicit) Casting
Smaller type → Larger type — done automatically by Java.
```java
int num = 100;
double d = num;   // int to double, automatic
System.out.println(d);   // 100.0
```
Order: `byte → short → int → long → float → double`

### b) Narrowing (Explicit) Casting
Larger type → Smaller type — must be done manually.
```java
double d = 9.78;
int num = (int) d;   // explicit cast
System.out.println(num);   // 9 (decimal part lost)
```

### Example combining both
```java
public class CastingExample {
    public static void main(String[] args) {
        int x = 10;
        double y = x;          // widening
        System.out.println(y); // 10.0

        double a = 25.99;
        int b = (int) a;        // narrowing
        System.out.println(b);  // 25
    }
}
```

---

## 6. Operators

### a) Arithmetic Operators
`+  -  *  /  %`
```java
int a = 10, b = 3;
System.out.println(a + b); // 13
System.out.println(a - b); // 7
System.out.println(a * b); // 30
System.out.println(a / b); // 3  (integer division)
System.out.println(a % b); // 1  (remainder)
```

### b) Assignment Operators
`=  +=  -=  *=  /=  %=`
```java
int x = 10;
x += 5;   // x = x + 5 → 15
x -= 3;   // x = 12
x *= 2;   // x = 24
System.out.println(x);
```

### c) Relational (Comparison) Operators
`==  !=  >  <  >=  <=`
```java
int a = 5, b = 8;
System.out.println(a == b); // false
System.out.println(a < b);  // true
System.out.println(a != b); // true
```

### d) Logical Operators
`&&  (AND)   ||  (OR)   !  (NOT)`
```java
int age = 25;
boolean hasID = true;

System.out.println(age >= 18 && hasID); // true
System.out.println(age < 18 || hasID);  // true
System.out.println(!hasID);             // false
```

### e) Unary Operators
`+  -  ++  --  !`
```java
int a = 5;
System.out.println(++a); // 6 (pre-increment)
System.out.println(a--); // 6 (post-decrement, prints then decreases)
System.out.println(a);   // 5
```

### f) Bitwise Operators
`&  |  ^  ~  <<  >>`
```java
int a = 5;  // 0101
int b = 3;  // 0011

System.out.println(a & b);  // 1  (AND)
System.out.println(a | b);  // 7  (OR)
System.out.println(a ^ b);  // 6  (XOR)
System.out.println(~a);     // -6 (NOT)
System.out.println(a << 1); // 10 (left shift)
System.out.println(a >> 1); // 2  (right shift)
```

### g) Ternary Operator
`condition ? valueIfTrue : valueIfFalse`
```java
int a = 10, b = 20;
int max = (a > b) ? a : b;
System.out.println(max); // 20
```

---

## Quick Recap
- Java = OOP + platform-independent (via JVM)
- Variables: local, instance, static
- 8 primitive types + reference types (String, arrays, classes)
- Type casting: widening (auto) vs narrowing (manual)
- Operators: arithmetic, assignment, relational, logical, unary, bitwise, ternary
