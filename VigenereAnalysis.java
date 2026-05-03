import java.io.*;
import java.nio.file.*;
import java.text.Normalizer;

public class VigenereAnalysis {
    static final double[] PT_FREQ = {
        0.1463, 0.0104, 0.0388, 0.0499, 0.1257, 0.0102,
        0.0130, 0.0128, 0.0618, 0.0040, 0.0002, 0.0278,
        0.0474, 0.0505, 0.1073, 0.0252, 0.0120, 0.0653,
        0.0781, 0.0434, 0.0463, 0.0167, 0.0001, 0.0021,
        0.0001, 0.0047
    };

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Uso: java VigenereAnalysis <arquivo_cifrado.txt>");
            System.exit(1);
        }
        String raw = Files.readString(Path.of(args[0]));
        String cipher = sanitize(raw);

        if (cipher.length() < 100) {
            System.err.println("Texto muito curto para criptoanálise confiável.");
            System.exit(1);
        }

        System.out.println("=== Etapa 1 - Índice de Coincidência ===");
        int keyLen = findKeyLength(cipher);

        System.out.println("\n=== Etapa 2 - Chave inferida ===");
        String key = inferKey(cipher, keyLen);
        System.out.println("Chave: " + key);

        String plain = decrypt(cipher, key);
        Files.writeString(Path.of("texto_decifrado.txt"), plain);
        System.out.println("\nArquivo gerado: texto_decifrado.txt (" + plain.length() + " caracteres)");
    }

    static String sanitize(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.toLowerCase().replaceAll("[^a-z]", "");
    }

    static double calcIC(String text) {
        int n = text.length();
        if (n < 2) return 0.0;
        int[] freq = new int[26];
        for (char c : text.toCharArray()) freq[c - 'a']++;
        double sum = 0;
        for (int f : freq) sum += (double) f * (f - 1);
        return sum / ((double) n * (n - 1));
    }

    static double avgIC(String text, int k) {
        double total = 0;
        for (int i = 0; i < k; i++) {
            StringBuilder sub = new StringBuilder();
            for (int j = i; j < text.length(); j += k) sub.append(text.charAt(j));
            total += calcIC(sub.toString());
        }
        return total / k;
    }

    static int findKeyLength(String cipher) {
        double target = 0.072;
        int bestK = 1;
        double bestDiff = Double.MAX_VALUE;
        for (int k = 1; k <= 10; k++) {
            double ic = avgIC(cipher, k);
            System.out.printf("k=%-2d IC_medio=%.4f%n", k, ic);
            double diff = Math.abs(ic - target);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestK = k;
            }
        }
        System.out.printf("-> Tamanho estimado da chave: %d%n", bestK);
        return bestK;
    }


    static int inferShift(String sub) {
        int n = sub.length();
        double[] freq = new double[26];
        for (char c : sub.toCharArray()) freq[c - 'a']++;
        for (int i = 0; i < 26; i++) freq[i] /= n;

        int bestShift = 0;
        double bestScore = -1;
        for (int d = 0; d < 26; d++) {
            double score = 0;
            for (int i = 0; i < 26; i++) {
                score += freq[(i + d) % 26] * PT_FREQ[i];
            }
            if (score > bestScore) {
                bestScore = score;
                bestShift = d;
            }
        }
        return bestShift;
    }

    static String inferKey(String cipher, int k) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < k; i++) {
            StringBuilder sub = new StringBuilder();
            for (int j = i; j < cipher.length(); j += k) sub.append(cipher.charAt(j));
            int shift = inferShift(sub.toString());
            System.out.printf("Posição %d: deslocamento %2d -> '%c'%n", i, shift, (char)('a' + shift));
            key.append((char)('a' + shift));
        }
        return key.toString();
    }

    static String decrypt(String cipher, String key) {
        int k = key.length();
        StringBuilder sb = new StringBuilder(cipher.length());
        for (int i = 0; i < cipher.length(); i++) {
            int c = cipher.charAt(i) - 'a';
            int d = key.charAt(i % k) - 'a';
            sb.append((char)('a' + (c - d + 26) % 26));
        }
        return sb.toString();
    }
}
