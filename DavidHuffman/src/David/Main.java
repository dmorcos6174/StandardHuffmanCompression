package David;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

public class  Main {

    public static Map<Character, String> lookupTableStatic = new HashMap<>();



    final static int ASCII_SIZE = 256;
    ///////////////////////////////////////////////////////////////////
    static class Node implements Comparable<Node> {//class that defines attributes and properties of the Node
        private final char symbol;
        private final int freq;
        private final Node lNode;
        private final Node rNode;


        Node(final char symbol,final int freq,final Node lNode,final Node rNode) {
            this.symbol = symbol;
            this.freq = freq;
            this.lNode = lNode;
            this.rNode = rNode;
        }

        boolean isLeaf(){//method that checks left and right children to see if node is a leaf
            return this.lNode == null && this.rNode == null;
        }

        @Override       //etdaret a3melaha override 3shan el comparison makansh sha8al kwayes
        public int compareTo(Node o) {//compareTo method was overriden to be able to compare Nodes based on their frequencies
            final int freqCompare = Integer.compare(this.freq, o.freq);
            if(freqCompare != 0){//if the frequencies are not equal
                return freqCompare;
            }
            //ensures tree is always the same(deterministic)
            return Integer.compare(this.symbol, o.symbol);
        }
    }
    ////////////////////////////////////////////////////////////////////////
    public HuffmanEncodedResult compress(final String data){

        final int[] freq = buildFreqTable(data);//build the frequency table for the symbols in the string
        final Node root = buildTree(freq);//build the huffman tree using the frequency table
        final Map<Character, String> lookupTable = LookupTableBuild(root);//map every character with it's binary coded representation in the lookup table hashmap

        return new HuffmanEncodedResult(generateData(data, lookupTable), root);
    }

    ////////////////////COMPRESS HELPER METHODS//////////////////////////////////////

    private String generateData(String data, Map<Character, String> lookupTable) {//method that generates the string of bits using the lookup Table built previously
        final StringBuilder builder = new StringBuilder();
        for(final char character: data.toCharArray()){
            builder.append(lookupTable.get(character));
        }
        return builder.toString();
    }

    private static int[] buildFreqTable(final String data){
        final int[] freq = new int[ASCII_SIZE];

        for(final char character : data.toCharArray()){
            freq[character]++;
        }

        return freq;
    }

    private static Node buildTree(int[] frequency){//builds the tree in terms of a priority queue of nodes
        final PriorityQueue<Node> priorityQueue = new PriorityQueue<>();

        for(char i = 0 ; i < ASCII_SIZE; i++){
            if(frequency[i] > 0){//if frequency of a character is more than one, a node is added for it in the priority queue
                priorityQueue.add(new Node(i, frequency[i], null, null));
            }
        }

        if(priorityQueue.size() == 1){//in-case string consists of only one symbol
            priorityQueue.add(new Node('\0',1 ,null, null));
        }

        while(priorityQueue.size() > 1){//builds the actual tree by adding up the least two nodes (in terms of frequency) into one node, until one node is left which is the root node
            final Node left = priorityQueue.poll();
            final Node right = priorityQueue.poll();
            final Node parent = new Node('\0', left.freq + right.freq, left, right);
            priorityQueue.add(parent);
        }

        return priorityQueue.poll();//returns the root node
    }

    private static Map<Character,String> LookupTableBuild(final Node root){//builds the lookup table for the symbols (as keys) providing the binary-coded representation as a value
        final Map<Character, String> lookupTable = new HashMap<>();

        buildLookupTableRec(root, "", lookupTable);//recursive function that assigns binary values for each symbol according to it's location on the tree by adding 0s and 1s recursively according to position
        lookupTableStatic = lookupTable;
        return lookupTable;
    }

    private static void buildLookupTableRec(Node node,
                                            String s,
                                            Map<Character, String> lookupTable) {
        if(!node.isLeaf()){
            buildLookupTableRec(node.lNode, s + '0', lookupTable);
            buildLookupTableRec(node.rNode, s + '1', lookupTable);
        }
        else{
            lookupTable.put(node.symbol, s);
        }
    }

    ////////////////////////END COMPRESS HELPER METHODS///////////////////////////////////
    public String decompress(final HuffmanEncodedResult result){//decompress method traverses the huffman tree using a while loop limited by the length of the encoded bit string, keeps traversing until it finds a leaf,
        StringBuilder resultStringBuilder = new StringBuilder();//then appends the symbol of this leaf to the string builder, then resets the node current to the root, to traverse fro the next symbol
        Node current = result.getRoot();

        int i = 0;
        while(i < result.getData().length()){
            while(!current.isLeaf()){
                char bit = result.getData().charAt(i);
                if(bit == '1'){
                    current = current.rNode;
                }
                else if(bit == '0'){
                    current = current.lNode;
                }
                else{
                    throw new IllegalArgumentException("Invalid Bit" + bit);
                }
                i++;
            }
            resultStringBuilder.append(current.symbol);
            current = result.root;
        }
        return resultStringBuilder.toString();
    }

    static class HuffmanEncodedResult{
        final Node root;
        final String data;

        HuffmanEncodedResult(final String data, final Node root){
            this.data = data;
            this.root = root;
        }
        public Node getRoot(){
            return this.root;
        }
        public String getData(){
            return this.data;
        }
    }

    public static void main(String[] args) {
//        final String test = "abcdeffg";
//        final int[] ft = buildFreqTable(test);
//        final Node n = buildTree(ft);
//        final Map<Character, String> lookup = LookupTableBuild(n);
//        System.out.println(n);

        ///////////////////////////////TAKING INPUT FROM INPUT.TXT FILE///////////////////////////////////////
        String input = "";

        try{
            File file = new File("C:\\Users\\David\\Desktop\\DavidHuffman\\src\\David\\uncompressed.txt");
            Scanner scanInput = new Scanner(file);
            input = scanInput.nextLine();
        } catch (IOException err){
            System.out.println(err.getMessage());
        }

        //String test = "hello world!";
        Main encoder = new Main();
        HuffmanEncodedResult result = encoder.compress(input);
//        System.out.println("compressed message: " + result.getData());
//        System.out.println("uncompressed message: " + encoder.decompress(result));
//        System.out.println(lookupTableStatic.toString());



        Scanner scan = new Scanner(System.in);
        String choice = "";

        while(true){
            System.out.println("------------------------ " + "Standard Huffman encoder & decoder" + " ------------------------");
            System.out.println("1- Compress\n" +
                                "2- Decompress\n" +
                                "3- Exit");
            choice = scan.nextLine();
            switch (choice){
                case "1":////////COMPRESS
                    try{
                        ///////////////////Clearing the output file of any existing compressed text///////////////////////
                        FileWriter fwOb = new FileWriter("C:\\Users\\David\\Desktop\\DavidHuffman\\src\\David\\compressed.txt", false);
                        PrintWriter pwOb = new PrintWriter(fwOb, false);
                        pwOb.flush();
                        pwOb.close();
                        fwOb.close();
                        //////////////////Writing compressed message to file and displaying it for user//////////////////
                        FileWriter fw = new FileWriter("C:\\Users\\David\\Desktop\\DavidHuffman\\src\\David\\compressed.txt");
                        fw.write(result.getData());
                        fw.close();
                        System.out.println("Compression successful...\n" +
                                            "Written in File...");
                        System.out.println("Dictionary: " + lookupTableStatic.toString());
                        System.out.println("compressed message: " + result.getData());
                    } catch(IOException err){
                        System.out.println(err.getMessage());
                    }
                    break;
                case "2":///////DECOMPRESS
                    System.out.println("uncompressed message: " + encoder.decompress(result));
                    break;
                default:
                    System.exit(0);
            }
        }
    }
}
