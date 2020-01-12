package com.vizalgo.primitives;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrieNode {
    // TODO: Lookup Java property equivalent (Kotlin?)
    public char value;
    public Map<Character, TrieNode> subNodes = new HashMap<Character, TrieNode>();
    public Set<String> words = new HashSet<>();
}
