package com.vizalgo.primitives;

import java.util.HashMap;
import java.util.Map;

// A tree structure representing a dictionary.
public class Trie {
    private Map<Character, TrieNode> trie = new HashMap<Character, TrieNode>();
    public void add(String node) {
        if (node.isEmpty()) {
            return;
        }
        traverseForAdd(trie, 0, node);
    }

    // Traverse Trie to determine if the string is contained in the dictionary.
    public boolean contains(String node) {
        Map<Character, TrieNode> nodes = trie;
        char[] chars = node.toCharArray();
        TrieNode n = null;
        for (int i=0; i<node.length(); i++) {
            n = nodes.get(chars[i]);
            if (n == null) {
                return false;
            }
            nodes = n.subNodes;
        }
        if (n == null) {
            return false;
        }
        return n.words.contains(node);
    }

    // Util method to call recursively on subnodes to add a new node to the Trie.
    private static TrieNode traverseForAdd(Map<Character, TrieNode> trie, int index, String word) {
        TrieNode trieNode;
        char c = word.charAt(index);
        if (trie.containsKey(c)) {
            trieNode = trie.get(c);
        } else {
            trieNode = new TrieNode();
            trieNode.value = c;
            trie.put(c, trieNode);
        }
        index++;
        if (index == word.length()) {
            trieNode.words.add(word);
        } else {
            traverseForAdd(trieNode.subNodes, index, word);
        }
        return trieNode;
    }
}
