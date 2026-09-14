# Especificação — Fut Soccer Brasil

Documento de referência do projeto desenvolvido para a disciplina de **Projeto de Software**.
Descreve o escopo, os requisitos, a modelagem e o planejamento das entregas.

---

## 1. Visão geral

**Produto:** Fut Soccer Brasil
**Plataforma:** Android (API 24+)
**Repositório:** https://github.com/joaovitorsg011/fut-soccer-champ

### 1.1 Problema

A organização de campeonatos amadores costuma ser feita em planilhas e grupos de mensagens. A tabela
de classificação é calculada manualmente, o que torna comum a divergência entre os placares
registrados e a pontuação exibida. Além disso, cada edição do campeonato começa do zero: não há
histórico de times nem de jogadores entre temporadas.

### 1.2 Solução

Um aplicativo Android que centraliza a organização do campeonato — times, elenco, rodadas, partidas
e resultados — e deriva automaticamente a classificação e os rankings individuais a partir das
partidas registradas.

### 1.3 Objetivos

1. Eliminar o cálculo manual da tabela e os erros dele decorrentes.
2. Registrar a produção individual dos jogadores (gols e defesas) por partida.
3. Preservar o histórico entre temporadas, permitindo comparar times e jogadores ao longo do tempo.
4. Manter todos os dados na nuvem, acessíveis de qualquer dispositivo.

### 1.4 Fora de escopo

- Aplicativo para espectadores ou torcedores.
- Cartões, substituições e súmula arbitral completa.
- Fases eliminatórias com chaveamento (mata-mata).
- Modo offline com sincronização posterior.

---

## 2. Usuários

| Perfil | Descrição | Permissões |
|--------|-----------|------------|
| **Organizador** | Responsável por cadastrar e manter o campeonato | Acesso total aos dados que criou |

O aplicativo não possui tela de cadastro público: as contas são criadas no console do Firebase.
Um organizador enxerga e altera apenas os campeonatos cujo `ownerId` corresponde ao seu usuário.

---

## 3. Arquitetura

### 3.1 Camadas

```
┌────────────────────────────────────────────────┐
│  presentation                                  │
│  Telas Compose · ViewModels · Navegação        │
│  Estado exposto por StateFlow                  │
└───────────────────────┬────────────────────────┘
                        ↓
┌────────────────────────────────────────────────┐
│  domain                                        │
│  Kotlin puro, sem Android e sem Firebase       │
│  Classificação · Rankings · Sorteio da tabela  │
└───────────────────────┬────────────────────────┘
                        ↓
┌────────────────────────────────────────────────┐
│  data                                          │
│  Models · Repositories · Service locator       │
└───────────────────────┬────────────────────────┘
                        ↓
            Firebase Authentication
            Cloud Firestore
```

Padrões aplicados: **MVVM**, **Repository Pattern** e **Service Locator** para as dependências.

### 3.2 Decisões de projeto

| Decisão | Alternativa descartada | Justificativa |
|---------|------------------------|---------------|
| Classificação derivada das partidas | Persistir a tabela no banco | Elimina estado duplicado; correções de placar se propagam sozinhas |
| Escudos em Base64 no documento | Firebase Storage | Storage exige plano Blaze; a imagem reduzida a 256 px cabe folgadamente no limite de 1 MB do documento |
| Regras de negócio na camada `domain` | Lógica dentro dos ViewModels | Permite testar classificação e sorteio na JVM, sem emulador |
| Service locator | Biblioteca de injeção de dependência | Projeto pequeno; evita processamento de anotações no build |

### 3.3 Tecnologias

| Camada | Stack |
|--------|-------|
| Linguagem | Kotlin 2.0 |
| Interface | Jetpack Compose, Material 3, Navigation Compose, Coil |
| Estado | ViewModel, StateFlow, Flow |
| Backend | Firebase Authentication, Cloud Firestore |
| Testes | JUnit 4, kotlinx-coroutines-test |
| Build | Gradle 8.14, AGP 8.7, JDK 17 |

---

## 4. Requisitos funcionais

### 4.1 Autenticação

| ID | Requisito | Status |
|----|-----------|--------|
| RF01 | Autenticar por e-mail e senha | Implementado |
| RF02 | Manter a sessão ativa entre execuções | Implementado |
| RF03 | Encerrar a sessão pelo menu lateral | Implementado |
| RF04 | Exibir mensagem específica para credenciais inválidas e falha de rede | Implementado |

### 4.2 Campeonatos

| ID | Requisito | Status |
|----|-----------|--------|
| RF05 | Criar campeonato com nome, temporada, limite de times e descrição | Implementado |
| RF06 | Listar os campeonatos do organizador, do mais recente ao mais antigo | Implementado |
| RF07 | Editar os dados do campeonato | Implementado |
| RF08 | Excluir o campeonato e, em cascata, times, jogadores, rodadas e partidas | Implementado |

### 4.3 Times

| ID | Requisito | Status |
|----|-----------|--------|
| RF09 | Cadastrar time com nome, sigla e escudo escolhido da galeria | Implementado |
| RF10 | Impedir nomes duplicados no mesmo campeonato | Implementado |
| RF11 | Respeitar o limite de times definido no campeonato | Implementado |
| RF12 | Editar e excluir time, removendo suas partidas e seu elenco | Implementado |

### 4.4 Elenco

| ID | Requisito | Status |
|----|-----------|--------|
| RF13 | Cadastrar jogador com nome, número da camisa e posição | Implementado |
| RF14 | Identificar goleiros para o registro de defesas | Implementado |
| RF15 | Impedir jogadores com nome repetido no mesmo time | Implementado |
| RF16 | Editar e excluir jogador | Implementado |

### 4.5 Rodadas e partidas

A tabela é gerada inteiramente pelo sistema. O organizador não monta confrontos, não escolhe o
mandante e não cria rodadas manualmente — o comportamento segue o de plataformas como Copa Fácil e
a tabela do Campeonato Brasileiro, em que o calendário nasce pronto do sorteio.

| ID | Requisito | Status |
|----|-----------|--------|
| RF17 | Sortear a tabela completa do turno a partir dos times cadastrados, definindo confrontos e mando de campo automaticamente | Implementado |
| RF18 | Não oferecer criação, edição ou exclusão manual de rodadas e confrontos | Implementado |
| RF19 | Permitir refazer o sorteio apenas enquanto nenhuma partida tiver resultado registrado | Implementado |
| RF20 | Bloquear o sorteio assim que o primeiro placar for salvo, preservando a integridade da competição | Implementado |
| RF21 | Exibir uma rodada por vez, navegando por deslize lateral e por um seletor de rodadas | Implementado |
| RF22 | Definir data, horário e local da partida | Implementado |

### 4.6 Resultados

O cartão da partida reproduz a leitura de um placar esportivo: escudo e nome de cada time nas
extremidades e o resultado em destaque ao centro. O próprio placar é o controle — tocá-lo abre o
registro do resultado. Não há botões auxiliares de adicionar, editar ou remover no cartão.

No registro, o placar não é digitado: toca-se no escudo de um time para destacá-lo e ver seu elenco,
e os gols são lançados jogador a jogador. O placar exibido é a soma desses lançamentos, o que
garante que todo gol do resultado tenha um autor identificado.

| ID | Requisito | Status |
|----|-----------|--------|
| RF23 | Apresentar cada partida como cartão com escudo, nome e placar em destaque | Implementado |
| RF24 | Abrir o registro do resultado ao tocar no placar, sem botões auxiliares no cartão | Implementado |
| RF25 | Selecionar o time tocando em seu escudo, destacando-o e listando seu elenco | Implementado |
| RF26 | Atribuir os gols aos jogadores do time selecionado | Implementado |
| RF27 | Registrar o número de defesas do goleiro | Implementado |
| RF28 | Compor o placar somando os gols atribuídos aos jogadores de cada time | Implementado |
| RF29 | Corrigir ou limpar um resultado já registrado | Implementado |

### 4.7 Classificação

| ID | Requisito | Status |
|----|-----------|--------|
| RF30 | Calcular P, J, V, E, D, GP, GC e SG a partir das partidas encerradas | Implementado |
| RF31 | Atribuir 3 pontos por vitória, 1 por empate e 0 por derrota | Implementado |
| RF32 | Recalcular a tabela a cada resultado registrado ou alterado | Implementado |
| RF33 | Desempatar por pontos, vitórias, saldo de gols e gols marcados | Implementado |
| RF34 | Exibir o aproveitamento percentual de cada time | Implementado |

### 4.8 Rankings individuais

| ID | Requisito | Status |
|----|-----------|--------|
| RF35 | Exibir a artilharia do campeonato | Implementado |
| RF36 | Exibir o ranking de goleiros por número de defesas | Implementado |
| RF37 | Desempatar por número de partidas disputadas | Implementado |
| RF38 | Exibir a média por jogo de cada atleta | Implementado |

### 4.9 Estatísticas

| ID | Requisito | Status |
|----|-----------|--------|
| RF39 | Exibir partidas realizadas e pendentes | Implementado |
| RF40 | Exibir total e média de gols por partida | Implementado |
| RF41 | Exibir a maior goleada do campeonato | Implementado |
| RF42 | Exibir melhor ataque e melhor defesa | Implementado |
| RF43 | Filtrar estatísticas por torneio, temporada e time | Planejado — AC3 |

### 4.10 Interface

| ID | Requisito | Status |
|----|-----------|--------|
| RF44 | Alternar entre tema claro e escuro, preservando a escolha | Implementado |
| RF45 | Iniciar no tema configurado no sistema na primeira execução | Implementado |
| RF46 | Fechar o teclado ao tocar fora dos campos | Implementado |
| RF47 | Navegar entre as seções do campeonato por menu lateral | Implementado |

### 4.11 Hierarquia de competições

| ID | Requisito | Status |
|----|-----------|--------|
| RF48 | Cadastrar ligas, como entidade que agrupa torneios e times | Planejado — AC2 |
| RF49 | Cadastrar torneios dentro de uma liga | Planejado — AC2 |
| RF50 | Cadastrar temporadas dentro de um torneio | Planejado — AC2 |
| RF51 | Vincular times e jogadores à liga, e não à temporada | Planejado — AC2 |
| RF52 | Definir quais times da liga disputam cada temporada | Planejado — AC2 |
| RF53 | Consolidar o histórico de um time somando todas as suas temporadas | Planejado — AC3 |
| RF54 | Consolidar o histórico de um jogador somando todas as suas temporadas | Planejado — AC3 |

---

## 5. Requisitos não funcionais

| ID | Requisito | Como é atendido |
|----|-----------|-----------------|
| RNF01 | Executar em Android 7.0 ou superior | `minSdk 24` |
| RNF02 | Interface em Material Design 3 | Jetpack Compose com Material 3 |
| RNF03 | Persistir os dados na nuvem | Cloud Firestore |
| RNF04 | Autenticar os usuários | Firebase Authentication |
| RNF05 | Impedir acesso a dados de outro organizador | Regras de segurança em `firestore.rules` |
| RNF06 | Manter placar e classificação sempre coerentes | Classificação derivada, nunca persistida |
| RNF07 | Refletir alterações sem recarregar a tela | Listeners em tempo real do Firestore expostos como `Flow` |
| RNF08 | Operar dentro do plano gratuito do Firebase | Imagens comprimidas em Base64, sem Storage |
| RNF09 | Permitir verificação automatizada das regras de negócio | Camada `domain` isolada e coberta por testes de unidade |
| RNF10 | Código sem comentários, documentação centralizada | README e este documento |

---

## 6. Modelo de dados

### 6.1 Estrutura atual

```
users/{userId}
  name · email

championships/{championshipId}
  name · season · teamLimit · description · ownerId · createdAt

teams/{teamId}
  championshipId · name · abbreviation · logo · createdAt

players/{playerId}
  championshipId · teamId · name · number · position · createdAt

rounds/{roundId}
  championshipId · number

matches/{matchId}
  championshipId · roundId · homeTeamId · awayTeamId
  homeGoals · awayGoals · date · time · place · finished
  goals   playerId → gols na partida
  saves   playerId → defesas na partida
```

### 6.2 Estrutura planejada (AC2)

```
leagues/{leagueId}
  name · ownerId · createdAt

tournaments/{tournamentId}
  leagueId · name · createdAt

seasons/{seasonId}
  leagueId · tournamentId · label · teamIds · createdAt

teams/{teamId}
  leagueId · name · abbreviation · logo · createdAt

players/{playerId}
  leagueId · teamId · name · number · position · createdAt

rounds/{roundId}
  seasonId · number

matches/{matchId}
  seasonId · roundId · homeTeamId · awayTeamId
  homeGoals · awayGoals · date · time · place · finished
  goals · saves
```

A diferença essencial é o vínculo de `teams` e `players`: eles passam a pertencer à **liga**, e a
temporada apenas relaciona quais times a disputam. É isso que torna possível somar o desempenho de
um time ou de um jogador ao longo de várias temporadas.

### 6.3 Regras de segurança

Cada documento carrega o identificador do campeonato ao qual pertence. As regras verificam, a cada
leitura e escrita, se o `ownerId` do campeonato correspondente é o usuário autenticado.

---

## 7. Regras de negócio

### 7.1 Pontuação

```
Vitória = 3 pontos
Empate  = 1 ponto
Derrota = 0 pontos
```

### 7.2 Cálculo da classificação

Para cada partida encerrada, os dois times recebem:

| Campo | Mandante | Visitante |
|-------|----------|-----------|
| J | +1 | +1 |
| V / E / D | conforme o placar | conforme o placar |
| P | 3, 1 ou 0 | 3, 1 ou 0 |
| GP | gols marcados | gols marcados |
| GC | gols sofridos | gols sofridos |

`SG = GP − GC` e `aproveitamento = P × 100 ÷ (J × 3)`.

Partidas sem placar registrado não entram no cálculo.

### 7.3 Critérios de desempate

```
1. Pontos
2. Número de vitórias
3. Saldo de gols
4. Gols marcados
5. Ordem alfabética
```

### 7.4 Sorteio da tabela

Algoritmo do círculo (round-robin): os times são embaralhados, o primeiro é fixado e os demais giram
uma posição a cada rodada. Com número ímpar de participantes, um time folga por rodada. O mando de
campo alterna entre rodadas para equilibrar jogos em casa e fora.

Para `n` times pares, o resultado é `n − 1` rodadas com `n ÷ 2` partidas cada, e nenhum confronto se
repete.

Todas essas definições são do sistema. Confrontos, ordem das rodadas e mando de campo não são
editáveis pelo organizador.

### 7.5 Trava do sorteio

O sorteio pode ser refeito quantas vezes for preciso enquanto o campeonato não começou. A partir do
momento em que **qualquer partida tem resultado registrado**, a competição é considerada iniciada e
o sorteio fica indisponível.

```
Nenhum resultado registrado   →  sorteio liberado
Ao menos um resultado salvo   →  sorteio bloqueado
Resultado removido, voltando
a zero resultados             →  sorteio liberado novamente
```

Sem essa trava, refazer a tabela invalidaria os jogos já disputados e, por consequência, a
classificação.

### 7.6 Registro individual

O placar de cada time é a soma dos gols lançados para os seus jogadores. Não existe placar digitado
manualmente, então todo gol da partida tem autor conhecido e a artilharia nunca diverge do
resultado.

Apenas jogadores marcados como goleiro recebem defesas, e apenas eles aparecem no ranking de
defesas.

---

## 8. Interface

### 8.1 Fluxo de navegação

```
Splash
  └── Login
        └── Meus campeonatos
              └── Campeonato  (menu lateral)
                    ├── Classificação          tela inicial
                    ├── Times ──► Elenco do time
                    ├── Rodadas e partidas
                    ├── Artilharia e defesas
                    ├── Estatísticas
                    └── Tema claro / escuro
```

### 8.2 Identidade visual

| Elemento | Definição |
|----------|-----------|
| Cor primária | Verde `#0B6B3A` |
| Fundo de splash e login | Gradiente verde com círculos sutis |
| Tipografia | Material 3 padrão, títulos em peso alto |
| Formulários | Cantos de 16 dp, ícone à esquerda, container tonal |
| Cartão de partida | Escudos nas extremidades, placar em destaque ao centro, sem botões auxiliares |
| Navegação entre rodadas | Uma rodada por tela, com deslize lateral e seletor no topo |

---

## 9. Testes

Os testes cobrem a camada `domain`, onde estão as regras que sustentam o produto.

| Suíte | Cenários |
|-------|----------|
| `CalculateStandingsUseCaseTest` | Pontuação por vitória e empate, acúmulo de gols, partidas sem resultado, desempate por saldo, desempate por gols marcados, aproveitamento |
| `CalculateRankingsUseCaseTest` | Soma de gols entre partidas, exclusão de partidas não encerradas, filtro de goleiros no ranking de defesas, desempate por número de jogos, vínculo com o time |
| `GenerateRoundsUseCaseTest` | Número de rodadas com times pares e ímpares, folga por rodada, ausência de confrontos repetidos, calendários distintos entre sorteios, time jogando uma vez por rodada |

```bash
./gradlew testDebugUnitTest
```

---

## 10. Planejamento das entregas

| Entrega | Data | Funcionalidade apresentada |
|:-------:|:----:|----------------------------|
| **AC1** | 14/09 | Autenticação, criação de campeonato, cadastro e listagem de times |
| **AC2** | 13/10 | Hierarquia Liga → Torneio → Temporada, com times e jogadores vinculados à liga |
| **AC3** | 08/11 | Histórico consolidado e estatísticas filtráveis por torneio, temporada e time |
| **Final** | 22/11 | Refinamento da interface e fechamento do produto |

As funcionalidades de rodadas, partidas, resultados, classificação e rankings já estão implementadas
e serão apresentadas junto das entregas em que forem evoluídas.

---

## 11. Equipe

| Nome | RA |
|------|-----|
| | |
