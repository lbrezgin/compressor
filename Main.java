import java.io.*;
import java.util.*;


public class Main {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String choiseStr;
		String sourceFile, resultFile, firstFile, secondFile;
		
		loop: while (true) {
			
			choiseStr = sc.next();
								
			switch (choiseStr) {
			case "comp":
				System.out.print("source file name: ");
				sourceFile = sc.next();
				System.out.print("archive name: ");
				resultFile = sc.next();
				comp(sourceFile, resultFile);
				break;
			case "decomp":
				System.out.print("archive name: ");
				sourceFile = sc.next();
				System.out.print("file name: ");
				resultFile = sc.next();
				decomp(sourceFile, resultFile);
				break;
			case "size":
				System.out.print("file name: ");
				sourceFile = sc.next();
				size(sourceFile);
				break;
			case "equal":
				System.out.print("first file name: ");
				firstFile = sc.next();
				System.out.print("second file name: ");
				secondFile = sc.next();
				System.out.println(equal(firstFile, secondFile));
				break;
			case "about":
				about();
				break;
			case "exit":
				break loop;
			}
		}

		sc.close();
	}

	public static void comp(String sourceFile, String resultFile) {
		ArrayList<String> words = fileSplitByWords(sourceFile);
		HashMap<String, Integer> wordCount = findEachWordCount(words);
		ArrayList<HuffmanNode> nodesArray = buildNodeArray(wordCount);
		HuffmanNode root = buildTree(nodesArray);

		String s = "";
		HashMap<String, String> codesMap = new HashMap<String, String>();
		createCodesMap(root, s, codesMap);
		String newFileBits = "";
		for(int i = 0; i < words.size(); i++){
			newFileBits += codesMap.get(words.get(i));
		}

		File newFile = new File(resultFile);
		try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(newFile))) {
			byte[] bitBytes = bitsToBytes(newFileBits);
			dos.write(bitBytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		saveTreeToFile(root, "treeFile");
		System.out.println("File compressed");
	}

	public static void decomp(String sourceFile, String resultFile) {
		HuffmanNode root = restoreTreeFromFile("treeFile");

		try (DataInputStream dis = new DataInputStream(new FileInputStream(sourceFile));
			 BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile))) {
			 byte[] compressedData = dis.readAllBytes();
			 String bitString = bytesToBits(compressedData);

			 HuffmanNode current = root;
			 StringBuilder result = new StringBuilder();

			 for (char bit : bitString.toCharArray()) {
				 current = (bit == '0') ? current.left : current.right;

				 if (current.left == null && current.right == null) {
					 String valueToWrite = current.value.equals("SpAcEiNtExT") ? " " : current.value;
					 result.append(valueToWrite);
					 current = root;
				 }
			}

			String decompressedText = result.toString().replaceAll("\\s+$", "");
			writer.write(decompressedText);


			System.out.println("File decompressed");
		} catch (IOException e) {
			throw new RuntimeException("Error during decompression: " + e.getMessage());
		}
	}

	private static String bytesToBits(byte[] bytes) {
		StringBuilder bitString = new StringBuilder();
		for (byte b : bytes) {
			for (int i = 7; i >= 0; i--) {
				bitString.append((b >> i) & 1);
			}
		}
		return bitString.toString();
	}


	public static void size(String sourceFile) {
		try {
			FileInputStream f = new FileInputStream(sourceFile);
			System.out.println("size: " + f.available());
			f.close();
		}
		catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
		
	}
	
	public static boolean equal(String firstFile, String secondFile) {
		try {
			FileInputStream f1 = new FileInputStream(firstFile);
			FileInputStream f2 = new FileInputStream(secondFile);
			int k1, k2;
			byte[] buf1 = new byte[1000];
			byte[] buf2 = new byte[1000];
			do {
				k1 = f1.read(buf1);
				k2 = f2.read(buf2);
				if (k1 != k2) {
					f1.close();
					f2.close();
					return false;
				}
				for (int i=0; i<k1; i++) {
					if (buf1[i] != buf2[i]) {
						f1.close();
						f2.close();
						return false;
					}
						
				}
			} while (!(k1 == -1 && k2 == -1));
			f1.close();
			f2.close();
			return true;
		}
		catch (IOException ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}
	
	public static void about() {
		System.out.println("");
	}

	private static byte[] bitsToBytes(String bitString) {
		int len = bitString.length();
		int byteCount = (len+7)/8;
		byte[] result = new byte[byteCount];
		int index=0;
		int val=0;
		for (int i=0; i<len; i++) {
			val = (val << 1) | (bitString.charAt(i)=='1'?1:0);
			if ((i & 7) == 7) {
				result[index++] = (byte) val;
				val=0;
			}
		}
		if ((len & 7) != 0) {
			val = val << (8-(len&7));
			result[index] = (byte) val;
		}
		return result;
	}

	public static void createCodesMap(HuffmanNode node, String code, HashMap<String, String> codesMap) {
		if (node != null) {
			if (node.left == null && node.right == null) {
				codesMap.put(node.value, code);
			}

			createCodesMap(node.left, code + "0", codesMap);
			createCodesMap(node.right, code + "1", codesMap);
		}
	}

	public static HuffmanNode buildTree(ArrayList<HuffmanNode> nodesArray){
		PriorityQueue<HuffmanNode> pq = new PriorityQueue<>(Comparator.comparingInt(HuffmanNode::getCount));
		pq.addAll(nodesArray);

		while (pq.size() > 1) {
			HuffmanNode left = pq.poll();
			HuffmanNode right = pq.poll();

			int count = left.count + right.count;
			HuffmanNode newNode = new HuffmanNode("±", count, left, right);
			pq.offer(newNode);
		}
		return pq.poll();
	}

	public static ArrayList<HuffmanNode> buildNodeArray(HashMap<String, Integer> valueAndCount){
		ArrayList<HuffmanNode> nodesArray = new ArrayList<HuffmanNode>();

		for(Map.Entry<String, Integer> entry : valueAndCount.entrySet()){
			HuffmanNode newNode = new HuffmanNode(entry.getKey(), entry.getValue(), null, null);
			nodesArray.add(newNode);
		}
		return nodesArray;
	}

	private static ArrayList<String> fileSplitByWords(String filename){
		BufferedReader br;
        ArrayList<String> words = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(filename));
            String s;
            while ((s = br.readLine()) != null) {
				s = s.replaceAll(" ", "_sPlIt_SpAcEiNtExT_sPlIt_");
				String[] wordArray = s.split("_sPlIt_");
                for (String word : wordArray) {
					words.add(word);
                }
				words.add("\n");
            }
			words.removeLast();
            br.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return words;
	}

	static HashMap<String, Integer> findEachWordCount(ArrayList<String> words){
		HashMap<String, Integer> count = new HashMap<String, Integer>();
		for(String ch: words){
			if (count.containsKey(ch)){
				count.put(ch, count.get(ch)+1);
			} else {
				count.put(ch, 1);
			}
		}
		return count;
	}
//-----------------------------------
	public static void saveTreeToFile(HuffmanNode root, String treeFile) {
		try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(treeFile))) {
			saveNode(dos, root);
		} catch (IOException e) {
			throw new RuntimeException("Error while saving the tree: " + e.getMessage());
		}
	}

	private static void saveNode(DataOutputStream dos, HuffmanNode node) throws IOException {
		if (node == null) return;

		if (node.left == null && node.right == null) {
			dos.writeChar('L');
			dos.writeUTF(node.value);
		} else {
			dos.writeChar('I');
		}

		saveNode(dos, node.left);
		saveNode(dos, node.right);
	}

	public static HuffmanNode restoreTreeFromFile(String treeFile) {
		try (DataInputStream dis = new DataInputStream(new FileInputStream(treeFile))) {
			return restoreNode(dis);
		} catch (IOException e) {
			throw new RuntimeException("Error while restoring the tree: " + e.getMessage());
		}
	}

	private static HuffmanNode restoreNode(DataInputStream dis) throws IOException {
		char nodeType = dis.readChar();
		if (nodeType == 'L') {
			String value = dis.readUTF();
			return new HuffmanNode(value, 0, null, null);
		} else if (nodeType == 'I') {
			HuffmanNode left = restoreNode(dis);
			HuffmanNode right = restoreNode(dis);
			return new HuffmanNode(null, 0, left, right);
		}
		return null;
	}


}

class HuffmanNode{
	HuffmanNode left;
	HuffmanNode right;
	int count;
	String value;

	HuffmanNode(String value, int count, HuffmanNode left, HuffmanNode right){
		this.left = left;
		this.right = right;
		this.count = count;
		this.value = value;
	}

	public int getCount() {
		return count;
	}
}
