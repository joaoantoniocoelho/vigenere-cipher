import java.io.*;
import java.nio.file.*;
import java.text.Normalizer;

public class VigenereCipher {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Uso: java VigenereCipher <arquivo.txt> <chave>");
            System.exit(1);
        }

        String inputPath = args[0];
        String key = args[1];

        String raw = Files.readString(Path.of(inputPath));
        String sanitized = sanitize(raw);
        String sanitizedKey = sanitize(key);

        if (sanitizedKey.isEmpty()) {
            System.err.println("Chave inválida: nenhum caractere alfabético encontrado.");
            System.exit(1);
        }

        String encrypted = encrypt(sanitized, sanitizedKey);

        Files.writeString(Path.of("texto_criptografado.txt"), encrypted);
        System.out.println("Arquivo gerado: texto_criptografado.txt (" + encrypted.length() + " caracteres)");
    }

    static String sanitize(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.toLowerCase().replaceAll("[^a-z]", "");
    }

    static String encrypt(String text, String key) {
        StringBuilder sb = new StringBuilder(text.length());
        int keyLen = key.length();
        for (int i = 0; i < text.length(); i++) {
            int p = text.charAt(i) - 'a';
            int k = key.charAt(i % keyLen) - 'a';
            sb.append((char) ('a' + (p + k) % 26));
        }
        return sb.toString();
    }
}
