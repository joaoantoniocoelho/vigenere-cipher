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

        // Lê o conteúdo original do arquivo
        String raw = Files.readString(Path.of(inputPath));

        // Higieniza o texto original e a chave
        String sanitized = sanitize(raw);
        String sanitizedKey = sanitize(key);

        if (sanitizedKey.isEmpty()) {
            System.err.println("Chave inválida: nenhum caractere alfabético encontrado.");
            System.exit(1);
        }

        // Aplica a cifra de Vigenère
        String encrypted = encrypt(sanitized, sanitizedKey);

        // Salva o resultado em arquivo
        Files.writeString(Path.of("texto_criptografado.txt"), encrypted);

        System.out.println("Arquivo gerado: texto_criptografado.txt (" 
                + encrypted.length() + " caracteres)");
    }

    static String sanitize(String input) {
        // Normaliza o texto para separar letras de seus acentos.
        // Exemplo: "á" vira "a" + acento separado.
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Converte para minúsculas e remove tudo que não for letra de a a z.
        // Isso remove acentos, pontuação, números, espaços e caracteres especiais.
        return normalized.toLowerCase().replaceAll("[^a-z]", "");
    }

    static String encrypt(String text, String key) {
        StringBuilder sb = new StringBuilder(text.length());
        int keyLen = key.length();

        for (int i = 0; i < text.length(); i++) {
            // Converte a letra do texto para número.
            // 'a' = 0, 'b' = 1, ..., 'z' = 25.
            int p = text.charAt(i) - 'a';

            // Pega a letra correspondente da chave.
            // O operador % faz a chave repetir ciclicamente.
            int k = key.charAt(i % keyLen) - 'a';

            // Aplica Vigenère:
            // C = (P + K) mod 26
            int encryptedValue = (p + k) % 26;

            // Converte o número de volta para letra.
            sb.append((char) ('a' + encryptedValue));
        }

        return sb.toString();
    }
}