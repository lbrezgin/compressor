import java.io.*;
import java.util.*;


public class Main {
	//Переменная деревьев. Тут хранятся все деревья, для всех файлов
	public static HashMap<String, HuffmanNode> trees = new HashMap<String, HuffmanNode>();

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
				System.out.print("first.txt file name: ");
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
		//Делим весь тест на слова по пробелам
		ArrayList<String> words = fileSplitByWords(sourceFile);
		//Считаем все слова, сохраняем в хэш, ключ слово, значение количество слова в тексте
		HashMap<String, Integer> wordCount = findEachWordCount(words);
		//Создаем массив из листьев дерева
		ArrayList<HuffmanNode> nodesArray = buildNodeArray(wordCount);
		//Строим дерево, сохраняем корневой элемент
		HuffmanNode root = buildTree(nodesArray);

		String s = "";
		HashMap<String, String> codesMap = new HashMap<String, String>();
		//Создаем хэш ключ слово, значение код по дереву
		createCodesMap(root, s, codesMap);
		//Проходимся по массиву слов, добавляем в строку вместо слова его код
		String newFileBits = "";
		for(int i = 0; i < words.size(); i++){
			newFileBits += codesMap.get(words.get(i));
		}

		//Записываем в файл
		File newFile = new File(resultFile);
		try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(newFile))) {
			byte[] bitBytes = bitsToBytes(newFileBits);
			dos.write(bitBytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		//Добавляем дерево в хэш деревьев. Сохраняем только корневой элемент.
		//Остальные элементы дерева будут сохранены, так как они связаны между собой.
		//Если скомпрессировать еще один файл, в переменной "nodesArray" будут новые узлы,
		//но старые остануться в памяти тоже.
		trees.put(resultFile, root);
		System.out.println("File compressed");
	}

	public static void decomp(String sourceFile, String resultFile) {
		//Получаем корневой элемент дерева.
		HuffmanNode root = trees.get(sourceFile);

		try (DataInputStream dis = new DataInputStream(new FileInputStream(sourceFile));
			 BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile))) {
			//Читаем закодированный файл по байтам
			byte[] compressedData = dis.readAllBytes();
			//Получаем из байтов строку битов
			String bitString = bytesToBits(compressedData);

			//Текущий элемент, это корень дерева
			HuffmanNode current = root;
			//Сюда будем добавлять раскодированные элементы файла
			StringBuilder result = new StringBuilder();

			for (char bit : bitString.toCharArray()) {
				//Если в строке 0, идем на лево, если 1, идем на право
				current = (bit == '0') ? current.left : current.right;

				//Если у узла нет левого узла и правого узла, значит мы нашли наше слово
				if (current.left == null && current.right == null) {
					//Проверяем, не является ли значение узла пробелом, так как все пробелы были
					//заменены на специальную строку в методе "fileSplitByWords"
					String valueToWrite = current.value.equals("SpAcEiNtExT") ? " " : current.value;
					result.append(valueToWrite);
					//Возвращаем в текущий элемент корень дерева
					current = root;
				}
			}

			//Небольшой костыль, удаляем лишние пробелы в конце строки, так как почему-то после декомпрессии
			//появлялись лишние пробелы
			String decompressedText = result.toString().replaceAll("\\s+$", "");
			writer.write(decompressedText);

			System.out.println("File decompressed");
		} catch (IOException e) {
			throw new RuntimeException("Error during decompression: " + e.getMessage());
		}
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

	//Methods for compressor
	private static byte[] bitsToBytes(String bitString) {
		//Украл этот метод у Захара
		//Смотрим сколько у нас битов
		int len = bitString.length();
		//Добавляем 7 и делим на 8, в случае если у нас не ровное количество бит
		int byteCount = (len+7)/8;
		byte[] result = new byte[byteCount];
		int index=0;
		int val=0;
		for (int i=0; i<len; i++) {
			//Сдвигаемся на один влево, проверяем что у нас там
			val = (val << 1) | (bitString.charAt(i)=='1'?1:0);
			//если индекс 7, значит мы проверили весь байт
			if ((i & 7) == 7) {
				//записываем байт в массив байтов
				result[index++] = (byte) val;
				val=0;
			}
		}
		//обработка последнего бита, если количество бит не кратно 8
		if ((len & 7) != 0) {
			val = val << (8-(len&7));
			result[index] = (byte) val;
		}
		return result;
	}

	//Создаем хэш кодов через рекурсию
	private static void createCodesMap(HuffmanNode node, String code, HashMap<String, String> codesMap) {
		//Если node == null, мы дошли до конца
		if (node != null) {
			//Если нет левого и правого узла, значит мы нашли код для нашего слова
			if (node.left == null && node.right == null) {
				codesMap.put(node.value, code);
			}

			//Если есть правый или левый элемент у узла, идем дальше
			createCodesMap(node.left, code + "0", codesMap);
			createCodesMap(node.right, code + "1", codesMap);
		}
	}

	//Строим дерево по алгоритму хоффмана, связываем узлы
	private static HuffmanNode buildTree(ArrayList<HuffmanNode> nodesArray){
		//Приоритетный список, который всегда себя сортирует
		PriorityQueue<HuffmanNode> pq = new PriorityQueue<>(Comparator.comparingInt(HuffmanNode::getCount));
		//Добавляем туда наши узлы дерева
		pq.addAll(nodesArray);

		//Если узлов больше одного
		while (pq.size() > 1) {
			//вытаскиваем два последних элемента, poll() удаялет их из изначального списка
			HuffmanNode left = pq.poll();
			HuffmanNode right = pq.poll();

			//Суммируем их количество
			int count = left.count + right.count;
			//Создаем новый служебный узел
			HuffmanNode newNode = new HuffmanNode(null, count, left, right);
			//Добавляем обратно в список новый узел
			pq.offer(newNode);
		}
		//Последний элемент который остался - корневой элемент
		return pq.poll();
	}

	//Создаем массив узлов
	private static ArrayList<HuffmanNode> buildNodeArray(HashMap<String, Integer> valueAndCount){
		ArrayList<HuffmanNode> nodesArray = new ArrayList<HuffmanNode>();

		for(Map.Entry<String, Integer> entry : valueAndCount.entrySet()){
			HuffmanNode newNode = new HuffmanNode(entry.getKey(), entry.getValue(), null, null);
			nodesArray.add(newNode);
		}
		return nodesArray;
	}

	//Делим весь текст по пробелам
	private static ArrayList<String> fileSplitByWords(String filename){
		BufferedReader br;
        ArrayList<String> words = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(filename));
            String s;
            while ((s = br.readLine()) != null) {
				//Заменяем все пробелы на специальную строку
				//Если делить текст просто по " ", будут проблемы в местах, где идут множественные пробелы,
				//они просто исчезнут
				s = s.replaceAll(" ", "_sPlIt_SpAcEiNtExT_sPlIt_");
				//Делим текст по _sPlIt_, в итоге вместо пробелов у нас остается SpAcEiNtExT, который на этапе
				//декомпрессии будем менять на пробелы
				String[] wordArray = s.split("_sPlIt_");
                for (String word : wordArray) {
					words.add(word);
                }
				//Добавляем символ новой строки, когда прошлись по всей строке
				words.add("\n");
            }
			//Удаляем последний символ новой строки в конце файла, т.к. он лишний
			words.removeLast();
            br.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return words;
	}

	//Ищем количество всех слов
	private static HashMap<String, Integer> findEachWordCount(ArrayList<String> words){
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

	//Получаем биты из байтов, не сам писал этот метод
	//Хз как это работает
	private static String bytesToBits(byte[] bytes) {
		StringBuilder bitString = new StringBuilder();
		//Проходимся по каждому байту
		for (byte b : bytes) {
			//Проходимся по каждому биту в байте
			for (int i = 7; i >= 0; i--) {
				//побитовое & возвращает 1 если 1 & 1 и 0, если 0 & 1
				bitString.append((b >> i) & 1);
			}
		}
		return bitString.toString();
	}
}

//Класс нашего узла в дереве
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
