# LangChain4j na prática — demo

Demo da apresentação sobre LangChain4j: um copiloto de **prevenção à fraude** construído em 6 passos,
do chat mais simples até um agente com tools, RAG, memória e guardrail.

**Stack:** Java 25 · Spring Boot 4.1 · Gradle 9 · LangChain4j 1.22

> Todos os dados (clientes, transações e políticas) são **fictícios**.

## Os 6 passos

| Passo | Conceito | Onde está no código |
|---|---|---|
| 1 | `ChatModel` — API de baixo nível | `DemoController#chatBasico` |
| 2 | AI Service declarativo + memória | `ai/AssistenteAtendimento` · `config/LangChain4jConfig` |
| 3 | Saída estruturada (texto → record) | `ai/AnalisadorOcorrencia` · `ai/modelo/AnaliseRisco` |
| 4 | RAG sobre políticas internas | `rag/IngestaoPoliticas` · `ai/ConsultorPoliticas` · `resources/politicas` |
| 5 | Tools / agente | `tools/FerramentasAntifraude` · `ai/AgenteAntifraude` |
| 6 | Guardrail de entrada | `guardrail/BloqueioDadosCartaoGuardrail` |
| + | Observabilidade (tokens, latência) | `observabilidade/LogChatModelListener` |

## Configurando o Ollama (passo a passo)

O Ollama é o servidor que executa o modelo e expõe uma API compatível com a da OpenAI em
`http://localhost:11434/v1` — é nela que a aplicação se conecta.

**1. Instalar**
- Windows/macOS: baixe o instalador em https://ollama.com/download e execute.
- Linux: `curl -fsSL https://ollama.com/install.sh | sh`

**2. Verificar se está rodando**

```bash
ollama --version
curl http://localhost:11434     # deve responder "Ollama is running"
```

No Windows/macOS o Ollama sobe sozinho após a instalação (ícone na bandeja). Se não estiver rodando,
inicie com `ollama serve`.

**3. Entrar na conta (só para modelos `-cloud`)**

```bash
ollama signin
```

Abre o navegador para login. Crie a conta gratuita em https://ollama.com se ainda não tiver.

**4. Baixar/registrar o modelo**

```bash
ollama pull gpt-oss:120b-cloud     # padrão da demo (nuvem)
# ou, 100% local:
ollama pull qwen2.5:7b
```

**5. Testar o modelo no terminal**

```bash
ollama run gpt-oss:120b-cloud "Responda apenas: ok"
```

Saia do modo interativo com `/bye`.

**6. Testar a API que a aplicação usa**

```bash
curl http://localhost:11434/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{"model":"gpt-oss:120b-cloud","messages":[{"role":"user","content":"oi"}]}'
```

**7. Apontar a aplicação para o modelo**

O padrão já é `gpt-oss:120b-cloud` em `http://localhost:11434/v1`. Para mudar, use variáveis de ambiente:

| Variável | Padrão | Para que serve |
|---|---|---|
| `LLM_MODEL` | `gpt-oss:120b-cloud` | nome do modelo (como em `ollama list`) |
| `LLM_BASE_URL` | `http://localhost:11434/v1` | endereço do Ollama (ex.: outra máquina na rede) |
| `LLM_API_KEY` | `ollama` | ignorada pelo Ollama, mas obrigatória no cliente |
| `LLM_LOG` | `false` | `true` imprime prompts e respostas brutos |

**Comandos úteis**

| Comando | O que faz |
|---|---|
| `ollama list` | lista os modelos disponíveis |
| `ollama ps` | mostra os modelos carregados na memória |
| `ollama stop <modelo>` | descarrega um modelo da memória |
| `ollama rm <modelo>` | remove um modelo |

**Problemas comuns**
- `connection refused` na porta 11434: o Ollama não está rodando — execute `ollama serve`.
- `model not found`: rode `ollama pull <modelo>` com o mesmo nome usado em `LLM_MODEL`.
- Erro de autorização com modelo `-cloud`: rode `ollama signin` novamente.
- Resposta muito lenta com modelo local: use um modelo menor (`qwen2.5:7b`) ou confirme se há GPU disponível.

## Como rodar

### Opção A — Ollama com `gpt-oss:120b-cloud` (padrão)

```bash
# 1-2. Instale o Ollama, entre na conta e registre o modelo (detalhes na seção acima)
ollama signin
ollama pull gpt-oss:120b-cloud

# 3. Suba a aplicação
./gradlew bootRun
```

Abra **http://localhost:8080**.

> O sufixo `-cloud` significa que a inferência acontece nos servidores do Ollama: o app fala com o
> Ollama local (`http://localhost:11434/v1`), que encaminha as requisições para a nuvem. Como os prompts
> **saem da máquina**, use apenas dados fictícios (como os desta demo). Requer internet e conta no Ollama.

O gpt-oss suporta tools e raciocínio. Ele pode responder só com tool calls, sem texto — o tratamento
disso está em `config/LangChain4jConfig`.

**Alternativa 100% local** (nada sai da máquina), com um modelo menor que suporte tools:

```bash
ollama pull qwen2.5:7b
LLM_MODEL=qwen2.5:7b ./gradlew bootRun
```

Outros modelos locais que funcionam bem com tools: `llama3.1:8b`, `qwen2.5:14b` (melhor qualidade, mais lento).

### Opção B — OpenAI ou gateway corporativo compatível com OpenAI

```bash
export OPENAI_API_KEY=sk-...
SPRING_PROFILES_ACTIVE=openai ./gradlew bootRun

# Gateway interno compatível com a API da OpenAI:
LLM_BASE_URL=https://seu-gateway/v1 LLM_MODEL=nome-do-modelo SPRING_PROFILES_ACTIVE=openai ./gradlew bootRun
```

### Opção C — Claude (Anthropic)

```bash
export ANTHROPIC_API_KEY=sk-ant-...
SPRING_PROFILES_ACTIVE=claude ./gradlew bootRun

# Trocar o modelo (padrão: claude-sonnet-5):
LLM_MODEL=claude-haiku-4-5-20251001 SPRING_PROFILES_ACTIVE=claude ./gradlew bootRun
```

A Anthropic fala um protocolo próprio (não o da OpenAI), então esse perfil usa um starter
e um bean diferentes por baixo — ver o comentário em `config/LangChain4jConfig`.

Para ver os prompts e respostas brutos no console: `LLM_LOG=true`.

### Requisitos
- JDK 25 (o Gradle usa toolchain; se não houver JDK 25 instalado, ele tenta provisionar)
- Na primeira execução o Gradle baixa as dependências e o modelo de embeddings (~25 MB, embutido no jar do LangChain4j)

### Testes
```bash
./gradlew test   # testa o guardrail sem chamar nenhum LLM
```

## Endpoints
`POST /api/demo/{1-chat|2-assistente|3-estruturado|4-rag|5-agente}` com corpo `{"conversaId": "abc", "mensagem": "..."}`.
