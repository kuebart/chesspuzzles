import java.io.*;
import java.nio.file.*;

public class ZstdExtract {
    public static void main(String[] args) throws Exception {
        Path path = Path.of("C:/workspace/chesspuzzles/puzzles_part.zst");
        byte[] data = Files.readAllBytes(path);
        System.out.printf("File size: %d bytes%n", data.length);
        System.out.printf("Magic: %02X %02X %02X %02X%n", data[0]&0xFF, data[1]&0xFF, data[2]&0xFF, data[3]&0xFF);
        
        // Zstd magic: 28 B5 2F FD
        if ((data[0]&0xFF) == 0x28 && (data[1]&0xFF) == 0xB5 && (data[2]&0xFF) == 0x2F && (data[3]&0xFF) == 0xFD) {
            System.out.println("Valid zstd file detected");
        }
    }
}
