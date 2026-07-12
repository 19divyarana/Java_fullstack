# Java OOPs — Complete Interview Notes

## 1. What is OOP?

**Definition:** Object-Oriented Programming (OOP) is a programming paradigm based on the concept of "objects" — real-world entities that bundle **data (fields/attributes)** and **behavior (methods)** together, instead of writing code as a sequence of instructions acting on separate data.

**Real-time example:** Think of a "Car" — it has data (color, brand, speed) and behavior (start(), accelerate(), brake()). Instead of managing these separately, OOP lets you model a `Car` as one unit.

**Four Pillars of OOP:**
1. Encapsulation
2. Inheritance
3. Polymorphism
4. Abstraction

---

## 2. Class and Object

**Definition:**
- **Class** — a blueprint/template that defines properties and behavior common to all objects of that type.
- **Object** — a real-world instance of a class, having actual state and occupying memory.

**Real-time example:** `Car` is a class (blueprint). Your actual Honda City in the parking lot is an `object` (instance).

```java
class Car {
    String brand;
    int speed;

    void accelerate() {
        speed += 10;
        System.out.println(brand + " speed is now " + speed);
    }
}

public class Main {
    public static void main(String[] args) {
        Car car1 = new Car();   // object creation
        car1.brand = "Honda";
        car1.accelerate();
    }
}
```

---

## 3. Encapsulation

**Definition:** Wrapping data (variables) and code (methods) together as a single unit, and restricting direct access to some of an object's components using access modifiers (usually by making fields `private` and exposing `public` getters/setters). This is called **data hiding**.

**Real-time example:** An ATM machine — you can't directly touch the bank's database (your balance), you can only interact through defined interfaces (withdraw, deposit, check balance) which apply validation rules internally.

```java
class BankAccount {
    private double balance;   // hidden from outside

    public void deposit(double amount) {
        if (amount > 0) balance += amount;
    }

    public double getBalance() {
        return balance;
    }
}

public class Main {
    public static void main(String[] args) {
        BankAccount acc = new BankAccount();
        acc.deposit(5000);
        // acc.balance = -1000;  // not allowed, balance is private
        System.out.println(acc.getBalance());
    }
}
```

**Interview tip:** Encapsulation is about **protecting data integrity** — you control how data is read/modified via methods, not direct field access.

---

## 4. Inheritance

**Definition:** A mechanism where one class (**child/subclass**) acquires the properties and behaviors of another class (**parent/superclass**), promoting code reusability. Achieved using the `extends` keyword.

**Real-time example:** A `SavingsAccount` and `CurrentAccount` are both types of `BankAccount` — they share common features (balance, deposit) but also have their own specific behavior (interest calculation, overdraft limit).

```java
class Account {
    double balance;
    void deposit(double amt) { balance += amt; }
}

class SavingsAccount extends Account {
    double interestRate = 4.0;
    void addInterest() {
        balance += balance * interestRate / 100;
    }
}

public class Main {
    public static void main(String[] args) {
        SavingsAccount sa = new SavingsAccount();
        sa.deposit(1000);      // inherited method
        sa.addInterest();      // own method
        System.out.println(sa.balance);
    }
}
```

**Types of Inheritance in Java:** Single, Multilevel, Hierarchical.
**Note:** Java does **not** support multiple inheritance with classes (to avoid the Diamond Problem) — but it's achieved through **interfaces**.

---

## 5. Polymorphism

**Definition:** "Poly" = many, "morph" = forms. The ability of an object/method to take multiple forms. Two types:
- **Compile-time (Static) Polymorphism** → Method Overloading
- **Runtime (Dynamic) Polymorphism** → Method Overriding

**Real-time example:** A person behaves differently in different roles — as a *teacher* in a classroom, as a *customer* in a shop, as a *parent* at home. Same person, different behavior depending on context.

### a) Method Overloading (Compile-time)
Same method name, different parameter list, within the same class.

```java
class Calculator {
    int add(int a, int b) { return a + b; }
    double add(double a, double b) { return a + b; }
    int add(int a, int b, int c) { return a + b + c; }
}
```

### b) Method Overriding (Runtime)
Subclass provides a specific implementation of a method already defined in its parent class.

```java
class Animal {
    void sound() { System.out.println("Animal makes a sound"); }
}

class Dog extends Animal {
    @Override
    void sound() { System.out.println("Dog barks"); }
}

public class Main {
    public static void main(String[] args) {
        Animal a = new Dog();   // upcasting
        a.sound();              // Dog barks -> resolved at runtime
    }
}
```

**Interview tip:** Overriding is resolved via **dynamic method dispatch** at runtime (based on object type), while overloading is resolved at **compile time** (based on method signature).

---

## 6. Abstraction

**Definition:** Hiding internal implementation details and showing only the essential features/functionality to the user. Achieved using **abstract classes** and **interfaces**.

**Real-time example:** When you drive a car, you only use the steering wheel, brake, and accelerator — you don't need to know how the engine or fuel injection system works internally. That complexity is abstracted away.

### a) Abstract Class
```java
abstract class Shape {
    abstract double area();   // no body, must be implemented by subclass

    void display() {          // can have normal methods too
        System.out.println("This is a shape");
    }
}

class Circle extends Shape {
    double radius;
    Circle(double r) { radius = r; }

    @Override
    double area() { return Math.PI * radius * radius; }
}
```

### b) Interface (100% abstraction, until Java 8 default methods)
```java
interface Payment {
    void pay(double amount);
}

class CreditCardPayment implements Payment {
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using Credit Card");
    }
}
```

**Abstract class vs Interface (common interview question):**

| Feature | Abstract Class | Interface |
|---|---|---|
| Methods | Can have abstract + concrete methods | Only abstract (till Java 7); default/static allowed from Java 8 |
| Multiple inheritance | Not supported | Supported (a class can implement multiple interfaces) |
| Constructors | Can have | Cannot have |
| Access modifiers | Any (private, protected, public) | Public by default |
| When to use | When classes share common code/state | When unrelated classes need to guarantee certain behavior |

---

## 7. Other Important Concepts (frequently asked)

### Constructor
Special method used to initialize objects; same name as class, no return type.
```java
class Student {
    String name;
    Student(String name) {   // constructor
        this.name = name;
    }
}
```

### `this` keyword
Refers to the current object instance. Used to resolve naming conflicts between instance variables and parameters (as seen above).

### `super` keyword
Refers to the immediate parent class — used to call parent constructor or parent methods.
```java
class Employee {
    Employee() { System.out.println("Employee created"); }
}
class Manager extends Employee {
    Manager() {
        super();   // calls Employee's constructor
        System.out.println("Manager created");
    }
}
```

### `static` keyword
Belongs to the class rather than any instance — shared across all objects.
**Real-time example:** A company's name is the same for all employees — it can be a `static` field.
```java
class Employee {
    static String company = "TCS";
    String name;
}
```

### `final` keyword
- `final variable` → constant, can't be reassigned
- `final method` → cannot be overridden
- `final class` → cannot be extended

### Association, Aggregation, Composition (often confused)
- **Association** — general relationship between two classes (e.g., Teacher teaches Student).
- **Aggregation** — "has-a" relationship where child can exist independently of parent (e.g., Department has Professors, but Professors can exist without the Department).
- **Composition** — strong "has-a" relationship where child cannot exist without parent (e.g., House has Rooms; Rooms cease to exist if the House is destroyed).

---

## 8. Quick Interview Q&A Cheat Sheet

**Q: Why is Java not 100% object-oriented?**
A: Because it uses primitive data types (int, char, boolean, etc.) which are not objects.

**Q: Can we overload the `main` method?**
A: Yes, but JVM always calls `public static void main(String[] args)` as the entry point.

**Q: Can a constructor be `private`?**
A: Yes — commonly used in Singleton design pattern to restrict object creation.

**Q: Difference between Overloading and Overriding?**
A: Overloading = same class, same name, different parameters (compile-time). Overriding = parent-child classes, same signature, different implementation (runtime).

**Q: What is the diamond problem, and how does Java avoid it?**
A: It occurs when a class inherits from two classes having the same method — ambiguity arises. Java avoids it by disallowing multiple inheritance with classes; interfaces solve this with explicit override rules (Java 8+ default methods require the implementing class to resolve conflicts).

**Q: Is Encapsulation the same as Abstraction?**
A: No. Abstraction hides *complexity* (what to show), Encapsulation hides *data* (how to protect it). Abstraction is design-level, Encapsulation is implementation-level.

---

## 9. One Combined Real-World Example (all 4 pillars together)

```java
// Abstraction + Inheritance
abstract class Employee {
    protected String name;     // Encapsulation (protected access)
    private double basicSalary;

    Employee(String name, double basicSalary) {
        this.name = name;
        this.basicSalary = basicSalary;
    }

    public double getBasicSalary() { return basicSalary; }  // controlled access

    abstract double calculateSalary();   // Abstraction

    void showDetails() {
        System.out.println(name + "'s salary: " + calculateSalary());
    }
}

// Inheritance
class Developer extends Employee {
    Developer(String name, double basicSalary) {
        super(name, basicSalary);
    }

    @Override
    double calculateSalary() {          // Polymorphism (Overriding)
        return getBasicSalary() + 5000; // tech allowance
    }
}

class Manager extends Employee {
    Manager(String name, double basicSalary) {
        super(name, basicSalary);
    }

    @Override
    double calculateSalary() {          // Polymorphism (Overriding)
        return getBasicSalary() + 10000; // management allowance
    }
}

public class Main {
    public static void main(String[] args) {
        Employee e1 = new Developer("Ravi", 30000);
        Employee e2 = new Manager("Priya", 50000);
        e1.showDetails();
        e2.showDetails();
    }
}
```

This single example is a great one to narrate end-to-end in an interview — it naturally touches all four pillars.
