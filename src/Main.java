// Sapozhnikov Arkady
// Haffman
// 10.12.2019

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;


public class Main {

    private static final int SIZE = 256;
    private static Node[] tree;
    private static String[] codes;
    private static int[] freq;
    private static int M;
    private static ArrayList<Integer> symbols;
    private static ArrayList<Integer> symbolsSet;

    public static void main(String[] args) throws IOException {
        zipper();
        unZipper();
        checking();
    }

    private static void unZipper() throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                new FileInputStream("zip.txt"));
        PrintWriter printWriter = new PrintWriter(
                new FileOutputStream("output.txt"));
        M = 0;
        freq = new int[SIZE];
        freqFilling(bufferedInputStream);
        tree = new Node[2 * M - 1];
        treeFiling();
        lineProcessing(bufferedInputStream, printWriter);
    }

    private static void freqFilling(BufferedInputStream bufferedInputStream) throws IOException {
        M = read(bufferedInputStream);
        for (int i = 0; i < M; i++) {
            int symbol = read(bufferedInputStream);
            int number = read(bufferedInputStream);
            freq[symbol] = number;
        }
    }

    private static int read(BufferedInputStream bufferedInputStream) throws IOException {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) bufferedInputStream.read();
        }
        return (ByteBuffer.wrap(bytes)).getInt();
    }

    private static void lineProcessing(BufferedInputStream bufferedInputStream, PrintWriter printWriter) throws IOException {
        int x = bufferedInputStream.read();
        while (x != -1) {
            String line = Integer.toBinaryString(x);
            line = addNulls(8 - line.length()) + line;
            char[] charsOfLine = line.toCharArray();
            printWriter.print((char) findSymb(charsOfLine, 0, tree.length - 1));
            x = bufferedInputStream.read();
        }
        bufferedInputStream.close();
        printWriter.close();
    }

    private static int findSymb(char[] chars, int charsIndex, int treeIndex) {
        if (tree[treeIndex].symb >= 0) {
            return tree[treeIndex].symb;
        }
        if (chars[charsIndex] == '0') {
            return findSymb(chars, charsIndex + 1, tree[treeIndex].left);
        } else {
            return findSymb(chars, charsIndex + 1, tree[treeIndex].right);
        }
    }

    private static void zipper() throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                new FileInputStream("input.txt"));
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                new FileOutputStream("zip.txt"));
        freq = new int[SIZE];
        symbolsSet = new ArrayList<>();
        symbols = new ArrayList<>();
        freqFillingZip(bufferedInputStream);
        tree = new Node[2 * M - 1];
        treeFiling();
        codes = new String[256];
        for (int i = 0; i < 2 * M - 1; i++) {
            setCodes(i);
        }
        outputZip(bufferedOutputStream);
    }

    private static void treeFiling() {
        int len = 0;
        for (int i = 0; i < SIZE; i++) {
            if (freq[i] != 0) {
                tree[len] = new Node(freq[i], 0, 0, false, i, "");
                len++;
            }
        }
        for (int i = 0; i < M; i++) {
            boolean flag = false;
            int min1 = -1;
            for (int j = 0; j < len; j++) {
                if (!tree[j].used) {
                    if (min1 < 0 || tree[min1].freq >= tree[j].freq) {
                        flag = true;
                        min1 = j;
                    }
                }
            }
            if (flag) {
                tree[min1].used = true;
            }
            flag = false;
            int min2 = -1;
            for (int j = 0; j < len; j++) {
                if (!tree[j].used) {
                    if (min2 < 0 || tree[min2].freq >= tree[j].freq) {
                        flag = true;
                        min2 = j;
                    }
                }
            }
            if (flag) {
                tree[min2].used = true;
                tree[len] = new Node(tree[min1].freq + tree[min2].freq, min1, min2, false, -1, "");
                len++;
            }
        }
    }

    private static void freqFillingZip(BufferedInputStream bufferedInputStream)
            throws IOException {
        int x = bufferedInputStream.read();
        M = 0;
        while (x != -1) {
            if (freq[x] == 0) {
                symbolsSet.add(x);
                M += 1;
            }
            symbols.add(x);
            freq[x] += 1;
            x = bufferedInputStream.read();
        }
        bufferedInputStream.close();
    }

    private static void setCodes(int num) {
        if (tree[num].symb >= 0) {
            codes[tree[num].symb] = tree[num].code;
            return;
        }
        tree[tree[num].left].code = tree[num].code + "0";
        tree[tree[num].right].code = tree[num].code + "1";
        setCodes(tree[num].left);
        setCodes(tree[num].right);
    }

    private static void outputZip(BufferedOutputStream bufferedOutputStream) throws IOException {
        printInt(M, bufferedOutputStream);
        for (Integer symbol : symbolsSet) {
            printInt(symbol, bufferedOutputStream);
            printInt(freq[symbol], bufferedOutputStream);
        }
        for (Integer symbol : symbols) {
//            1 byte always
            bufferedOutputStream.write(Integer.parseInt(codes[symbol] + addNulls(8 - codes[symbol].length()), 2));
        }
        bufferedOutputStream.close();
    }

    private static String addNulls(int k) {
        return "0".repeat(Math.max(0, k));
    }

    private static void printInt(Integer x, BufferedOutputStream bufferedOutputStream) throws IOException {
//        byte[] bytes = ByteBuffer.allocate(4).putInt(x).array();
        bufferedOutputStream.write(x >> 24);
        bufferedOutputStream.write((x << 8) >> 24);
        bufferedOutputStream.write((x << 16) >> 24);
        bufferedOutputStream.write((x << 24) >> 24);
    }

    private static void checking() throws FileNotFoundException {
        Scanner scanner1 = new Scanner(new File("input.txt"));
        Scanner scanner2 = new Scanner(new File("output.txt"));
        System.out.println(scanner1.nextLine().equals(scanner2.nextLine()));
    }

    static class Node {
        int freq;
        int left;
        int right;
        boolean used;
        int symb;
        String code;

        Node(int newFreq, int newRight, int newLeft, boolean newUsed, int newSymb, String newCode) {
            freq = newFreq;
            left = newLeft;
            right = newRight;
            used = newUsed;
            symb = newSymb;
            code = newCode;
        }
    }
}
