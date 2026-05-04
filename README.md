# Cifra de Vigenère — Criptografia e Criptoanálise

Trabalho 1 de Segurança de Sistemas — PUCRS
Alunos: João Coelho, Matheus Berwaldt e Vitor Kepeler

Implementação em Java da Cifra de Vigenère (criptografia) e de um algoritmo de criptoanálise capaz de quebrar a cifra sem conhecer a senha.

---

## Compilação

```bash
javac VigenereCipher.java
javac VigenereAnalysis.java
```

---

## Parte 1 — Criptografia (`VigenereCipher.java`)

### Uso

```bash
java VigenereCipher <arquivo.txt> <chave>
```

**Exemplo:**
```bash
java VigenereCipher DomCasmurro.txt segredo
# Gera: texto_criptografado.txt
```

### Como funciona

#### 1. Higienização do texto (`sanitize`)

Antes de cifrar, o texto passa por uma limpeza para garantir que só letras de `a` a `z` entrem na cifra:

- Normalização Unicode NFD: decompõe caracteres acentuados em letra base + acento (ex: `á` → `a` + `´`)
- Conversão para minúsculas
- Remoção de tudo que não for letra de `a` a `z` (pontuação, espaços, números, acentos soltos)

```
"Dom Casmurro!" → "domcasmurro"
"ção"           → "cao"
```

#### 2. Criptografia (`encrypt`)

A Cifra de Vigenère funciona somando o valor de cada letra do texto com o valor da letra correspondente da chave, em módulo 26:

```
cifrado[i] = (texto[i] + chave[i % tamanho_chave]) % 26
```

A chave é repetida ciclicamente ao longo do texto. Por exemplo, com a chave `seg`:

```
texto:   d  o  m  c  a  s  m  u  r
chave:   s  e  g  s  e  g  s  e  g
         +  +  +  +  +  +  +  +  +
cifrado: v  s  s  v  e  y  e  y  y
```

#### 3. Saída

O texto cifrado é salvo em `texto_criptografado.txt`.

---

## Parte 2 — Criptoanálise (`VigenereAnalysis.java`)

Quebra a cifra **sem conhecer a senha**, usando apenas análise estatística.

### Uso

```bash
java VigenereAnalysis <arquivo_cifrado.txt>
```

**Exemplo:**
```bash
java VigenereAnalysis texto_criptografado.txt
```

### Como funciona

#### Etapa 1 — Descoberta do tamanho da chave (Índice de Coincidência)

O **Índice de Coincidência (IC)** mede o quão "natural" é a distribuição de letras de um texto. Um texto em português tem IC ≈ 0,072. Um texto cifrado com chave errada parece aleatório, com IC ≈ 0,038.

**Estratégia:** Para cada tamanho de chave candidato `k` de 1 a 10:

1. Dividir o texto cifrado em `k` subtextos intercalados. O subtexto `i` contém as letras nas posições `i, i+k, i+2k, ...`

   Com k=3, o texto `abcdefghi` vira:
   ```
   subtexto 0: a d g  (posições 0, 3, 6)
   subtexto 1: b e h  (posições 1, 4, 7)
   subtexto 2: c f i  (posições 2, 5, 8)
   ```

2. Calcular o IC de cada subtexto pela fórmula:
   ```
   IC = Σ(fi × (fi - 1)) / (n × (n - 1))
   ```
   onde `fi` é a contagem da letra `i` e `n` é o comprimento do subtexto.

3. Calcular a **média dos ICs** dos `k` subtextos.

4. Quando `k` é o tamanho correto da chave, cada subtexto foi cifrado com um deslocamento fixo (César simples), então sua distribuição de letras se parece com português → IC alto.

O `k` cuja média de IC mais se aproxima de 0,072 é o tamanho mais provável da chave.

**Saída desta etapa:**
```
=== Etapa 1 - Índice de Coincidência ===
k=1  IC_medio=0.0468
k=2  IC_medio=0.0468
...
k=7  IC_medio=0.0768   <- pico próximo de 0.072
k=8  IC_medio=0.0468
...
-> Tamanho estimado da chave: 7
```

#### Etapa 2 — Descoberta da chave (Análise de Frequência)

Com o tamanho `k` descoberto, cada posição da chave é um deslocamento de César independente. Para descobrir o deslocamento de cada posição:

1. Extrair o subtexto da posição `i` (letras em `i, i+k, i+2k, ...`)

2. Contar a frequência relativa de cada letra nesse subtexto

3. Comparar com a distribuição conhecida do português usando **produto escalar**:
   ```
   score(d) = Σ freq_observada[(i + d) % 26] × freq_portuguesa[i]
   ```
   O deslocamento `d` que maximiza o score é o deslocamento correto.

   **Por que funciona:** se o subtexto foi deslocado por `d`, "deslocar de volta" por `d` faz as frequências observadas se alinharem com as do português, maximizando o produto escalar.

4. A letra correspondente ao deslocamento `d` é o caractere da chave naquela posição.

**Distribuição do português usada (hardcoded):**
```
a=14,6%  e=12,6%  o=10,7%  s=7,8%  r=6,5%  i=6,2%  n=5,1%  ...
```

**Saída desta etapa:**
```
=== Etapa 2 - Chave inferida ===
Posição 0: deslocamento 18 -> 's'
Posição 1: deslocamento  4 -> 'e'
Posição 2: deslocamento  6 -> 'g'
Posição 3: deslocamento 17 -> 'r'
Posição 4: deslocamento  4 -> 'e'
Posição 5: deslocamento  3 -> 'd'
Posição 6: deslocamento 14 -> 'o'
Chave: segredo
```

#### Decifração

Com a chave descoberta, aplica o inverso da cifra:

```
decifrado[i] = (cifrado[i] - chave[i % k] + 26) % 26
```

O `+26` garante que o resultado nunca seja negativo antes do módulo.

---

## Exemplo de execução completa

```bash
# 1. Cifrar Dom Casmurro com a chave "segredo"
java VigenereCipher DomCasmurro.txt segredo

# 2. Quebrar a cifra sem usar a chave
java VigenereAnalysis texto_criptografado.txt
```

**Saída esperada (resumida):**
```
=== Etapa 1 - Índice de Coincidência ===
k=7  IC_medio=0.0768
-> Tamanho estimado da chave: 7

=== Etapa 2 - Chave inferida ===
Chave: segredo

Arquivo gerado: texto_decifrado.txt (308887 caracteres)
```
