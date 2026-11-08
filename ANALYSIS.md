# Assignment 4 — analysis (complete in English)
## Environment and commands

**Selected language:** Java

**Java version:** OpenJDK 17.0.17 (Eclipse Temurin).

**Operating system:** Windows (64-bit).

**Development environment:** IntelliJ IDEA.

**CPU:** 12th Gen Intel(R) Core(TM) i3-1215U.

**RAM:** 7.71 GB.

**Build and test commands:**

```bash
py -3 tools/run.py --setup
py -3 tools/run.py --check
```

**Example execution:**

```bash
py -3 tools/run.py --input examples/lcs.jsonl
```

**Test results:** All 44 public test cases passed, including the state-reset test that runs two complete batches in one process.

**Final commit URL:** To be added after the final commit is pushed to GitHub.
## Algorithm explanations
### 1. LCS (Longest Common Subsequence)

**State and meaning of indices**

Let `A` and `B` be the two input strings. Define `dp[i][j]` as the length of the longest common subsequence of the prefixes `A[0..i-1]` and `B[0..j-1]`. The indices `i` and `j` represent prefix lengths, not character positions.

**Transition and base cases**

The base cases are `dp[0][j] = 0` and `dp[i][0] = 0`, because an empty string has no common subsequence with another string.

If `A[i-1] == B[j-1]`, then:

`dp[i][j] = dp[i-1][j-1] + 1`

Otherwise:

`dp[i][j] = max(dp[i-1][j], dp[i][j-1])`

**Processing order**

The table is filled from top to bottom and left to right. Each cell depends only on the previous row, previous column, or previous diagonal, so all required values are already computed.

**Correctness argument**

For matching final characters, an optimal common subsequence can include that character after an optimal subsequence of the two preceding prefixes. For different final characters, an optimal solution must exclude at least one of them. The algorithm checks both possibilities and selects the larger result. Therefore, by induction over prefix lengths, every table cell contains the correct LCS length.

**Reconstruction and ties**

Reconstruction starts at `dp[n][m]`. If the current characters match, that character is added to the answer and both indices decrease. Otherwise, the algorithm moves toward the neighboring cell with the larger DP value. When both values are equal, the implementation follows its chosen deterministic branch. The collected characters are reversed at the end.

**Complexity**

For strings of lengths `n` and `m`, the algorithm uses `O(nm)` time and `O(nm)` memory. The output subsequence has length at most `min(n,m)`. The implementation uses a two-dimensional integer array for the DP table.

**Boundary test**

An empty input string paired with a non-empty string must return length `0` and an empty sequence. This detects incorrect DP initialization or reconstruction at the table boundary.
### 2. Merge (Optimal Merge Pattern)

**State and meaning of indices**

Let the input contain `n` weights. Define `dp[l][r]` as the minimum total cost of merging all consecutive elements in the interval from index `l` to index `r`, inclusive. A split index `k` separates the interval into `[l..k]` and `[k+1..r]`.

**Transition and base cases**

For an interval containing one element, no merge is required:

`dp[i][i] = 0`

For a longer interval, try every possible split:

`dp[l][r] = min(dp[l][k] + dp[k+1][r] + sum(l,r))`

Here, `sum(l,r)` is the total weight of the interval. Prefix sums are used to calculate interval sums in constant time.

**Processing order**

Intervals are processed by increasing length. When calculating an interval, both smaller subintervals have already been calculated. Therefore, every candidate split uses available DP values.

**Correctness argument**

Every valid merge plan for an interval has a final merge operation. That operation divides the interval into two consecutive subintervals at some split `k`. The two subintervals must each be merged optimally; otherwise, replacing a non-optimal subplan with a better one would reduce the total cost. The recurrence considers every possible final split and adds the cost of the final merge. Thus, it finds the optimal cost.

**Reconstruction and ties**

The implementation stores the best split index in a separate `split` table. Reconstruction recursively visits the left and right subintervals and then records their merge. If multiple splits have equal cost, the implementation keeps the first split encountered because it updates the answer only when a strictly smaller cost is found. This makes the plan deterministic.

**Complexity**

There are `O(n²)` intervals and up to `O(n)` possible splits per interval, giving `O(n³)` time. The DP and split tables require `O(n²)` memory. Prefix sums require `O(n)` additional memory. The output plan contains `n-1` merge operations for a non-empty input.

**Boundary test**

An empty input must return cost `0` and an empty plan. A one-element input must also return cost `0` and an empty plan. These cases detect invalid interval boundaries and incorrect base-case handling.
### 3. Tree (Maximum Weight Independent Set)

**State and meaning of indices**

The input is a weighted tree with `n` vertices. For each vertex `u`, define two DP states:

- `take[u]`: maximum weight of a valid independent set in the subtree rooted at `u` when vertex `u` is selected.
- `skip[u]`: maximum weight when vertex `u` is not selected.

An independent set cannot contain two vertices connected by an edge.

**Transition and base cases**

If vertex `u` is selected, none of its children can be selected:

`take[u] = weight[u] + sum(skip[v])`

If vertex `u` is not selected, each child can either be selected or skipped:

`skip[u] = sum(max(take[v], skip[v]))`

The initial values for a leaf are `take[u] = weight[u]` and `skip[u] = 0`, because a leaf has no children.

**Processing order**

The implementation first performs an iterative DFS to establish parent relationships and a traversal order. It then processes vertices in reverse DFS order. This guarantees that each child is processed before its parent, so all child DP states are ready.

**Correctness argument**

For every subtree, a valid solution either includes its root or excludes it. If the root is included, all children must be excluded. If the root is excluded, each child subtree can independently choose its better state. These two cases cover all valid independent sets. By induction from leaves to the root, the DP computes the optimal value for every subtree.

**Reconstruction and ties**

Starting at the root, the algorithm follows the stored DP values. If a parent is selected, all its children must be skipped. Otherwise, each child is selected only when `take[child]` is strictly greater than `skip[child]`. On equal values, the implementation skips the child. This produces a deterministic valid selection.

**Complexity**

The iterative DFS and DP each visit every vertex and edge a constant number of times. For `n` vertices, time complexity is `O(n)` and memory complexity is `O(n)`, including adjacency lists, parent/order arrays, and DP states. The selected-vertex output contains at most `n` indices.

**Boundary test**

An empty tree must return weight `0` and an empty selection. A tree with negative vertex weights checks that the algorithm can correctly choose no vertices rather than selecting a harmful vertex.
### 4. Naive String Matching

**State and meaning of indices**

Let `text` have length `n` and `pattern` have length `m`. The index `i` is a candidate starting position in the text. The index `j` is the current character position inside the pattern.

**Transition and base cases**

For every valid starting position `i` from `0` to `n-m`, compare characters while `j < m` and `text[i+j] == pattern[j]`. If all `m` characters match, record `i`.

If the pattern is longer than the text, there are no matches. If the pattern is empty, every boundary position from `0` through `n` is a match.

**Processing order**

Candidate starting positions are checked from left to right. Within each candidate, characters are compared from the beginning of the pattern to its end. Every candidate is checked independently.

**Correctness argument**

The algorithm examines every possible starting position where a pattern of length `m` can fit. It records a position only after all pattern characters match the corresponding text characters. Therefore, it reports every valid occurrence and no invalid occurrence.

**Overlapping matches**

The next candidate position is checked even after a match is found. Consequently, overlapping occurrences are not skipped. For example, searching for `ana` in `banana` must return positions `1` and `3`.

**Complexity**

The worst-case time complexity is `O(nm)`, because up to `m` character comparisons are performed at each of up to `n-m+1` positions. Auxiliary memory is `O(1)`, excluding the output list. The output requires `O(z)` memory for `z` matches.

**Boundary test**

Searching for `aa` in `aaaa` must return positions `0`, `1`, and `2`. This detects implementations that incorrectly skip overlapping matches.
### 5. KMP (Knuth–Morris–Pratt)

**State and meaning of indices**

Let `text` have length `n` and `pattern` have length `m`. The algorithm uses:

- `i`: current index in the text.
- `j`: number of pattern characters currently matched.
- `lps[j]`: length of the longest proper prefix of `pattern[0..j]` that is also a suffix of that substring.

A proper prefix is a prefix that is shorter than the whole string.

**Transition and base cases**

First, the LPS array is constructed. If two characters match, the LPS construction advances both indices. If they differ and the current prefix length is positive, the algorithm falls back to the previous LPS value. Otherwise, it advances to the next pattern position.

During searching, matching characters advance both `i` and `j`. When `j == m`, a match is recorded at `i - m`, and `j` is reset to `lps[j - 1]` to allow overlapping matches. On a mismatch, the algorithm uses the LPS value to avoid rechecking characters already known to match.

An empty pattern is handled separately and matches all text boundaries from `0` to `n`.

**Processing order**

The LPS array is built from left to right before searching begins. The search then scans the text from left to right. Each fallback uses an already calculated LPS value.

**Correctness argument**

The LPS value identifies the longest prefix of the pattern that can still match the suffix of the text already examined. After a mismatch, any shorter possible match must be a border of that prefix. Following LPS links checks these possible borders without skipping a valid occurrence. When all `m` pattern characters match, the corresponding starting position is a valid occurrence. Therefore, the algorithm reports all matches.

**Reconstruction and ties**

KMP does not reconstruct a separate solution. It records each occurrence position. After a full match, it falls back using `lps[m-1]`, rather than resetting to zero, so overlapping matches are preserved.

**Complexity**

Building the LPS array takes `O(m)` time and `O(m)` memory. Searching takes `O(n)` time because the text index only moves forward and pattern fallbacks are amortized linear. Total time complexity is `O(n+m)`. Auxiliary memory is `O(m)`, excluding the output list, which requires `O(z)` for `z` matches.

**Boundary test**

Searching for `aa` in `aaaa` must return positions `0`, `1`, and `2`. This checks whether the implementation correctly falls back after a match and preserves overlapping occurrences.
### 6. RK (Rabin–Karp)

**State and meaning of indices**

Let `n` be the text length and `m` the pattern length. The algorithm maintains:

- `i`: starting position of the current text window.
- `patternHash`: hash value of the pattern.
- `windowHash`: hash value of the current text window.
- `power`: the highest positional power used when removing the outgoing character.

The implementation uses base `31` and modulus `101`.

**Transition and base cases**

The pattern hash and the first text-window hash are calculated using a polynomial rolling hash. When the window moves one position to the right, the outgoing character is removed, the remaining hash is multiplied by the base, and the incoming character is added. The result is reduced modulo `101`.

A hash match is only a candidate match. The implementation compares the actual characters before recording an occurrence. If the pattern is empty, every text boundary is returned. If the pattern is longer than the text, there are no matches.

**Processing order**

The initial hashes are calculated first. Then the text windows are processed from left to right. Each new hash is calculated from the previous window hash, avoiding a complete hash recomputation.

**Correctness argument**

Every possible text window of length `m` is examined. If its hash differs from the pattern hash, the two strings cannot be equal under the same hash function. If the hashes are equal, direct character verification determines whether the strings are actually equal. Thus, hash collisions cannot create false-positive matches, and every real match is reported.

**Collision handling**

The modulus `101` is deliberately small, so different strings can have the same hash. The implementation verifies characters after every hash match. The `rk-collision` test checks that a collision does not produce an incorrect reported position.

**Complexity**

Hash initialization takes `O(m)` time, and rolling through the text takes `O(n)` expected time when candidate verifications are limited. However, the worst-case time complexity is `O(nm)` because many windows may have the same hash and require character-by-character verification. Auxiliary memory is `O(1)`, excluding the output list, which requires `O(z)` for `z` matches.

The candidate-sensitive search cost is `O(n + c*m)`, where `c` is the number of windows that require direct verification. This explains why a small modulus can increase the number of candidate checks.

**Boundary test**

The collision test uses different strings that produce the same hash. The expected result must contain only actual matches. This detects an implementation that incorrectly treats hash equality as proof of string equality.
### 7. Suffix Array and Kasai's LCP Algorithm
#### Suffix Array construction

**State and meaning of indices**

For a string of length `n`, each suffix is identified by its starting index `i`. The suffix is `text[i..n-1]`.

The implementation maintains:
- `suffixes`: starting indices of suffixes in sorted order.
- `rank[i]`: the current equivalence-class rank of the suffix starting at `i`.
- `newRank[i]`: the updated rank after processing the current doubling step.
- `k`: the number of characters considered in each ranking step.

**Transition and base cases**

Initially, each suffix is ranked by its first character. Then the algorithm repeatedly sorts suffixes using pairs of ranks:

`(rank[i], rank[i+k])`

If the second part is outside the string, its rank is treated as `-1`.

After sorting, new ranks are assigned. Two suffixes receive different ranks if their rank pairs differ. The value of `k` is doubled after each iteration.

An empty string produces an empty suffix array.

**Processing order**

The algorithm begins with one-character prefixes and doubles the compared prefix length in every iteration. Sorting and rank assignment are repeated until all suffixes have distinct ranks or the compared length covers the string.

**Correctness argument**

At each iteration, the rank represents the lexicographical order of the first `k` characters of each suffix, with shorter suffixes ordered before longer suffixes when their prefixes are equal. Sorting pairs of ranks therefore produces the correct order for prefixes of length up to `2k`. By repeatedly doubling the prefix length, the algorithm eventually distinguishes and correctly orders all suffixes.

**Complexity**

There are `O(log n)` doubling iterations. Each iteration sorts `n` suffix indices using Java's `Arrays.sort` on an `Integer[]` with a comparator. Each sort takes `O(n log n)` comparisons, so the total construction time is `O(n log² n)`. The rank arrays and suffix array require `O(n)` memory, excluding sorting implementation overhead.
#### Kasai's LCP construction

**State and meaning of indices**

After constructing the suffix array, the algorithm builds an inverse array where `inverse[i]` gives the position of suffix `i` in the suffix array.

`lcp[position]` stores the length of the longest common prefix between the suffix at `suffixes[position]` and the previous suffix in sorted order. The first LCP value is zero.

**Transition and processing order**

The algorithm visits suffixes in their original text order. For each suffix, it finds its position in the suffix array and compares it with the previous suffix in sorted order.

A variable `h` stores the length of the common prefix already known from the previous iteration. After processing a suffix, `h` is decreased by one if it is positive.

**Correctness argument**

When moving from suffix `i` to suffix `i+1` in the original text, a common prefix of length `h` for the previous suffix pair implies that at least `h-1` characters can be reused for the next comparison. The algorithm extends this known prefix until the characters differ. This avoids unnecessary repeated comparisons and computes every adjacent LCP value correctly.

**Repeated substring**

The implementation finds the maximum value in the LCP array. The corresponding suffix's starting index identifies a longest repeated substring. If several maximum values are equal, the first maximum encountered in suffix-array order is selected.

**Complexity**

Kasai's algorithm takes `O(n)` time because the total number of successful character extensions is linear, and the stored prefix length decreases by at most one between iterations. It uses `O(n)` additional memory for the inverse and LCP arrays.

The complete suffix operation takes `O(n log² n)` time due to the sorting-based doubling implementation and `O(n)` auxiliary memory, excluding sorting overhead and output storage.

**Boundary test**

An empty string must produce empty suffix and LCP arrays and an empty repeated substring. A string such as `banana` checks suffix ordering and repeated-substring detection.
### 8. Aho–Corasick

**State and meaning of indices**

The algorithm searches for multiple patterns in a collection of texts using a trie and failure links.

Each trie state represents a prefix of one or more patterns. The implementation uses:
- `next[state][c]`: transition for a character.
- `fail[state]`: failure link to the longest proper suffix that is also a trie prefix.
- `terminal[i]`: trie state representing the end of pattern `i`.
- `visits[state]`: number of times a text scan reaches that state.

The implementation uses a fixed alphabet of 95 printable ASCII characters.

**Transition and base cases**

All patterns are inserted into the trie. Each new character creates a state if the required transition does not exist.

The root state represents the empty prefix. Failure links are initialized using breadth-first search. For each state and character:
- If a trie transition exists, its failure link is assigned using the failure state's transition.
- Otherwise, the missing transition is replaced by the corresponding transition from the failure state.

This creates a complete transition table.

**Processing order**

Failure links are constructed in breadth-first order. This guarantees that the failure link of a state is ready before its children are processed.

For each text, the algorithm scans characters from left to right and follows the automaton transitions. It increments the visit count of every reached state.

After scanning, states are processed in reverse BFS order. Each state's visit count is added to its failure state. This propagates occurrences to suffix states.

**Correctness argument**

The trie represents every pattern prefix. Failure links preserve the longest suffix of the current matched text that is also a trie prefix. Therefore, every pattern occurrence corresponds to reaching a terminal state or one of its suffix-related states.

The reverse BFS propagation adds each state's visits to its failure ancestors. Consequently, terminal states receive the total number of occurrences of their patterns, including occurrences that end at suffix states. Thus, all pattern counts are calculated correctly.

**Suffix patterns and duplicates**

A pattern can be a suffix of another pattern. Failure links allow the algorithm to count both patterns when they end at the same text position.

Duplicate patterns share the same terminal trie state. Each original pattern position uses that terminal state when producing its result, so duplicate patterns receive identical occurrence counts in their original input positions.

**Reset between texts**

The trie and failure links are built once for the input request. The visit-count array is initialized separately for each text. This prevents matches from one text from affecting the counts of another text.

The implementation also creates fresh automaton data for each request, preventing state from leaking between separate requests.

**Complexity**

Let `S` be the number of trie states, `P` the total length of all patterns, `A = 95` the alphabet size, and `T` the total length of all texts.

Trie construction takes `O(P)` time. Initializing and completing the dense transition table and building failure links takes `O(S × A)` time. Scanning all texts takes `O(T)` time. Visit propagation takes `O(S)` time per text.

For `q` texts, the total time is `O(P + S × A + T + q × S)`. Memory usage is `O(S × A + P + q × p)` when storing the output counts for `p` patterns across `q` texts. The automaton itself uses `O(S × A)` memory.

**Boundary test**

An empty dictionary must return an empty count row for each text. A test with overlapping patterns, duplicate patterns, and suffix patterns checks whether the algorithm correctly counts all occurrences and preserves the original pattern order.
### Own tests
I created an additional test file: `tests/student/own.jsonl`.

Command used:

`py -3 tools/run.py --input tests/student/own.jsonl`

All 11 test requests completed successfully.

| Algorithm | Test case | Actual result |
|---|---|---|
| LCS | Repeated characters | Length 2, sequence "aa" |
| LCS | No common characters | Length 0, empty sequence |
| Merge | Four zero-sized blocks | Cost 0, valid chronological plan |
| Merge | Large block sizes | Cost 5,000,000,000 |
| Tree | All negative weights | Weight 0, empty selection |
| Tree | Three-vertex chain | Weight 2, vertices [0, 2] |
| Naive | Overlapping matches | Positions [0, 1, 2] |
| KMP | Overlapping matches | Positions [0, 1, 2] |
| RK | Hash collision check | Position [0], no false match |
| Suffix | Repeated characters | SA [3, 2, 1, 0], LCP [0, 1, 2, 3] |
| Aho | Duplicate and suffix patterns | Counts [[4, 3, 4], [0, 0, 0]] |

The actual outputs matched the expected results for all test cases.
### Search comparison
I compared three string-search algorithms: Naive, KMP, and Rabin–Karp.

**Test environment and methodology:**
- Programming language: Java 17
- Input sizes: 500, 1500, and 3000 characters
- Pattern length: one third of the text length
- Data types: random strings and repetitive strings
- Random seed: 42
- Measurements: 5 timed runs per algorithm and test case
- Reported value: median execution time in milliseconds
- Java process was kept alive during measurements.
- Compilation and initial JVM startup were excluded.
- Timing includes JSON request/response processing.
- All three algorithms were checked for identical results before timing.

**Results:**

| Data type | Text size | Naive (ms) | KMP (ms) | Rabin–Karp (ms) |
|---|---:|---:|---:|---:|
| Random | 500 | 0.3071 | 0.3039 | 0.3458 |
| Random | 1500 | 0.3468 | 0.4130 | 0.4795 |
| Random | 3000 | 0.7105 | 0.8387 | 0.7723 |
| Repetitive | 500 | 1.3106 | 0.2606 | 0.2908 |
| Repetitive | 1500 | 1.2232 | 0.5645 | 0.4373 |
| Repetitive | 3000 | 3.4246 | 0.4074 | 0.3080 |

**Analysis:**

On random strings, all three algorithms showed relatively close performance. Naive was competitive on these small inputs.

On repetitive strings, Naive became noticeably slower because it repeatedly compared long matching prefixes before finding a mismatch. KMP performed more consistently because it uses the prefix-function to avoid unnecessary comparisons. Rabin–Karp was also fast on these test cases.

The results demonstrate that algorithm performance depends on input characteristics. KMP and Rabin–Karp performed better than Naive on the selected repetitive inputs, while no single algorithm was the fastest in every random test.

The benchmark results are saved in `tests/student/benchmark_results.csv`.
### Sources and AI assistance
**Sources used:**
- Official Assignment 4 README and instructions.
- Assignment 4 `CONTRACTS.md` — algorithm requirements, input constraints, and output formats.
- Assignment 4 `TESTING.md` — recommended edge cases and testing requirements.
- Course lecture materials on dynamic programming, graph algorithms, string matching, suffix arrays, and Aho-Corasick.

**AI assistance:**

ChatGPT was used as a learning and debugging assistant to clarify algorithm concepts, review implementation ideas and explain complexity.

I independently verified the implementations using the public checker, additional test cases, and performance measurements. The results were reviewed and compared with the expected behavior.