import java.util.*;
/** Implement these algorithms. docs/guide/CONTRACTS.md defines each field and limit. */
public final class Algorithms {
    /** Compare two versions of a sequence while preserving the order of shared characters. Implement lcs according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> lcs(Map<String,Object> input) {

        String a = (String) input.get("a");
        String b = (String) input.get("b");

        int n = a.length();
        int m = b.length();

        int[][] dp = new int[n + 1][m + 1];

        // Build the DP table
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {

                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(
                            dp[i - 1][j],
                            dp[i][j - 1]
                    );
                }
            }
        }

        // Reconstruct the LCS
        StringBuilder sequence = new StringBuilder();

        int i = n;
        int j = m;

        while (i > 0 && j > 0) {

            if (a.charAt(i - 1) == b.charAt(j - 1)) {

                sequence.append(a.charAt(i - 1));

                i--;
                j--;

            } else if (dp[i - 1][j] >= dp[i][j - 1]) {

                i--;

            } else {

                j--;
            }
        }

        sequence.reverse();

        Map<String,Object> result = new LinkedHashMap<>();

        result.put("length", dp[n][m]);
        result.put("sequence", sequence.toString());

        return result;
    }
    /** Plan the cheapest sequence of adjacent block merges when the original order must be preserved. Implement merge according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> merge(Map<String,Object> input) {

        List<Object> values = (List<Object>) input.get("sizes");

        int n = values.size();

        Map<String,Object> result = new LinkedHashMap<>();

        if (n <= 1) {
            result.put("cost", 0L);
            result.put("plan", new ArrayList<>());
            return result;
        }

        long[] prefix = new long[n + 1];

        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] +
                    ((Number) values.get(i)).longValue();
        }

        long[][] dp = new long[n][n];
        int[][] split = new int[n][n];

        // Interval Dynamic Programming
        for (int len = 2; len <= n; len++) {

            for (int l = 0; l + len <= n; l++) {

                int r = l + len - 1;

                dp[l][r] = Long.MAX_VALUE;

                long sum = prefix[r + 1] - prefix[l];

                for (int k = l; k < r; k++) {

                    long cost = dp[l][k]
                            + dp[k + 1][r]
                            + sum;

                    if (cost < dp[l][r]) {

                        dp[l][r] = cost;
                        split[l][r] = k;
                    }
                }
            }
        }

        // Reconstruct the chronological merging plan
        List<Object> plan = new ArrayList<>();

        class PlanBuilder {

            void build(int l, int r) {

                if (l == r) {
                    return;
                }

                int k = split[l][r];

                build(l, k);
                build(k + 1, r);

                plan.add(Arrays.asList(l, k, r));
            }
        }

        new PlanBuilder().build(0, n - 1);

        result.put("cost", dp[0][n - 1]);
        result.put("plan", plan);

        return result;
    }
    /** Choose a maximum-value set when neighbouring vertices are incompatible and conflicts form a tree. Implement tree according to docs/guide/CONTRACTS.md. */
    @SuppressWarnings("unchecked")
    static Map<String,Object> tree(Map<String,Object> input) {
        int n = ((Number) input.get("n")).intValue();

        List<Object> weightsList = (List<Object>) input.get("weights");
        List<Object> edges = (List<Object>) input.get("edges");

        Map<String,Object> result = new LinkedHashMap<>();

        if (n == 0) {
            result.put("weight", 0L);
            result.put("selected", new ArrayList<>());
            return result;
        }

        // Build adjacency list
        List<Integer>[] graph = new ArrayList[n];

        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }

        for (Object edgeObject : edges) {
            List<Object> edge = (List<Object>) edgeObject;

            int u = ((Number) edge.get(0)).intValue();
            int v = ((Number) edge.get(1)).intValue();

            graph[u].add(v);
            graph[v].add(u);
        }

        // Read vertex weights
        long[] weights = new long[n];

        for (int i = 0; i < n; i++) {
            weights[i] = ((Number) weightsList.get(i)).longValue();
        }

        // Iterative traversal
        int[] parent = new int[n];
        Arrays.fill(parent, -2);

        int[] order = new int[n];
        int[] stack = new int[n];

        int top = 0;
        int count = 0;

        stack[top++] = 0;
        parent[0] = -1;

        while (top > 0) {
            int u = stack[--top];
            order[count++] = u;

            for (int v : graph[u]) {
                if (v != parent[u]) {
                    parent[v] = u;
                    stack[top++] = v;
                }
            }
        }

        // Dynamic programming
        long[] take = new long[n];
        long[] skip = new long[n];

        for (int i = count - 1; i >= 0; i--) {
            int u = order[i];

            take[u] = weights[u];
            skip[u] = 0;

            for (int v : graph[u]) {
                if (parent[v] == u) {
                    take[u] += skip[v];
                    skip[u] += Math.max(take[v], skip[v]);
                }
            }
        }

        // Reconstruct selected vertices
        List<Integer> selected = new ArrayList<>();

        int[] nodeStack = new int[n];
        boolean[] parentChosenStack = new boolean[n];

        top = 0;
        nodeStack[top] = 0;
        parentChosenStack[top++] = false;

        while (top > 0) {
            top--;

            int u = nodeStack[top];
            boolean parentChosen = parentChosenStack[top];

            boolean choose = !parentChosen && take[u] > skip[u];

            if (choose) {
                selected.add(u);
            }

            for (int v : graph[u]) {
                if (parent[v] == u) {
                    nodeStack[top] = v;
                    parentChosenStack[top++] = choose;
                }
            }
        }

        result.put("weight", Math.max(take[0], skip[0]));
        result.put("selected", selected);

        return result;
    }
    /** Establish a simple exact-search baseline against which the other two searches can be checked. Implement naive according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> naive(Map<String,Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");

        List<Integer> positions = new ArrayList<>();

        int n = text.length();
        int m = pattern.length();

        // Empty pattern matches every position
        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                positions.add(i);
            }
        } else if (m <= n) {

            // Check every possible starting position
            for (int i = 0; i <= n - m; i++) {
                int j = 0;

                // Compare characters one by one
                while (j < m && text.charAt(i + j) == pattern.charAt(j)) {
                    j++;
                }

                // If the whole pattern matches
                if (j == m) {
                    positions.add(i);
                }
            }
        }

        Map<String,Object> result = new LinkedHashMap<>();
        result.put("positions", positions);

        return result;
    }
    /** Reuse information about matched prefixes to avoid restarting the search after every mismatch. Implement kmp according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> kmp(Map<String,Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");

        List<Integer> positions = new ArrayList<>();

        int n = text.length();
        int m = pattern.length();

        // Empty pattern matches every position
        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                positions.add(i);
            }
        } else if (m <= n) {

            // Build the LPS (Longest Prefix Suffix) array
            int[] lps = new int[m];

            int len = 0;
            int i = 1;

            while (i < m) {
                if (pattern.charAt(i) == pattern.charAt(len)) {
                    len++;
                    lps[i] = len;
                    i++;
                } else {
                    if (len != 0) {
                        len = lps[len - 1];
                    } else {
                        lps[i] = 0;
                        i++;
                    }
                }
            }

            // Search for the pattern in the text
            int textIndex = 0;
            int patternIndex = 0;

            while (textIndex < n) {
                if (text.charAt(textIndex) == pattern.charAt(patternIndex)) {
                    textIndex++;
                    patternIndex++;
                }

                if (patternIndex == m) {
                    positions.add(textIndex - patternIndex);

                    // Continue searching to find overlapping matches
                    patternIndex = lps[patternIndex - 1];

                } else if (textIndex < n &&
                        text.charAt(textIndex) != pattern.charAt(patternIndex)) {

                    if (patternIndex != 0) {
                        patternIndex = lps[patternIndex - 1];
                    } else {
                        textIndex++;
                    }
                }
            }
        }

        Map<String,Object> result = new LinkedHashMap<>();
        result.put("positions", positions);

        return result;
    }
    /** Reject windows with unequal fingerprints and verify every equal-hash candidate. Implement rk according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> rk(Map<String,Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");

        List<Integer> positions = new ArrayList<>();

        int n = text.length();
        int m = pattern.length();

        final int BASE = 31;
        final int MOD = 101;

        // Empty pattern matches every position
        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                positions.add(i);
            }
        } else if (m <= n) {

            // Calculate BASE^(m-1) modulo MOD
            long highestPower = 1;

            for (int i = 1; i < m; i++) {
                highestPower = (highestPower * BASE) % MOD;
            }

            // Calculate the initial pattern hash
            long patternHash = 0;

            for (int i = 0; i < m; i++) {
                patternHash = (patternHash * BASE + (int) pattern.charAt(i)) % MOD;
            }

            // Calculate the first text window hash
            long textHash = 0;

            for (int i = 0; i < m; i++) {
                textHash = (textHash * BASE + (int) text.charAt(i)) % MOD;
            }

            // Check every possible window
            for (int i = 0; i <= n - m; i++) {

                // Equal hashes are only candidates
                if (patternHash == textHash) {

                    boolean match = true;

                    // Verify actual characters to avoid hash collisions
                    for (int j = 0; j < m; j++) {
                        if (text.charAt(i + j) != pattern.charAt(j)) {
                            match = false;
                            break;
                        }
                    }

                    if (match) {
                        positions.add(i);
                    }
                }

                // Calculate the next window hash
                if (i < n - m) {
                    textHash = (textHash -
                            (int) text.charAt(i) * highestPower) % MOD;

                    if (textHash < 0) {
                        textHash += MOD;
                    }

                    textHash = (textHash * BASE +
                            (int) text.charAt(i + m)) % MOD;
                }
            }
        }

        Map<String,Object> result = new LinkedHashMap<>();
        result.put("positions", positions);

        return result;
    }
    /** Order the text suffixes and use adjacent common prefixes to find repeated substrings. Implement suffix according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> suffix(Map<String,Object> input) {
        String text = (String) input.get("text");
        int n = text.length();

        Map<String,Object> result = new LinkedHashMap<>();

        // Handle empty text
        if (n == 0) {
            result.put("sa", new ArrayList<Integer>());
            result.put("lcp", new ArrayList<Integer>());
            result.put("repeated", "");
            return result;
        }

        // Initial suffix positions and character ranks
        Integer[] suffixes = new Integer[n];
        int[] rank = new int[n];
        int[] newRank = new int[n];

        for (int i = 0; i < n; i++) {
            suffixes[i] = i;
            rank[i] = text.charAt(i);
        }

        // Prefix doubling algorithm
        for (int k = 1; k < n; k *= 2) {
            final int length = k;
            final int[] currentRank = rank;

            Arrays.sort(suffixes, (a, b) -> {
                if (currentRank[a] != currentRank[b]) {
                    return Integer.compare(currentRank[a], currentRank[b]);
                }

                int rankA = (a + length < n)
                        ? currentRank[a + length] : -1;

                int rankB = (b + length < n)
                        ? currentRank[b + length] : -1;

                return Integer.compare(rankA, rankB);
            });

            newRank[suffixes[0]] = 0;

            for (int i = 1; i < n; i++) {
                int previous = suffixes[i - 1];
                int current = suffixes[i];

                boolean different = rank[previous] != rank[current];

                int previousSecond = previous + k < n
                        ? rank[previous + k] : -1;

                int currentSecond = current + k < n
                        ? rank[current + k] : -1;

                if (previousSecond != currentSecond) {
                    different = true;
                }

                newRank[current] = newRank[previous]
                        + (different ? 1 : 0);
            }

            int[] temp = rank;
            rank = newRank;
            newRank = temp;

            if (rank[suffixes[n - 1]] == n - 1) {
                break;
            }

            if (k > n / 2) {
                break;
            }
        }

        // Convert suffix array to a regular list
        List<Integer> sa = new ArrayList<>();

        for (int suffix : suffixes) {
            sa.add(suffix);
        }

        // Kasai algorithm: build LCP array
        int[] inverse = new int[n];

        for (int i = 0; i < n; i++) {
            inverse[suffixes[i]] = i;
        }

        int[] lcpArray = new int[n];
        int h = 0;

        for (int i = 0; i < n; i++) {
            int position = inverse[i];

            if (position == 0) {
                h = 0;
                continue;
            }

            int j = suffixes[position - 1];

            while (i + h < n && j + h < n
                    && text.charAt(i + h) == text.charAt(j + h)) {
                h++;
            }

            lcpArray[position] = h;

            if (h > 0) {
                h--;
            }
        }

        // Find the longest repeated substring
        int maxLength = 0;
        int bestPosition = 0;

        List<Integer> lcp = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            lcp.add(lcpArray[i]);

            if (lcpArray[i] > maxLength) {
                maxLength = lcpArray[i];
                bestPosition = suffixes[i];
            }
        }

        String repeated = maxLength == 0
                ? ""
                : text.substring(bestPosition, bestPosition + maxLength);

        result.put("sa", sa);
        result.put("lcp", lcp);
        result.put("repeated", repeated);

        return result;
    }
    /** Build one dictionary automaton and count its patterns in several independent texts. Implement aho according to docs/guide/CONTRACTS.md. */
    @SuppressWarnings("unchecked")
    static Map<String,Object> aho(Map<String,Object> input) {
        List<Object> patterns = (List<Object>) input.get("patterns");
        List<Object> texts = (List<Object>) input.get("texts");

        int p = patterns.size();

        // Count total pattern length
        int totalLength = 0;

        for (Object pattern : patterns) {
            totalLength += ((String) pattern).length();
        }

        // Each trie state represents one prefix
        final int ALPHABET = 95;
        final int OFFSET = 32;

        int maxStates = totalLength + 1;

        int[][] next = new int[maxStates][ALPHABET];

        for (int i = 0; i < maxStates; i++) {
            Arrays.fill(next[i], -1);
        }

        int[] fail = new int[maxStates];
        int[] terminal = new int[p];

        int states = 1;

        // Build the trie
        for (int i = 0; i < p; i++) {
            String pattern = (String) patterns.get(i);

            int current = 0;

            for (int j = 0; j < pattern.length(); j++) {
                int c = pattern.charAt(j) - OFFSET;

                if (next[current][c] == -1) {
                    next[current][c] = states++;
                }

                current = next[current][c];
            }

            // Duplicate patterns share the same terminal state
            terminal[i] = current;
        }

        // Build failure links using BFS
        int[] queue = new int[states];
        int[] order = new int[states];

        int head = 0;
        int tail = 0;
        int orderCount = 0;

        order[orderCount++] = 0;

        // Initialize root transitions
        for (int c = 0; c < ALPHABET; c++) {
            int child = next[0][c];

            if (child != -1) {
                fail[child] = 0;
                queue[tail++] = child;
            } else {
                next[0][c] = 0;
            }
        }

        // Process all trie states
        while (head < tail) {
            int u = queue[head++];

            order[orderCount++] = u;

            for (int c = 0; c < ALPHABET; c++) {
                int v = next[u][c];

                if (v != -1) {
                    fail[v] = next[fail[u]][c];
                    queue[tail++] = v;
                } else {
                    next[u][c] = next[fail[u]][c];
                }
            }
        }

        // Process every text independently
        List<Object> allCounts = new ArrayList<>();

        for (Object textObject : texts) {
            String text = (String) textObject;

            // Count visits to automaton states
            int[] visits = new int[states];

            int current = 0;

            for (int i = 0; i < text.length(); i++) {
                int c = text.charAt(i) - OFFSET;

                current = next[current][c];
                visits[current]++;
            }

            // Propagate counts through failure links
            for (int i = orderCount - 1; i > 0; i--) {
                int u = order[i];
                visits[fail[u]] += visits[u];
            }

            // Build result in original dictionary order
            List<Integer> row = new ArrayList<>();

            for (int i = 0; i < p; i++) {
                row.add(visits[terminal[i]]);
            }

            allCounts.add(row);
        }

        Map<String,Object> result = new LinkedHashMap<>();
        result.put("counts", allCounts);

        return result;
    }
}
