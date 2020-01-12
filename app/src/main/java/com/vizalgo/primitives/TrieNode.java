package com.vizalgo.primitives;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TrieNode {
    // TODO: Lookup Java property equivalent (Kotlin?)
    public char value;
    public Map<Character, TrieNode> subNodes = new HashMap<Character, TrieNode>();
    public List<String> words = new LinkedList<String>();
}
