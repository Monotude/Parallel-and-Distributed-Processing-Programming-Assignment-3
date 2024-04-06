Part 1:
The concurrent linked list utilizes atomic operations for thread-safe updates, preventing data corruption during concurrent access by multiple servants. By avoiding explicit locking mechanisms the linked list is more efficient, enabling fast insertion, removal, and traversal operations even under heavy concurrent workload.

Part 2:
The program simulates temperature readings from 8 sensors and gives top 5 highest and lowest temperatures and the interval with the largest temperature difference. 8 threads are used with locks in correct areas to prevent data corruption.
