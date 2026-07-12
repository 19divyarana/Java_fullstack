# Java Practice — Numerical & Pattern Questions

Practice these using `for`/`while` loops and conditionals. Try solving each yourself before checking the solution code.

---

## Part 1: Numerical Questions

### 1. Check if a number is Even or Odd
```java
int num = 7;
if (num % 2 == 0) {
    System.out.println(num + " is Even");
} else {
    System.out.println(num + " is Odd");
}
```

### 2. Check if a number is Prime
```java
int num = 29;
boolean isPrime = true;

if (num <= 1) {
    isPrime = false;
} else {
    for (int i = 2; i <= Math.sqrt(num); i++) {
        if (num % i == 0) {
            isPrime = false;
            break;
        }
    }
}
System.out.println(num + " is Prime: " + isPrime);
```

### 3. Print all Prime numbers between 1 and 100
```java
for (int num = 2; num <= 100; num++) {
    boolean isPrime = true;
    for (int i = 2; i <= Math.sqrt(num); i++) {
        if (num % i == 0) {
            isPrime = false;
            break;
        }
    }
    if (isPrime) {
        System.out.print(num + " ");
    }
}
```

### 4. Factorial of a number
```java
int num = 5;
long factorial = 1;

for (int i = 1; i <= num; i++) {
    factorial *= i;
}
System.out.println("Factorial of " + num + " = " + factorial);
```

### 5. Fibonacci Series (first n terms)
```java
int n = 10;
int a = 0, b = 1;

for (int i = 1; i <= n; i++) {
    System.out.print(a + " ");
    int next = a + b;
    a = b;
    b = next;
}
// Output: 0 1 1 2 3 5 8 13 21 34
```

### 6. Check if a number is a Palindrome
```java
int num = 12321;
int original = num;
int reversed = 0;

while (num != 0) {
    int digit = num % 10;
    reversed = reversed * 10 + digit;
    num /= 10;
}

if (original == reversed) {
    System.out.println("Palindrome");
} else {
    System.out.println("Not a Palindrome");
}
```

### 7. Reverse a number
```java
int num = 4562;
int reversed = 0;

while (num != 0) {
    int digit = num % 10;
    reversed = reversed * 10 + digit;
    num /= 10;
}
System.out.println("Reversed: " + reversed);
```

### 8. Sum of digits of a number
```java
int num = 4562;
int sum = 0;

while (num != 0) {
    sum += num % 10;
    num /= 10;
}
System.out.println("Sum of digits: " + sum);
```

### 9. Check Armstrong Number (e.g. 153 = 1³+5³+3³)
```java
int num = 153;
int original = num;
int sum = 0;

while (num != 0) {
    int digit = num % 10;
    sum += digit * digit * digit;
    num /= 10;
}

if (sum == original) {
    System.out.println(original + " is an Armstrong number");
} else {
    System.out.println(original + " is not an Armstrong number");
}
```

### 10. GCD (HCF) of two numbers
```java
int a = 36, b = 60;
int x = a, y = b;

while (y != 0) {
    int temp = y;
    y = x % y;
    x = temp;
}
System.out.println("GCD: " + x);
```

### 11. LCM of two numbers
```java
int a = 12, b = 18;

// find GCD first
int x = a, y = b;
while (y != 0) {
    int temp = y;
    y = x % y;
    x = temp;
}
int gcd = x;
int lcm = (a * b) / gcd;
System.out.println("LCM: " + lcm);
```

### 12. Swap two numbers without a third variable
```java
int a = 5, b = 10;
a = a + b;
b = a - b;
a = a - b;
System.out.println("a = " + a + ", b = " + b);
```

### 13. Count digits in a number
```java
int num = 34567;
int count = 0;

while (num != 0) {
    count++;
    num /= 10;
}
System.out.println("Number of digits: " + count);
```

### 14. Find the largest of three numbers
```java
int a = 15, b = 42, c = 27;
int largest = a;

if (b > largest) largest = b;
if (c > largest) largest = c;

System.out.println("Largest: " + largest);
```

---

## Part 2: Pattern Questions

### 1. Square Pattern
```
* * * * *
* * * * *
* * * * *
* * * * *
```
```java
int n = 4;
for (int i = 1; i <= n; i++) {
    for (int j = 1; j <= n; j++) {
        System.out.print("* ");
    }
    System.out.println();
}
```

### 2. Right Triangle (Star Pattern)
```
*
* *
* * *
* * * *
```
```java
int n = 4;
for (int i = 1; i <= n; i++) {
    for (int j = 1; j <= i; j++) {
        System.out.print("* ");
    }
    System.out.println();
}
```

### 3. Inverted Right Triangle
```
* * * *
* * *
* *
*
```
```java
int n = 4;
for (int i = n; i >= 1; i--) {
    for (int j = 1; j <= i; j++) {
        System.out.print("* ");
    }
    System.out.println();
}
```

### 4. Pyramid Pattern
```
   *
  * *
 * * *
* * * *
```
```java
int n = 4;
for (int i = 1; i <= n; i++) {
    for (int j = i; j < n; j++) {
        System.out.print(" ");
    }
    for (int k = 1; k <= i; k++) {
        System.out.print("* ");
    }
    System.out.println();
}
```

### 5. Number Pattern (1,2,3...)
```
1
1 2
1 2 3
1 2 3 4
```
```java
int n = 4;
for (int i = 1; i <= n; i++) {
    for (int j = 1; j <= i; j++) {
        System.out.print(j + " ");
    }
    System.out.println();
}
```

### 6. Number Pattern (repeated row number)
```
1
2 2
3 3 3
4 4 4 4
```
```java
int n = 4;
for (int i = 1; i <= n; i++) {
    for (int j = 1; j <= i; j++) {
        System.out.print(i + " ");
    }
    System.out.println();
}
```

### 7. Floyd's Triangle
```
1
2 3
4 5 6
7 8 9 10
```
```java
int n = 4;
int num = 1;
for (int i = 1; i <= n; i++) {
    for (int j = 1; j <= i; j++) {
        System.out.print(num + " ");
        num++;
    }
    System.out.println();
}
```

### 8. Diamond Pattern
```
   *
  * * *
 * * * * *
* * * * * * *
 * * * * *
  * * *
   *
```
```java
int n = 4;

// upper half
for (int i = 1; i <= n; i++) {
    for (int j = i; j < n; j++) {
        System.out.print(" ");
    }
    for (int k = 1; k <= (2 * i - 1); k++) {
        System.out.print("*");
    }
    System.out.println();
}

// lower half
for (int i = n - 1; i >= 1; i--) {
    for (int j = n; j > i; j--) {
        System.out.print(" ");
    }
    for (int k = 1; k <= (2 * i - 1); k++) {
        System.out.print("*");
    }
    System.out.println();
}
```

### 9. Alphabet Pattern
```
A
B B
C C C
D D D D
```
```java
int n = 4;
char ch = 'A';

for (int i = 1; i <= n; i++) {
    for (int j = 1; j <= i; j++) {
        System.out.print(ch + " ");
    }
    ch++;
    System.out.println();
}
```

### 10. Hollow Square Pattern
```
* * * * *
*       *
*       *
*       *
* * * * *
```
```java
int n = 5;
for (int i = 1; i <= n; i++) {
    for (int j = 1; j <= n; j++) {
        if (i == 1 || i == n || j == 1 || j == n) {
            System.out.print("* ");
        } else {
            System.out.print("  ");
        }
    }
    System.out.println();
}
```

---

## Tips for Practice
- Always identify **rows** (outer loop) and **columns** (outer loop's contents, inner loop) separately for patterns.
- For numerical problems, try solving with `while` loop too, not just `for` — builds flexibility.
- Once comfortable, try harder variants: Pascal's triangle, butterfly pattern, palindrome patterns using numbers instead of stars.
