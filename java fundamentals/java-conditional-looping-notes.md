# Java — Conditional & Looping Statements

## Part 1: Conditional Statements

Used to make decisions in code — execute certain blocks only if a condition is true.

### a) if statement
```java
int age = 20;
if (age >= 18) {
    System.out.println("You are eligible to vote");
}
```

### b) if-else statement
```java
int age = 15;
if (age >= 18) {
    System.out.println("Eligible to vote");
} else {
    System.out.println("Not eligible to vote");
}
```

### c) if-else-if ladder
Used when there are multiple conditions to check in sequence.
```java
int marks = 75;

if (marks >= 90) {
    System.out.println("Grade A");
} else if (marks >= 75) {
    System.out.println("Grade B");
} else if (marks >= 50) {
    System.out.println("Grade C");
} else {
    System.out.println("Fail");
}
```

### d) Nested if
An `if` inside another `if`.
```java
int age = 25;
boolean hasLicense = true;

if (age >= 18) {
    if (hasLicense) {
        System.out.println("You can drive");
    } else {
        System.out.println("Get a license first");
    }
} else {
    System.out.println("Too young to drive");
}
```

### e) switch statement
Used when comparing one variable against multiple fixed values — cleaner than long if-else-if chains.
```java
int day = 3;
String dayName;

switch (day) {
    case 1:
        dayName = "Monday";
        break;
    case 2:
        dayName = "Tuesday";
        break;
    case 3:
        dayName = "Wednesday";
        break;
    default:
        dayName = "Invalid day";
}
System.out.println(dayName);
```

**Note:** `break` stops execution from falling through to the next case. Without `break`, all cases below the matched one also run.

### f) Switch expression (Java 14+, modern style)
```java
int day = 3;
String dayName = switch (day) {
    case 1 -> "Monday";
    case 2 -> "Tuesday";
    case 3 -> "Wednesday";
    default -> "Invalid day";
};
System.out.println(dayName);
```

---

## Part 2: Looping Statements

Used to execute a block of code repeatedly.

### a) for loop
Best when the number of iterations is known.
```java
for (int i = 1; i <= 5; i++) {
    System.out.println("Count: " + i);
}
```
Structure: `for (initialization; condition; update)`

### b) while loop
Best when the number of iterations is not known in advance — condition checked before each iteration.
```java
int i = 1;
while (i <= 5) {
    System.out.println("Count: " + i);
    i++;
}
```

### c) do-while loop
Similar to `while`, but the block runs **at least once** since condition is checked after execution.
```java
int i = 1;
do {
    System.out.println("Count: " + i);
    i++;
} while (i <= 5);
```

### d) for-each loop (enhanced for loop)
Used to iterate over arrays or collections.
```java
int[] numbers = {10, 20, 30, 40};

for (int num : numbers) {
    System.out.println(num);
}
```

### e) Nested loops
A loop inside another loop — commonly used for patterns, matrices.
```java
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        System.out.print(i + "" + j + " ");
    }
    System.out.println();
}
// Output:
// 11 12 13
// 21 22 23
// 31 32 33
```

---

## Part 3: Loop Control Statements

### a) break
Exits the loop immediately.
```java
for (int i = 1; i <= 10; i++) {
    if (i == 5) {
        break;
    }
    System.out.println(i);
}
// Prints 1 2 3 4, then stops
```

### b) continue
Skips the current iteration and moves to the next one.
```java
for (int i = 1; i <= 5; i++) {
    if (i == 3) {
        continue;
    }
    System.out.println(i);
}
// Prints 1 2 4 5 (skips 3)
```

### c) Labeled break/continue
Used to control outer loops from within a nested loop.
```java
outer:
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        if (j == 2) {
            continue outer;
        }
        System.out.println(i + "-" + j);
    }
}
```

---

## Example: Combining Conditionals + Loops
A simple program to print all even numbers between 1 and 20 using a loop and a conditional.
```java
public class EvenNumbers {
    public static void main(String[] args) {
        for (int i = 1; i <= 20; i++) {
            if (i % 2 == 0) {
                System.out.println(i);
            }
        }
    }
}
```

---

## Quick Recap
| Statement | Use case |
|---|---|
| `if / if-else / else-if` | Decision-making based on conditions |
| `switch` | Multiple fixed-value comparisons |
| `for` | Known number of iterations |
| `while` | Unknown iterations, condition-checked-first |
| `do-while` | Runs at least once |
| `for-each` | Iterating arrays/collections |
| `break` | Exit loop early |
| `continue` | Skip current iteration |
