Collections Framework — Questions & Answers
1. What is the Collections Framework?
A unified architecture of interfaces and classes (List, Set, Map, Queue, etc.) for storing and manipulating groups of objects, providing ready-made data structures and algorithms instead of writing them from scratch.
2. List vs Set vs Map?

List — ordered, allows duplicates, index-based access (ArrayList, LinkedList)
Set — no duplicates, mostly unordered (HashSet, LinkedHashSet, TreeSet)
Map — key-value pairs, keys unique (HashMap, LinkedHashMap, TreeMap)

3. ArrayList vs LinkedList?

ArrayList — backed by a dynamic array; fast random access (O(1) get by index); slow insert/delete in the middle (O(n) shifting)
LinkedList — doubly linked list; fast insert/delete at ends (O(1)); slow random access (O(n) traversal)

4. HashMap vs TreeMap vs LinkedHashMap?

HashMap — no ordering guarantee, O(1) average lookup
LinkedHashMap — maintains insertion order, slightly more overhead
TreeMap — sorted by key (natural or custom Comparator), O(log n) operations (Red-Black tree internally)

5. How does HashMap work internally?
Uses an array of buckets. key.hashCode() determines the bucket index. Collisions (same bucket) are handled via a linked list, which converts to a balanced tree (Red-Black tree) if a bucket exceeds 8 entries (Java 8+), improving worst-case lookup from O(n) to O(log n).
6. Why must equals() and hashCode() be overridden together for custom keys?
hashCode() decides the bucket; equals() confirms actual equality within that bucket. If only one is overridden, two "equal" objects could get different hash codes (landing in different buckets — lookup fails) or the same hash code but fail equals() (treated as distinct even if logically same). The contract: equal objects must have equal hash codes (the reverse isn't required).
7. HashSet vs TreeSet vs LinkedHashSet?
Same relationship as the Map trio — HashSet is unordered/fastest, LinkedHashSet preserves insertion order, TreeSet keeps elements sorted.
8. How does HashSet guarantee no duplicates?
Internally backed by a HashMap — each element is stored as a key with a dummy constant value. Adding a "duplicate" just overwrites the same key, so the set size doesn't grow.
9. Iterator vs ListIterator?
Iterator — forward-only traversal, works on any Collection, supports remove().
ListIterator — bidirectional (forward + backward), only for List, also supports add() and set().
10. What is ConcurrentModificationException?
Thrown when a collection is structurally modified (add/remove) while being iterated using a regular Iterator, outside the iterator's own remove() method. Fix: use Iterator.remove(), or CopyOnWriteArrayList/ConcurrentHashMap for concurrent scenarios.
javaList<Integer> list = new ArrayList<>(List.of(1,2,3));
for (Integer i : list) {
    if (i == 2) list.remove(i); // ❌ throws ConcurrentModificationException
}
11. Comparable vs Comparator?

Comparable — defines the natural ordering, implemented inside the class itself (compareTo()), only one sort order possible
Comparator — defines custom ordering, implemented externally, and you can create multiple different comparators for the same class

javaclass Employee implements Comparable<Employee> {
    int age;
    public int compareTo(Employee other) { return this.age - other.age; } // natural order
}

Comparator<Employee> bySalary = (e1, e2) -> e1.salary - e2.salary; // custom order
12. What is fail-fast vs fail-safe iterator?
Fail-fast (ArrayList, HashMap) — throws ConcurrentModificationException if the collection is modified during iteration; uses a modCount check.
Fail-safe (CopyOnWriteArrayList, ConcurrentHashMap) — iterates over a cloned/snapshot copy, so modifications during iteration don't throw, but the iterator may not reflect the latest changes.
13. Why is HashMap not thread-safe? What's the alternative?
Concurrent structural modification can corrupt internal bucket/linked-list structure or cause infinite loops during resize. Use ConcurrentHashMap (segment/bucket-level locking, much better throughput than Hashtable, which locks the entire map).
14. Array vs ArrayList?
Array — fixed size, can hold primitives, no built-in utility methods.
ArrayList — dynamically resizable, only holds objects (autoboxed for primitives), rich API (add, remove, contains, etc.).
15. What is the default capacity and load factor of HashMap?
Default capacity: 16. Default load factor: 0.75 (map resizes/doubles when 75% full) — balances memory usage against collision frequency.
16. PriorityQueue — how does it order elements?
Backed by a binary heap; by default orders elements by natural ordering (min-heap — smallest element polled first), or by a custom Comparator passed to the constructor for different priority logic (e.g., max-heap).
17. What's the difference between Collection and Collections?
Collection — the root interface of the framework (List, Set, Queue all extend it).
Collections — a utility class with static helper methods (Collections.sort(), Collections.reverse(), Collections.unmodifiableList()).
18. How do you make a collection immutable?
Collections.unmodifiableList(list) — wraps but still backed by mutable original (changes to original still show). List.of(...) (Java 9+) — creates a truly immutable list from scratch.
19. Queue vs Deque?
Queue — FIFO (insert at rear, remove from front), e.g., LinkedList, PriorityQueue.
Deque (double-ended queue) — insert/remove from both ends, can be used as either a queue or a stack (ArrayDeque is preferred over legacy Stack class for stack operations).
20. Why is Vector/Hashtable considered legacy compared to ArrayList/HashMap?
Vector and Hashtable are synchronized on every method call (thread-safe but slow, even in single-threaded use). ArrayList/HashMap are unsynchronized (faster by default); use Collections.synchronizedList() or concurrent collections when thread safety is actually needed.
