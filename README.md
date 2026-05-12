## Huffman File Compressor CLI Tool.

A simple Java CLI tool that implements file compression and decompression using the [Huffman coding](https://en.wikipedia.org/wiki/Huffman_coding) algorithm.

### Features
* Huffman-based lossless compression
* File decompression
* File size inspection
* File equality checking
* Binary archive generation

### How Huffman Compression Works

The program:

* Reads characters from the source file
* Counts frequency of each character
* Builds a Huffman tree using a priority queue
* Generates binary codes for each character
* Replaces characters with Huffman codes
* Stores compressed binary data into a file

Decompression performs the reverse process using the stored Huffman tree.

### Requirements
* Java 17+ recommended
* Any Java IDE or terminal

### Compilation

```
javac Main.java
```

### Running

```
java Main
```

### Available Commands

**Compress a file**

```
comp
```

Program will ask for:

```
source file name:
archive name:
```

**Decompress a file**

```
decomp
```

Program will ask for:

```
archive name:
file name:
```

**Check file size**

```
size
```

**Compare two files**

```
equal
```

**Exit program**

```
exit
```
